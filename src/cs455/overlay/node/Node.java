package cs455.overlay.node;

import java.util.ArrayList;

public class Node implements Comparable<Node> {

	public ArrayList<Edge> edges = new ArrayList<Edge>();
	private boolean visited  = false;
	private boolean complete = false;
	private boolean summaryRecevied = false;
	private Node previous;
	private int weight = 0;
	private Integer distance = Integer.MAX_VALUE;
	private String nodeIPAddress = null;
	private int nodePort = 0;
	String ip = null;
	int port = 0;
	
	public Node(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIPAddress() {
		return ip;
	}

	public void setIPAddress(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public ArrayList<Edge> getEdges() {
		return edges;
	}
	
	public Edge getEdgeInConnectedNodes(Node node) {
		for(Edge e: edges) {
			if(e.getNode().getPort() == node.getPort()) {
				return e;
			}
		}
		return null;
	}
	
	public void addOneEdge(Node node, Integer linkWeight) {
		edges.add(new Edge(node, linkWeight));
	}
	
	public void addEdge(Node node, Integer linkWeight) {
		edges.add(new Edge(node, linkWeight));
		node.edges.add(new Edge(this, linkWeight));
	}
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public int getDistance() {
		return distance;
	}

	public void setDistance(Integer i) {
		distance = i;
	}
	
	public String getNodeIPAddress() {
		return nodeIPAddress;
	}

	public void setNodeIPAddress(String nodeIPAddress) {
		this.nodeIPAddress = nodeIPAddress;
	}

	public int getNodePort() {
		return nodePort;
	}

	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	public boolean equals(Node aNode) {
		if(aNode == null) {
			return false;
		}
		boolean result = ip.equalsIgnoreCase(aNode.getIPAddress()) && port == aNode.getPort();
		return result;
	}
	
	public String toString() {
		StringBuffer path = new StringBuffer(this.getIPAddress());
		path.append(":");
		path.append(this.getPort());

		Node aNode = this;
		for (Edge anEdge : aNode.edges) {
			aNode = anEdge.getNode();
			path.append("|");
			path.append(aNode.getIPAddress());
			path.append(":");
			path.append(aNode.getPort());
		}
		
		return path.toString();
	}
	
	@Override
	public int compareTo(Node node) {
		return distance.compareTo(node.getDistance());
	}

	public void setPrevious(Node previous) {
		this.previous = previous;
	}
	
	public Node getPrevious() {
		return previous;
	}

	public void setWeight(int weight) {
		this.weight = weight;		
	}

	public int getWeight() {
		return this.weight;		
	}

	public boolean isSummaryRecevied() {
		return summaryRecevied;
	}

	public void setSummaryRecevied(boolean summaryRecevied) {
		this.summaryRecevied = summaryRecevied;
	}
}
