package ParkCard;
import javacard.framework.*;
import javacard.security.*;

public class CardModel {
    private static final short LEN_CUSTOMER_ID = 15;
    private static final short LEN_CARD_UUID   = 16; // UUID 128-bit = 16 bytes raw (1 AES block)
    private static final short LEN_NAME = 64;
    private static final short LEN_DOB  = 16;
    private static final short LEN_PHONE = 16;
    private static final short LEN_BALANCE_BLOCK = 16;

    private static final short IV_SIZE = 16;
    private static final short SALT_SIZE = 16;
    private static final short AES_KEY_SIZE = 16;

    // Plain text fields
    byte[] customerID;              // 15 bytes - PLAIN TEXT

    // RSA components
    private KeyPair rsaKeyPair;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    // Encrypted fields
    byte[] encryptedCardID;         // 16 bytes AES-CBC encrypted (padded from 15B customerID)
    byte[] cardUUID;                // 16 bytes AES-CBC encrypted (backend cardId as 128-bit raw UUID)
    byte[] name;                    // 64 bytes AES-CBC encrypted
    byte[] dateOfBirth;             // 16 bytes AES-CBC encrypted (format: YYYY-MM-DD)
    byte[] phoneNumber;             // 16 bytes AES-CBC encrypted
    byte[] encryptedBalance;        // 16 bytes AES-CBC encrypted (4-byte balance + padding)
    boolean cardIDEncrypted;
    boolean balanceInitialized;

    byte[] iv;
    byte[] salt;
    byte[] wrappedMasterKeyAdmin;
    boolean masterKeyWrappedAdmin;

    // Session key for encrypted admin PIN transmission
    byte[] sessionKey;
    boolean sessionKeySet;

    boolean dataReady;
    boolean dataEncrypted;
    boolean rsaKeyReady;

    // RSA private key encryption
    byte[] encryptedRSAPrivateKey;  // [modulus 128 bytes][private exponent 128 bytes] = 256 bytes
    boolean rsaPrivateKeyEncrypted;

    private byte[] sharedDecryptBuffer;  // 256 bytes transient - shared for decrypt ops and RSA key
    private byte[] masterKeyBuffer;      // 16 bytes transient - reused for master key operations

    public CardModel() {
        customerID = new byte[LEN_CUSTOMER_ID];

        rsaKeyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_1024);
        privateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
        publicKey = (RSAPublicKey) rsaKeyPair.getPublic();

        encryptedCardID = new byte[16];
        cardIDEncrypted = false;
        cardUUID = new byte[LEN_CARD_UUID];
        name = new byte[LEN_NAME];
        dateOfBirth = new byte[LEN_DOB];
        phoneNumber = new byte[LEN_PHONE];
        encryptedBalance = new byte[LEN_BALANCE_BLOCK];
        balanceInitialized = false;

        iv = new byte[IV_SIZE];
        salt = new byte[SALT_SIZE];
        generateIVAndSalt();

        dataReady = false;
        dataEncrypted = false;
        rsaKeyReady = false;

        wrappedMasterKeyAdmin = new byte[AES_KEY_SIZE];
        masterKeyWrappedAdmin = false;

        sessionKey = new byte[AES_KEY_SIZE];
        sessionKeySet = false;

        encryptedRSAPrivateKey = new byte[256];
        rsaPrivateKeyEncrypted = false;

        sharedDecryptBuffer = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        masterKeyBuffer = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
    }

    private void generateIVAndSalt() {
        RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rng.generateData(iv, (short)0, IV_SIZE);
        rng.generateData(salt, (short)0, SALT_SIZE);
    }

    public void ensureMasterKeyWithAdmin(CryptoManager crypto) {
        if (masterKeyWrappedAdmin) return;
        RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rng.generateData(masterKeyBuffer, (short)0, (short)16);
        crypto.wrapMasterKey(masterKeyBuffer, (short)0, wrappedMasterKeyAdmin, (short)0, iv, (short)0);
        masterKeyWrappedAdmin = true;
        Util.arrayFillNonAtomic(masterKeyBuffer, (short)0, (short)16, (byte)0);
    }

    public boolean isDataReady() { return dataReady; }
    public void setDataReady(boolean r) { dataReady = r; }
    public byte[] getIV() { return iv; }
    public byte[] getSalt() { return salt; }
    public boolean isDataEncrypted() { return dataEncrypted; }
    public boolean isRSAKeyReady() { return rsaKeyReady; }
    public byte[] getWrappedMasterKeyAdmin() { return wrappedMasterKeyAdmin; }
    public boolean isMasterKeyWrappedAdmin() { return masterKeyWrappedAdmin; }
    public void setMasterKeyWrappedAdmin(boolean ready) { masterKeyWrappedAdmin = ready; }

    public void setSessionKey(byte[] key, short offset) {
        Util.arrayCopyNonAtomic(key, offset, sessionKey, (short)0, AES_KEY_SIZE);
        sessionKeySet = true;
    }
    public byte[] getSessionKey() { return sessionKey; }
    public boolean isSessionKeySet() { return sessionKeySet; }

    /**
     * Store customer ID (plain text)
     */
    public void setCustomerID(byte[] id, short offset, short length) {
        if (length > LEN_CUSTOMER_ID) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        Util.arrayFillNonAtomic(customerID, (short)0, LEN_CUSTOMER_ID, (byte)0);
        Util.arrayCopyNonAtomic(id, offset, customerID, (short)0, length);
    }

    /**
     * Read customer ID (plain text - no PIN required)
     */
    public void getCustomerID(byte[] buffer, short offset) {
        Util.arrayCopyNonAtomic(customerID, (short)0, buffer, offset, LEN_CUSTOMER_ID);
    }

    /**
     * Write customer info from host.
     * Input APDU data: [customerID 15][cardUUID 16][name 64][dateOfBirth 16][phoneNumber 16] = 127 bytes.
     * customerID is padded to 16 bytes and encrypted into encryptedCardID.
     * cardUUID is 16-byte raw UUID (128-bit), encrypted into cardUUID field.
     * All fields AES-CBC encrypted. Requires admin authentication.
     */
    public void writeCustomerInfo(APDU apdu, CryptoManager crypto) {
        byte[] buf = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        short offset = ISO7816.OFFSET_CDATA;

        // Encrypt customerID: pad 15 bytes → 16 bytes (1 AES block)
        Util.arrayFillNonAtomic(sharedDecryptBuffer, (short)0, (short)16, (byte)0);
        Util.arrayCopyNonAtomic(buf, offset, sharedDecryptBuffer, (short)0, LEN_CUSTOMER_ID);
        crypto.encrypt(sharedDecryptBuffer, (short)0, (short)16, encryptedCardID, (short)0, iv, (short)0);
        cardIDEncrypted = true;
        offset += LEN_CUSTOMER_ID;

        // Encrypt cardUUID (16 bytes raw, 1 AES block)
        crypto.encrypt(buf, offset, LEN_CARD_UUID, cardUUID, (short)0, iv, (short)0);
        offset += LEN_CARD_UUID;

        // Encrypt name (64 bytes)
        crypto.encrypt(buf, offset, LEN_NAME, name, (short)0, iv, (short)0);
        offset += LEN_NAME;

        // Encrypt date of birth (16 bytes)
        crypto.encrypt(buf, offset, LEN_DOB, dateOfBirth, (short)0, iv, (short)0);
        offset += LEN_DOB;

        // Encrypt phone (16 bytes)
        crypto.encrypt(buf, offset, LEN_PHONE, phoneNumber, (short)0, iv, (short)0);

        dataEncrypted = true;
    }

    /**
     * Read customer info.
     * Returns [customerID 15][cardUUID 16][name 64][dateOfBirth 16][phoneNumber 16] = 127 bytes.
     * All encrypted fields are decrypted before returning.
     * Requires admin authentication.
     */
    public void readCustomerInfo(APDU apdu, CryptoManager crypto) {
        byte[] buf = apdu.getBuffer();
        short pos = 0;

        // Decrypt customerID (16 bytes → return first 15)
        crypto.decrypt(encryptedCardID, (short)0, (short)16, sharedDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(sharedDecryptBuffer, (short)0, buf, pos, LEN_CUSTOMER_ID);
        pos += LEN_CUSTOMER_ID;

        // Decrypt cardUUID (16 bytes raw UUID)
        crypto.decrypt(cardUUID, (short)0, LEN_CARD_UUID, sharedDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(sharedDecryptBuffer, (short)0, buf, pos, LEN_CARD_UUID);
        pos += LEN_CARD_UUID;

        // Decrypt name (64 bytes)
        crypto.decrypt(name, (short)0, LEN_NAME, sharedDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(sharedDecryptBuffer, (short)0, buf, pos, LEN_NAME);
        pos += LEN_NAME;

        // Decrypt date of birth (16 bytes)
        crypto.decrypt(dateOfBirth, (short)0, LEN_DOB, sharedDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(sharedDecryptBuffer, (short)0, buf, pos, LEN_DOB);
        pos += LEN_DOB;

        // Decrypt phone (16 bytes)
        crypto.decrypt(phoneNumber, (short)0, LEN_PHONE, sharedDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(sharedDecryptBuffer, (short)0, buf, pos, LEN_PHONE);
        pos += LEN_PHONE;

        apdu.setOutgoing();
        apdu.setOutgoingLength(pos); // 127 bytes total
        apdu.sendBytesLong(buf, (short)0, pos);
    }

    public void setBalance(int balance, CryptoManager crypto) {
        if (balance < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        Util.arrayFillNonAtomic(sharedDecryptBuffer, (short)0, LEN_BALANCE_BLOCK, (byte)0);
        writeInt(sharedDecryptBuffer, (short)0, balance);
        crypto.encrypt(sharedDecryptBuffer, (short)0, LEN_BALANCE_BLOCK, encryptedBalance, (short)0, iv, (short)0);
        balanceInitialized = true;
    }

    public int getBalance(CryptoManager crypto) {
        if (!balanceInitialized) {
            return 0;
        }

        crypto.decrypt(encryptedBalance, (short)0, LEN_BALANCE_BLOCK, sharedDecryptBuffer, (short)0, iv, (short)0);
        return readInt(sharedDecryptBuffer, (short)0);
    }

    public int deductBalance(int amount, CryptoManager crypto) {
        if (amount < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        int currentBalance = getBalance(crypto);
        if (currentBalance < amount) {
            ISOException.throwIt((short)0x6901);
        }

        int balanceAfter = currentBalance - amount;
        setBalance(balanceAfter, crypto);
        return balanceAfter;
    }

    /**
     * Clear all customer data: zeroes customerID, name, phoneNumber.
     * Used when returning card for reuse (card return / CLEAR_CARD_DATA command).
     * Requires admin authentication.
     */
    public void clearCardData() {
        Util.arrayFillNonAtomic(customerID, (short)0, LEN_CUSTOMER_ID, (byte)0);
        Util.arrayFillNonAtomic(encryptedCardID, (short)0, (short)16, (byte)0);
        cardIDEncrypted = false;
        Util.arrayFillNonAtomic(cardUUID, (short)0, LEN_CARD_UUID, (byte)0);
        Util.arrayFillNonAtomic(name, (short)0, LEN_NAME, (byte)0);
        Util.arrayFillNonAtomic(dateOfBirth, (short)0, LEN_DOB, (byte)0);
        Util.arrayFillNonAtomic(phoneNumber, (short)0, LEN_PHONE, (byte)0);
        Util.arrayFillNonAtomic(encryptedBalance, (short)0, LEN_BALANCE_BLOCK, (byte)0);
        dataEncrypted = false;
        balanceInitialized = false;

        rsaKeyReady = false;
        rsaPrivateKeyEncrypted = false;
    }

    /**
     * Generate RSA-1024 keypair on card.
     * Private key is encrypted with master key and stored in persistent memory.
     * Requires admin authentication (master key must be ready).
     */
    public void generateRSAKeyPair(CryptoManager crypto) {
        if (!crypto.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        rsaKeyPair.genKeyPair();
        rsaKeyReady = true;
        encryptRSAPrivateKey(crypto);
    }

    private void encryptRSAPrivateKey(CryptoManager crypto) {
        if (!crypto.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        short modLen = privateKey.getModulus(sharedDecryptBuffer, (short)0);
        if (modLen != 128) ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short expLen = privateKey.getExponent(sharedDecryptBuffer, (short)128);
        if (expLen != 128) ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        crypto.encrypt(sharedDecryptBuffer, (short)0, (short)256, encryptedRSAPrivateKey, (short)0, iv, (short)0);

        Util.arrayFillNonAtomic(sharedDecryptBuffer, (short)0, (short)256, (byte)0);
        privateKey.clearKey();
        rsaPrivateKeyEncrypted = true;
    }

    /**
     * Decrypt and import RSA private key into key object.
     * Called after admin PIN verification to enable signing.
     */
    public void decryptAndImportRSAPrivateKey(CryptoManager crypto) {
        if (!crypto.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (!rsaKeyReady || !rsaPrivateKeyEncrypted) return;

        Util.arrayFillNonAtomic(sharedDecryptBuffer, (short)0, (short)256, (byte)0);
        crypto.decrypt(encryptedRSAPrivateKey, (short)0, (short)256, sharedDecryptBuffer, (short)0, iv, (short)0);

        privateKey.clearKey();
        privateKey.setModulus(sharedDecryptBuffer, (short)0, (short)128);
        privateKey.setExponent(sharedDecryptBuffer, (short)128, (short)128);

        Util.arrayFillNonAtomic(sharedDecryptBuffer, (short)0, (short)256, (byte)0);
    }

    public short getPublicKeyExponent(byte[] buffer, short offset) {
        if (!rsaKeyReady) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        return publicKey.getExponent(buffer, offset);
    }

    public short getPublicKeyModulus(byte[] buffer, short offset) {
        if (!rsaKeyReady) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        return publicKey.getModulus(buffer, offset);
    }

    /**
     * Sign challenge with RSA private key (SHA1withRSA).
     */
    public short signChallenge(byte[] challenge, short chalOffset, short chalLen,
                               byte[] signature, short sigOffset) {
        if (!rsaKeyReady) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        Signature sig = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
        sig.init(privateKey, Signature.MODE_SIGN);
        return sig.sign(challenge, chalOffset, chalLen, signature, sigOffset);
    }

    private void writeInt(byte[] buffer, short offset, int value) {
        buffer[offset] = (byte)(value >> 24);
        buffer[(short)(offset + 1)] = (byte)(value >> 16);
        buffer[(short)(offset + 2)] = (byte)(value >> 8);
        buffer[(short)(offset + 3)] = (byte)value;
    }

    private int readInt(byte[] buffer, short offset) {
        return ((buffer[offset] & 0xFF) << 24)
            | ((buffer[(short)(offset + 1)] & 0xFF) << 16)
            | ((buffer[(short)(offset + 2)] & 0xFF) << 8)
            | (buffer[(short)(offset + 3)] & 0xFF);
    }
}
