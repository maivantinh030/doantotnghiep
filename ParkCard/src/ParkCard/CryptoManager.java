package ParkCard;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class CryptoManager {
    private static final short AES_KEY_SIZE = 16;

    private final Cipher cbcCipher;  // CBC mode for PIN wrap/unwrap and fields
    private final Cipher ecbCipher;  // ECB mode for photo encryption
    private final byte[] aesKey;   // master key plaintext (transient)
    private final byte[] pinKey;   // PIN-derived key (transient)
    private final byte[] derivedBuf; // reuse PBKDF2 output buffer to avoid reallocation
    private boolean keyReady;      // master key loaded
    private final PBKDF2 pbkdf2;

    public CryptoManager() {
        cbcCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        ecbCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
        aesKey = JCSystem.makeTransientByteArray(AES_KEY_SIZE, JCSystem.CLEAR_ON_DESELECT);
        pinKey = JCSystem.makeTransientByteArray(AES_KEY_SIZE, JCSystem.CLEAR_ON_DESELECT);
        derivedBuf = JCSystem.makeTransientByteArray((short)20, JCSystem.CLEAR_ON_DESELECT);
        keyReady = false;
        pbkdf2 = new PBKDF2();
    }

    /**
     * Derive PIN-key (16B) via PBKDF2 and keep it transient.
     *
     * @param preserveLoadedMaster true if a plaintext master key is already loaded and must remain usable
     */
    public void deriveKeyFromPIN(byte[] pin, short pinOff, byte pinLen,
                                 byte[] salt, short saltOff, short saltLen,
                                 boolean preserveLoadedMaster) {
        // Lower iterations to reduce card-side latency (demo setting; increase for production)
        pbkdf2.deriveKey(pin, pinOff, pinLen, salt, saltOff, saltLen, (short)200, derivedBuf, (short)0);
        Util.arrayCopyNonAtomic(derivedBuf, (short)0, pinKey, (short)0, AES_KEY_SIZE);
        // Only clear the keyReady flag when we actually need to unwrap again.
        if (!preserveLoadedMaster) {
            keyReady = false; // master key will be loaded after unwrap
        }
    }

    /**
     * Backward-compatible overload that clears keyReady (default behavior).
     */
    public void deriveKeyFromPIN(byte[] pin, short pinOff, byte pinLen,
                                 byte[] salt, short saltOff, short saltLen) {
        deriveKeyFromPIN(pin, pinOff, pinLen, salt, saltOff, saltLen, false);
    }

    /** Wrap plaintext master key (16B) using current PIN key (CBC mode). */
    public void wrapMasterKey(byte[] masterPlain, short masterOff,
                              byte[] wrappedOut, short wrappedOff,
                              byte[] iv, short ivOff) {
        AESKey pinKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        pinKeyObj.setKey(pinKey, (short)0);
        cbcCipher.init(pinKeyObj, Cipher.MODE_ENCRYPT, iv, ivOff, (short)16);
        cbcCipher.doFinal(masterPlain, masterOff, AES_KEY_SIZE, wrappedOut, wrappedOff);
    }

    /** Unwrap master key ciphertext into aesKey (plaintext) using current PIN key (CBC mode). */
    public void unwrapMasterKey(byte[] wrapped, short wrappedOff,
                                byte[] iv, short ivOff) {
        AESKey pinKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        pinKeyObj.setKey(pinKey, (short)0);
        cbcCipher.init(pinKeyObj, Cipher.MODE_DECRYPT, iv, ivOff, (short)16);
        cbcCipher.doFinal(wrapped, wrappedOff, AES_KEY_SIZE, aesKey, (short)0);
        keyReady = true;
    }

    /** Wrap the currently loaded master key (aesKey) with the current PIN key. */
    public void wrapLoadedMasterKey(byte[] wrappedOut, short wrappedOff,
                                    byte[] iv, short ivOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        wrapMasterKey(aesKey, (short)0, wrappedOut, wrappedOff, iv, ivOff);
    }

    /** Encrypt data with master key (CBC mode). ptLen must be multiple of 16. */
    public void encrypt(byte[] plaintext, short ptOff, short ptLen,
                       byte[] ciphertext, short ctOff,
                       byte[] iv, short ivOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (ptLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        cbcCipher.init(aesKeyObj, Cipher.MODE_ENCRYPT, iv, ivOff, (short)16);
        cbcCipher.doFinal(plaintext, ptOff, ptLen, ciphertext, ctOff);
    }

    /** Decrypt data with master key (CBC mode). ctLen must be multiple of 16. */
    public void decrypt(byte[] ciphertext, short ctOff, short ctLen,
                       byte[] plaintext, short ptOff,
                       byte[] iv, short ivOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (ctLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        cbcCipher.init(aesKeyObj, Cipher.MODE_DECRYPT, iv, ivOff, (short)16);
        cbcCipher.doFinal(ciphertext, ctOff, ctLen, plaintext, ptOff);
    }

    /** Encrypt photo with master key using ECB mode. Data must be padded to multiple of 16. */
    public void encryptPhotoECB(byte[] plaintext, short ptOff, short ptLen,
                                byte[] ciphertext, short ctOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (ptLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        ecbCipher.init(aesKeyObj, Cipher.MODE_ENCRYPT);
        ecbCipher.doFinal(plaintext, ptOff, ptLen, ciphertext, ctOff);
    }

    /** Decrypt photo with master key using ECB mode. ctLen must be multiple of 16. */
    public void decryptPhotoECB(byte[] ciphertext, short ctOff, short ctLen,
                                byte[] plaintext, short ptOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (ctLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        ecbCipher.init(aesKeyObj, Cipher.MODE_DECRYPT);
        ecbCipher.doFinal(ciphertext, ctOff, ctLen, plaintext, ptOff);
    }

    /**
     * Decrypt data với session key (ECB mode, vì chỉ có 1 block)
     * Dùng để giải mã admin PIN đã được mã hóa bằng session key
     */
    public void decryptWithSessionKey(byte[] ciphertext, short ctOff, short ctLen,
                                     byte[] plaintext, short ptOff,
                                     byte[] sessionKey, short keyOff) {
        if (ctLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey sessionKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        sessionKeyObj.setKey(sessionKey, keyOff);
        ecbCipher.init(sessionKeyObj, Cipher.MODE_DECRYPT);
        ecbCipher.doFinal(ciphertext, ctOff, ctLen, plaintext, ptOff);
    }
    
    /**
     * Encrypt data với session key (ECB mode)
     * Dùng để mã hóa admin PIN trước khi gửi xuống card
     */
    public void encryptWithSessionKey(byte[] plaintext, short ptOff, short ptLen,
                                     byte[] ciphertext, short ctOff,
                                     byte[] sessionKey, short keyOff) {
        if (ptLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        AESKey sessionKeyObj = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        sessionKeyObj.setKey(sessionKey, keyOff);
        ecbCipher.init(sessionKeyObj, Cipher.MODE_ENCRYPT);
        ecbCipher.doFinal(plaintext, ptOff, ptLen, ciphertext, ctOff);
    }

    /** Clear all secrets from RAM. */
    public void clearKey() {
        Util.arrayFillNonAtomic(aesKey, (short)0, AES_KEY_SIZE, (byte)0);
        Util.arrayFillNonAtomic(pinKey, (short)0, AES_KEY_SIZE, (byte)0);
        keyReady = false;
    }

    public boolean isKeyReady() {
        return keyReady;
    }
}