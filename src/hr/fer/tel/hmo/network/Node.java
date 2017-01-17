package hr.fer.tel.hmo.network;

/**
 * Represents a node in a network
 */
public class Node {

	private final int index;

	private final double powerConsumption;

	/**
	 * Create a new node with given power consumption and index.
	 *
	 * @param index            nodes index
	 * @param powerConsumption power consumption
	 */
	public Node(int index, double powerConsumption) {
		this.index = index;
		this.powerConsumption = powerConsumption;
	}

	public int getIndex() {
		return index;
	}

	public double getPowerConsumption() {
		return powerConsumption;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Node)) {
			return false;
		}

		Node node = (Node) o;

		return index == node.index;
	}

	@Override
	public int hashCode() {
		return index;
	}

}
