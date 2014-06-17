package cs455.overlay.node;

public final class Edge {
	private Node node;
	private Integer linkWeight;

	public Edge(Node node, Integer linkWeight) {
		this.node = node;
		this.linkWeight = linkWeight;
	}

	public Node getNode() {
		return node;
	}

	public int getLinkWeight() {
		return linkWeight;
	}
	
	public void setLinkWeight(int linkWeight) {
		this.linkWeight = linkWeight;
	}
}
