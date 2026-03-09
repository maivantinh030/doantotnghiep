package ParkCard;

import javacard.framework.*;
import javacard.security.*;

/**
 * PBKDF2-HMAC-SHA1 Optimized for JavaCard
 * Fix: Memory Leak & EEPROM wear
 */
public class PBKDF2 {

    private static final short BLOCK_SIZE = 64;   // SHA-1 block size
    private static final short HASH_SIZE  = 20;   // SHA-1 output size
    
    // Buffer size cho Salt + Counter (Salt thường 16 bytes + 4 bytes counter = 20)
    // Giảm từ 32 xuống 20 để tiết kiệm 12 bytes RAM (salt chỉ cần 16 bytes)
    private static final short SALT_BUF_SIZE = 20; 

    private final MessageDigest sha1;

    // Transient Buffers (RAM) - KHÔNG DÙNG EEPROM CHO CÁC BIẾN NÀY
    private final byte[] ipad;
    private final byte[] opad;
    private final byte[] innerHash;
    
    // Các buffer phục vụ deriveKey (chuyển từ biến cục bộ thành biến toàn cục)
    private final byte[] bufferU;
    private final byte[] bufferResult;
    private final byte[] bufferSaltCounter;

    public PBKDF2() {
        sha1 = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);

        // Khởi tạo TẤT CẢ bộ nhớ đệm trong RAM (Transient)
        // Chỉ chạy 1 lần khi cài Applet -> Không bao giờ bị leak RAM nữa
        ipad = JCSystem.makeTransientByteArray(BLOCK_SIZE, JCSystem.CLEAR_ON_DESELECT);
        opad = JCSystem.makeTransientByteArray(BLOCK_SIZE, JCSystem.CLEAR_ON_DESELECT);
        innerHash = JCSystem.makeTransientByteArray(HASH_SIZE, JCSystem.CLEAR_ON_DESELECT);
        
        bufferU = JCSystem.makeTransientByteArray(HASH_SIZE, JCSystem.CLEAR_ON_DESELECT);
        bufferResult = JCSystem.makeTransientByteArray(HASH_SIZE, JCSystem.CLEAR_ON_DESELECT);
        bufferSaltCounter = JCSystem.makeTransientByteArray(SALT_BUF_SIZE, JCSystem.CLEAR_ON_DESELECT);
    }

    /**
     * HMAC-SHA1
     * Dùng các buffer transient có sẵn để tính toán nhanh
     */
    private void hmac(byte[] key, short keyOff, short keyLen,
                      byte[] data, short dataOff, short dataLen,
                      byte[] output, short outOff) {

        // Reset ipad/opad
        Util.arrayFillNonAtomic(ipad, (short) 0, BLOCK_SIZE, (byte) 0x00);
        Util.arrayFillNonAtomic(opad, (short) 0, BLOCK_SIZE, (byte) 0x00);

        if (keyLen <= BLOCK_SIZE) {
            Util.arrayCopyNonAtomic(key, keyOff, ipad, (short) 0, keyLen);
            Util.arrayCopyNonAtomic(key, keyOff, opad, (short) 0, keyLen);
        } else {
            // Hash key nếu dài hơn block (ít gặp với SHA-1)
            sha1.reset();
            sha1.doFinal(key, keyOff, keyLen, ipad, (short) 0);
            Util.arrayCopyNonAtomic(ipad, (short) 0, opad, (short) 0, HASH_SIZE);
        }

        // XOR Key với hằng số (0x36 và 0x5C)
        for (short i = 0; i < BLOCK_SIZE; i++) {
            ipad[i] ^= (byte) 0x36;
            opad[i] ^= (byte) 0x5C;
        }

        // Inner hash: SHA1(K_ipad || text)
        sha1.reset();
        sha1.update(ipad, (short) 0, BLOCK_SIZE);
        sha1.doFinal(data, dataOff, dataLen, innerHash, (short) 0);

        // Outer hash: SHA1(K_opad || InnerHash)
        sha1.reset();
        sha1.update(opad, (short) 0, BLOCK_SIZE);
        sha1.doFinal(innerHash, (short) 0, HASH_SIZE, output, outOff);
    }

    /**
     * PBKDF2-HMAC-SHA1
     * KHÔNG cấp phát bộ nhớ mới trong hàm này
     */
    public void deriveKey(byte[] password, short passwordOff, short passwordLen,
                          byte[] salt, short saltOff, short saltLen,
                          short iterations,
                          byte[] output, short outOff) {

        // 1. Kiểm tra độ dài salt để tránh tràn buffer
        if ((short)(saltLen + 4) > SALT_BUF_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // 2. Dọn dẹp buffer trước khi dùng (quan trọng vì tái sử dụng)
        Util.arrayFillNonAtomic(bufferU, (short)0, HASH_SIZE, (byte)0);
        Util.arrayFillNonAtomic(bufferResult, (short)0, HASH_SIZE, (byte)0);
        Util.arrayFillNonAtomic(bufferSaltCounter, (short)0, SALT_BUF_SIZE, (byte)0);
        // 3. Chuẩn bị Salt || Counter (INT_32_BE)
        // saltCounter = salt || 0x00000001
        short saltCounterLen = (short) (saltLen + 4);
        Util.arrayCopyNonAtomic(salt, saltOff, bufferSaltCounter, (short) 0, saltLen);
        
        // Counter i = 1 (Big Endian: 00 00 00 01)
        bufferSaltCounter[saltLen] = 0;
        bufferSaltCounter[(short) (saltLen + 1)] = 0;
        bufferSaltCounter[(short) (saltLen + 2)] = 0;
        bufferSaltCounter[(short) (saltLen + 3)] = 1;

        // 4. Tính U1 = PRF(Password, Salt || 1)
        // Kết quả lưu vào bufferU
        hmac(password, passwordOff, passwordLen,
             bufferSaltCounter, (short) 0, saltCounterLen,
             bufferU, (short) 0);

        // Result ban đầu = U1
        Util.arrayCopyNonAtomic(bufferU, (short) 0, bufferResult, (short) 0, HASH_SIZE);

        // 5. Vòng lặp tính U2 ... Uc
        // U_new = PRF(Password, U_old)
        // Result = Result ^ U_new
        for (short i = 1; i < iterations; i++) {
            // Tính U tiếp theo từ U hiện tại, ghi đè lại vào chính bufferU
            hmac(password, passwordOff, passwordLen,
                 bufferU, (short) 0, HASH_SIZE,
                 bufferU, (short) 0);

            // XOR vào kết quả
            for (short j = 0; j < HASH_SIZE; j++) {
                bufferResult[j] ^= bufferU[j];
            }
        }

        // 6. Copy kết quả ra buffer output của người gọi
        Util.arrayCopyNonAtomic(bufferResult, (short) 0, output, outOff, HASH_SIZE);
        
        // (Optional) Xóa dữ liệu nhạy cảm trong buffer tạm
        Util.arrayFillNonAtomic(bufferU, (short)0, HASH_SIZE, (byte)0);
    }
}