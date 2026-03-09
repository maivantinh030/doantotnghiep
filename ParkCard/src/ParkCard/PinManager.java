package ParkCard;
import javacard.framework.*;
import javacard.security.*;

public class PinManager {
    private OwnerPIN userPIN;
    private OwnerPIN adminPIN;
    private boolean pinCreated;
    private boolean adminPinCreated;
    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte PIN_MAX_TRIES = 3;
    private static final byte ADMIN_PIN_MAX_TRIES = 5;

    // References provided from outside
    private CryptoManager cryptoManager;
    private CardModel cardModel;

    public PinManager() {
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        adminPIN = new OwnerPIN(ADMIN_PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pinCreated = false;
        adminPinCreated = false;
    }

    public void setCryptoManager(CryptoManager crypto) { this.cryptoManager = crypto; }
    public void setCardModel(CardModel model) { this.cardModel = model; }
    
    public boolean isPINValidated() {
        return userPIN.isValidated();
    }
    
    public boolean isAdminPINValidated() {
        return adminPIN.isValidated();
    }

    public void createPIN(APDU apdu) {
        if (pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        userPIN.update(buf, ISO7816.OFFSET_CDATA, (byte) lc);
        pinCreated = true;
    }

    /**
     * Create Admin PIN - chỉ được gọi khi admin connect lần đầu
     * APDU data: [PIN bytes]
     */
    public void createAdminPIN(APDU apdu) {
        if (adminPinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        adminPIN.update(buf, ISO7816.OFFSET_CDATA, (byte) lc);
        adminPinCreated = true;
    }

    /**
     * Verify user PIN -> derive PIN key -> unwrap user wrapped key
     */
    public boolean verify(byte[] buf, short offset, byte len) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        if (userPIN.check(buf, offset, len)) {
            if (cryptoManager != null && cardModel != null) {
                byte[] salt = cardModel.getSalt();
                cryptoManager.deriveKeyFromPIN(buf, offset, len, salt, (short)0, (short)16);
                
                if (cardModel.isMasterKeyWrapped()) {
                    // User wrapped key đã có sẵn (được tạo khi admin verify), unwrap nó
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKey(), (short)0, cardModel.getIV(), (short)0);
                } else {
                    // Chưa có user wrapped key, tạo master key mới
                    cardModel.ensureMasterKey(cryptoManager);
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKey(), (short)0, cardModel.getIV(), (short)0);
                }
            }
            return true;
        }
        return false;
    }

    public byte getTriesRemaining() { return userPIN.getTriesRemaining(); }

		/**
		 * Change PIN -> unwrap master with old PIN -> rewrap with new PIN (no bulk re-encrypt).
		 * Assumes old PIN has already been verified via verify() call.
		 */
		public void changePIN(APDU apdu) {
			byte[] buf = apdu.getBuffer();
			apdu.setIncomingAndReceive();
			
			byte oldLen = buf[ISO7816.OFFSET_CDATA];
			short oldOffset = (short)(ISO7816.OFFSET_CDATA + 1);
			byte newLen = buf[(short)(oldOffset + oldLen)];
			short newOffset = (short)(oldOffset + oldLen + 1);
			
			// Validate length
			if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			}
			
			// Verify old PIN
			if (!userPIN.check(buf, oldOffset, oldLen)) {
				ISOException.throwIt((short)(0x63C0 | userPIN.getTriesRemaining()));
			}
			
			// Re-wrap master key vi new PIN (nu c� crypto setup)
			if (cardModel != null && cryptoManager != null && cryptoManager.isKeyReady()) {
				byte[] salt = cardModel.getSalt();
				byte[] iv = cardModel.getIV();
				
				// Derive new PIN key (preserve loaded master key)
				cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16, true);
				
				// Re-wrap master key vi new PIN key
				cryptoManager.wrapLoadedMasterKey(
					cardModel.getWrappedMasterKey(), (short)0, 
					iv, (short)0
				);
			}
			
			// Update PIN
			userPIN.update(buf, newOffset, newLen);
			
			// Restore authenticated state
			userPIN.check(buf, newOffset, newLen);
		}

    public void getPinTries(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = userPIN.getTriesRemaining();
        buf[1] = pinCreated ? (byte)1 : (byte)0;
        buf[2] = userPIN.isValidated() ? (byte)1 : (byte)0;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)3);
        apdu.sendBytesLong(buf, (short)0, (short)3);
    }
    
    public void getAdminPinTries(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = adminPIN.getTriesRemaining();
        buf[1] = adminPinCreated ? (byte)1 : (byte)0;
        buf[2] = adminPIN.isValidated() ? (byte)1 : (byte)0;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)3);
        apdu.sendBytesLong(buf, (short)0, (short)3);
    }

    public void resetPinCounter(APDU apdu) {
        userPIN.resetAndUnblock();
    }
    
    /**
     * Verify admin PIN -> derive admin PIN key -> unwrap admin wrapped key
     * Nếu chưa có master key, tạo mới và wrap với cả admin và user PIN
     */
    public boolean verifyAdmin(byte[] buf, short offset, byte len) {
        if (!adminPinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        if (adminPIN.check(buf, offset, len)) {
            if (cryptoManager != null && cardModel != null) {
                byte[] salt = cardModel.getSalt();
                cryptoManager.deriveKeyFromPIN(buf, offset, len, salt, (short)0, (short)16);
                
                if (cardModel.isMasterKeyWrappedAdmin()) {
                    // Admin wrapped key đã có, unwrap nó
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKeyAdmin(), (short)0, cardModel.getIV(), (short)0);
                } else {
                    // Chưa có admin wrapped key, tạo master key mới và wrap với cả admin và user PIN
                    cardModel.ensureMasterKeyWithAdmin(cryptoManager, true);
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKeyAdmin(), (short)0, cardModel.getIV(), (short)0);
                    // Tự động wrap master key với user PIN mặc định "1234"
                    syncMasterKeyToUserPIN();
                }
            }
            return true;
        }
        return false;
    }
    
    public byte getAdminTriesRemaining() { return adminPIN.getTriesRemaining(); }
    
    /**
     * Admin reset User PIN - chỉ admin mới có quyền này
     * Admin đã authenticated, master key đã được load trong cryptoManager
     * Điều này đảm bảo master key được sync: admin dùng master key đang trong RAM để wrap cho user
     * APDU data: [newLen(1)][newPIN]
     */
    public void resetUserPIN(APDU apdu) {
        // Phải verify admin PIN trước
        if (!adminPIN.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        // Master key phải đã được load (từ admin verify)
        if (cryptoManager == null || !cryptoManager.isKeyReady()) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        byte[] buf = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        
        byte newLen = buf[ISO7816.OFFSET_CDATA];
        short newOffset = (short)(ISO7816.OFFSET_CDATA + 1);
        
        if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Re-wrap master key (đã load trong RAM từ admin verify) với new User PIN
        // Điều này đảm bảo master key được sync: user sẽ dùng cùng master key với admin
        if (cardModel != null && cryptoManager != null) {
            byte[] salt = cardModel.getSalt();
            byte[] iv = cardModel.getIV();
            
            // Derive new User PIN key từ new PIN (preserve loaded master key)
            cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16, true);
            
            // Re-wrap master key (đang trong RAM từ admin) với new User PIN key
            cryptoManager.wrapLoadedMasterKey(
                cardModel.getWrappedMasterKey(), (short)0, 
                iv, (short)0
            );
            cardModel.setMasterKeyWrapped(true);
        }
        
        // Update User PIN
        userPIN.update(buf, newOffset, newLen);
        pinCreated = true;
        userPIN.resetAndUnblock();
    }
    
    /**
     * Wrap master key (đang trong RAM) với user PIN mặc định "1234"
     * Được gọi khi admin verify lần đầu để tạo user wrapped key
     */
    private void syncMasterKeyToUserPIN() {
        if (cryptoManager == null || cardModel == null) {
            return;
        }
        // Master key phải đã được load trong RAM
        if (!cryptoManager.isKeyReady()) {
            return;
        }
        
        // Chỉ sync nếu chưa có user wrapped key
        if (cardModel.isMasterKeyWrapped()) {
            return;
        }
        
        // User PIN phải đã được tạo
        if (!pinCreated) {
            return;
        }
        
        // Derive user PIN key từ PIN mặc định "1234" (preserve master key đã load)
        byte[] defaultPin = { '1', '2', '3', '4' };
        byte[] salt = cardModel.getSalt();
        if (salt == null) {
            return; // Salt chưa được khởi tạo
        }
        cryptoManager.deriveKeyFromPIN(defaultPin, (short)0, (byte)4, salt, (short)0, (short)16, true);
        
        // Wrap master key (đang trong RAM) với user PIN key
        byte[] iv = cardModel.getIV();
        if (iv == null) {
            return; // IV chưa được khởi tạo
        }
        
        cryptoManager.wrapLoadedMasterKey(
            cardModel.getWrappedMasterKey(), (short)0, 
            iv, (short)0
        );
        cardModel.setMasterKeyWrapped(true);
    }
}