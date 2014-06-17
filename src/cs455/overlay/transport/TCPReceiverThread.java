package cs455.overlay.transport;
import java.net.*;
import java.io.*;

public class TCPReceiverThread {
	private Socket socket;
	private DataInputStream din;
	
	public TCPReceiverThread(Socket socket) throws IOException {
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
	}
	public byte[] run() {
		int dataLength;
		byte[] data = null;
		while (socket != null) {
			try {
				dataLength = din.readInt();
				data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
			} catch (SocketException se) {
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe) {
				if (ioe.getMessage() != null)
					System.out.println(ioe.getMessage()) ;
				break;
			}
		}
		return data;
	}
}
