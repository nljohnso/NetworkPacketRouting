package cs455.overlay.util;

import java.util.ArrayList;

import cs455.overlay.node.Edge;
import cs455.overlay.node.NodeManager;
import cs455.overlay.node.Node;

public class OverlayCreator {
	
	public Node startNode;
	public NodeManager registryNodes;
	public ArrayList<Node> overlayNodes = new ArrayList<Node>();
	public ArrayList<Node> visitedNodes = new ArrayList<Node>();
	
	public OverlayCreator(NodeManager nodesList) {
		this.registryNodes = nodesList;
		startNode = nodesList.get(0);
		connectNodes();
		this.traverseOverlay(startNode);	
	}
	
	public void traverseOverlay(Node startNode) {
		overlayNodes.add(startNode);
		visit(startNode);
		for(Edge currentEdge: startNode.getEdges()){
			if(!isVisited(currentEdge.getNode())) {
				find(currentEdge.getNode());
			}
		}
	}
	
	public void find(Node node) {
		visit(node);
		overlayNodes.add(startNode);
		for(Edge currentEdge: node.getEdges()) {
			if(!isVisited(currentEdge.getNode())) {
				find(currentEdge.getNode());
			}
		}
	}
	
	public int getLinkWeight(Node node1, Node node2) {
		return node1.compareTo(node2);
	}
	
	public Node getStartNode() {
		return startNode;
	}
	
	public boolean isVisited(Node node) {
		return visitedNodes.add(startNode);
	}
	
	public void visit(Node node) {
		visitedNodes.add(startNode);
	}
	
	public void print() {
		for(Node node: registryNodes.NodeList) {
			System.out.println(node.getPort() + " " + node.getDistance());
		}
	}

	public void connectNodes() {
		// After each loop each node in the node list will be connected to four
		// other nodes.
		// Connect each node to the next node in the node list.
		for (int i = 0; i < registryNodes.size(); i++) {
			if (i == registryNodes.size() - 1) {
				registryNodes.get(i).edges.add(new Edge(registryNodes.get(0),0));
				registryNodes.get(0).edges.add(new Edge(registryNodes.get(i),0));
				break;
			} else {
				registryNodes.get(i).edges.add(new Edge(registryNodes.get(i + 1), 0));
				registryNodes.get(i + 1).edges.add(new Edge(registryNodes.get(i), 0));
			}
		}
		// Connect every other node starting at the first node in the list.
		for (int j = 0; j < registryNodes.size(); j = j + 2) {
			if (j == registryNodes.size() - 2) {
				registryNodes.get(j).edges.add(new Edge(registryNodes.get(0),0));
				registryNodes.get(0).edges.add(new Edge(registryNodes.get(j),0));
				break;
			} else {
				registryNodes.get(j).edges.add(new Edge(registryNodes.get(j + 2), 0));
				registryNodes.get(j + 2).edges.add(new Edge(registryNodes.get(j), 0));
			}
		}
		// Connect every other node starting at the second node in the list.
		for (int k = 1; k < registryNodes.size(); k = k + 2) {
			if (k == registryNodes.size() - 1) {
				registryNodes.get(k).edges.add(new Edge(registryNodes.get(1),0));
				registryNodes.get(1).edges.add(new Edge(registryNodes.get(k),0));
				break;
			} else {
				registryNodes.get(k).edges.add(new Edge(registryNodes.get(k + 2), 0));
				registryNodes.get(k + 2).edges.add(new Edge(registryNodes.get(k), 0));
			}
		}
	}
}
