package hr.fer.tel.hmo.instance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used for reading instance definition from stream
 */
public class InstanceReader implements AutoCloseable {

	/**
	 * Regular expression for finding numbers in double format
	 */
	private static final Pattern NUM = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");

	/**
	 * Reader used for reading lines
	 */
	private BufferedReader reader;

	/**
	 * Current line number for notifying about errors
	 */
	private int lineNumber;

	/**
	 * Create a new reader from given stream
	 *
	 * @param stream input stream
	 */
	InstanceReader(InputStream stream) {
		reader = new BufferedReader(new InputStreamReader(stream));
		lineNumber = 0;
	}

	/**
	 * Read a single value from stream
	 *
	 * @return value
	 * @throws IOException on read exception
	 */
	Double singleValue() throws IOException {
		String line = readLine();
		String[] sl = line.split("=");
		if (sl.length != 2) {
			throwException("Single value wrong format, expecting value=X;");
		}

		List<Double> num = getNumbers(sl[1]);
		if (num.size() != 1) {
			throwException("Single value wrong format, expecting value=X;");
		}

		return num.get(0);
	}

	/**
	 * Read array of values from stream
	 *
	 * @return array of values
	 * @throws IOException on read exception
	 */
	List<Double> array() throws IOException {
		String line = readLine();
		String[] sl = line.split("=");
		if (sl.length != 2) {
			throwException("Array wrong format, expecting array=[X,X,...];");
		}

		return getNumbers(sl[1]);
	}

	/**
	 * Read matrix of values from stream
	 *
	 * @return matrix of values
	 * @throws IOException on read exception
	 */
	List<List<Double>> matrix() throws IOException {
		List<List<Double>> matrix = new ArrayList<>();
		readLine(); // read first line

		String line = readLine();
		while (!line.contains("];")) {
			matrix.add(getNumbers(line));
			line = readLine();
		}

		return matrix;
	}

	/**
	 * Close this reader
	 *
	 * @throws IOException on close exception
	 */
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Read next line from stream, skip empty lines
	 *
	 * @return next line
	 * @throws IOException when there are no more lines or exception on read line
	 */
	private String readLine() throws IOException {
		String line = reader.readLine();
		if (line == null) {
			throw new IOException("No more lines");
		}
		lineNumber++;
		line = line.trim();
		if (line.isEmpty()) {
			return readLine();
		}
		return line;
	}

	/**
	 * Throw exception at given line number with custom message
	 *
	 * @param msg custom message
	 */
	private void throwException(String msg) {
		throw new IllegalArgumentException(String.format("[%d] %s", lineNumber, msg));
	}

	/**
	 * Extract all numbers from given string into a list
	 *
	 * @param str string
	 * @return list of numbers
	 */
	private List<Double> getNumbers(String str) {
		List<Double> numbers = new ArrayList<>();
		Matcher m = NUM.matcher(str);
		while (m.find()) {
			numbers.add(Double.parseDouble(m.group()));
		}
		return numbers;
	}

}
