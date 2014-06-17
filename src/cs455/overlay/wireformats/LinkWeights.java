package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.wireformats.Message;

public class LinkWeights extends Message {

	private int nbrPeerNodes;
	private String[] msgNodeInfo;
	
	public LinkWeights(int nbrPeerNodes, String[] msgNodeInfo) {
		super(LINK_WEIGHTS);
		this.nbrPeerNodes = nbrPeerNodes;
		this.msgNodeInfo = msgNodeInfo;
	}		
	
	public LinkWeights(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNbrPeerNodes() {
		return nbrPeerNodes;
	}

	public void setNbrPeerNodes(int nbrPeerNodes) {
		this.nbrPeerNodes = nbrPeerNodes;
	}

	public String[] getMsgNodeInfo() {
		return msgNodeInfo;
	}

	public void setMsgNodeInfo(String[] msgNodeInfo) {
		this.msgNodeInfo = msgNodeInfo;
	}

	@Override
	public byte[] marshal() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(getSize());
		dout.writeInt(getType());
		dout.writeInt(getNbrPeerNodes());

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
		setNbrPeerNodes(din.readInt());
		
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

//	@Override
//	protected int getSize() {
//		int total = 8;
//		
//		int msgNodeInfoLength = msgNodeInfo.length;
//		
//		for (int i = 0; i < msgNodeInfoLength; i++) {
//			byte[] msgNodeInfoBytes = msgNodeInfo[i].getBytes();
//			total += msgNodeInfoBytes.length;
//		}
//		
//		return total;
//	}
	
	@Override
	protected int getSize() {
		int total = 12;
		
		int msgNodeInfoLength = msgNodeInfo.length;
		
		for (int i = 0; i < msgNodeInfoLength; i++) {
			total += 4;
			byte[] msgNodeInfoBytes = msgNodeInfo[i].getBytes();
			total += msgNodeInfoBytes.length;
		}
		
		return total;
	}

}
