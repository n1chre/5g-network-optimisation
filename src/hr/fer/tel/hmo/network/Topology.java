package hr.fer.tel.hmo.network;

import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.List;

/**
 * Whole network topology and given problem.
 */
public class Topology {

	/**
	 * Network configuration used to evaluate placement and routing
	 */
	private final Network network;

	/**
	 * Components that are placed onto servers
	 */
	private final Component[] components;

	/**
	 * List of service chains
	 */
	private final List<ServiceChain> serviceChains;

	/**
	 * List of demanded bandwidth from one component to another.
	 */
	private final Matrix<Integer, Integer, Double> demands;

	public Topology(Network network, Component[] components, Matrix<Integer, Integer, Double> demands,
	                List<ServiceChain> serviceChains) {
		this.network = network;
		this.components = components;
		this.serviceChains = serviceChains;
		this.demands = demands;
	}

	public Network getNetwork() {
		return network;
	}

	public Component[] getComponents() {
		return components;
	}

	public List<ServiceChain> getServiceChains() {
		return serviceChains;
	}

	public Matrix<Integer, Integer, Double> getDemands() {
		return demands;
	}
}
