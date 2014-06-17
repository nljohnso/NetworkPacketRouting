package cs455.overlay.wireformats;

import java.io.IOException;

public class ExchangeResponse extends Message {
	
	public ExchangeResponse(String ipAddress, int port) {
		super(EXCHANGE_RES, ipAddress, port);
	}

	public ExchangeResponse(int type, byte[] bytes) {
		super(type);
		try {
			unMarshal(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
