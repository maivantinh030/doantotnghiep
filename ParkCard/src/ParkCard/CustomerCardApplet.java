package ParkCard;
import javacard.framework.*;

public class CustomerCardApplet extends Applet {
    private CardModel model;
    private PinManager pinMgr;
    private GameManager gameMgr;
    private CryptoManager cryptoMgr;
    
    // Instruction codes - Sequential from 0x01
    private static final byte INS_CREATE_PIN = (byte) 0x01;
    private static final byte INS_VERIFY_PIN = (byte) 0x02;
    private static final byte INS_CHANGE_PIN = (byte) 0x03;
    private static final byte INS_GET_PIN_TRIES = (byte) 0x04;
    private static final byte INS_RESET_PIN_COUNTER = (byte) 0x05;
    private static final byte INS_WRITE_INFO = (byte) 0x07;
    private static final byte INS_START_PHOTO_WRITE = (byte) 0x08;
    private static final byte INS_WRITE_PHOTO_CHUNK = (byte) 0x09;
    private static final byte INS_FINISH_PHOTO_WRITE = (byte) 0x0A;
    private static final byte INS_READ_INFO = (byte) 0x0B;
    private static final byte INS_READ_PHOTO_CHUNK = (byte) 0x0C;
    private static final byte INS_RECHARGE_BALANCE = (byte) 0x0D;
    private static final byte INS_CHECK_BALANCE = (byte) 0x0E;
    private static final byte INS_MAKE_PAYMENT = (byte) 0x0F;
    private static final byte INS_GET_CRYPTO_INFO = (byte) 0x10;
    
    // Game management instruction codes
    private static final byte INS_ADD_OR_INCREASE_TICKETS = (byte) 0x11;
    private static final byte INS_DECREASE_GAME_TICKETS = (byte) 0x12;
    private static final byte INS_READ_GAMES = (byte) 0x13;
    private static final byte INS_UPDATE_GAME_TICKETS = (byte) 0x14;
    private static final byte INS_FIND_GAME = (byte) 0x15;
    private static final byte INS_REMOVE_GAME = (byte) 0x16;
    
    // RSA Challenge-Response instructions
    private static final byte INS_SET_CUSTOMER_ID = (byte) 0x17;
    private static final byte INS_GET_CUSTOMER_ID = (byte) 0x18;
    private static final byte INS_SIGN_CHALLENGE = (byte) 0x1B;
    private static final byte INS_GET_RSA_STATUS = (byte) 0x1C;
    private static final byte INS_GENERATE_RSA_KEYPAIR = (byte) 0x1D;
    private static final byte INS_GET_PUBLIC_KEY = (byte) 0x1E;
    
    // Admin PIN management instruction codes
    private static final byte INS_CREATE_ADMIN_PIN = (byte) 0x20;
    private static final byte INS_VERIFY_ADMIN_PIN = (byte) 0x1F;
    private static final byte INS_RESET_USER_PIN = (byte) 0x21;
    private static final byte INS_GET_ADMIN_PIN_TRIES = (byte) 0x22;
    
    // Session key và encrypted PIN commands
    private static final byte INS_SET_SESSION_KEY = (byte) 0x23;
    private static final byte INS_VERIFY_ADMIN_PIN_ENCRYPTED = (byte) 0x24;
    private static final byte INS_VERIFY_PIN_ENCRYPTED = (byte) 0x25;
    private static final byte INS_GET_SESSION_KEY_STATUS = (byte) 0x26;

    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    private static final short SW_INSUFFICIENT_BALANCE = (short) 0x6901;
    private static final short SW_RSA_NOT_READY = (short) 0x6A88;
    
    // Buffer tái sử dụng cho decrypted PIN (tiết kiệm 16 bytes mỗi lần verify)
    private byte[] decryptedPinBuffer;

    private CustomerCardApplet() {
        model = new CardModel();
        pinMgr = new PinManager();
        gameMgr = new GameManager(model.gameList, model.gameCountRef);
        cryptoMgr = new CryptoManager();
        
        // Set references
        pinMgr.setCryptoManager(cryptoMgr);
        pinMgr.setCardModel(model);
        
        // Khởi tạo buffer tái sử dụng cho decrypted PIN
        decryptedPinBuffer = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new CustomerCardApplet().register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        byte ins = buf[ISO7816.OFFSET_INS];

        switch (ins) {
            // PIN management
            case INS_CREATE_PIN: pinMgr.createPIN(apdu); break;
            case INS_VERIFY_PIN: verifyPIN(apdu); break;
            case INS_CHANGE_PIN: changePIN(apdu); break;
            case INS_GET_PIN_TRIES: pinMgr.getPinTries(apdu); break;
            case INS_RESET_PIN_COUNTER: pinMgr.resetPinCounter(apdu); break;
            case INS_GET_CRYPTO_INFO: getCryptoInfo(apdu); break;
            
            // Commands cn authentication v� encryption
            case INS_WRITE_INFO:
            	requireAuthenticated();
            	model.writeCustomerInfo(apdu, cryptoMgr); 
            	break;
            case INS_START_PHOTO_WRITE: 
            	requireAuthenticated(); 
            	model.startPhotoWrite(); 
            	break;
            case INS_WRITE_PHOTO_CHUNK: 
            	requireAuthenticated(); 
            	model.writePhotoChunk(apdu); 
            	break;
            case INS_FINISH_PHOTO_WRITE: 
            	requireAuthenticated(); 
            	model.finishPhotoWrite(apdu, cryptoMgr); 
            	break;
            case INS_READ_INFO: 
            	requireAuthenticated(); 
            	model.readAllData(apdu, cryptoMgr); 
            	break;
            case INS_READ_PHOTO_CHUNK: 
            	requireAuthenticated(); 
            	model.readPhotoChunk(apdu, cryptoMgr); 
            	break;
            case INS_RECHARGE_BALANCE: 
            	requireAuthenticated(); 
            	model.rechargeBalance(apdu, cryptoMgr); 
            	break;
            case INS_CHECK_BALANCE: 
            	requireAuthenticated(); 
            	model.checkBalance(apdu, cryptoMgr); 
            	break;
            case INS_MAKE_PAYMENT: 
            	requireAuthenticated(); 
            	model.makePayment(apdu, cryptoMgr); 
            	break;
            
            // Game commands kh�ng cn encryption
            case INS_ADD_OR_INCREASE_TICKETS: 
            	// requireAuthenticated(); 
            	gameMgr.addOrIncreaseTickets(apdu); 
            	break;
            case INS_DECREASE_GAME_TICKETS:  
            	gameMgr.decreaseGameTickets(apdu); 
            	break;
            case INS_READ_GAMES:  
            	gameMgr.readGames(apdu); 
            	break;
            case INS_UPDATE_GAME_TICKETS:  
            	gameMgr.updateGameTickets(apdu); 
            	break;
            case INS_FIND_GAME:  
            	gameMgr.findGame(apdu); 
            	break;
            case INS_REMOVE_GAME:  
            	gameMgr.removeGame(apdu); 
            	break;
           
            case INS_SET_CUSTOMER_ID: 
            	setCustomerID(apdu);
            	break;
            case INS_GET_CUSTOMER_ID: 
            	getCustomerID(apdu); 
            	break;
            case INS_SIGN_CHALLENGE: 
            	signChallenge(apdu); 
            	break;
            case INS_GET_RSA_STATUS: 
            	getRSAStatus(apdu); 
            	break;
            case INS_GENERATE_RSA_KEYPAIR:
            	generateRSAKeyPair(apdu);
            	break;
            case INS_GET_PUBLIC_KEY:
            	getPublicKey(apdu);
            	break;
            
            // Admin PIN management
            case INS_CREATE_ADMIN_PIN: pinMgr.createAdminPIN(apdu); break;
            case INS_VERIFY_ADMIN_PIN: verifyAdminPIN(apdu); break;
            case INS_RESET_USER_PIN: resetUserPIN(apdu); break;
            case INS_GET_ADMIN_PIN_TRIES: pinMgr.getAdminPinTries(apdu); break;
            
            // Session key và encrypted PIN commands
            case INS_SET_SESSION_KEY: setSessionKey(apdu); break;
            case INS_GET_SESSION_KEY_STATUS: getSessionKeyStatus(apdu); break;
            case INS_VERIFY_ADMIN_PIN_ENCRYPTED: verifyAdminPINEncrypted(apdu); break;
            case INS_VERIFY_PIN_ENCRYPTED: verifyPINEncrypted(apdu); break;
            
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void requireAuthenticated() {
        if (!model.isDataReady() || !cryptoMgr.isKeyReady()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    private void verifyPIN(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        if (pinMgr.verify(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            // Verify th�nh c�ng - key � ��c derive trong pinMgr.verify()
            model.setDataReady(true);
            if (!model.isDataEncrypted()) {
                model.initializeBalance(cryptoMgr);
            }
        } else {
            model.setDataReady(false);
            cryptoMgr.clearKey();
            if (pinMgr.getTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
    }

    private void changePIN(APDU apdu) {
        
        pinMgr.changePIN(apdu);
        // PinManager đã xử lý việc keep authentication state và master key
        // Không cần reset vì user vẫn authenticated sau khi đổi PIN thành công
    }
    
    /**
     * Command mi: Ly IV v� Salt � client bit (cn cho testing/debugging)
     * Trong production c� th b command n�y
     */
    private void getCryptoInfo(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte[] iv = model.getIV();
        byte[] salt = model.getSalt();
        
        // Return: [16 bytes IV][16 bytes Salt]
        Util.arrayCopyNonAtomic(iv, (short)0, buf, (short)0, (short)16);
        Util.arrayCopyNonAtomic(salt, (short)0, buf, (short)16, (short)16);
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)32);
        apdu.sendBytesLong(buf, (short)0, (short)32);
    }
    
    // ========== NEW RSA COMMANDS ==========
    
    /**
     * INS_SET_CUSTOMER_ID (0x90)
     * L�u customer ID (plain text, ti �a 15 bytes)
     * Input: [customer_id bytes]
     * Output: 9000 success
     */
    private void setCustomerID(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc > 15) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        model.setCustomerID(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        
        // Send response 9000
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);
    }
    
    /**
     * INS_GET_CUSTOMER_ID (0x91)
     * �c customer ID (plain text, KH�NG cn PIN)
     * Input: none
     * Output: [15 bytes customer_id]
     */
    private void getCustomerID(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        
        model.getCustomerID(buf, (short)0);
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)15);
        apdu.sendBytesLong(buf, (short)0, (short)15);
    }
    
    /**
     * INS_SIGN_CHALLENGE (0x94)
     * Sign challenge vi RSA private key (SHA1withRSA)
     * Input: [challenge bytes] (32 bytes)
     * Output: [signature bytes] (128 bytes cho RSA-1024)
     * 
     * Flow:
     * 1. Terminal nhn challenge t server (32 bytes)
     * 2. Terminal gi challenge xung card
     * 3. Card sign vi private key
     * 4. Return signature (128 bytes)
     * 5. Terminal gi signature l�n server verify
     */
    private void signChallenge(APDU apdu) {
        if (!model.isRSAKeyReady()) {
            ISOException.throwIt(SW_RSA_NOT_READY);
        }
        
        // Đảm bảo master key đã sẵn sàng và RSA private key đã được decrypt
        // (Sau khi disconnect và connect lại, private key bị clear nên cần decrypt lại)
        if (!cryptoMgr.isKeyReady()) {
            // Master key chưa sẵn sàng, không thể decrypt private key để sign
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        // DEBUG: Throw exception trước khi decrypt để xem có đến được đây không
          // DEBUG: Before decryptAndImportRSAPrivateKey
        
        // Decrypt và import RSA private key
        model.decryptAndImportRSAPrivateKey(cryptoMgr);
        
        // DEBUG: Throw exception sau khi decrypt để xem có lỗi trong decrypt không
        // ISOException.throwIt((short)0x6984); // DEBUG: After decryptAndImportRSAPrivateKey
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 32) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Sign challenge (input ti OFFSET_CDATA, output ti offset 0)
        short sigLen = model.signChallenge(
            buf, ISO7816.OFFSET_CDATA, (short)32,  // challenge input
            buf, (short)0                           // signature output
        );
        
        // Return signature
        apdu.setOutgoing();
        apdu.setOutgoingLength(sigLen);
        apdu.sendBytesLong(buf, (short)0, sigLen);
    }
    
    /**
     * INS_GET_RSA_STATUS (0x1C)
     * Kiểm tra trạng thái RSA key
     * Output: [1 byte status]
     *   0x00 = RSA not ready
     *   0x01 = RSA ready
     */
    private void getRSAStatus(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = model.isRSAKeyReady() ? (byte)0x01 : (byte)0x00;
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)1);
        apdu.sendBytes((short)0, (short)1);
    }
    
    /**
     * INS_GENERATE_RSA_KEYPAIR (0x1D)
     * Generate RSA keypair trong thẻ
     * Input: none
     * Output: 9000 success
     * 
     * Lưu ý: Command này mất vài giây để thực hiện (tạo khóa RSA-1024)
     * Private key sẽ được mã hóa bằng Master Key và lưu vào persistent memory
     */
    private void generateRSAKeyPair(APDU apdu) {
        if (!model.isDataReady() || !cryptoMgr.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); // Phải verify PIN trước
        }
        model.generateRSAKeyPair(cryptoMgr);
        // Success - chỉ return SW_NO_ERROR
    }
    
    /**
     * INS_GET_PUBLIC_KEY (0x1E)
     * Lấy RSA public key từ thẻ
     * Input: [1 byte component] (0x00 = modulus, 0x01 = exponent)
     * Output: [public key component bytes]
     * 
     * Modulus: 128 bytes (RSA-1024)
     * Exponent: thường 3 bytes (0x010001)
     */
    private void getPublicKey(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 1) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte component = buf[ISO7816.OFFSET_CDATA];
        short len = 0;
        
        if (component == 0x00) {
            // Get modulus (128 bytes)
            len = model.getPublicKeyModulus(buf, (short)0);
        } else if (component == 0x01) {
            // Get exponent (3 bytes)
            len = model.getPublicKeyExponent(buf, (short)0);
        } else {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
        
        apdu.setOutgoing();
        apdu.setOutgoingLength(len);
        apdu.sendBytesLong(buf, (short)0, len);
    }
    
    private void verifyAdminPIN(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        if (pinMgr.verifyAdmin(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            // Verify thành công - key đã được derive trong pinMgr.verifyAdmin()
            model.setDataReady(true);
            if (!model.isDataEncrypted()) {
                model.initializeBalance(cryptoMgr);
            }
        } else {
            model.setDataReady(false);
            cryptoMgr.clearKey();
            if (pinMgr.getAdminTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
    }
    
    private void resetUserPIN(APDU apdu) {
        // Must be authenticated with admin PIN first
        if (!pinMgr.isAdminPINValidated()) {
            ISOException.throwIt((short)0x6985);  // Conditions of use not satisfied - must verify admin PIN first
        }
        pinMgr.resetUserPIN(apdu);
    }
    
    /**
     * INS_SET_SESSION_KEY (0x23)
     * Lần đầu connect, terminal gửi session key xuống card
     * Input: [16 bytes session key]
     * Output: 9000 success
     * 
     * Lưu ý: Session key chỉ được set một lần duy nhất. Nếu đã được set rồi,
     * command này sẽ return success ngay mà không set lại (idempotent).
     */
    private void setSessionKey(APDU apdu) {
        // Nếu session key đã được set rồi, return success ngay (không set lại)
        if (model.isSessionKeySet()) {
            return; // Return 9000 (success)
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 16) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Lưu session key vào CardModel (chỉ set lần đầu)
        model.setSessionKey(buf, ISO7816.OFFSET_CDATA);
    }
    
    /**
     * INS_GET_SESSION_KEY_STATUS (0x26)
     * Kiểm tra xem session key đã được set chưa
     * Input: none
     * Output: [1 byte status]
     *   0x00 = Session key chưa được set
     *   0x01 = Session key đã được set
     */
    private void getSessionKeyStatus(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = model.isSessionKeySet() ? (byte)0x01 : (byte)0x00;
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)1);
        apdu.sendBytes((short)0, (short)1);
    }
    
    /**
     * INS_VERIFY_ADMIN_PIN_ENCRYPTED (0x24)
     * Verify admin PIN đã được mã hóa bằng session key
     * Input: [encrypted admin PIN bytes] (đã được mã hóa bằng session key, padded to 16 bytes)
     * Output: 9000 success, 6A80 wrong PIN, 6983 blocked
     * 
     * Flow:
     * 1. Terminal mã hóa admin PIN bằng session key
     * 2. Gửi admin PIN đã mã hóa xuống card
     * 3. Card giải mã admin PIN bằng session key
     * 4. Dùng admin PIN để unwrap master key
     * 5. Master key được dùng để giải mã private key RSA (nếu cần)
     */
    private void verifyAdminPINEncrypted(APDU apdu) {
        if (!model.isSessionKeySet()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); // Session key chưa được set
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        // Admin PIN được mã hóa, phải là bội số của 16 (AES block size)
        // PIN thường 4-8 bytes, sau khi padding sẽ là 16 bytes
        if (lc != 16) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Giải mã admin PIN bằng session key - dùng buffer tái sử dụng
        cryptoMgr.decryptWithSessionKey(buf, ISO7816.OFFSET_CDATA, (short)16, decryptedPinBuffer, (short)0, model.getSessionKey(), (short)0);
        
        // Tìm độ dài thực của PIN (loại bỏ padding)
        byte pinLen = 0;
        for (short i = 0; i < 16; i++) {
            if (decryptedPinBuffer[i] == 0x00) {
                pinLen = (byte)i;
                break;
            }
        }
        if (pinLen == 0) pinLen = 16; // Không có padding, PIN đầy 16 bytes
        
        // Verify admin PIN với PIN đã giải mã
        if (pinMgr.verifyAdmin(decryptedPinBuffer, (short)0, pinLen)) {
            // Verify thành công - key đã được derive trong pinMgr.verifyAdmin()
            model.setDataReady(true);
            if (!model.isDataEncrypted()) {
                model.initializeBalance(cryptoMgr);
            }
            
            // Giải mã RSA private key ngay sau khi verify admin PIN
            // Luồng: Verify PIN → Unwrap Master Key → Giải mã RSA private key
            // Cần decrypt để có thể sign challenge sau này
            if (model.isRSAKeyReady() && cryptoMgr.isKeyReady()) {
                model.decryptAndImportRSAPrivateKey(cryptoMgr);
            }
        } else {
            model.setDataReady(false);
            cryptoMgr.clearKey();
            if (pinMgr.getAdminTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
        
        // Xóa PIN đã giải mã khỏi RAM
        Util.arrayFillNonAtomic(decryptedPinBuffer, (short)0, (short)16, (byte)0);
    }
    
    /**
     * INS_VERIFY_PIN_ENCRYPTED (0x25)
     * Verify user PIN đã được mã hóa bằng session key
     * Input: [encrypted user PIN bytes] (đã được mã hóa bằng session key, padded to 16 bytes)
     * Output: 9000 success, 6A80 wrong PIN, 6983 blocked
     * 
     * Flow:
     * 1. Terminal mã hóa user PIN bằng session key
     * 2. Gửi user PIN đã mã hóa xuống card
     * 3. Card giải mã user PIN bằng session key
     * 4. Dùng user PIN để unwrap master key
     * 5. Master key được dùng để giải mã private key RSA (nếu cần)
     */
    private void verifyPINEncrypted(APDU apdu) {
        if (!model.isSessionKeySet()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); // Session key chưa được set
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        // User PIN được mã hóa, phải là bội số của 16 (AES block size)
        // PIN thường 4-8 bytes, sau khi padding sẽ là 16 bytes
        if (lc != 16) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Giải mã user PIN bằng session key - dùng buffer tái sử dụng
        cryptoMgr.decryptWithSessionKey(buf, ISO7816.OFFSET_CDATA, (short)16, decryptedPinBuffer, (short)0, model.getSessionKey(), (short)0);
        
        // Tìm độ dài thực của PIN (loại bỏ padding)
        byte pinLen = 0;
        for (short i = 0; i < 16; i++) {
            if (decryptedPinBuffer[i] == 0x00) {
                pinLen = (byte)i;
                break;
            }
        }
        if (pinLen == 0) pinLen = 16; // Không có padding, PIN đầy 16 bytes
        
        // Verify user PIN với PIN đã giải mã
        if (pinMgr.verify(decryptedPinBuffer, (short)0, pinLen)) {
            // Verify thành công - key đã được derive trong pinMgr.verify()
            model.setDataReady(true);
            if (!model.isDataEncrypted()) {
                model.initializeBalance(cryptoMgr);
            }
        } else {
            model.setDataReady(false);
            cryptoMgr.clearKey();
            if (pinMgr.getTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
        
        // Xóa PIN đã giải mã khỏi RAM
        Util.arrayFillNonAtomic(decryptedPinBuffer, (short)0, (short)16, (byte)0);
    }
    
    public void deselect() {
        // Clear key và reset dataReady khi deselect
        if (model.isDataReady()) {
            cryptoMgr.clearKey();
            model.setDataReady(false);
        }
    }
}