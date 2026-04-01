import com.licel.jcardsim.io.JavaxSmartCardInterface;
import javacard.framework.AID;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import ParkCard.CustomerCardApplet;

/**
 * Test: set/get customerID (10 ký tự) với luồng bảo mật PIN đầy đủ.
 *
 * Luồng:
 *   1. Tạo Admin PIN
 *   2. Xác thực Admin PIN  →  card mở khóa (security)
 *   3. Ghi Customer ID "PARKCARD01" (10 ký tự)
 *   4. Đọc Customer ID lại và kiểm tra
 */
public class ParkCardTest {

    // AID: 11 11 11 11 11 00  (lấy từ ParkCard.jcproj)
    private static final byte[] APPLET_AID_BYTES = {
        0x11, 0x11, 0x11, 0x11, 0x11, 0x00
    };

    private static final byte CLA = 0x00;

    // INS codes (phải khớp với CustomerCardApplet.java)
    private static final byte INS_CREATE_ADMIN_PIN    = (byte) 0x20;
    private static final byte INS_VERIFY_ADMIN_PIN    = (byte) 0x1F;
    private static final byte INS_GET_ADMIN_PIN_TRIES = (byte) 0x22;
    private static final byte INS_SET_CUSTOMER_ID     = (byte) 0x17;
    private static final byte INS_GET_CUSTOMER_ID     = (byte) 0x18;
    private static final byte INS_WRITE_INFO          = (byte) 0x07;  // yêu cầu auth
    private static final byte INS_READ_INFO           = (byte) 0x0B;  // yêu cầu auth

    private static final int SW_OK = 0x9000;

    private JavaxSmartCardInterface sim;

    @Before
    public void setUp() throws Exception {
        sim = new JavaxSmartCardInterface();
        AID appletAID = new AID(APPLET_AID_BYTES, (short) 0, (byte) APPLET_AID_BYTES.length);

        // Format chuẩn JavaCard install params:
        //   [aidLen][aidBytes][privilegesLen=0][appParamsLen=0]
        byte[] installParams = new byte[1 + APPLET_AID_BYTES.length + 2];
        installParams[0] = (byte) APPLET_AID_BYTES.length;
        System.arraycopy(APPLET_AID_BYTES, 0, installParams, 1, APPLET_AID_BYTES.length);
        // privileges length = 0, app params length = 0 (đã là 0 mặc định)

        sim.installApplet(appletAID, CustomerCardApplet.class,
                          installParams, (short) 0, (byte) installParams.length);
        sim.selectApplet(appletAID);
        System.out.println("\n--- Card selected ---");
    }

    // ---------------------------------------------------------------
    // Test 1: Luồng đầy đủ — tạo PIN → xác thực → ghi ID → đọc ID
    // ---------------------------------------------------------------
    @Test
    public void testSetAndGetCustomerID_WithAuth() {
        System.out.println("\n[TEST 1] Set/Get CustomerID với xác thực PIN");

        // Bước 1: Tạo Admin PIN = "ADMIN123" (8 ký tự, trong giới hạn 4-8)
        byte[] pin = "ADMIN123".getBytes();
        ResponseAPDU resp = send(INS_CREATE_ADMIN_PIN, pin);
        assertSW("Tạo Admin PIN", SW_OK, resp);

        // Bước 2: Xác thực Admin PIN (security — card sẽ unwrap master key)
        resp = send(INS_VERIFY_ADMIN_PIN, pin);
        assertSW("Xác thực Admin PIN", SW_OK, resp);
        System.out.println("  >> PIN xác thực thành công, master key đã được nạp vào RAM");

        // Bước 3: Ghi Customer ID "PARKCARD01" (10 ký tự)
        byte[] customerID = "PARKCARD01".getBytes();  // 10 bytes
        resp = send(INS_SET_CUSTOMER_ID, customerID);
        assertSW("Ghi Customer ID", SW_OK, resp);
        System.out.println("  >> Đã ghi Customer ID: PARKCARD01");

        // Bước 4: Đọc Customer ID lại (card trả về 15 bytes, 5 bytes cuối là 0x00)
        resp = sendNoData(INS_GET_CUSTOMER_ID, 15);
        assertSW("Đọc Customer ID", SW_OK, resp);

        byte[] result = resp.getData();
        assertEquals("Độ dài response phải là 15 bytes", 15, result.length);

        // Kiểm tra 10 bytes đầu khớp "PARKCARD01"
        for (int i = 0; i < customerID.length; i++) {
            assertEquals("Byte [" + i + "] không khớp", customerID[i], result[i]);
        }
        // 5 bytes còn lại phải là 0x00 (padding)
        for (int i = customerID.length; i < 15; i++) {
            assertEquals("Byte [" + i + "] phải là 0x00", (byte) 0x00, result[i]);
        }

        System.out.println("  >> Đọc lại Customer ID: " + extractString(result, 10));
        System.out.println("[TEST 1] PASSED");
    }

    // ---------------------------------------------------------------
    // Test 2: Không cần xác thực để set/get CustomerID
    //         (CustomerID là plain-text, không yêu cầu requireAuthenticated)
    // ---------------------------------------------------------------
    @Test
    public void testSetAndGetCustomerID_WithoutAuth() {
        System.out.println("\n[TEST 2] Set/Get CustomerID KHÔNG cần xác thực (plain-text field)");

        byte[] customerID = "PARKCARD01".getBytes();
        ResponseAPDU resp = send(INS_SET_CUSTOMER_ID, customerID);
        assertSW("Ghi Customer ID (no auth)", SW_OK, resp);

        resp = sendNoData(INS_GET_CUSTOMER_ID, 15);
        assertSW("Đọc Customer ID (no auth)", SW_OK, resp);

        byte[] result = resp.getData();
        assertEquals("PARKCARD01", extractString(result, 10));

        System.out.println("  >> Customer ID: " + extractString(result, 10));
        System.out.println("[TEST 2] PASSED");
    }

    // ---------------------------------------------------------------
    // Test 3: PIN sai thì bị từ chối, trừ số lần thử
    // ---------------------------------------------------------------
    @Test
    public void testWrongPIN_IsRejected() {
        System.out.println("\n[TEST 3] PIN sai bị từ chối");

        byte[] correctPin = "ADMIN123".getBytes();
        byte[] wrongPin   = "WRONGPIN".getBytes();

        send(INS_CREATE_ADMIN_PIN, correctPin);

        // Lần 1: PIN sai
        ResponseAPDU resp = send(INS_VERIFY_ADMIN_PIN, wrongPin);
        assertNotEquals("PIN sai phải bị từ chối", SW_OK, resp.getSW());
        System.out.println("  >> PIN sai → SW: " + String.format("0x%04X", resp.getSW()));

        // Kiểm tra số lần thử còn lại giảm xuống
        resp = sendNoData(INS_GET_ADMIN_PIN_TRIES, 3);
        assertSW("Đọc số lần thử", SW_OK, resp);
        byte triesLeft = resp.getData()[0];
        assertEquals("Phải còn 4 lần thử (5 - 1)", 4, triesLeft);
        System.out.println("  >> Số lần thử còn lại: " + triesLeft);

        // PIN đúng vẫn hoạt động
        resp = send(INS_VERIFY_ADMIN_PIN, correctPin);
        assertSW("PIN đúng vẫn OK sau khi thử sai", SW_OK, resp);
        System.out.println("  >> PIN đúng vẫn xác thực được");
        System.out.println("[TEST 3] PASSED");
    }

    // ---------------------------------------------------------------
    // Test 4: CardID 10 ký tự + bảo mật đầy đủ qua WRITE_INFO/READ_INFO
    //
    //   INS_WRITE_INFO (0x07) và INS_READ_INFO (0x0B) đều yêu cầu
    //   requireAuthenticated() — đây là luồng bảo mật thực sự.
    //
    //   READ_INFO trả về: [15 bytes cardID][64 bytes name][16 bytes phone]
    //   → cardID được đọc ra từ response cùng với dữ liệu được mã hóa.
    // ---------------------------------------------------------------
    @Test
    public void testCardID_WithFullSecurity() {
        final String CARD_ID  = "PARKCARD01";   // 10 ký tự
        final String NAME     = padRight("Nguyen Van A", 64);  // pad đủ 64 bytes
        final String PHONE    = padRight("0912345678", 16);    // pad đủ 16 bytes
        final byte[] PIN      = "ADMIN123".getBytes();

        System.out.println("\n[TEST 4] CardID 10 ky tu + bao mat WRITE_INFO/READ_INFO");
        System.out.println("  CardID : " + CARD_ID);
        System.out.println("  Name   : " + NAME.trim());
        System.out.println("  Phone  : " + PHONE.trim());

        // --- Bước 1: Tạo PIN ---
        assertSW("Tao Admin PIN", SW_OK, send(INS_CREATE_ADMIN_PIN, PIN));

        // --- Bước 2: Thử WRITE_INFO khi CHƯA xác thực → phải bị từ chối ---
        byte[] infoPayload = buildInfoPayload(CARD_ID, NAME, PHONE);
        ResponseAPDU resp = send(INS_WRITE_INFO, infoPayload);
        assertNotEquals("WRITE_INFO khong co auth phai bi tu choi", SW_OK, resp.getSW());
        System.out.println("  [OK] WRITE_INFO khong auth bi tu choi  SW=" +
                           String.format("0x%04X", resp.getSW()));

        // --- Bước 3: Thử READ_INFO khi CHƯA xác thực → phải bị từ chối ---
        resp = sendNoData(INS_READ_INFO, 95);
        assertNotEquals("READ_INFO khong co auth phai bi tu choi", SW_OK, resp.getSW());
        System.out.println("  [OK] READ_INFO  khong auth bi tu choi  SW=" +
                           String.format("0x%04X", resp.getSW()));

        // --- Bước 4: Ghi CardID (plain-text, không cần auth) ---
        assertSW("Set CardID", SW_OK, send(INS_SET_CUSTOMER_ID, CARD_ID.getBytes()));
        System.out.println("  [OK] Da ghi CardID: " + CARD_ID);

        // --- Bước 5: Xác thực PIN → card mở khóa, master key vào RAM ---
        assertSW("Verify PIN", SW_OK, send(INS_VERIFY_ADMIN_PIN, PIN));
        System.out.println("  [OK] PIN xac thuc thanh cong, master key san sang");

        // --- Bước 6: WRITE_INFO sau khi xác thực → thành công ---
        assertSW("WRITE_INFO sau auth", SW_OK, send(INS_WRITE_INFO, infoPayload));
        System.out.println("  [OK] Da ghi name+phone (ma hoa AES-CBC)");

        // --- Bước 7: READ_INFO → trả về [cardID 15B][name 64B][phone 16B] ---
        resp = sendNoData(INS_READ_INFO, 95);
        assertSW("READ_INFO sau auth", SW_OK, resp);

        byte[] result = resp.getData();
        assertEquals("Response phai la 95 bytes", 95, result.length);

        // Kiểm tra CardID (15 bytes đầu)
        String returnedID = new String(result, 0, 15).trim();
        assertEquals("CardID khong khop", CARD_ID, returnedID);
        System.out.println("  [OK] CardID doc lai : " + returnedID);

        // Kiểm tra Name (64 bytes tiếp theo, sau khi decrypt)
        String returnedName = new String(result, 15, 64).trim();
        assertEquals("Name khong khop", NAME.trim(), returnedName);
        System.out.println("  [OK] Name   doc lai : " + returnedName);

        // Kiểm tra Phone (16 bytes cuối)
        String returnedPhone = new String(result, 79, 16).trim();
        assertEquals("Phone khong khop", PHONE.trim(), returnedPhone);
        System.out.println("  [OK] Phone  doc lai : " + returnedPhone);

        System.out.println("[TEST 4] PASSED");
    }

    /** Tạo payload 95 bytes cho INS_WRITE_INFO: [15 bytes cardID][64 bytes name][16 bytes phone] */
    private byte[] buildInfoPayload(String cardID, String name, String phone) {
        byte[] payload = new byte[95];
        byte[] cb = cardID.getBytes();
        byte[] nb = name.getBytes();
        byte[] pb = phone.getBytes();
        System.arraycopy(cb, 0, payload, 0,  Math.min(cb.length, 15));
        System.arraycopy(nb, 0, payload, 15, Math.min(nb.length, 64));
        System.arraycopy(pb, 0, payload, 79, Math.min(pb.length, 16));
        return payload;
    }

    /** Pad chuỗi bằng space đến độ dài cố định */
    private String padRight(String s, int len) {
        return String.format("%-" + len + "s", s);
    }

    // ---------------------------------------------------------------
    // Test 5: Đọc thông tin mã hóa KHI có PIN → thành công
    // ---------------------------------------------------------------
    @Test
    public void testReadEncryptedInfo_WithPIN() {
        final String CARD_ID = "PARKCARD01";
        final String NAME    = padRight("Nguyen Van A", 64);
        final String PHONE   = padRight("0912345678", 16);
        final byte[] PIN     = "ADMIN123".getBytes();

        System.out.println("\n[TEST 5] Doc thong tin ma hoa KHI CO PIN");

        // Setup: tạo PIN, xác thực, ghi dữ liệu
        assertSW("Tao PIN", SW_OK, send(INS_CREATE_ADMIN_PIN, PIN));
        assertSW("Verify PIN", SW_OK, send(INS_VERIFY_ADMIN_PIN, PIN));
        assertSW("Ghi data", SW_OK, send(INS_WRITE_INFO, buildInfoPayload(CARD_ID, NAME, PHONE)));
        System.out.println("  Da ghi: CardID=" + CARD_ID + " | Name=" + NAME.trim() + " | Phone=" + PHONE.trim());

        // Đọc lại — đang authenticated → phải thành công và trả về đúng nội dung
        ResponseAPDU resp = sendNoData(INS_READ_INFO, 95);
        assertSW("READ_INFO co PIN phai thanh cong", SW_OK, resp);

        byte[] result = resp.getData();
        assertEquals("Response phai 95 bytes", 95, result.length);

        String gotID    = new String(result,  0, 15).trim();
        String gotName  = new String(result, 15, 64).trim();
        String gotPhone = new String(result, 79, 16).trim();

        assertEquals("CardID sai", CARD_ID,       gotID);
        assertEquals("Name sai",   NAME.trim(),   gotName);
        assertEquals("Phone sai",  PHONE.trim(),  gotPhone);

        System.out.println("  CardID : " + gotID   + "  [OK]");
        System.out.println("  Name   : " + gotName  + "  [OK]");
        System.out.println("  Phone  : " + gotPhone + "  [OK]");
        System.out.println("[TEST 5] PASSED");
    }

    // ---------------------------------------------------------------
    // Test 6: Đọc thông tin mã hóa KHI KHÔNG có PIN → bị từ chối
    // ---------------------------------------------------------------
    @Test
    public void testReadEncryptedInfo_WithoutPIN() {
        final String CARD_ID = "PARKCARD01";
        final String NAME    = padRight("Nguyen Van A", 64);
        final String PHONE   = padRight("0912345678", 16);
        final byte[] PIN     = "ADMIN123".getBytes();

        System.out.println("\n[TEST 6] Doc thong tin ma hoa KHI KHONG CO PIN");

        // Setup: tạo PIN, xác thực, ghi dữ liệu, rồi deselect (xóa session)
        assertSW("Tao PIN", SW_OK, send(INS_CREATE_ADMIN_PIN, PIN));
        assertSW("Verify PIN", SW_OK, send(INS_VERIFY_ADMIN_PIN, PIN));
        assertSW("Ghi data", SW_OK, send(INS_WRITE_INFO, buildInfoPayload(CARD_ID, NAME, PHONE)));
        System.out.println("  Da ghi data khi authenticated");

        // Simulate deselect: reselect card (master key bị xóa khỏi RAM)
        AID appletAID = new AID(APPLET_AID_BYTES, (short) 0, (byte) APPLET_AID_BYTES.length);
        sim.selectApplet(appletAID);
        System.out.println("  Reselect card (master key bi xoa khoi RAM)");

        // Thử đọc mà KHÔNG verify PIN → phải bị từ chối (0x6982)
        ResponseAPDU resp = sendNoData(INS_READ_INFO, 95);
        assertNotEquals("READ_INFO khong PIN phai bi tu choi", SW_OK, resp.getSW());
        assertEquals("SW phai la 0x6982 (Security Status Not Satisfied)",
                     0x6982, resp.getSW());

        System.out.println("  READ_INFO bi tu choi  SW=" + String.format("0x%04X", resp.getSW()) + "  [OK]");

        // Xác nhận: sau khi nhập đúng PIN thì đọc được
        assertSW("Verify PIN", SW_OK, send(INS_VERIFY_ADMIN_PIN, PIN));
        resp = sendNoData(INS_READ_INFO, 95);
        assertSW("READ_INFO sau khi co PIN phai thanh cong", SW_OK, resp);

        String gotID = new String(resp.getData(), 0, 15).trim();
        assertEquals("CardID sau verify phai khop", CARD_ID, gotID);
        System.out.println("  Sau khi verify PIN: CardID=" + gotID + "  [OK]");
        System.out.println("[TEST 6] PASSED");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private ResponseAPDU send(byte ins, byte[] data) {
        return sim.transmitCommand(new CommandAPDU(CLA, ins, 0, 0, data));
    }

    private ResponseAPDU sendNoData(byte ins, int expectedLen) {
        return sim.transmitCommand(new CommandAPDU(CLA, ins, 0, 0, expectedLen));
    }

    private void assertSW(String msg, int expected, ResponseAPDU resp) {
        assertEquals(
            msg + " → SW mong đợi: " + String.format("0x%04X", expected)
                + ", nhận được: " + String.format("0x%04X", resp.getSW()),
            expected, resp.getSW());
    }

    private String extractString(byte[] data, int len) {
        return new String(data, 0, Math.min(len, data.length)).trim();
    }
}
