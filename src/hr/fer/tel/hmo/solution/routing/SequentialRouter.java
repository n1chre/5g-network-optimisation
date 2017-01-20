package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;
import java.util.function.Function;

/**
 * Router that finds routes sequentially
 */
public abstract class SequentialRouter extends Router {

	private final Topology topology;

	NodeProxy[] nodes;
	Matrix<NodeProxy, NodeProxy, LinkProxy> neighbors;

	SequentialRouter(Topology topology) {
		this.topology = topology;
	}

	private void initialize() {

		int numNodes = topology.getNetwork().getNumberOfNodes();
		nodes = new NodeProxy[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new NodeProxy(topology.getNetwork().getNode(i));
		}

		// create neighbors
		// node -> list of links which go out of it
		Matrix<Integer, Integer, Link> links = topology.getNetwork().getLinks();

		neighbors = new Matrix<>();
		for (int n1 : links.keys()) {
			links.getFor(n1).entrySet().forEach(e -> neighbors.put(
					nodes[n1],
					nodes[e.getKey()],
					new LinkProxy(nodes[e.getKey()], e.getValue()))
			);
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

		final class tmp {
			private int cmp1, cmp2;
			private double delay;
			private double bandwidth;

			private tmp(int cmp1, int cmp2, double delay, double bandwidth) {
				this.cmp1 = cmp1;
				this.cmp2 = cmp2;
				this.delay = delay;
				this.bandwidth = bandwidth;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) {
					return true;
				}
				if (!(o instanceof tmp)) {
					return false;
				}

				tmp tmp = (tmp) o;

				return cmp1 == tmp.cmp1 && cmp2 == tmp.cmp2;
			}

			@Override
			public int hashCode() {
				int result = cmp1;
				result = 31 * result + cmp2;
				return result;
			}
		}

		Set<tmp> tmps = new HashSet<>();

		for (ServiceChain sc : topology.getServiceChains()) {

			int ncs = sc.getNumberOfComponents();
			if (ncs <= 1) {
				continue;
			}

			double delay = sc.getLatency();

			int prevCompIdx = sc.getComponent(0).getIndex();
			for (int i = 1; i < ncs; i++) {
				int currCompIdx = sc.getComponent(i).getIndex();

				Double bandwidth = topology.getDemands().get(prevCompIdx, currCompIdx);
				tmps.add(new tmp(prevCompIdx, currCompIdx, delay, bandwidth));

				prevCompIdx = currCompIdx;

			}
		}

		// randomize
		List<tmp> tmps_ = new ArrayList<>(tmps);
		Collections.shuffle(tmps_, Util.RANDOM);

		for (tmp t : tmps_) {
			int node1 = CACHE[t.cmp1];
			int node2 = CACHE[t.cmp2];

			if (node1 == node2) {
				routes.put(t.cmp1, t.cmp2, new Route(t.cmp1, t.cmp2, Collections.singletonList(node1)));
				continue;
			}

			nodes[node1].used = true;
			nodes[node2].used = true;

			// Ovo je krivo, delay treba racunati drukcije jer ovak trazim rutu s max delayom
			// koji je zapravo delay za cijeli service chain (i to neki, prvi). zabrijal sam si
			List<Integer> r = path(nodes[node1], nodes[node2], t.delay, t.bandwidth);
			if (r == null) {
				return null;
			}

			routes.put(t.cmp1, t.cmp2, new Route(t.cmp1, t.cmp2, r));
		}
		return routes;
	}
}
