package hr.fer.tel.hmo.network;

/**
 * Link between two nodes in a network.
 */
public class Link {

	private final double bandwidth;

	private final double powerConsumption;

	private final double delay;

	/**
	 * Create a new link with given parameters
	 *
	 * @param bandwidth        link's bandwidth
	 * @param powerConsumption link's power consumption
	 * @param delay            link's delay
	 */
	public Link(double bandwidth, double powerConsumption, double delay) {
		this.bandwidth = bandwidth;
		this.powerConsumption = powerConsumption;
		this.delay = delay;
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

	@Override
	public String toString() {
		return String.format("Link[%.2f,%.2f,%.2f]", bandwidth, powerConsumption, delay);
	}
}
