package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Server;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.solution.proxies.ServerProxy;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Place all components onto servers in a greedy way (minimum power)
 */
public class GreedyPlacer extends Placer {

	GreedyPlacer(Topology topology, Function<Placement, Boolean> isValid) {
		super(topology, isValid);
	}

	@Override
	public Placement next() {

		int numNps = topology.getNetwork().getNumberOfNodes();
		NodeProxy[] nps = new NodeProxy[numNps];
		for (int i = 0; i < numNps; i++) {
			nps[i] = new NodeProxy(topology.getNetwork().getNode(i));
		}

		int numSps = topology.getNetwork().getNumberOfServers();
		ServerProxy[] sps = new ServerProxy[numSps];
		for (int i = 0; i < numSps; i++) {
			Server s = topology.getNetwork().getServer(i);
			sps[i] = new ServerProxy(s, nps[s.getNode().getIndex()]);
		}

		Placement p = new Placement(
				topology.getComponents().length,
				topology.getNetwork().getNumberOfServers()
		);

		// different starting solution every time because of this
		Component[] cs = Arrays.copyOf(topology.getComponents(), topology.getComponents().length);
		Util.shuffle(cs);

		for (Component c : cs) {

			ServerProxy sp = null;
			double power = Double.MAX_VALUE;

			for (ServerProxy sp_ : sps) {
				if (!sp_.canGo(c)) {
					continue;
				}
				double pow = sp_.powerUp(c);

				if (pow < power) {
					power = pow;
					sp = sp_;
				}
			}

			if (sp == null) {
				// lolz
				return next();
			}

			sp.emplace(c);
			p.place(c.getIndex(), sp.index);
		}

		return p;
	}

}
