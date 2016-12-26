package hr.fer.tel.hmo.vnf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents one component that needs to be placed on a server.
 */
public class Component {

	private int index;

	private List<Double> resources;

	private Map<Component, Double> bandwidth;

	private List<ServiceChain> serviceChains;

	/**
	 * Create a new component that needs resources
	 *
	 * @param index     component's index
	 * @param resources resources needed
	 */
	public Component(int index, List<Double> resources) {
		this.index = index;
		this.resources = resources;
		this.bandwidth = new HashMap<>();
		this.serviceChains = new LinkedList<>();
	}

	/**
	 * Adds service chain to this component which it's a part of.
	 *
	 * @param serviceChain service chain to add
	 */
	public void addServiceChain(ServiceChain serviceChain) {
		serviceChains.add(serviceChain);
	}

	/**
	 * Set desired bandwidth from this component to another
	 *
	 * @param component other component
	 * @param bandwidth desired bandwidth
	 */
	public void setBandwidth(Component component, double bandwidth) {
		this.bandwidth.put(component, bandwidth);
	}

	public int getIndex() {
		return index;
	}

	public List<Double> getResources() {
		return resources;
	}

	public double getDemandedBandwidthFor(Component other) {
		return bandwidth.getOrDefault(other, 0.0);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Component)) {
			return false;
		}

		Component component = (Component) o;

		return index == component.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
