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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Link)) {
			return false;
		}

		Link link = (Link) o;

		if (Double.compare(link.bandwidth, bandwidth) != 0) {
			return false;
		}
		return Double.compare(link.powerConsumption, powerConsumption) == 0 && Double.compare(link.delay, delay) == 0;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(bandwidth);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(powerConsumption);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(delay);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

}
