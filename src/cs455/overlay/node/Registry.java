package cs455.overlay.node;

import java.io.*;
import java.net.*;
import java.util.*;

import cs455.overlay.transport.*;
import cs455.overlay.util.OverlayCreator;
import cs455.overlay.wireformats.*;
import cs455.overlay.node.Node;

public class Registry {

	private static boolean debug = false;
	private OverlayCreator overlay;
	private static Registry instance = null; // Singleton Instance.
	private static int portNumber;           // Port number to listen on.
	private static NodeManager nodesList;    // List of MessagingNodes registered.
	private int  allMsgSent     = 0;
	private int  allMsgRec      = 0;
	private int  allMsgRelayed  = 0;
	private long allPayloadSent = 0L;
	private long allPayloadRec  = 0L;

	private static String REG_SUCCESS_MSG   = "Registration request successful. The number of messaging nodes currently constituting the overlay is: ";
	private static String REG_FAILURE_MSG   = "Unable to register Node.";
	private static String DEREG_SUCCESS_MGS = "Deregistration request successful. The number of messaging nodes currently constituting the overlay is: ";
	private static String DEREG_FAILURE_MSG = "Unable to deregister Node.";

	/**
	 * Constructor for Registry Singleton.
	 */
	private Registry() {
		nodesList = new NodeManager();
		commandThread.start();
		commThread.start();
	}

	/**
	 * Thread for handling command input.
	 */
	private Thread commandThread = new Thread() {
		public void run() {
			this.setName("Registry-commandThread");  //Set thread name -- need to see in debugger.
			queryForInput();
		}
	};

	/**
	 * Thread for handling communications.
	 */
	private Thread commThread = new Thread() {
		public void run() {
			this.setName("Registry-commThread");   //Set thread name -- need to see in debugger.
			ServerSocket serverSocket = null;
			Message msg = null;
			try {
				serverSocket = new ServerSocket(portNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true) { 
				try {
					byte[] rawData = readMessage(serverSocket);
					EventFactory eFact = new EventFactory();
					msg = eFact.buildMessage(rawData);
					handleMessage(msg);
				} catch (IOException e1) {
					nodesList.removeMessagingNode(msg.getIPAddress(),
							msg.getPort());
				}
			}
		}
	};

	/**
	 * Set the port number for the registry
	 * @param portNumber - Registry port number to set.
	 */
	public static void setPortNumber(int portNumber) {
		Registry.portNumber = portNumber;
	}

	/**
	 * Process the message received in a new thread.  If after processing it returned
	 * a Message, this needs to be sent back to the Node.
	 * 
	 * @param msg - Message which was received.
	 */
	private void handleMessage(Message msg) {

		Thread processThread = new Thread() {

			Message msg = null;

			public void run() {
				Message responseMsg = null;

				try {
					switch (msg.getType()) {

					case Protocol.REGISTER:
						responseMsg = process((RegisterRequest) msg);
						break;

					case Protocol.DEREGISTER:
						responseMsg = process((Deregister) msg);
						break;

					case Protocol.TASK_COMPLETE:
						responseMsg = process((TaskComplete) msg);
						break;

					case Protocol.TASK_SUMMARY_RES:
						responseMsg = process((TaskSummaryResponse) msg);
						break;

					default:
						break;
					}

					if (responseMsg != null) {
						Socket socket = new Socket(msg.getIPAddress(), msg.getPort());
						TCPSender tcpSender = new TCPSender(socket);
						tcpSender.sendData(responseMsg.marshal());
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								// Eat it.
							}
						}
					}
				} catch (UnknownHostException he) {
					he.printStackTrace();
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

	/**
	 * Process the TaskComplete message.
	 */
	private synchronized Message process(TaskComplete msg) throws UnknownHostException, IOException {
		
		Node node = nodesList.find(msg.getIPAddress(), msg.getPort());
		if (node != null)
			node.setComplete(true);
		if (nodesList.allComplete()) {
			System.out.println("-----------------------------------------------");
			for(Node aNode: nodesList.getNodes()) {
				TaskSummaryRequest summaryRqst = new TaskSummaryRequest();
				Socket socket = new Socket(aNode.getIPAddress(), aNode.getPort());
				TCPSender tcpSender = new TCPSender(socket);
				tcpSender.sendData(summaryRqst.marshal());
				if(socket != null)
					socket.close();
			}
		}
		
		return null;
	}

	/**
	 * Process the TaskSummaryRepsonse message.
	 */
	private synchronized Message process(TaskSummaryResponse msg) {

		System.out.println("Node: \t\t\t\t" + msg.getIPAddress() + ":" + msg.getPort());
		System.out.println("Messages Sent: \t\t\t" + msg.getMessagesSent());
		System.out.println("Messages Received: \t\t" + msg.getMessagesReceived());
		System.out.println("Summation of Send Messages: \t" + msg.getSummOfMessagesSent());
		System.out.println("Summation of Received Messages: " + msg.getSummOfMessagesReceived());
		System.out.println("-----------------------------------------------");

		setAllMsgSent(getTotalMsgSent() + msg.getMessagesSent());
		setAllMsgRec(getTotalMsgRecevied() + msg.getMessagesReceived());
		setAllPayloadSent((getTotalPayloadSent() + msg.getSummOfMessagesSent()));
		setAllPayloadRec((getTotalPayloadReceived() + msg.getSummOfMessagesReceived()));
		
		Node node = nodesList.find(msg.getIPAddress(), msg.getPort());
		if (node != null) {
			node.setSummaryRecevied(true);
		}
		
		if (nodesList.allSummarysReceived()) {
			System.out.println("------------------GRAND TOTALS:----------------");
			System.out.println("Messages Sent:\t\t\t" + getTotalMsgSent());
			System.out.println("Messages Received:\t\t" + getTotalMsgRecevied());
			System.out.println("Summation of Send Messages:\t" + getTotalPayloadSent());
			System.out.println("Summation of Received Messages:\t" + getTotalPayloadReceived());
			System.out.println("-----------------------------------------------");
			System.out.println("-----------------------------------------------");
		}
		
		return null;
	}

	/**
	 * Process the RegisterReqeust message.
	 */
	private synchronized Message process(RegisterRequest msg) throws UnknownHostException,
			IOException {
		byte statusCode = 0;
		RegisterResponse rsp;
		if (nodesList.addNode(msg.getIPAddress(), msg.getPort())) {
			statusCode = 1;
			rsp = new RegisterResponse(statusCode, REG_SUCCESS_MSG
					+ nodesList.size() + ".");
			rsp.setIPAddress(msg.getIPAddress());
			rsp.setPort(msg.getPort());
			return rsp;
		} else {
			rsp = new RegisterResponse(statusCode, REG_FAILURE_MSG);
			rsp.setIPAddress(msg.getIPAddress());
			rsp.setPort(msg.getPort());
			return rsp;
		}
	}

	/**
	 * Process the Deregister message.
	 */
	private synchronized Message process(Deregister msg) throws UnknownHostException,
			IOException {
		nodesList.removeMessagingNode(msg.getIPAddress(), msg.getPort());
		return new DeregisterResponse();
	}

	/**
	 * Read a message from socket in byte form.
	 * 
	 * @param serverSocket
	 *            Socket to read from.
	 * @return byte message read.
	 * @throws IOException
	 *             Problem occurred reading message.
	 */
	private byte[] readMessage(ServerSocket serverSocket) throws IOException {

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

	/**
	 * Manages querying for input commands.
	 */
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

		System.out.println("Exiting Registry.");
	}
	
	/**
	 * Send the link weights to the nodes.
	 */
	private void sendOverlayLinkWeights() throws UnknownHostException, IOException {
		List<String> linkInfoList = new ArrayList<String>();
		Random generator = new Random();
		
		for(Node aNode: nodesList.getNodes()) {
			for(Edge aEdge: aNode.edges) {
				if(aEdge.getLinkWeight() == 0) {
					int random = generator.nextInt(10) + 1;
					aEdge.setLinkWeight(random);
					aEdge.getNode().getEdgeInConnectedNodes(aNode).setLinkWeight(random);
				}
				
				linkInfoList.add(aNode.getIPAddress() + ":" + aNode.getPort() + " " + 
						aEdge.getNode().getIPAddress() + ":" + aEdge.getNode().getPort() + " " +
						aEdge.getLinkWeight());
			}
		}
        
		String[] linkInfo = new String[linkInfoList.size()];
		int i = 0;
		
		for (String aString : linkInfoList) {
			linkInfo[i++] = aString;
		}

		for (Node aNode : nodesList.getNodes()) {
			LinkWeights link = new LinkWeights(linkInfo.length, linkInfo);
			Socket socket = new Socket(aNode.getIPAddress(), aNode.getPort());
			TCPSender tcpSender = new TCPSender(socket);
			tcpSender.sendData(link.marshal());
			if(socket != null)
				socket.close();
		}
		
		//Don't believe we're suppose to print this.
		//overlay.print();
	}

	/**
	 * If not already created, create a Registry object and the returns the
	 * singleton instance.
	 * 
	 * @return Registry singleton.
	 */
	public static Registry getInstance() {
		if (instance == null)
			instance = new Registry();

		return instance;
	}

	public void execute(String command) throws UnknownHostException, IOException {
		int numOfConnections = 0;
		if (command.equalsIgnoreCase("list-messaging-nodes")) {
			nodesList.printList();
		} else if (command.equalsIgnoreCase("list-weights")) {
			printLinkWeights();
		} else if (command.substring(0, command.length() - 2).equalsIgnoreCase("setup-overlay")) {
			numOfConnections = Integer.parseInt(command.substring(command.length() - 1, command.length()));
			setupOverlay(numOfConnections);
		} else if (command.equalsIgnoreCase("send-overlay-link-weights")) {
			sendOverlayLinkWeights();
		} else if (command.equalsIgnoreCase("start")) {
			start();
		} else {
		
			System.out.println("Unkown command.");
		}
	}

	private void start() throws UnknownHostException, IOException {
		for(Node aNode: nodesList.getNodes()) {
			if (debug)
				System.out.println("Sending Initiate Task To Address: " + aNode.getIPAddress() + " Port:" + aNode.getPort());
			TaskInitiate initiate = new TaskInitiate();
			Socket socket = new Socket(aNode.getIPAddress(), aNode.getPort());
			TCPSender tcpSender = new TCPSender(socket);
			tcpSender.sendData(initiate.marshal());
			if(socket != null)
				socket.close();
		}
	}

	public void printLinkWeights() {
		ArrayList<Node> visited = new ArrayList<Node>();
		for (int i = 0; i < nodesList.size(); i++) {
			Node currentNode = nodesList.get(i);
			visited.add(currentNode);
			ArrayList<Edge> visitedEdge = currentNode.edges;
			for (int j = 0; j < visitedEdge.size(); j++) {
				if (!visitedEdge.get(j).getNode().isVisited())
					System.out.println(currentNode.getIPAddress() + ":" + currentNode.getPort() + " " + 
							visitedEdge.get(j).getNode().getIPAddress() + ":" + visitedEdge.get(j).getNode().getPort() + " " +
							visitedEdge.get(j).getLinkWeight());
			}
		}
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public synchronized String deRegister(Node node) {
		if (nodesList.removeMessagingNode(node)) {
			return DEREG_SUCCESS_MGS + nodesList.size() + ".";
		} else {
			return DEREG_FAILURE_MSG;
		}
	}

	/**
	 * 
	 */
	public void listMessagingNodes() {
		nodesList.printList();
	}

	/**
	 * 
	 * @return
	 */
	public int assignLinkWeights() {
		Random generator = new Random();
		int randomNumber = generator.nextInt(10) + 1;
		return randomNumber;
	}

	/**
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * 
	 */
	public void setupOverlay(int numOfConnections) throws UnknownHostException, IOException {
		overlay = new OverlayCreator(nodesList);
		nodesList = overlay.registryNodes;
		int numOfLinks;
		String[] nodeInfo;
		for(Node aNode: nodesList.NodeList) {
			numOfLinks = 0;
			nodeInfo = new String[aNode.edges.size()];
			for(Edge aEdge: aNode.edges) {
				nodeInfo[numOfLinks] = aEdge.getNode().getIPAddress() + ":" + aEdge.getNode().getPort();
				numOfLinks++;
			}
			if(aNode.edges.size() < numOfConnections) {
				MessagingNodesList mNodeList = new MessagingNodesList(numOfLinks, nodeInfo);
				Socket socket = new Socket(aNode.getIPAddress(), aNode.getPort());
				TCPSender tcpSender = new TCPSender(socket);
				tcpSender.sendData(mNodeList.marshal());
				if(socket != null)
					socket.close();
			}
		}
	}

	public int getTotalMsgSent() {
		return allMsgSent;
	}

	public void setAllMsgSent(int allMsgSent) {
		this.allMsgSent = allMsgSent;
	}

	public int getTotalMsgRecevied() {
		return allMsgRec;
	}

	public void setAllMsgRec(int allMsgRec) {
		this.allMsgRec = allMsgRec;
	}

	public long getTotalPayloadSent() {
		return allPayloadSent;
	}

	public void setAllPayloadSent(long allPayloadSent) {
		this.allPayloadSent = allPayloadSent;
	}

	public long getTotalPayloadReceived() {
		return allPayloadRec;
	}

	public void setAllPayloadRec(long allPayloadRec) {
		this.allPayloadRec = allPayloadRec;
	}

	public int getAllMsgRelayed() {
		return allMsgRelayed;
	}

	public void setAllMsgRelayed(int allMsgRelayed) {
		this.allMsgRelayed = allMsgRelayed;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Registry registry = Registry.getInstance();
		Registry.setPortNumber(Integer.parseInt(args[0]));
		try {
			registry.commandThread.join();
			registry.commThread.join();
			System.out.println("Threads finished");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
