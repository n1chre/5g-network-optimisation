package hr.fer.tel.hmo;

import hr.fer.tel.hmo.instance.Instance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by fhrenic on 22/12/2016.
 */
public class Main {

	public static void main(String[] args) {

		args = new String[]{"/Users/fhrenic/Programming/vnf-placer/instance_small.txt"};

		InputStream stream;
		if (args.length > 0) {
			try {
				Path p = Paths.get(args[0]);
				stream = Files.newInputStream(p);
			} catch (IOException ex) {
				stream = System.in;
			}
		} else {
			stream = System.in;
		}

		Instance id;
		try {
			id = Instance.readFromStream(stream);
		} catch (IOException ex) {
			System.err.println("Error while reading from stream");
			return;
		}

		System.out.println(id.getVnfDemands());

	}

}
