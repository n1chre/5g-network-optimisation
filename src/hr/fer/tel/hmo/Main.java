package hr.fer.tel.hmo;

import hr.fer.tel.hmo.instance.Instance;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.placement.Placer;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.tabu.alg.Tabu;
import hr.fer.tel.hmo.tabu.alg.TabuProblem;
import hr.fer.tel.hmo.tabu.impl.RoutingProblem;
import hr.fer.tel.hmo.tabu.impl.RoutingSolution;
import hr.fer.tel.hmo.util.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 */
public class Main {

	private static final int TABU_RUNS = 100;
	private static final Object RS_LOCK = new Object();

	private static RoutingSolution bestRS = null;

	public static void main(String[] args) {

		args = new String[]{"./instance_big.txt"};

		InputStream stream;
		if (args.length > 0) {
			try {
				Path p = Paths.get(args[0]);
				stream = Files.newInputStream(p);
			} catch (IOException ex) {
				System.err.println("File not found, reading from stdin");
				stream = System.in;
			}
		} else {
			stream = System.in;
		}

		Instance instance;
		try {
			instance = Instance.readFromStream(stream);
		} catch (IOException ex) {
			System.err.println("Error while reading from stream");
			ex.printStackTrace();
			return;
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			return;
		}
		System.err.println("Network configured...");

		Topology t = instance.getTopology();

		// Create main solvers
		Evaluator evaluator = new Evaluator(t);
		Placer placer = Placer.get(t, evaluator::isValid);
		Router router = Router.get(t);
		System.err.println("Created evaluator, placer and router...");

		System.err.println("Starting tabu runs...");

		for (int i = 0; i < TABU_RUNS; i++) {
			// make parallel
			new Solver(evaluator, router, placer, i).run();
		}

		System.out.println(bestRS.getSolution());
		System.out.println("Best fitness = " + -bestRS.getFitness());
	}

	/**
	 * Solves one tabu problem
	 */
	private static class Solver implements Runnable {

		private final Evaluator evaluator;
		private final Router router;
		private final Placer placer;
		private final int id;

		Solver(Evaluator evaluator, Router router, Placer placer, int id) {
			this.evaluator = evaluator;
			this.router = router;
			this.placer = placer;
			this.id = id;
		}

		@Override
		public void run() {

			System.err.printf("\tStarting solver[%d]\t", id);

			Placement p;
			Matrix<Integer, Integer, Route> rts;

			do {
				p = placer.next();
				rts = router.findRouting(p);
			} while (rts == null);
			Solution s = new Solution(p, rts);

			try {
				evaluator.assertSolution(s);
			} catch (RuntimeException ex) {
				return;
			}

			TabuProblem<RoutingSolution> tp = new RoutingProblem(evaluator, router, s);
			RoutingSolution rs = Tabu.search(tp);
			if (rs != null) {

				try {
					evaluator.assertSolution(rs.getSolution());
				} catch (RuntimeException ex) {
					return;
				}

				System.err.printf("-> %.2f%n", -rs.getFitness());

				synchronized (RS_LOCK) {
					if (bestRS == null || rs.isBetterThan(bestRS)) {
						bestRS = rs;
						System.err.printf("Found new best solution (%.2f)!%n", -bestRS.getFitness());
					}
				}

			}
		}
	}
}
