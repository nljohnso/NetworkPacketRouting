package cs455.overlay.dijkstra;

import java.util.*;

import cs455.overlay.node.NodeManager;
import cs455.overlay.node.Node;

public class NodePriorityQueue {

	private PriorityQueue<Node> queue = new PriorityQueue<Node>();

	public NodePriorityQueue() {

	}

	public void add(Node node) {
		queue.add(node);
	}

	public void add(Collection<Node> nodes) {
		queue.addAll(nodes);
	}

	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	public void updateDistance(Node node) {
		this.queue.remove(node);
		this.queue.add(node);
	}

	public boolean hasMore() {
		return !queue.isEmpty();
	}

	public Node remove() {
		return queue.remove();
	}

	public boolean remove(Node aNode) {
		return queue.remove(aNode);
	}

	public void add(NodeManager registryNodes) {
		queue.addAll(registryNodes.NodeList);
	}
}
