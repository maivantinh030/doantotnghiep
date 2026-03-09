package ParkCard;

import javacard.framework.*;

public class GameManager {
    private byte[] gameList;
    private short[] gameCountRef;
    private static final short MAX_GAMES = 50;
    private static final short GAME_ENTRY_SIZE = 3;

    public GameManager(byte[] list, short[] countRef) {
        this.gameList = list;
        this.gameCountRef = countRef;
    }

    private short getGameCount() { return gameCountRef[0]; }
    private void setGameCount(short c) { gameCountRef[0] = c; }

    private short findGameIndex(short gameCode) {
        for (short i = 0; i < getGameCount(); i++) {
            short offset = (short)(i * GAME_ENTRY_SIZE);
            short stored = (short)((gameList[(short)(offset + 1)] << 8) | (gameList[(short)(offset + 2)] & 0xFF));
            if (stored == gameCode) return i;
        }
        return -1;
    }

    public void addOrIncreaseTickets(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 3) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        byte tickets = buf[ISO7816.OFFSET_CDATA];
        short gameCode = (short)((buf[ISO7816.OFFSET_CDATA + 1] << 8) | (buf[ISO7816.OFFSET_CDATA + 2] & 0xFF));

        if (tickets <= 0) ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        short index = findGameIndex(gameCode);
        if (index == -1) {
            if (getGameCount() >= MAX_GAMES) ISOException.throwIt(ISO7816.SW_FILE_FULL);
            short offset = (short)(getGameCount() * GAME_ENTRY_SIZE);
            gameList[offset] = tickets;
            gameList[(short)(offset + 1)] = buf[ISO7816.OFFSET_CDATA + 1];
            gameList[(short)(offset + 2)] = buf[ISO7816.OFFSET_CDATA + 2];
            setGameCount((short)(getGameCount() + 1));
        } else {
            short offset = (short)(index * GAME_ENTRY_SIZE);
            byte current = gameList[offset];
            if ((short)(current + tickets) > 255) ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            gameList[offset] = (byte)(current + tickets);
        }
    }

    public void decreaseGameTickets(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 3) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
        byte tickets = buf[ISO7816.OFFSET_CDATA + 2];

        short index = findGameIndex(gameCode);
        if (index == -1 || tickets <= 0 || gameList[(short)(index * GAME_ENTRY_SIZE)] < tickets) {
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        }

        gameList[(short)(index * GAME_ENTRY_SIZE)] -= tickets;
    }

    public void readGames(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short count = getGameCount();
        buf[0] = (byte)(count >> 8);
        buf[1] = (byte)count;

        short bytes = (short)(count * GAME_ENTRY_SIZE);
        if (bytes > 0) {
            Util.arrayCopyNonAtomic(gameList, (short)0, buf, (short)2, bytes);
        }

        apdu.setOutgoing();
        apdu.setOutgoingLength((short)(2 + bytes));
        apdu.sendBytesLong(buf, (short)0, (short)(2 + bytes));
    }

    public void updateGameTickets(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 3) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
        byte newTickets = buf[ISO7816.OFFSET_CDATA + 2];

        short index = findGameIndex(gameCode);
        if (index == -1) ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);

        gameList[(short)(index * GAME_ENTRY_SIZE)] = newTickets;
    }

    public void findGame(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));

        short index = findGameIndex(gameCode);
        if (index == -1) {
            buf[0] = 0;
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)1);
            apdu.sendBytesLong(buf, (short)0, (short)1);
        } else {
            short offset = (short)(index * GAME_ENTRY_SIZE);
            buf[0] = 1;
            buf[1] = gameList[offset];
            buf[2] = gameList[(short)(offset + 1)];
            buf[3] = gameList[(short)(offset + 2)];
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)4);
            apdu.sendBytesLong(buf, (short)0, (short)4);
        }
    }

    public void removeGame(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));

        short index = findGameIndex(gameCode);
        if (index == -1) ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);

        short from = (short)((index + 1) * GAME_ENTRY_SIZE);
        short to = (short)(index * GAME_ENTRY_SIZE);
        short remain = (short)((getGameCount() - index - 1) * GAME_ENTRY_SIZE);

        if (remain > 0) {
            Util.arrayCopyNonAtomic(gameList, from, gameList, to, remain);
        }
        setGameCount((short)(getGameCount() - 1));
    }
}