package hr.fer.tel.hmo.network;

/**
 * Link between two nodes in a network.
 */
public class Link {

	private Node from;

	private Node to;

	private double bandwidth;

	private double powerConsumption;

	private double delay;

	/**
	 * Create a new link with given parameters
	 *
	 * @param from             from which node
	 * @param to               to which node
	 * @param bandwidth        link's bandwidth
	 * @param powerConsumption link's power consumption
	 * @param delay            link's delay
	 */
	public Link(Node from, Node to, double bandwidth, double powerConsumption, double delay) {
		this.from = from;
		this.to = to;
		this.bandwidth = bandwidth;
		this.powerConsumption = powerConsumption;
		this.delay = delay;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public double getBandwidth() {
		return bandwidth;
	}

	public double getPowerConsumption() {
		return powerConsumption;
	}

	public double getDelay() {
		return delay;
	}
}
