import com.licel.jcardsim.io.JavaxSmartCardInterface;
import javacard.framework.AID;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import ParkCard.CustomerCardApplet;

import static org.junit.Assert.assertEquals;

public class ParkCardBalanceTest {

    private static final byte[] APPLET_AID_BYTES = {
        0x11, 0x11, 0x11, 0x11, 0x11, 0x00
    };

    private static final byte CLA = 0x00;
    private static final byte INS_CREATE_ADMIN_PIN = (byte) 0x20;
    private static final byte INS_VERIFY_ADMIN_PIN = (byte) 0x1F;
    private static final byte INS_SET_BALANCE = (byte) 0x0D;
    private static final byte INS_GET_BALANCE = (byte) 0x0E;
    private static final byte INS_DEDUCT_BALANCE = (byte) 0x0F;
    private static final byte INS_CLEAR_CARD_DATA = (byte) 0x30;

    private static final int SW_OK = 0x9000;
    private static final int SW_INSUFFICIENT_BALANCE = 0x6901;

    private JavaxSmartCardInterface sim;

    @Before
    public void setUp() throws Exception {
        sim = new JavaxSmartCardInterface();
        AID appletAID = new AID(APPLET_AID_BYTES, (short) 0, (byte) APPLET_AID_BYTES.length);

        byte[] installParams = new byte[1 + APPLET_AID_BYTES.length + 2];
        installParams[0] = (byte) APPLET_AID_BYTES.length;
        System.arraycopy(APPLET_AID_BYTES, 0, installParams, 1, APPLET_AID_BYTES.length);

        sim.installApplet(appletAID, CustomerCardApplet.class,
            installParams, (short) 0, (byte) installParams.length);
        sim.selectApplet(appletAID);
    }

    @Test
    public void balanceCommands_shouldSetGetDeductAndReset() {
        byte[] pin = "ADMIN123".getBytes();

        assertSW("Tao Admin PIN", SW_OK, send(INS_CREATE_ADMIN_PIN, pin));
        assertSW("Verify Admin PIN", SW_OK, send(INS_VERIFY_ADMIN_PIN, pin));

        assertSW("SET_BALANCE", SW_OK, sendInt(INS_SET_BALANCE, 120000));

        ResponseAPDU response = sendNoData(INS_GET_BALANCE, 4);
        assertSW("GET_BALANCE", SW_OK, response);
        assertEquals(120000, extractInt(response));

        response = sendInt(INS_DEDUCT_BALANCE, 20000);
        assertSW("DEDUCT_BALANCE", SW_OK, response);
        assertEquals(100000, extractInt(response));

        response = sendInt(INS_DEDUCT_BALANCE, 150000);
        assertEquals(SW_INSUFFICIENT_BALANCE, response.getSW());

        response = sendNoData(INS_GET_BALANCE, 4);
        assertSW("GET_BALANCE sau khi tru that bai", SW_OK, response);
        assertEquals(100000, extractInt(response));

        assertSW("Verify Admin PIN truoc khi clear", SW_OK, send(INS_VERIFY_ADMIN_PIN, pin));
        assertSW("CLEAR_CARD_DATA", SW_OK, sendNoData(INS_CLEAR_CARD_DATA));

        response = sendNoData(INS_GET_BALANCE, 4);
        assertSW("GET_BALANCE sau reset", SW_OK, response);
        assertEquals(0, extractInt(response));
    }

    private ResponseAPDU send(byte ins, byte[] data) {
        return sim.transmitCommand(new CommandAPDU(CLA, ins, 0, 0, data));
    }

    private ResponseAPDU sendNoData(byte ins, int expectedLen) {
        return sim.transmitCommand(new CommandAPDU(CLA, ins, 0, 0, expectedLen));
    }

    private ResponseAPDU sendNoData(byte ins) {
        return sim.transmitCommand(new CommandAPDU(CLA, ins, 0, 0));
    }

    private ResponseAPDU sendInt(byte ins, int value) {
        byte[] payload = new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
        return send(ins, payload);
    }

    private int extractInt(ResponseAPDU response) {
        byte[] data = response.getData();
        assertEquals(4, data.length);
        return ((data[0] & 0xFF) << 24)
            | ((data[1] & 0xFF) << 16)
            | ((data[2] & 0xFF) << 8)
            | (data[3] & 0xFF);
    }

    private void assertSW(String message, int expected, ResponseAPDU response) {
        assertEquals(message, expected, response.getSW());
    }
}
