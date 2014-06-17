package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Message implements Protocol {

	private int    type;
	private String iPAddress;
    private int    port;

	public Message(int type) {
		this.setType(type);
	}
	
    public Message(int type, String ipAddress, int port) {
    	this(type);
    	this.iPAddress = ipAddress;
    	this.port = port;
    }
    
	public String getIPAddress() {
		return iPAddress;
	}

	public void setIPAddress(String ipAddress) {
		this.iPAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	protected int getSize() {
		
		int total = 12; //Type length in bytes.
		
		byte[] ipAddressBytes = getIPAddress().getBytes();
		total += ipAddressBytes.length;
		
		return total;
	}
	
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		
		dout.writeInt(getSize());
		
		dout.writeInt(getType());

		byte[] ipAddressBytes = iPAddress.getBytes();
		dout.writeInt(ipAddressBytes.length);
		dout.write(ipAddressBytes);
		
        dout.writeInt(port);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}
	
	public void unMarshal(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream baInputStream =
		new ByteArrayInputStream(marshalledBytes);
		DataInputStream din =
		new DataInputStream(new BufferedInputStream(baInputStream));
		
		
		setType(din.readInt());
		
		int ipAddressLength = din.readInt();
		byte[] ipAddressBytes = new byte[ipAddressLength];
		din.readFully(ipAddressBytes);
		iPAddress = new String(ipAddressBytes);
		port = din.readInt();
		baInputStream.close();
		din.close();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
