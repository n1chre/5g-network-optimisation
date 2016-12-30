package hr.fer.tel.hmo;

import hr.fer.tel.hmo.instance.Instance;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.placement.Placer;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.util.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 */
public class Main {

	public static void main(String[] args) {

		int x = 0;

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

		// Create evalator
		Evaluator evaluator = new Evaluator(t);
		System.err.println("Evaluator created...");

		Placer p = Placer.get(t, evaluator::isValid);
//		System.out.println(p.getInitialPlacements(500).size());


		Matrix<Integer,Integer,Route> rts;
		for (Placement p_ : p.getInitialPlacements(500)){
			Router r = new Router(t);
			rts = r.findRouting(p_);
			if (rts!=null){
				System.out.println("bravo odi spat");
			}
		}
		System.out.println("pas materrrr");


	}

}
