package cs455.overlay.wireformats;

import java.io.IOException;

public class Deregister extends Message {
	
	public Deregister(String ipAddress, int port) {
		super(DEREGISTER, ipAddress, port);
	}

	public Deregister(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
