package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExchangeRequest extends Message {
	
	private int payloadNbr = 0;
	private String path;
	
	public ExchangeRequest(String ipAddress, int port, int payloadNbr, String path) {
		super(EXCHANGE_REQ, ipAddress, port);
		this.payloadNbr = payloadNbr;
		this.path = path;
	}

	public ExchangeRequest(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(getSize());
		
		dout.writeInt(getType());

		byte[] ipAddressBytes = getIPAddress().getBytes();
		dout.writeInt(ipAddressBytes.length);
		dout.write(ipAddressBytes);
		
        dout.writeInt(getPort());
		
        dout.writeInt(payloadNbr);

        byte[] pathBytes = path.getBytes();
		dout.writeInt(pathBytes.length);
		dout.write(pathBytes);
        
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
		DataInputStream din =
		new DataInputStream(new BufferedInputStream(baInputStream));
		
		setType(din.readInt());
		
		int ipAddressLength = din.readInt();
		byte[] ipAddressBytes = new byte[ipAddressLength];
		din.readFully(ipAddressBytes);
		setIPAddress(new String(ipAddressBytes));
		setPort(din.readInt());

		payloadNbr = din.readInt();
		
		int pathLength = din.readInt();
		byte[] pathBytes = new byte[pathLength];
		din.readFully(pathBytes);
		path = new String(pathBytes);
		
		baInputStream.close();
		din.close();
	}
	
	@Override
	protected int getSize() {

		int total = 20;
		
		byte[] pathBytes = path.getBytes();
		total += pathBytes.length;
		
		byte[] ipAddressBytes = getIPAddress().getBytes();
		total += ipAddressBytes.length;
		
		return total;
	}

	
	public int getPayloadNbr() {
		return payloadNbr;
	}

	public void setPayloadNbr(int payloadNbr) {
		this.payloadNbr = payloadNbr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}	
}
