package hr.fer.tel.hmo;

import hr.fer.tel.hmo.instance.Instance;
import hr.fer.tel.hmo.network.Network;
import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

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

		String name = "instance" + (x == 0 ? "_small" : "") + ".txt";
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

		Network network = instance.getNetwork();
		Component[] components = instance.getComponents();
		List<ServiceChain> serviceChains = instance.getServiceChains();

		// Create evalator
		Evaluator evaluator = new Evaluator(network, components, serviceChains);

		System.err.println("Evaluator created...");

	}

}
