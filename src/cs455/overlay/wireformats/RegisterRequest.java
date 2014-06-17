package cs455.overlay.wireformats;

import java.io.IOException;

public class RegisterRequest extends Message {

	public RegisterRequest(String ipAddress, int port) {
		super(REGISTER, ipAddress, port);
	}

	public RegisterRequest(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
