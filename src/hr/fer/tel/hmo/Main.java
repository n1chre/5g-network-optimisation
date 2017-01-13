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
import hr.fer.tel.hmo.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 */
public class Main {

	private static final int TABU_RUNS = 420;
	private static final Object RS_LOCK = new Object();

	private static RoutingSolution bestRS = null;

	public static void main(String[] args) {

		int x = 1;

		String name = "instance" + (x == 0 ? "_small" : "-bez_43_44") + ".txt";
		args = new String[]{"/Users/fhrenic/Programming/vnf-placer/" + name};

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
			new Solver(evaluator, router, placer, i).run();
		}

		System.out.println(bestRS.getSolution());
		System.out.println("Best fitness = " + -bestRS.getFitness());
	}

	/**
	 * Solves one tabu problem
	 */
	private static class Solver implements Runnable {

		private Evaluator evaluator;
		private Router router;
		private Placer placer;
		private int id;

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
			if (!evaluator.isValid(p)) {
				throw new RuntimeException("Initial placement not valid");
			}

			Solution s = new Solution(p, rts);
			evaluator.assertSolution(s);

			TabuProblem<RoutingSolution> tp = new RoutingProblem(evaluator, router, s);
			RoutingSolution rs = Tabu.search(tp);
			if (rs != null) {

				try {
					evaluator.assertSolution(rs.getSolution());
				} catch (RuntimeException ex) {
					System.out.println(rs.getSolution());
					System.out.println(ex.getMessage());
					return;
				}

				System.err.printf("-> %.2f%n", -rs.getFitness());

				synchronized (RS_LOCK) {
					if (bestRS == null || rs.isBetterThan(bestRS)) {
						bestRS = rs;
						System.err.printf("Found new best solution (%.2f)!%n", -bestRS.getFitness());
						try {
							String fname = String.format("/Users/fhrenic/sols2/sol_%d.txt", (int) -rs.getFitness());
							Util.toFile(rs.getSolution().toString(), fname);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}

			}
		}
	}
}
