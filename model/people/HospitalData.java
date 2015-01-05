package model.people;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * This is a simple class that loads the hospital data.  This class then 
 * provides a convient way to access that data.
 */
public class HospitalData {
	
	/** 
	 * A databse of Hospital Bed sizes indexed by hospital zipcode.  Each zipcode with 
	 * at least one hospital has an entry in this database.  The int[] retrieved 
	 * contains the number of beds each hospital in that zipcode has.
	 */
	public static TreeMap<Integer , int[]> hospitalDatabase;
		
	/** The name of the file that contains the hospital Data. */
	private static String fileName = "hospitalData.txt";
	
	static {
		try {
			loadData();	
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	/** Load / Initalize all hospital data. */
	private static void loadData() throws Exception {
				
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		StringTokenizer st;
		String line = br.readLine(); //throw away 1st line
		line = br.readLine();
		
		LinkedList<HospitalRecord> hospitals = new LinkedList<>();
			
		int zip;
		int numBeds;
		while(line != null) {
			
			st = new StringTokenizer(line);
			
			zip = Integer.parseInt(st.nextToken());
			numBeds = Integer.parseInt(st.nextToken());
			
			hospitals.add(new HospitalRecord(zip , numBeds));

			line = br.readLine();
		}
		
		//sort the hospitals
		HospitalRecord[] array = hospitals.toArray(new HospitalRecord[0]);
		Arrays.sort(array);
				
		//create database
		hospitalDatabase = new TreeMap<>();
				
		int startingIndex = 0;
		int currentZip = array[startingIndex].zip;
		int numHospitals = 0;
		for (int i = 0 ; i < array.length ; i++) {
			//if still accessing hospitals from the same zipcode
			if(array[i].zip == currentZip) {
				//keep count of hospitals in the zipcode
				numHospitals++;				
			} else {
				//build array of hospital beds
				int[] tempArray = new int[numHospitals];
				for (int j = 0 ; j < numHospitals ; j++) {
					tempArray[j] = array[startingIndex + j].numBeds;
				}
				hospitalDatabase.put(currentZip , tempArray);
				
				//prepare for next run
				startingIndex = i;
				currentZip = array[startingIndex].zip;
				numHospitals = 1;
			}
			
			//enter the final array
			if(i == array.length - 1) {				
				int[] tempArray = new int[numHospitals];
				for (int j = 0 ; j < numHospitals ; j++) {
					tempArray[j] = array[startingIndex + j].numBeds;
				}
				hospitalDatabase.put(currentZip , tempArray);				
			}
		}

	}
	
	
	/** Retrieve a list of hospitals sizes (in beds) that are in a zipcode. */
	public static int[] listHospitals(int realZipcode) {
		if(hospitalDatabase.containsKey(realZipcode)) {
			return hospitalDatabase.get(realZipcode);
		} else {
			return null;
		}
	}
	
	
	/** A simple wrapper for where, and how big a hospital is. */
	static class HospitalRecord implements Comparable {
		int zip;
		int numBeds;
		
		HospitalRecord(int zip , int numBeds) {
			this.zip = zip;
			this.numBeds = numBeds;
		}
		
		/** Via zip first then numBeds. */
		@Override
		public int compareTo(Object o) {
			HospitalRecord other = (HospitalRecord) o;
			
			if(this.zip < other.zip) {
				return -1;				
			} else if(this.zip > other.zip) {
				return 1;
			} else if(this.numBeds < other.numBeds) {
				return -1;
			} else if(this.numBeds > other.numBeds) {
				return 1;
			} else {
				return 0;
			}
		}		
	}
	
	
	/** Test Read in - print out the hospital data. */
	public static void main(String[] args) {
		
		int totalNumBeds = 0;
		int numZipsWithHospitals = 0;
		Set<Integer> keys = hospitalDatabase.keySet();
		for (Integer elem : keys) {
			numZipsWithHospitals++;
			int[] array = hospitalDatabase.get(elem);
			System.out.println("Zipcode :: " + elem + " has the following numBeds");
			for (int i = 0 ; i < array.length ; i++) {
				System.out.println("\t" + array[i]);
				totalNumBeds += array[i];
			}
		}
		
		System.out.println("total number of beds is :: " + totalNumBeds);
		System.out.println("Num zips with Hospitals is :: " + numZipsWithHospitals);
	}
}
