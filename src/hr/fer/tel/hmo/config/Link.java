package hr.fer.tel.hmo.config;

/**
 * Link between two nodes in a network.
 */
public class Link {

	private double bandwidth;

	private double powerConsumption;

	private double delay;

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
}
