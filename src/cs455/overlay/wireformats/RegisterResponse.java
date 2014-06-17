package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse extends Message {

	private byte   statusCode;
	private String additionalInfo;
	
	public RegisterResponse(byte statusCode, String additionalInfo) {
		super(REGISTER_RES);
		this.statusCode = statusCode;
		this.additionalInfo = additionalInfo;
	}
	
	public RegisterResponse(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte getStatusCode() {
		return statusCode;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	@Override
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(getSize());
		dout.writeInt(getType());
		dout.writeByte(statusCode);
		
		byte[] additionalInfoBytes = additionalInfo.getBytes();
		dout.writeInt(additionalInfoBytes.length);
		dout.write(additionalInfoBytes);	
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}
	
	@Override
	public void unMarshal(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream baInputStream =
		new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		setType(din.readInt());
		statusCode = din.readByte();
		
		int additionalInfoItemBytes = din.readInt();
		byte[] additionalInfoBytes = new byte[additionalInfoItemBytes];
		din.readFully(additionalInfoBytes);
		additionalInfo = new String(additionalInfoBytes);
		
		baInputStream.close();
		din.close();
	}

	@Override
	protected int getSize() {
		int total = 5;
		
		byte[] additionalInfoBytes = additionalInfo.getBytes();
		total += additionalInfoBytes.length;
		
		return total;
	}

}
