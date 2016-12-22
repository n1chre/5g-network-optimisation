package hr.fer.tel.hmo.instance;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents an instance of vnf placement problem.
 * Its only job is to transfer values from file to Java and to validate them.
 */
public class Instance {

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
	 * resourceAvailability[resourceIndex][componentIndex]
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
	 * Initialize a new instance
	 */
	private Instance() {

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
		return instance;
	}

	public int getNumberOfServers() {
		return numberOfServers;
	}

	public int getNumberOfVns() {
		return numberOfVns;
	}

	public int getNumberOfResources() {
		return numberOfResources;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public int getNumberOfServiceChains() {
		return numberOfServiceChains;
	}

	public List<Double> getPMax() {
		return PMax;
	}

	public List<Double> getPMin() {
		return PMin;
	}

	public List<List<Double>> getRequirements() {
		return requirements;
	}

	public List<List<Double>> getResourceAvailability() {
		return resourceAvailability;
	}

	public List<List<Double>> getServerPlacement() {
		return serverPlacement;
	}

	public List<List<Double>> getServiceChain() {
		return serviceChain;
	}

	public List<Double> getPNode() {
		return PNode;
	}

	public List<List<Double>> getEdges() {
		return edges;
	}

	public List<List<Double>> getVnfDemands() {
		return vnfDemands;
	}

	public List<Double> getMaximalLatency() {
		return maximalLatency;
	}
}
