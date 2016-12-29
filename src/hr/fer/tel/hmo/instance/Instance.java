package hr.fer.tel.hmo.instance;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Network;
import hr.fer.tel.hmo.network.Node;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an instance of vnf placement problem.
 * Its only job is to transfer values from file to Java and to validate them.
 */
public class Instance {

	/**
	 * Configured topology
	 */
	private Topology topology;

	// ==============================================================================

	/**
	 * Number of servers in network
	 */
	private int numberOfServers;

	/**
	 * Number of virtual network functions
	 */
	private int numberOfVns;

	/**
	 * Number of resources per server
	 */
	private int numberOfResources;

	/**
	 * Number of nodes in network
	 */
	private int numberOfNodes;

	/**
	 * Number of service chains
	 */
	private int numberOfServiceChains;

	/**
	 * Maximum amount of power
	 * PMax[serverIndex]
	 */
	private List<Double> PMax;

	/**
	 * Minimum amount of power consumed by each server
	 * PMin[serverIndex]
	 */
	private List<Double> PMin;

	/**
	 * Requirements for each resource given for each component
	 * requirements[resourceIndex][componentIndex]
	 */
	private List<List<Double>> requirements;

	/**
	 * Represents in what order is each resource available at each server.
	 * resourceAvailability[resourceIndex][serverIndex]
	 */
	private List<List<Double>> resourceAvailability;

	/**
	 * Determines where is each server places (to which node is it connected to)
	 * serverPlacement[serverIndex][nodeIndex]
	 */
	private List<List<Double>> serverPlacement;

	/**
	 * Defines service chains.
	 */
	private List<List<Double>> serviceChain;

	/**
	 * Power consumption of each node.
	 */
	private List<Double> PNode;

	/**
	 * List of edges between nodes with their properties
	 */
	private List<List<Double>> edges;

	/**
	 * List of demanded bandwidths between two components
	 */
	private List<List<Double>> vnfDemands;

	/**
	 * Maximal permitted latency for each service chain
	 */
	private List<Double> maximalLatency;

	/**
	 * Create a new instance
	 */
	private Instance() {
		// everything should be configured from other methods
		// jbg
	}

	/**
	 * Read all instance properties from stream
	 *
	 * @param stream input stream from which instance definition is read
	 * @return newly created (read) instance
	 */
	public static Instance readFromStream(InputStream stream) throws IOException {
		InstanceReader reader = new InstanceReader(stream);

		Instance instance = new Instance();

		instance.numberOfServers = reader.singleValue().intValue();
		instance.numberOfVns = reader.singleValue().intValue();
		instance.numberOfResources = reader.singleValue().intValue();
		instance.numberOfNodes = reader.singleValue().intValue();
		instance.numberOfServiceChains = reader.singleValue().intValue();

		instance.PMax = reader.array();
		instance.PMin = reader.array();
		instance.requirements = reader.matrix();
		instance.resourceAvailability = reader.matrix();
		instance.serverPlacement = reader.matrix();
		instance.serviceChain = reader.matrix();
		instance.PNode = reader.array();
		instance.edges = reader.matrix();
		instance.vnfDemands = reader.matrix();
		instance.maximalLatency = reader.array();

		reader.close();

		if (!instance.isValid()) {
			throw new IllegalArgumentException("Instance configuration is not valid");
		}

		Network n = instance.configureNetwork();
		Component[] cs = instance.configureComponents();
		List<ServiceChain> scs = instance.configureServiceChains(cs);
		Matrix<Integer, Integer, Double> ds = instance.configureDemands();

		instance.topology = new Topology(n, cs, ds, scs);

		return instance;
	}

	/**
	 * Configure the whole network
	 */
	private Network configureNetwork() {
		Network network = new Network(numberOfNodes, numberOfServers);

		// configure nodes
		int nodeIndex = 0;
		for (double power : PNode) {
			if (power < 0) {
				networkException("Power consumption can't be negative");
			}
			Node node = new Node(nodeIndex++, power);
			if (!network.addNode(node)) {
				networkException("Can't add any more nodes");
			}
		}

		// connect nodes with links
		for (List<Double> edge : edges) {
			int n1 = edge.get(0).intValue() - 1;
			int n2 = edge.get(1).intValue() - 1;
			double bandwidth = edge.get(2);
			double powerConsumption = edge.get(3);
			double delay = edge.get(4);

			if (bandwidth < 0) {
				networkException("Bandwidth can't be negative");
			}
			if (powerConsumption < 0) {
				networkException("Power consumption can't be negative");
			}
			if (delay < 0) {
				networkException("Delay can't be negative");
			}

			Link link = new Link(bandwidth, powerConsumption, delay);

			if (!network.addLink(n1, n2, link)) {
				networkException("Invalid node indexes for link: " + edge);
			}
		}

		// create servers
		for (int serverIndex = 0; serverIndex < numberOfServers; serverIndex++) {
			double pmin = PMin.get(serverIndex);
			double pmax = PMax.get(serverIndex);

			if (pmin < 0 || pmax < 0) {
				networkException("Power consumption can't be negative");
			}

			List<Double> resources = new ArrayList<>(numberOfResources);
			for (List<Double> list : resourceAvailability) {
				double res = list.get(serverIndex);
				if (res < 0) {
					networkException("Resource need can't be negative");
				}
				resources.add(res);
			}

			int nodeIdx = 0;
			for (Double d : serverPlacement.get(serverIndex)) {
				if (1 == d.intValue()) {
					break;
				}
				nodeIdx++;
			}

			if (nodeIdx == numberOfNodes) {
				continue; // no node was found
			}

			if (!network.addServer(serverIndex, pmin, pmax, nodeIdx, resources)) {
				networkException("Server configured badly");
			}
		}

		return network;
	}

	/**
	 * Configure all components
	 *
	 * @return components
	 */
	private Component[] configureComponents() {
		Component[] components = new Component[numberOfVns];

		// create all components
		for (int componentIndex = 0; componentIndex < numberOfVns; componentIndex++) {
			List<Double> resources = new ArrayList<>(numberOfResources);
			for (List<Double> list : requirements) {
				double res = list.get(componentIndex);
				if (res < 0) {
					componentException("Resource need can't be negative");
				}
				resources.add(res);
			}
			components[componentIndex] = new Component(componentIndex, resources);
		}

		return components;
	}

	/**
	 * @param components configured components
	 * @return service chains
	 */
	private List<ServiceChain> configureServiceChains(Component[] components) {
		List<ServiceChain> serviceChains = new LinkedList<>();

		// configure service chains
		for (int scIndex = 0; scIndex < numberOfServiceChains; scIndex++) {
			List<Double> chain = serviceChain.get(scIndex);
			double latency = maximalLatency.get(scIndex);

			if (latency < 0) {
				componentException("Latency can't be negative");
			}

			ServiceChain sc = new ServiceChain(latency);
			serviceChains.add(sc);

			for (int componentIndex = 0; componentIndex < chain.size(); componentIndex++) {
				boolean in = chain.get(componentIndex).intValue() == 1;

				if (in) {
					Component c = components[componentIndex];
					sc.addComponent(c);
					c.addServiceChain(sc);
				}
			}
		}

		return serviceChains;
	}

	private Matrix<Integer, Integer, Double> configureDemands() {
		Matrix<Integer, Integer, Double> demands = new Matrix<>();

		for (List<Double> demand : vnfDemands) {
			int ci1 = demand.get(0).intValue() - 1;
			int ci2 = demand.get(1).intValue() - 1;

			if (ci1 < 0 || ci1 >= numberOfVns || ci2 < 0 || ci2 >= numberOfVns) {
				componentException("Component index out of bounds: " + vnfDemands);
			}

			double bandwidth = demand.get(2);
			if (bandwidth < 0) {
				componentException("Demanded bandwidth can't be negative");
			}

			demands.put(ci1, ci2, bandwidth);
		}

		return demands;
	}

	/**
	 * @return true if instance is properly configured
	 */
	private boolean isValid() {

		if (numberOfServers <= 0 || numberOfServers <= 0 || numberOfVns <= 0
				|| numberOfResources <= 0 || numberOfServiceChains <= 0) {
			return false;
		}

		// check arrays

		if (!Util.checkArray(PMax, numberOfServers)) {
			return false;
		}

		if (!Util.checkArray(PMin, numberOfServers)) {
			return false;
		}

		if (!Util.checkArray(PNode, numberOfNodes)) {
			return false;
		}

		if (!Util.checkArray(maximalLatency, numberOfServiceChains)) {
			return false;
		}

		// check matrix

		if (!Util.checkMatrix(requirements, numberOfResources, numberOfVns)) {
			return false;
		}

		if (!Util.checkMatrix(resourceAvailability, numberOfResources, numberOfServers)) {
			return false;
		}

		if (!Util.checkMatrix(serverPlacement, numberOfServers, numberOfNodes)) {
			return false;
		}

		if (!Util.checkMatrix(serviceChain, numberOfServiceChains, numberOfVns)) {
			return false;
		}

		if (!Util.checkMatrix(edges, -1, 5)) {
			return false;
		}

		if (!Util.checkMatrix(vnfDemands, -1, 3)) {
			return false;
		}

		return true;
	}

	public Topology getTopology() {
		return topology;
	}

	/**
	 * Throw exception when configuring network
	 *
	 * @param msg message
	 */
	private static void networkException(String msg) {
		throw new IllegalArgumentException("[Network:Conf] " + msg);
	}

	/**
	 * Throw exception when configuring components/service chains
	 *
	 * @param msg message
	 */
	private static void componentException(String msg) {
		throw new IllegalArgumentException("[Component:Conf] " + msg);
	}

}
