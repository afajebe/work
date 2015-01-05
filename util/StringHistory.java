package util;

import java.io.Serializable;
import java.util.Vector;


/**
 * A Simple class that stores a list of Strings - that presumable represent some data.
 */
public class StringHistory implements Serializable{
	
	/** The data. */
	private	Vector<String> data;
	
	/** The delimiter placed between datapoints (when calling toString()). */
	private String		delimiter;
	
	
	/**
	 * Creates a new instance of StringHistory that uses tab to delimit individual pieces of data.
	 */
	public StringHistory() {
		this.data = new Vector<String>();
		delimiter = "\t";
	}
	
	
	/**
	 * Creates a new instance of StringHistory
	 *
	 * @param delimiter - This String is placed between every piece of data when "toString()" is called.
	 */
	public StringHistory(String delimiter) {
		this.data = new Vector<String>();
		this.delimiter = delimiter;
	}
	
	
	/** 
	 * Set the delimter. 
	 * 
	 * @param delimiter - The String is placed between individual datapoints when "toString()" is called.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	
	/** 
	 * Add a new datapoint. 
	 * 
	 * @param datum - Appends datum to this objects dataset
	 */
	public void addDataPoint(String datum) {
		data.add(datum);
	}
	
	
	/** 
	 * Return the collected data. 
	 * 
	 * @return An array that contains the argument from every call to "addDataPoint(datum)"
	 */
	public String[] getData() {
		return data.toArray(new String[0]);
	}
	
	
	/** 
	 * Return the recorded data. 
	 * 
	 * @return The following String - "datum1 + delimiter + datum2 + delimiter + datum3..."
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
	
		for (String datum : data) {
			buffer.append(datum + delimiter);
		}
		
		return buffer.toString();
	}
}

