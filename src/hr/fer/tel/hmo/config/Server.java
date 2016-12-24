package hr.fer.tel.hmo.config;

import java.util.List;

/**
 * Represents a server that is connected to network
 */
public class Server {

	/**
	 * Server's index
	 */
	private int index;

	/**
	 * Minimal power consumption. Uses this much power when 0% resources are used
	 */
	private double pmin;

	/**
	 * Maximal power consumption. Uses this much power when 100% resources are used
	 */
	private double pmax;

	/**
	 * Node that this server is connected to.
	 */
	private Node node;

	/**
	 * Available resources
	 */
	private List<Double> resources;

	/**
	 * Create a new server
	 *
	 * @param index     server's index
	 * @param pmin      pmin
	 * @param pmax      pmax
	 * @param node      node that it's connected to
	 * @param resources available resources
	 */
	public Server(int index, double pmin, double pmax, Node node, List<Double> resources) {
		this.index = index;
		this.pmin = pmin;
		this.pmax = pmax;
		this.node = node;
		this.resources = resources;
	}

	/**
	 * Calculate power consumption for this server depending on used resources
	 *
	 * @param usedResources resources that are being used
	 * @return power consumption
	 */
	public double getPower(List<Double> usedResources) {
		// only calculate how much is processor used
		double usage = usedResources.get(0) / resources.get(0);

		return pmin + (pmax - pmin) * usage;
	}

	public int getIndex() {
		return index;
	}

	public double getPmin() {
		return pmin;
	}

	public double getPmax() {
		return pmax;
	}

	public Node getNode() {
		return node;
	}

	public List<Double> getResources() {
		return resources;
	}
}
