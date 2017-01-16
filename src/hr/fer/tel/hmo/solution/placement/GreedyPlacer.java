package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Server;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.solution.proxies.ServerProxy;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

			ServerProxy sp;

			List<ServerProxy> sps_ = Arrays.stream(sps)
					.filter(s -> s.canGo(c))
					.sorted(Comparator.comparing(s -> s.powerUp(c)))
					.collect(Collectors.toList());

			double rnd = Util.randomDouble();
			if (rnd > 0.5 || sps_.size() == 1) {
				sp = sps_.get(0);
			} else if (rnd > 0.2 || sps_.size() == 2) {
				sp = sps_.get(1);
			} else if (rnd > 0.05 || sps_.size() == 3) {
				sp = sps_.get(2);
			} else {
				sp = sps_.get(3);
			}

			if (sp == null) {
				System.out.println("idiote");
				// lolz
				return next();
			}

			sp.emplace(c);
			p.place(c.getIndex(), sp.index);
		}

		return p;
	}

}
