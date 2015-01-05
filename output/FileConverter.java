package output;

import util.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author jparker
 */
public class FileConverter {
	
	static HashMap<String, StateRecord> records;
	
	public static void main(String[] args) {
	
		String[] filesToConvert = {
			"contagious.txt",
			"nonContagious.txt",
			"symptomatic.txt",
			"nonSymptomatic.txt",
			"recovered.txt",
			"susceptibles.txt"
		};
		
		for (String fileName : filesToConvert) {
			System.out.println("Converting " + fileName);
			
			//initalize a new HashMap
			records = new HashMap<String,StateRecord>();

			try {
				BufferedReader br = new BufferedReader(new FileReader(fileName));

				StringTokenizer st;
				String line;


				line = br.readLine();  //remove the 1st line
				line = br.readLine();  //remove the 2nd line
				line = br.readLine();  //remove the 3rd line
				line = br.readLine();  //remove the 4th line (the column labels)

				line = br.readLine();  //read in the first data line

				//a line consists of the following tokens
				//DayNum Zip0 Zip1 Zip2....
				int dayNum = 0;
				while (line != null || line.equals("\n")) {
//					System.out.println("Processing...day " + dayNum);
					st = new StringTokenizer(line);

					st.nextToken();	//remove the dayNum
					int currentZip = 0;

					while (st.hasMoreTokens()) {
						addAt(currentZip, dayNum, Integer.parseInt(st.nextToken()));
						currentZip++;
					}

					line = br.readLine();
					dayNum++;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//output map...			
			StateRecord[] printMe = records.values().toArray(new StateRecord[0]);
			//flatten each record
			for (int i = 0; i < printMe.length; i++) {
				printMe[i].flatten();
			}
			
			//create a String Buffer
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < printMe.length; i++) {
				sb.append(printMe[i].toString() + "\n");
			}

			try {
				Utility.writeToNewFile(
					fileName.substring(0, fileName.indexOf(".")) + " By State.txt", 
					sb.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}	

	static void addAt(int modelPlaceIndex, int day, int value) {

		//find the state associated with this index		
		String state = IndexToStateMap.stateFinder.get(modelPlaceIndex);

		//retrieve the correct StateRecord
		StateRecord sr = records.get(state);
		
		//the that StateRecord doesn't exist -- add it
		if(sr == null) {
			sr = new StateRecord(state);
			records.put(state, sr);
		}
		
		//add the datapoint
		sr.addPoint(day, value);
	}



	static class StateRecord {

		String state;

		Vector<Integer> record;

		Integer[] recordArray;


		StateRecord(String state) {
			this.state = state;
			this.record = new Vector<Integer>(1000);
		}
		
		void addPoint(int day, int datum) {
			if(record.size() <= day) {
				record.add(day, 0);
			}
			record.set(day, record.get(day) + datum);
		}
		
		void flatten() {
			this.recordArray = record.toArray(new Integer[0]);
		}
		
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(state + "\t");
			for (int i = 0; i < recordArray.length; i++) {
				sb.append(recordArray[i] + "\t");
			}
			return sb.toString();
		}
		
		/** So this can be hashed. */
		public int hashCode() {
			return state.hashCode();
		}

		/** So this can be hashed. */
		public boolean equals(Object o) {
			StateRecord other = (StateRecord) o;
			return state.equals(other.state);
		}
	}
}
