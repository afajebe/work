package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;


/** A collection of convience functions. */
public class Utility {
	
	/** 
	 * @param booleanArray - An array of booleans
	 * @return True - if every entry in this array is true
	 */
	public static boolean allTrue(boolean[] booleanArray) {
		
		for (int i = 0; i < booleanArray.length; i++) {
			if(!booleanArray[i]) {
				return false;
			}
		}
		return true;
	}
	
	/** Return the number of seconds (to the tenths). */
	public static double toSec(long time) {
		return 0.1 * (double)(time / 100000000L);
	}
	

	/** Return the minimum value in this array. */
	public static long min(long[] array) {
		long min = Long.MAX_VALUE;
		for (int i = 0 ; i < array.length ; i++) {
			min = Math.min(min , array[i]);
		}
		return min;
	}
		
	
	/** Return the maximum value in this array. */
	public static long max(long[] array) {
		long max = Long.MIN_VALUE;
		for (int i = 0 ; i < array.length ; i++) {
			max = Math.max(max , array[i]);
		}
		return max;		
	}
	
	
	/** Return the sum of this array. */
	public static int sum(int[] array) {
		int sum = 0;
		for (int i = 0 ; i < array.length ; i++) {
			sum += array[i];
		}
		return sum;
	}
	
	
	/**
	 * Convert a text file into a String.  This methods reads each line of the file,
	 * and appends it to a StringBuffer (after reinserting the '\n' char that is
	 * removed by the readLine() method).  After all lines are read the buffer is
	 * returned
	 */
	public static String readTextFile(File f) throws Exception {
		BufferedReader buffer = new BufferedReader(new FileReader(f));
		StringBuffer stringBuffer = new StringBuffer();
		String line = buffer.readLine();
		
		while(line != null) {
			stringBuffer.append(line + "\n");
			line = buffer.readLine();
		}
		return stringBuffer.toString();
	}
	
	
	/** Write a String to a new file. */
	public static void writeToNewFile(String fileName , String writeMe) throws Exception {
		
		FileOutputStream fos = new FileOutputStream(fileName);
		PrintWriter dout = new PrintWriter(fos);
		
		dout.write(writeMe);
		
		dout.close();
		fos.close();
	}

	
	/** Serialize an object to the file "fileName". */
	public static void serialize(Serializable ser , String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			
			out.writeObject(ser);
			
			out.close();
			fos.close();			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Serilization Failure\n");
		}
	}
	
	
	/** 
	 * Create an Object from a file. 
	 *
	 * @param f - A handle to a serialized object.
	 */
	public static Object deserialize(File f) throws IOException {
		Object obj = null;
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			System.exit(0);
		} 
		
		ois.close();
		fis.close();
		
		return obj;
	}
	
	
}
