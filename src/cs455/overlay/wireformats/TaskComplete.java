package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete extends Message {
	
	public TaskComplete(String ipAddress, int port) {
		super(TASK_COMPLETE, ipAddress, port);
	}

	public TaskComplete(int type, byte[] bytes) {
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
		
		int ipAddressLength = din.readInt();
		byte[] ipAddressBytes = new byte[ipAddressLength];
		din.readFully(ipAddressBytes);
		String ipAddress = new String(ipAddressBytes);
		setIPAddress(ipAddress);
		
		setPort(din.readInt());
		
		baInputStream.close();
		din.close();
	}
	
}
