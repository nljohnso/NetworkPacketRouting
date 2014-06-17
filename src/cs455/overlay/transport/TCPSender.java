package cs455.overlay.transport;

import java.io.*;
import java.net.Socket;

public class TCPSender {
	private DataOutputStream dout;
	
	public TCPSender(Socket socket) throws IOException {
		dout = new DataOutputStream(socket.getOutputStream());
	}

	public void sendData(byte[] dataToSend) throws IOException {
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}
}
