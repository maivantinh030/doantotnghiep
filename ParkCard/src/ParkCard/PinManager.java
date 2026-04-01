package ParkCard;

import javacard.framework.*;
import javacard.security.*;

public class PinManager {
    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte ADMIN_PIN_MAX_TRIES = 5;

    private OwnerPIN adminPIN;
    private boolean adminPinCreated;

    private CryptoManager cryptoManager;
    private CardModel cardModel;

    public PinManager() {
        adminPIN = new OwnerPIN(ADMIN_PIN_MAX_TRIES, PIN_MAX_LENGTH);
        adminPinCreated = false;
    }

    public void setCryptoManager(CryptoManager crypto) { this.cryptoManager = crypto; }
    public void setCardModel(CardModel model) { this.cardModel = model; }

    public boolean isPINValidated() {
        return false;
    }

    public boolean isAdminPINValidated() {
        return adminPIN.isValidated();
    }

    // User PIN is removed by design.
    public void createPIN(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }

    // User PIN is removed by design.
    public boolean verify(byte[] buf, short offset, byte len) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        return false;
    }

    // User PIN is removed by design.
    public byte getTriesRemaining() {
        return 0;
    }

    // User PIN is removed by design.
    public void changePIN(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }

    // User PIN is removed by design.
    public void getPinTries(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }

    // User PIN is removed by design.
    public void resetPinCounter(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }

    public void createAdminPIN(APDU apdu) {
        if (adminPinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        adminPIN.update(buf, ISO7816.OFFSET_CDATA, (byte) lc);
        adminPinCreated = true;
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

    public boolean verifyAdmin(byte[] buf, short offset, byte len) {
        if (!adminPinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (adminPIN.check(buf, offset, len)) {
            if (cryptoManager != null && cardModel != null) {
                byte[] salt = cardModel.getSalt();
                cryptoManager.deriveKeyFromPIN(buf, offset, len, salt, (short)0, (short)16);

                if (cardModel.isMasterKeyWrappedAdmin()) {
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKeyAdmin(), (short)0, cardModel.getIV(), (short)0);
                } else {
                    cardModel.ensureMasterKeyWithAdmin(cryptoManager);
                    cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKeyAdmin(), (short)0, cardModel.getIV(), (short)0);
                }
            }
            return true;
        }

        return false;
    }

    public byte getAdminTriesRemaining() {
        return adminPIN.getTriesRemaining();
    }

    // User PIN is removed by design.
    public void resetUserPIN(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
}