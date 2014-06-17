package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessagingNodesList extends Message {
	
	private int      peerMessagingNodes;
	private String[] msgNodeInfo;
	
	public MessagingNodesList(int peerMessagingNodes, String[] msgNodeInfo) {
		super(MESSAGE_NODES_LIST);
		this.peerMessagingNodes = peerMessagingNodes;
		this.msgNodeInfo = msgNodeInfo;
	}

	public MessagingNodesList(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getPeerMessagingNodes() {
		return peerMessagingNodes;
	}

	public String[] getMsgNodeInfo() {
		return msgNodeInfo;
	}
	
	@Override
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(getSize());
		dout.writeInt(getType());
		dout.writeInt(peerMessagingNodes);

		int msgNodeInfoLength = msgNodeInfo.length;
		dout.writeInt(msgNodeInfoLength);
		
		for (int i = 0; i < msgNodeInfoLength; i++) {
			byte[] msgNodeInfoBytes = msgNodeInfo[i].getBytes();
			dout.writeInt(msgNodeInfoBytes.length);
			dout.write(msgNodeInfoBytes);	
		}
		
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
		peerMessagingNodes = din.readInt();
		
		int msgNodeInfoLength = din.readInt();
		msgNodeInfo = new String[msgNodeInfoLength];
		
		for (int i = 0; i < msgNodeInfoLength; i++) {
			int msgNodeInfoItemBytes = din.readInt();
			byte[] msgNodeInfoBytes = new byte[msgNodeInfoItemBytes];
			din.readFully(msgNodeInfoBytes);
			msgNodeInfo[i] = new String(msgNodeInfoBytes);
		}
		
		baInputStream.close();
		din.close();
	}

	@Override
	protected int getSize() {
		int total = 12;
		
		for (int i = 0; i < msgNodeInfo.length; i++) {
			total += 4;  //int size for length of string.
			byte[] msgNodeInfoBytes = msgNodeInfo[i].getBytes();
			total += msgNodeInfoBytes.length;
		}
		
		return total;
	}
}
