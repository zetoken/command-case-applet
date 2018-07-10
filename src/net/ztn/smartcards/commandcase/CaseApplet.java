package net.ztn.smartcards.commandcase;

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

	void processCase1(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		saveLc((short) -1);
		saveLe((short) -1);
		saveBuffer(buffer);
	}

	void processCase2(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		saveLc((short) -1);

		short le = apdu.setOutgoing();
		saveLe(le);

		saveBuffer(buffer);

		apdu.setOutgoingLength(le);
		apdu.sendBytes((short) 0, le);
	}

	void processCase3(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		short lc = apdu.setIncomingAndReceive();
		saveLc(lc);

		saveLe((short) -1);

		saveBuffer(buffer);
	}

	void processCase4(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short lc = apdu.setIncomingAndReceive();
		saveLc(lc);

		short le = apdu.setOutgoing();
		saveLe(le);

		saveBuffer(buffer);

		apdu.setOutgoingLength(le);
		apdu.sendBytes((short) 0, le);
	}

	void processGetLastX(APDU apdu, byte[] lastX) {
		byte[] buffer = apdu.getBuffer();

		short le = apdu.setOutgoing();
		apdu.setOutgoingLength(le);
		apdu.sendBytesLong(lastX, (short) 0, le);
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
}
