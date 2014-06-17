package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.*;

public class MessagingNode {
	
	private static boolean debug = false;
	
	private Thread commandThread = null;         //Thread for entering commands on.
	private Thread commThread = null;            //Thread to handle reading messages from socket.

	private String registryIPAddress = null;     //IP address of the Registry
	private int registryPort = 0;                //Port of the Registry

	private static String nodeIPAddress = null;  //IP address of this MessagingNode
	private int nodePort = 0;                    //Port of this MessagingNode
	private boolean completed  = false;           
	private boolean systemExit = false;          //Should exit the program.
	private NodeManager manager = null;
	private TCPSender tcpSender = null;
	private ServerSocket serverSocket;
	private String[] nodeInfo = null;

	private int sendTracker       = 0;
	private int receiveTracker    = 0;
	private long sendSummation    = 0;
	private long receiveSummation = 0;
	private int numOfRelayedMsg   = 0;
	
	/**
	 * Gets this objects host address.
	 */
	static {
		try {
			nodeIPAddress = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public MessagingNode(String IP, int pNumber) throws UnknownHostException, IOException {
		registryIPAddress = IP;
		registryPort = pNumber;
		serverSocket = new ServerSocket(0);
		serverSocket.setSoTimeout(0);
		nodePort = serverSocket.getLocalPort();

		//Thread to handle socket communications.
		commThread = new Thread() {
			public void run() {
				this.setName("Node-commThread");
				while (!systemExit) {
					try {
						byte[] rawData = readMessage(serverSocket);
						EventFactory eFact = new EventFactory();
						Message msg = eFact.buildMessage(rawData);
						if (debug) {
							System.out.println("Recieved Msg Type: " + msg.getType() + " at (Sec.millseconds): " + getTimestamp());
							System.out.println("");
						}
						handleMessage(msg);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};

		//Thread to handle keyboard entries.
		commandThread = new Thread() {
			public void run() {
				this.setName("Node-commandThread");
				queryForInput();
			}
		};

		//Register the MessageingNode at start-up.
		RegisterRequest register = new RegisterRequest(nodeIPAddress, nodePort);
		Socket socket = new Socket(registryIPAddress, registryPort);
		tcpSender = new TCPSender(socket);
		commThread.start();
		tcpSender.sendData(register.marshal());
		if (socket != null)
			socket.close();
	}

	/**
	 * Read a message in byte form.
	 * 
	 * @param serverSocket
	 *            Socket to read from.
	 * @return byte message read.
	 * @throws IOException
	 *             Problem occurred reading message.
	 */
	private byte[] readMessage(ServerSocket serverSocket) throws IOException {

		if (debug)
			System.out.println("Listening on port #: " + nodePort + " at: " + getTimestamp());
		
		Socket socket = serverSocket.accept();
		
		TCPReceiverThread tcpReceiver = new TCPReceiverThread(socket);
		byte[] bytes = tcpReceiver.run();
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// Eat it.
			}
		}

		return bytes;
	}
	
	private String getTimestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
		return sdf.format(date);

	}

	/**
	 * Starts the keyboard command processor thread.
	 */
	public void start() {
		commandThread.start();
	}

	/**
	 * Process the message received.
	 * 
	 * @param msg Message which was received.
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void handleMessage(Message msg) {

		Thread processThread = new Thread() {
			
			Message msg = null;
			
			public void run() {
				try {
						switch (msg.getType()) {
							case Protocol.REGISTER_RES:
								process((RegisterResponse) msg);
								break;
					
							case Protocol.DEREGISTER_RES:
								process((DeregisterResponse) msg);
								break;
					
							case Protocol.LINK_WEIGHTS:
								process((LinkWeights) msg);
								break;
					
							case Protocol.TASK_INITIATE:
									process((TaskInitiate) msg);
								break;
					
							case Protocol.MESSAGE_NODES_LIST:
								process((MessagingNodesList) msg);
								break;
					
							case Protocol.EXCHANGE_REQ:
								process((ExchangeRequest) msg);
								break;
								
							case Protocol.TASK_SUMMARY_REQ:
								process((TaskSummaryRequest) msg);
								break;
					
							default:
								break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			Thread initialize(Message msg) {
				this.msg = msg;
				return this;
			}
			
		}.initialize(msg);
		
		processThread.start();

	}

	private void process(TaskSummaryRequest msg) throws IOException {
		
		if (debug)
			System.out.println ("Received Task Summary Request.");

		TaskSummaryResponse summaryRsp = new TaskSummaryResponse(nodeIPAddress, nodePort, getNumOfMsgSent(), getSummOfSentMsg(), getNumOfMsgRec(), getSummOfRecMsg(), getNumOfRelayedMsg());
		Socket socket = new Socket(registryIPAddress, registryPort);
		tcpSender = new TCPSender(socket);
		tcpSender.sendData(summaryRsp.marshal());
		
		if (socket != null)
			socket.close();

		if (debug)
			System.out.println ("Sent Task Summary Response.");
		
	}

	private void process(MessagingNodesList msg) {
		nodeInfo = msg.getMsgNodeInfo();
		for (String line : nodeInfo) {
			System.out.println(line);
		}
	}

	private void process(TaskInitiate msg) throws UnknownHostException, IOException {

		if (debug)
			System.out.println ("Task Initiate Msg Received.");
		
		Node begin = manager.find(nodeIPAddress, nodePort); // Get this node.
		manager.calculatePaths(begin);
		Random random = new Random();
		
		for (int i = 0; i < 10; i++) {
			//for(int j = 0; j < 5; j++) {
				Node end = manager.getRandomSink(begin);           // Randomly select a sink node.
				Node path = manager.getShortestPathTo(begin,end);
	
				int payloadNbr = random.nextInt();
	
				//Take starting node info off path string (makes receiving the path string easier).
				String pathString = path.toString();
				String originNode = pathString.substring(0, pathString.indexOf("|") + 1);
				String newPath = pathString.substring(originNode.length(), pathString.length());
				
				String pathCopy = newPath;
				String nextIP = null;
				int nextPort = 0;
				if(newPath.contains("|")) {  
					String nodeInfo = newPath.substring(0, newPath.indexOf("|")); //current node info
					pathCopy = newPath.substring(nodeInfo.length(), newPath.length()); //new path minus current node
					pathCopy = pathCopy.substring(1, pathCopy.length());
					nextIP   = nodeInfo.substring(0, nodeInfo.indexOf(":"));
					nextPort = Integer.parseInt(nodeInfo.substring(nodeInfo.indexOf(":")+1, nodeInfo.length()));
					
				} else {
					nextIP = pathCopy.substring(0, pathCopy.indexOf(":"));
					nextPort = Integer.parseInt(pathCopy.substring(pathCopy.indexOf(":") + 1, pathCopy.length()));
				}
	
				//Increase # of sent messages and the summation of the payload.
				addNumOfMsgSent(1);
				addSummOfSentMsg(payloadNbr);
				
				//Send the Exchange Message.
				ExchangeRequest exchangeMsg = new ExchangeRequest(begin.getIPAddress(), begin.getPort(), payloadNbr, newPath);
				Socket socket;
				
				if (debug)
					System.out.println ("Sending Exchange Msg From (Address:Port): " + begin.getIPAddress() + ":" + begin.getPort() + " Sent To (Address:Port): " + nextIP + ":" + nextPort + " At (Second.Milliseconds): " + getTimestamp());
				
				socket = new Socket(nextIP, nextPort);
				tcpSender = new TCPSender(socket);
				tcpSender.sendData(exchangeMsg.marshal());
		
				if (socket != null)
					socket.close();
			//}
		}
		
		if (debug)
			System.out.println ("Task Initiate Msg Completed (" + nodePort + ").");
		
		System.out.println ("Task Initiate Sending TaskComplete Msg to Port: (" + nodePort + ").");
		
		TaskComplete completeMsg = new TaskComplete(nodeIPAddress, nodePort);
		Socket socket = new Socket(registryIPAddress, registryPort);
		tcpSender = new TCPSender(socket);
		tcpSender.sendData(completeMsg.marshal());
		if(socket != null)
			socket.close();
	
	}

	private  void process(ExchangeRequest msg) throws IOException {
		
		String path = msg.getPath();
		int payLoad = msg.getPayloadNbr();
		
	
		if(path.contains("|")) {
			
			//Increase # of relayed messages by one.
			addNumOfRelayedMsg(1);
			
			if (debug)
				System.out.println ("Sending Exchange Msg to path: " + path);
			
			String nodeInfo = path.substring(0, path.indexOf("|")); //current node info
			path = path.substring(nodeInfo.length(), path.length()); //new path minus current node			
			path = path.substring(1, path.length());

			//Get the next node in the path.
			String ip = nodeInfo.substring(0, nodeInfo.indexOf(":"));
			int port = Integer.parseInt(nodeInfo.substring(nodeInfo.indexOf(":")+1, nodeInfo.length()));
			
			
			
			
			
			//Send the exchange message.
			ExchangeRequest exchangeMsg = new ExchangeRequest(ip, port, payLoad, path);
			Socket socket = new Socket(ip, port);
			tcpSender = new TCPSender(socket);
			tcpSender.sendData(exchangeMsg.marshal());
			
			if (socket != null)
				socket.close();
			
		} else {
			
			//Payload has reached destination.
			//Add payload to summation and increase # of messages received by one.
			addSummOfRecMsg(payLoad);
			addNumOfMsgRec(1);		
		}
	}

	private void process(LinkWeights msg) {
		manager = new NodeManager(msg.getMsgNodeInfo());
		for (String line : msg.getMsgNodeInfo()) {
			System.out.println(line);
		}
	}

	private void process(RegisterResponse msg) {
		System.out.println("Status Code: " + msg.getStatusCode());
		System.out.println("Message    : " + msg.getAdditionalInfo());
	}

	private void process(DeregisterResponse msg) {
		System.out.println("Exited overlay.");
	}

	/**
	 * Prints the shortest paths.
	 */
	private void printShortestPath() {

		Node begin = manager.find(nodeIPAddress, nodePort);
		for (Node end : manager.NodeList) {
			if (!begin.equals(end)) {
				System.out.print ("Start--");
				Node path = manager.getShortestPathTo(begin, end);
				while(path != null) {
					System.out.print(path.getIPAddress() + ":" + path.getPort() + "--");
					if(path.getEdges().size() != 0) {
						System.out.print(path.getEdges().get(0).getLinkWeight() + "--");
						path = path.getEdges().get(0).getNode();
					}
					else {
						path = null;
					}

				}
				System.out.println ("End");
			}
		}
	}

	private void queryForInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command = null;
		do {
			System.out.print("cmd> ");
			try {
				command = br.readLine();
				execute(command);
			} catch (IOException ioe) {
				System.out.println("");
				System.out.println("IO error reading command!");
				System.out.println("");
				System.exit(1);
			}
		} while (!command.equalsIgnoreCase("exit"));
		
		systemExit = true;

	}

	public void execute(String command) throws IOException {

		if (command.equalsIgnoreCase("print-shortest-path")) {
			printShortestPath();
		} else if (command.equalsIgnoreCase("exit-overlay")) {
			exitOverlay();
		} else {
			System.out.println("Unknown command.");
		}
	}

	private void exitOverlay() throws IOException {
		Deregister deregister = new Deregister(nodeIPAddress, nodePort);
		Socket socket = new Socket(registryIPAddress, registryPort);
		tcpSender = new TCPSender(socket);
		tcpSender.sendData(deregister.marshal());
		socket.close();
	}

	public String getIPAddress() {
		return registryIPAddress;
	}

	public int getPortNumber() {
		return registryPort;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getNumOfMsgSent() {
		return sendTracker;
	}

	public synchronized void addNumOfMsgSent(int numOfMsgSent) {
		this.sendTracker += numOfMsgSent;
	}

	public int getNumOfMsgRec() {
		return receiveTracker;
	}

	public synchronized void addNumOfMsgRec(int numOfMsgRec) {
		this.receiveTracker += numOfMsgRec;
	}

	public long getSummOfSentMsg() {
		return sendSummation;
	}

	public synchronized void addSummOfSentMsg(long summOfSentMsg) {
		this.sendSummation += summOfSentMsg;
	}

	public long getSummOfRecMsg() {
		return receiveSummation;
	}

	public synchronized void addSummOfRecMsg(long summOfRecMsg) {
		this.receiveSummation += summOfRecMsg;
	}

	public int getNumOfRelayedMsg() {
		return numOfRelayedMsg;
	}

	public synchronized void addNumOfRelayedMsg(int numOfRelayedMsg) {
		this.numOfRelayedMsg += numOfRelayedMsg;
	}
	
	public static void main(String[] args) {
		MessagingNode node = null;
		try {
			node = new MessagingNode(args[0], Integer.parseInt(args[1]));
			node.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
