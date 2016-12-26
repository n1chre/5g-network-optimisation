package hr.fer.tel.hmo.network;

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
	private Network network;

	/**
	 * Components that are placed onto servers
	 */
	private Component[] components;

	/**
	 * List of service chains
	 */
	private List<ServiceChain> serviceChains;

	public Topology(Network network, Component[] components, List<ServiceChain> serviceChains) {
		this.network = network;
		this.components = components;
		this.serviceChains = serviceChains;
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
}
