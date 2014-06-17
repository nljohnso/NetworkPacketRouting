package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {
	
	public EventFactory() {
		
	}
	
	public Message buildMessage(byte[] bytes) throws IOException {
		
		ByteArrayInputStream baInputStream =
				new ByteArrayInputStream(bytes);
		DataInputStream din =
				new DataInputStream(new BufferedInputStream(baInputStream));
		
		din.readInt();
		int type = din.readInt();
		
		byte[] newBytes = new byte[bytes.length-4];
		System.arraycopy(bytes, 4, newBytes, 0, bytes.length - 4);
		
		switch(type) {
		case Protocol.REGISTER:
			return new RegisterRequest(type, newBytes);
			
		case Protocol.DEREGISTER:
			return new Deregister(type, newBytes);
			
		case Protocol.LINK_WEIGHTS:
			return new LinkWeights(type, newBytes);
			
		case Protocol.TASK_COMPLETE:
			return new TaskComplete(type, newBytes);
			
		case Protocol.TASK_INITIATE:
			return new TaskInitiate(type, newBytes);
			
		case Protocol.TASK_SUMMARY_REQ:
			return new TaskSummaryRequest(type, newBytes);
			
		case Protocol.TASK_SUMMARY_RES:
			return new TaskSummaryResponse(type, newBytes);
			
		case Protocol.REGISTER_RES:
			return new RegisterResponse(type, newBytes);
				
		case Protocol.DEREGISTER_RES:
			return new DeregisterResponse(type, newBytes);
			
		case Protocol.MESSAGE_NODES_LIST:
			return new MessagingNodesList(type, newBytes);
			
		case Protocol.EXCHANGE_REQ:
			return new ExchangeRequest(type, newBytes);
		}
		
		return null;
	}
}
