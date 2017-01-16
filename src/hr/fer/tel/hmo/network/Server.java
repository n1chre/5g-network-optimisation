package hr.fer.tel.hmo.network;

import hr.fer.tel.hmo.vnf.Component;

import java.util.List;

/**
 * Represents a server that is connected to network
 */
public class Server {

	/**
	 * Server's index
	 */
	private final int index;

	/**
	 * Minimal power consumption. Uses this much power when 0% resources are used
	 */
	private final double pmin;

	/**
	 * Maximal power consumption. Uses this much power when 100% resources are used
	 */
	private final double pmax;

	/**
	 * Node that this server is connected to.
	 */
	private final Node node;

	/**
	 * Available resources
	 */
	private final List<Double> resources;

	/**
	 * Available processor resource
	 */
	private final double processorResource;

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
		this.processorResource = resources.get(0);
	}

	/**
	 * Calculate additional power consumption for this server generated by given component
	 *
	 * @param component component that is placed on this server
	 * @return additional power consumption
	 */
	public double getAdditionalPower(Component component) {
		// only calculate how much is processor used
		double usage = component.getResources().get(0) / processorResource;

		return (pmax - pmin) * usage;
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
