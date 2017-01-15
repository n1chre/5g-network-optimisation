package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Router that finds routes sequentially
 */
public abstract class SequentialRouter extends Router {

	// delay is always 20
	private static final double DEFAULT_DELAY = 20.0;

	protected Topology topology;

	private NodeProxy[] nodes;
	Map<NodeProxy, List<LinkProxy>> neighbors;

	SequentialRouter(Topology topology) {
		this.topology = topology;
	}

	protected void initialize() {

		int numNodes = topology.getNetwork().getNumberOfNodes();
		nodes = new NodeProxy[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new NodeProxy(topology.getNetwork().getNode(i));
		}

		// create neighbors
		// node -> list of links which go out of it
		neighbors = new HashMap<>();
		Matrix<Integer, Integer, Link> links = topology.getNetwork().getLinks();
		for (int n1 : links.keys()) {
			neighbors.put(nodes[n1],
					links.getFor(n1).entrySet()
							.parallelStream()
							.map(e -> new LinkProxy(nodes[e.getKey()], e.getValue()))
							.collect(Collectors.toCollection(LinkedList::new)));
		}

	}

	/**
	 * Find a route that goes from one node to other with given demands
	 *
	 * @param from      start node
	 * @param end       end node
	 * @param delay     maximal delay
	 * @param bandwidth demanded bandwidth
	 * @return found route or null if it can find one
	 */
	protected abstract List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth);

	@Override
	public Matrix<Integer, Integer, Route> findRouting(Placement placement) {

		initialize();

		final Matrix<Integer, Integer, Route> routes = new Matrix<>();

		// create cache
		// cache[component index] = node index (component -> server -> node)
		Function<Integer, Integer> nodeIdx =
				cidx -> topology.getNetwork().getServer(
						placement.getPlacementFor(cidx)
				).getNode().getIndex();

		int numComps = topology.getComponents().length;
		int[] CACHE = new int[numComps];
		for (int i = 0; i < numComps; i++) {
			CACHE[i] = nodeIdx.apply(i);
		}

		class tmp {
			private int cmp1, cmp2;
			private double bandwidth;
		}

		List<tmp> tmps = new ArrayList<>();
		Matrix<Integer, Integer, Double> demands = topology.getDemands();
		for (int cmp1 : demands.keys()) {
			for (Map.Entry<Integer, Double> e : demands.getFor(cmp1).entrySet()) {
				tmp t = new tmp();
				t.cmp1 = cmp1;
				t.cmp2 = e.getKey();
				t.bandwidth = e.getValue();
				tmps.add(t);
			}
		}
		Collections.shuffle(tmps, Util.RANDOM);

		for (tmp t : tmps) {
			int node1 = CACHE[t.cmp1];
			int node2 = CACHE[t.cmp2];

			if (node1 == node2) {
				routes.put(t.cmp1, t.cmp2, new Route(t.cmp1, t.cmp2, Collections.singletonList(node1)));
				continue;
			}

			nodes[node2].used = true;

			List<Integer> r = path(
					nodes[node1], nodes[node2],
					DEFAULT_DELAY, t.bandwidth
			);
			if (r == null) {
				return null;
			}

			routes.put(t.cmp1, t.cmp2, new Route(t.cmp1, t.cmp2, r));
		}
		return routes;
	}
}
