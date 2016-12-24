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
	 * Number of servers in config
	 */
	private int numberOfServers;

	/**
	 * Number of virtual config functions
	 */
	private int numberOfVns;

	/**
	 * Number of resources per server
	 */
	private int numberOfResources;

	/**
	 * Number of nodes in config
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

	/**
	 * @return true if instance is properly configured
	 */
	public boolean isValid() {

		// check arrays

		if (!checkArray(PMax, numberOfServers)) {
			return false;
		}

		if (!checkArray(PMin, numberOfServers)) {
			return false;
		}

		if (!checkArray(PNode, numberOfNodes)) {
			return false;
		}

		if (!checkArray(maximalLatency, numberOfServiceChains)) {
			return false;
		}

		// check matrix

		if (!checkMatrix(requirements, numberOfResources, numberOfVns)) {
			return false;
		}

		if (!checkMatrix(resourceAvailability, numberOfResources, numberOfServers)) {
			return false;
		}

		if (!checkMatrix(serverPlacement, numberOfServers, numberOfNodes)) {
			return false;
		}

		if (!checkMatrix(serviceChain, numberOfServiceChains, numberOfVns)) {
			return false;
		}

		if (!checkMatrix(edges, -1, 5)) {
			return false;
		}

		if (!checkMatrix(vnfDemands, -1, 3)) {
			return false;
		}

		return true;
	}

	/**
	 * Check if matrix dimensions are n rows by m columns.
	 * If n=-1, only columns are checked
	 *
	 * @param matrix matrix to check
	 * @param n      expected number of rows
	 * @param m      expected number of columns
	 * @return true if matrix is n x m
	 */
	private static boolean checkMatrix(List<List<Double>> matrix, int n, int m) {
		if (matrix == null) {
			return false;
		}

		if (n >= 0 && matrix.size() != n) {
			return false;
		}

		for (List<Double> row : matrix) {
			if (row.size() != m) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if size of given array is n
	 *
	 * @param array array to check
	 * @param n     expected size
	 * @return true if it is of expected size
	 */
	private static boolean checkArray(List<Double> array, int n) {
		return array != null && array.size() == n;
	}

}
