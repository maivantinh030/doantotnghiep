package ParkCard;
import javacard.framework.*;

public class CustomerCardApplet extends Applet {
    private CardModel model;
    private PinManager pinMgr;
    private CryptoManager cryptoMgr;

    // --- Instruction codes ---

    // Crypto info
    private static final byte INS_GET_CRYPTO_INFO              = (byte) 0x10;

    // Customer data (requires admin auth)
    private static final byte INS_WRITE_INFO                   = (byte) 0x07;
    private static final byte INS_READ_INFO                    = (byte) 0x0B;
    private static final byte INS_SET_BALANCE                  = (byte) 0x0D;
    private static final byte INS_GET_BALANCE                  = (byte) 0x0E;
    private static final byte INS_DEDUCT_BALANCE               = (byte) 0x0F;

    // RSA challenge-response
    private static final byte INS_SET_CUSTOMER_ID              = (byte) 0x17;
    private static final byte INS_GET_CUSTOMER_ID              = (byte) 0x18;
    private static final byte INS_SIGN_CHALLENGE               = (byte) 0x1B;
    private static final byte INS_GET_RSA_STATUS               = (byte) 0x1C;
    private static final byte INS_GENERATE_RSA_KEYPAIR         = (byte) 0x1D;
    private static final byte INS_GET_PUBLIC_KEY               = (byte) 0x1E;

    // Admin PIN management
    private static final byte INS_VERIFY_ADMIN_PIN             = (byte) 0x1F;
    private static final byte INS_CREATE_ADMIN_PIN             = (byte) 0x20;
    private static final byte INS_GET_ADMIN_PIN_TRIES          = (byte) 0x22;

    // Session key + encrypted PIN
    private static final byte INS_SET_SESSION_KEY              = (byte) 0x23;
    private static final byte INS_VERIFY_ADMIN_PIN_ENCRYPTED   = (byte) 0x24;
    private static final byte INS_GET_SESSION_KEY_STATUS       = (byte) 0x26;

    // Card lifecycle (requires admin auth)
    private static final byte INS_CLEAR_CARD_DATA              = (byte) 0x30;

    // --- Status words ---
    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    private static final short SW_RSA_NOT_READY                 = (short) 0x6A88;
    private static final short SW_INSUFFICIENT_BALANCE          = (short) 0x6901;

    // Reusable transient buffer for decrypted PIN
    private byte[] decryptedPinBuffer;

    private CustomerCardApplet() {
        model = new CardModel();
        pinMgr = new PinManager();
        cryptoMgr = new CryptoManager();

        pinMgr.setCryptoManager(cryptoMgr);
        pinMgr.setCardModel(model);

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
            case INS_GET_CRYPTO_INFO:
                getCryptoInfo(apdu);
                break;

            case INS_WRITE_INFO:
                requireAuthenticated();
                model.writeCustomerInfo(apdu, cryptoMgr);
                break;

            case INS_READ_INFO:
                requireAuthenticated();
                model.readCustomerInfo(apdu, cryptoMgr);
                break;

            case INS_SET_BALANCE:
                requireAuthenticated();
                setBalance(apdu);
                break;

            case INS_GET_BALANCE:
                requireAuthenticated();
                getBalance(apdu);
                break;

            case INS_DEDUCT_BALANCE:
                requireAuthenticated();
                deductBalance(apdu);
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
                requireAuthenticated();
                model.generateRSAKeyPair(cryptoMgr);
                break;

            case INS_GET_PUBLIC_KEY:
                getPublicKey(apdu);
                break;

            case INS_CREATE_ADMIN_PIN:
                pinMgr.createAdminPIN(apdu);
                break;

            case INS_VERIFY_ADMIN_PIN:
                verifyAdminPIN(apdu);
                break;

            case INS_GET_ADMIN_PIN_TRIES:
                pinMgr.getAdminPinTries(apdu);
                break;

            case INS_SET_SESSION_KEY:
                setSessionKey(apdu);
                break;

            case INS_GET_SESSION_KEY_STATUS:
                getSessionKeyStatus(apdu);
                break;

            case INS_VERIFY_ADMIN_PIN_ENCRYPTED:
                verifyAdminPINEncrypted(apdu);
                break;

            case INS_CLEAR_CARD_DATA:
                requireAuthenticated();
                model.clearCardData();
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void requireAuthenticated() {
        if (!model.isDataReady() || !cryptoMgr.isKeyReady()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    /**
     * INS_GET_CRYPTO_INFO (0x10)
     * Returns [16 bytes IV][16 bytes Salt] = 32 bytes
     */
    private void getCryptoInfo(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        Util.arrayCopyNonAtomic(model.getIV(), (short)0, buf, (short)0, (short)16);
        Util.arrayCopyNonAtomic(model.getSalt(), (short)0, buf, (short)16, (short)16);
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)32);
        apdu.sendBytesLong(buf, (short)0, (short)32);
    }

    /**
     * INS_SET_CUSTOMER_ID (0x17)
     * Store customer ID (plain text, max 15 bytes). No PIN required.
     */
    private void setCustomerID(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc > 15) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        model.setCustomerID(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);
    }

    /**
     * INS_GET_CUSTOMER_ID (0x18)
     * Read customer ID (plain text). No PIN required.
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
     * INS_SET_BALANCE (0x0D)
     * Input: [4 bytes balance as signed int, big-endian]
     */
    private void setBalance(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 4) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        int balance = readInt(buf, ISO7816.OFFSET_CDATA);
        if (balance < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        model.setBalance(balance, cryptoMgr);
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);
    }

    /**
     * INS_GET_BALANCE (0x0E)
     * Output: [4 bytes balance as signed int, big-endian]
     */
    private void getBalance(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        int balance = model.getBalance(cryptoMgr);
        writeInt(buf, (short)0, balance);
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)4);
        apdu.sendBytesLong(buf, (short)0, (short)4);
    }

    /**
     * INS_DEDUCT_BALANCE (0x0F)
     * Input:  [4 bytes amount as signed int, big-endian]
     * Output: [4 bytes balanceAfter as signed int, big-endian]
     */
    private void deductBalance(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 4) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        int amount = readInt(buf, ISO7816.OFFSET_CDATA);
        if (amount < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        try {
            int balanceAfter = model.deductBalance(amount, cryptoMgr);
            writeInt(buf, (short)0, balanceAfter);
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)4);
            apdu.sendBytesLong(buf, (short)0, (short)4);
        } catch (ISOException e) {
            if (e.getReason() == SW_INSUFFICIENT_BALANCE) {
                throw e;
            }
            throw e;
        }
    }

    /**
     * INS_SIGN_CHALLENGE (0x1B)
     * Sign 32-byte challenge with RSA private key (SHA1withRSA).
     * Requires admin authentication (master key must be ready to decrypt private key).
     * Input:  [32 bytes challenge]
     * Output: [128 bytes signature]
     */
    private void signChallenge(APDU apdu) {
        if (!model.isRSAKeyReady()) {
            ISOException.throwIt(SW_RSA_NOT_READY);
        }
        if (!cryptoMgr.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        model.decryptAndImportRSAPrivateKey(cryptoMgr);

        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 32) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        short sigLen = model.signChallenge(buf, ISO7816.OFFSET_CDATA, (short)32, buf, (short)0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(sigLen);
        apdu.sendBytesLong(buf, (short)0, sigLen);
    }

    /**
     * INS_GET_RSA_STATUS (0x1C)
     * Output: [1 byte] 0x00=not ready, 0x01=ready
     */
    private void getRSAStatus(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = model.isRSAKeyReady() ? (byte)0x01 : (byte)0x00;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)1);
        apdu.sendBytes((short)0, (short)1);
    }

    /**
     * INS_GET_PUBLIC_KEY (0x1E)
     * Input:  [1 byte component] 0x00=modulus, 0x01=exponent
     * Output: [public key component bytes]
     */
    private void getPublicKey(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte component = buf[ISO7816.OFFSET_CDATA];
        short len;

        if (component == 0x00) {
            len = model.getPublicKeyModulus(buf, (short)0);
        } else if (component == 0x01) {
            len = model.getPublicKeyExponent(buf, (short)0);
        } else {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            return;
        }

        apdu.setOutgoing();
        apdu.setOutgoingLength(len);
        apdu.sendBytesLong(buf, (short)0, len);
    }

    /**
     * INS_VERIFY_ADMIN_PIN (0x1F)
     * Plaintext admin PIN verification.
     */
    private void verifyAdminPIN(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        if (pinMgr.verifyAdmin(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            model.setDataReady(true);
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
    }

    /**
     * INS_SET_SESSION_KEY (0x23)
     * Set 16-byte session key for encrypted PIN transmission.
     * Idempotent: if already set, returns success without overwriting.
     */
    private void setSessionKey(APDU apdu) {
        if (model.isSessionKeySet()) return;

        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 16) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        model.setSessionKey(buf, ISO7816.OFFSET_CDATA);
    }

    /**
     * INS_GET_SESSION_KEY_STATUS (0x26)
     * Output: [1 byte] 0x00=not set, 0x01=set
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
     * Verify admin PIN encrypted with session key (AES-ECB, padded to 16 bytes).
     * Input:  [16 bytes encrypted PIN]
     * Output: 9000 success, 6A80 wrong PIN, 6983 blocked
     */
    private void verifyAdminPINEncrypted(APDU apdu) {
        if (!model.isSessionKeySet()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc != 16) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        cryptoMgr.decryptWithSessionKey(buf, ISO7816.OFFSET_CDATA, (short)16,
                                        decryptedPinBuffer, (short)0,
                                        model.getSessionKey(), (short)0);

        // Find actual PIN length (strip null padding)
        byte pinLen = 0;
        for (short i = 0; i < 16; i++) {
            if (decryptedPinBuffer[i] == 0x00) {
                pinLen = (byte)i;
                break;
            }
        }
        if (pinLen == 0) pinLen = 16;

        if (pinMgr.verifyAdmin(decryptedPinBuffer, (short)0, pinLen)) {
            model.setDataReady(true);
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

        Util.arrayFillNonAtomic(decryptedPinBuffer, (short)0, (short)16, (byte)0);
    }

    public void deselect() {
        if (model.isDataReady()) {
            cryptoMgr.clearKey();
            model.setDataReady(false);
        }
    }

    private int readInt(byte[] buffer, short offset) {
        return ((buffer[offset] & 0xFF) << 24)
            | ((buffer[(short)(offset + 1)] & 0xFF) << 16)
            | ((buffer[(short)(offset + 2)] & 0xFF) << 8)
            | (buffer[(short)(offset + 3)] & 0xFF);
    }

    private void writeInt(byte[] buffer, short offset, int value) {
        buffer[offset] = (byte)(value >> 24);
        buffer[(short)(offset + 1)] = (byte)(value >> 16);
        buffer[(short)(offset + 2)] = (byte)(value >> 8);
        buffer[(short)(offset + 3)] = (byte)value;
    }
}
