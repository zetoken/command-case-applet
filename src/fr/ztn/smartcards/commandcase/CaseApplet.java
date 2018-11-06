package fr.ztn.smartcards.commandcase;

import javacard.framework.*;

public class CaseApplet extends Applet {
    byte[] lastBuffer;
    byte[] lastLc;
    byte[] lastLe;

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new CaseApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    public CaseApplet() {
        lastBuffer = new byte[300];
        lastLc = new byte[2];
        lastLe = new byte[2];
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] != (byte) 0x80) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case (byte) 0xC1:
                processCase1(apdu);
                break;
            case (byte) 0xC2:
                processCase2(apdu);
                break;
            case (byte) 0xC3:
                processCase3(apdu);
                break;
            case (byte) 0xC4:
                processCase4(apdu);
                break;
            case (byte) 0xC5:
                processGetProtocolT(apdu);
                break;
            case (byte) 0xCB:
                processGetLastX(apdu, lastBuffer);
                break;
            case (byte) 0xCC:
                processGetLastX(apdu, lastLc);
                break;
            case (byte) 0xCE:
                processGetLastX(apdu, lastLe);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Command Case 1 instruction: <code>80 C1 00 00</code>
     *
     * @param apdu Incoming APDU
     */
    void processCase1(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        saveLc((short) -1);
        saveLe((short) -1);
        saveBuffer(buffer);
    }

    /**
     * Command Case 2 instruction: <code>80 C2 P1 P2</code>
     * <p>
     * <code>P1</code>: selection of the outgoing length
     * <ul>
     * <li><code>0</code> to use <code>le</code>
     * <li><code>1</code> to use <code>P2</code>
     * <li><code>2</code> to use <code>min(le, P2)</code>
     * </ul>
     *
     * @param apdu Incoming APDU
     */
    void processCase2(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        saveLc((short) -1);

        short le = apdu.setOutgoing();
        saveLe(le);

        saveBuffer(buffer);

        short outgoingLength = getOutgoingLengthFromP1P2(buffer, le);
        apdu.setOutgoingLength(outgoingLength);
        apdu.sendBytes((short) 0, outgoingLength);
    }

    /**
     * Command Case 3 instruction: <code>80 C3 P1 P2 Lc UDC</code>
     *
     * @param apdu Incoming APDU
     */
    void processCase3(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        short lc = apdu.setIncomingAndReceive();
        saveLc(lc);

        saveLe((short) -1);

        saveBuffer(buffer);
    }

    /**
     * Command Case 2 instruction: <code>80 C4 P1 P2 Lc UDC Le</code>
     * <p>
     * <code>P1</code>: selection of the outgoing length
     * <ul>
     * <li><code>0</code> to use <code>le</code>
     * <li><code>1</code> to use <code>P2</code>
     * <li><code>2</code> to use <code>min(le, P2)</code>
     * </ul>
     *
     * @param apdu Incoming APDU
     */
    void processCase4(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        saveLc(lc);

        short le = apdu.setOutgoing();
        saveLe(le);

        saveBuffer(buffer);

        short outgoingLength = getOutgoingLengthFromP1P2(buffer, le);
        apdu.setOutgoingAndSend((short) 0, outgoingLength);
    }

    /**
     * Returns the content of the lastX buffer.
     *
     * @param apdu
     * @param lastX
     */
    void processGetLastX(APDU apdu, byte[] lastX) {
        byte[] buffer = apdu.getBuffer();

        short le = apdu.setOutgoing();
        apdu.setOutgoingLength(le);
        apdu.sendBytesLong(lastX, (short) 0, le);
    }

    /**
     * Returns the value of the transmission protocol T in use.
     * <p>
     * Command case 2 instruction: <code>80 C5 00 00 01</code>
     *
     * @param apdu Incoming APDU
     */
    void processGetProtocolT(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        buffer[0] = APDU.getProtocol();
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }

    void saveLc(short lc) {
        Util.setShort(lastLc, (short) 0, lc);
    }

    void saveLe(short le) {
        Util.setShort(lastLe, (short) 0, le);
    }

    void saveBuffer(byte[] buffer) {
        short length = (short) buffer.length < (short) lastBuffer.length ? (short) buffer.length
                : (short) lastBuffer.length;
        Util.arrayCopy(buffer, (short) 0, lastBuffer, (short) 0, length);
    }

    short getOutgoingLengthFromP1P2(byte[] buffer, short le) {
        switch (buffer[ISO7816.OFFSET_P1]) {
            case (byte) 0x00:
                return le;
            case (byte) 0x01:
                return Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_P2]);
            case (byte) 0x02:
                byte leAsByte = (byte) (le & (short) 0x00FF);
                return Util.makeShort((byte) 0x00,
                        buffer[ISO7816.OFFSET_P2] < leAsByte ? buffer[ISO7816.OFFSET_P2] : leAsByte);
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                return (short) -1;
        }
    }
}
