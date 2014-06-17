package cs455.overlay.node;

import java.util.*;

import cs455.overlay.dijkstra.NodePriorityQueue;

public class NodeManager {

	public ArrayList<Node> NodeList;
	private boolean pathCalculated = false;

	/**
	 * Null constructor.
	 */
	public NodeManager() {
		NodeList = new ArrayList<Node>();
	}

	/**
	 * Constructor which build the Nodes given the msgNodeInfo received from a message.
	 * @param msgNodeInfo
	 */
	public NodeManager(String[] msgNodeInfo) {
		NodeList = new ArrayList<Node>();
		for (String link : msgNodeInfo) {
			
			//Parse the data...
			String nodeStr1 = link.substring(0, link.indexOf(" "));
			String nodeStr2 = link.substring(nodeStr1.length()+1, link.indexOf(" ")+nodeStr1.length()+1);
			int weight = Integer.parseInt(link.substring(nodeStr1.length() + nodeStr2.length() + 2, link.length()));

			String ipAddrStr1 = nodeStr1.substring(0, nodeStr1.indexOf(":"));
			int portStr1 = Integer.parseInt(nodeStr1.substring(nodeStr1.indexOf(":")+1,nodeStr1.length()));

			String ipAddrStr2 = nodeStr2.substring(0, nodeStr2.indexOf(":"));
			int portStr2 = Integer.parseInt(nodeStr2.substring(nodeStr2.indexOf(":")+1,nodeStr2.length()));

			//Hook up the relationships...
			Node node1 = find(ipAddrStr1, portStr1);
			if (node1 == null) {
				node1 = new Node(ipAddrStr1, portStr1);
				NodeList.add(node1);
			}
				
			Node node2 = find(ipAddrStr2, portStr2);
			if (node2 == null) {
				node2 = new Node(ipAddrStr2, portStr2);
				NodeList.add(node2);
			}
			
			node1.edges.add(new Edge(node2, weight));
			node2.edges.add(new Edge(node1, weight));
		}
		
	}
	
	/**
	 * Calculates the path weights. 
	 * @param begin - Node to start calculation on.
	 */
	public void calculatePaths(Node begin) {
		pathCalculated = true;
		begin.setDistance(0);
		NodePriorityQueue nodeQueue = new NodePriorityQueue();
		nodeQueue.add(begin);

		while (!nodeQueue.isEmpty()) {
			Node aNode = nodeQueue.remove();

			for (Edge anEdge : aNode.getEdges()) {
				Node referancedNode = anEdge.getNode();
				int weight = anEdge.getLinkWeight();
				int distanceThroughU = aNode.getDistance() + weight;
				if (distanceThroughU < referancedNode.getDistance()) {
					nodeQueue.remove(referancedNode);
					referancedNode.setDistance(distanceThroughU);
					referancedNode.setPrevious(aNode);
					nodeQueue.add(referancedNode);
				}
			}
		}
	}

	/**
	 * Find the shortest path from 'begin' node to 'end' node.
	 * @param begin - Beginning node in the path to find.
	 * @param end - Ending node in the path to find.
	 * @return Node with single edges for the path found.
	 */
    public Node getShortestPathTo(Node begin, Node end) {
        
    	if (!pathCalculated) {
    		calculatePaths(begin);
    	}
    	
        Node previousNode = null;
        Node toAdd = null;

        for (Node aNode = end; aNode != null && !begin.equals(aNode); aNode = aNode.getPrevious()) {
        	toAdd = new Node(aNode.getIPAddress(), aNode.getPort());
        	if (previousNode != null) {
        		toAdd.addOneEdge(previousNode, aNode.getEdgeInConnectedNodes(previousNode).getLinkWeight());
        	}

        	previousNode = toAdd;    
        }
        
        toAdd = new Node(begin.getIPAddress(), begin.getPort());
        if (previousNode != null) {
        	toAdd.addOneEdge(previousNode, begin.getEdgeInConnectedNodes(previousNode).getLinkWeight());
    	}
        return toAdd;
    }

	public Node getRandomSink(Node avoidThisNode) {
		Node randomNode = null;
		Random random = new Random();
		do {
			int randomNbr = random.nextInt(NodeList.size());
			randomNode = NodeList.get(randomNbr);
		} while (randomNode.equals(avoidThisNode));

		return randomNode;
	}

	public boolean addNode(String ip, int port) {
		if (has(port)) {
			return false;
		} else {
			NodeList.add(new Node(ip, port));
			return true;
		}
	}

	public boolean removeMessagingNode(String ip, int port) {
		return removeMessagingNode(new Node(ip, port));
	}

	public boolean removeMessagingNode(Node node) {

		if (!has(node.getPort())) {
			return false;
		} else {
			NodeList.remove(node.getPort());
			return true;
		}
	}

	public boolean has(int port) {
		for (int i = 0; i < NodeList.size(); i++) {
			if (NodeList.get(i).getPort() == port) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds node based on IP address and port within the list of nodes held onto.
	 * @param ipAddress - IP Address to look for.
	 * @param port - Matching port to look for.
	 * @return Node located or null if not found.
	 */
	public Node find(String ipAddress, int port) {
		if (NodeList == null)
			return null;
		
		for (Node aNode : NodeList) {
			if (aNode.getIPAddress().equals(ipAddress) && aNode.getPort() == port) {
				return aNode;
			}
		}
		return null;
	}

	/**
	 * Returns the size of the entire list of nodes.
	 * @return quantity of nodes held onto.
	 */
	public int size() {
		return NodeList.size();
	}

	public Node get(int i) {
		return NodeList.get(i);
	}

	/*
	 * Returns the entire list of nodes.
	 */
	public ArrayList<Node> getNodes() {
		return NodeList;
	}

	/*
	 * Prints the list of nodes (IP and Port).
	 */
	public void printList() {
		for (int i = 0; i < NodeList.size(); i++) {
			System.out.println(NodeList.get(i).getIPAddress() + " "	+ NodeList.get(i).getPort());
		}
	}

	/**
	 * Determines if all the nodes have completed processing from start/initialization.
	 * @return true - all completed; false - more to be completed.
	 * @return
	 */
	public boolean allComplete() {
		for (Node aNode : NodeList) {
			if (!aNode.isComplete()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Determines if all the summary responses were received.
	 * @return true - all received; false - more to be received.
	 */
	public boolean allSummarysReceived() {
		
		for (Node aNode : NodeList) {
			if (!aNode.isSummaryRecevied()) {
				return false;
			}
		}
		
		return true;
	}	
}
