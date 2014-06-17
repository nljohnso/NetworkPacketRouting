package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskSummaryResponse extends Message {

	private int    messagesSent;
	private long   summOfMessagesSent;
	private int    messagesReceived;
	private long   summOfMessagesReceived;
	private int    messagesRelayed;

	public TaskSummaryResponse(String ipAddress, int port, int messagesSent, long summOfMessagesSent, int messagesReceived, long summOfMessagesReceived, int messagesRelayed) {
		super(TASK_SUMMARY_RES, ipAddress, port);
		this.messagesSent = messagesSent;
		this.summOfMessagesSent = summOfMessagesSent;
		this.messagesReceived = messagesReceived;
		this.summOfMessagesReceived = summOfMessagesReceived;
		this.messagesRelayed = messagesRelayed;
	}
	
	public TaskSummaryResponse(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getMessagesSent() {
		return messagesSent;
	}

	public long getSummOfMessagesSent() {
		return summOfMessagesSent;
	}

	public int getMessagesReceived() {
		return messagesReceived;
	}

	public long getSummOfMessagesReceived() {
		return summOfMessagesReceived;
	}

	public int getMessagesRelayed() {
		return messagesRelayed;
	}

	@Override
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(getSize());
		dout.writeInt(getType());

		if(getIPAddress() != null) {
			byte[] ipAddressBytes = getIPAddress().getBytes();
			dout.writeInt(ipAddressBytes.length);
			dout.write(ipAddressBytes);
		} else {
			dout.writeInt(0);
		}
		
		dout.writeInt(getPort());
		dout.writeInt(messagesSent);
		dout.writeLong(summOfMessagesSent);
		dout.writeInt(messagesReceived);
		dout.writeLong(summOfMessagesReceived);
		dout.writeInt(messagesRelayed);
		
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
		
		messagesSent = din.readInt();
		summOfMessagesSent = din.readLong();
		messagesReceived = din.readInt();
		summOfMessagesReceived = din.readLong();
		messagesRelayed = din.readInt();
		
		baInputStream.close();
		din.close();
	}
	
	@Override
	protected int getSize() {
		int result = 40;
		
		if(getIPAddress() != null) {
			byte[] ipAddressBytes = getIPAddress().getBytes();
			result += ipAddressBytes.length;
		}
		
		return result;
		
	}
}
