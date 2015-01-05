package model;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Vector;


/**
 *  A ModelPlaceRecord stores statistics of a ModelPlace.  These statistics can then be compiled
 *	at the end of a run to obtain an overall picture of the model's progress.
 *
 *  Currently 4 values are stored in each datapoint.  Those are ::
 *	-the number of susceptibles in a zipcode
 *	-the number of exposed in a zipcode
 *	-the number of infected in a zipcode
 *	-the number of recovered in a zipcode
 *
 *  When this ModelPlaceRecord is created it starts storing values in multiple Vectors.  When
 *	this ModelPlaceRecord is serialized (to be sent to the GlobalModel over RMI) it's Vectors
 *	are flattened into int[].  Then when you deseralize a ModelPlaceRecord you get an int[]
 *	and all traces of the Vectors are lost.  You cannot add data to a ModelPlaceRecord once it
 *	has been serialized.  You cannot retrieve data from a ModelPlaceRecord until it has been
 *	serialized.
 */
public class ModelPlaceRecord implements Externalizable {
	
	transient Vector<Integer> susVec;
//	transient	Vector<Integer>	expVec;
//	transient	Vector<Integer>	infVec;
	transient Vector<Integer> contagiousVec;
	transient Vector<Integer> nonContagiousVec;
	transient Vector<Integer> symptomaticVec;
	transient Vector<Integer> nonSymptomaticVec;
	transient Vector<Integer> recVec;
	
	int		placeIndex;
	int[]		numSus;
//	int[]		numExp;
//	int[]		numInf;
	int[] numContagious;
	int[] numNonContagious;
	int[] numSymptomatic;
	int[] numNonSymptomatic;
	int[]		numRec;
	
	int threadNum;


	/**  Create an object to store information about a ModelPlace. */
	public ModelPlaceRecord(int placeIndex, int threadNum) {
		this.placeIndex = placeIndex;
		this.threadNum = threadNum;

		susVec = new Vector<Integer>();
//		expVec = new Vector<Integer>();
//		infVec = new Vector<Integer>();
		contagiousVec = new Vector<Integer>();
		nonContagiousVec = new Vector<Integer>();
		symptomaticVec = new Vector<Integer>();
		nonSymptomaticVec = new Vector<Integer>();
		recVec = new Vector<Integer>();

	}


	/**  Do nothing constructor to support Externalizable. */
	public ModelPlaceRecord() {
	}


	/**  Return the placeIndex index of this record. */
	public int placeIndex() {
		return placeIndex;
	}


	/**  Return the index of the Thread that owned this ModelPlace. */
	public int getThreadNum() {
		return threadNum;
	}


	/**
	 * Record some information about a zipcode at the present time
	 *
	 * @param sus - The number of susceptible agents
	//	 * @param exp - The number of exposed agents
	//	 * @param inf - The number of infected agents
	 * @param contagious - The number of contagious agents
	 * @param nonContagious - The number of nonContagious agents
	 * @param symptomatic - The number of symptomatic agents
	 * @param nonSymptomatic - The number of nonSymptomatic agents
	 * @param rec - The number of recovered agents
	 */
	public void addDataPoint(int sus, int contagious, int nonContagious, int symptomatic, int nonSymptomatic, int rec) {

//		if ((sus < 0) || (exp < 0) || (inf < 0) || (rec < 0)) {		
		if ((sus < 0) || (contagious < 0) || (nonContagious < 0) || (symptomatic < 0) || (nonSymptomatic < 0) || (rec < 0)) {

			throw new IllegalArgumentException("An input value was negative");
		}

		susVec.add(sus);
//		expVec.add(exp);
//		infVec.add(inf);
		contagiousVec.add(contagious);
		nonContagiousVec.add(nonContagious);
		symptomaticVec.add(symptomatic);
		nonSymptomaticVec.add(nonSymptomatic);
		recVec.add(rec);
	}

	
	/**  Return the history of susceptibles in this record. */
	public int[] getSusHistory() {
		if(numSus == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numSus;
	}
	
	
//	/**  Return the history of exposed in this record. */
//	public int[] getExpHistory() {
//		if(numExp == null) {
//			throw new IllegalStateException("Cannot call this before deserialization");
//		}
//		return numExp;
//	}
//	
//	
//	/**  Return the history of infectives in this record. */
//	public int[] getInfHistory() {
//		if(numInf == null) {
//			throw new IllegalStateException("Cannot call this before deserialization");
//		}
//		return numInf;
//	}
	
	
	/** Return the history of contagious in this record. */
	public int[] getContagiousHistory() {
		if(this.numContagious == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numContagious;
	}
	
	
	/** Return the history of nonContagious in this record. */
	public int[] getNonContagiousHistory() {
		if(numNonContagious == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numNonContagious;
	}
	
	
	/** Return the history of symptomatics in this record. */
	public int[] getSymptomaticHistory() {
		if(numSymptomatic == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numSymptomatic;
	}
	
	
	/** Return the history of nonSymptomatics in this record. */
	public int[] getNonSymptomaticHistory() {
		if(numNonSymptomatic == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numNonSymptomatic;
	}
	
	
	/**  Return the history of recovereds in this record. */
	public int[] getRecHistory() {
		if(numRec == null) {
			throw new IllegalStateException("Cannot call this before deserialization");
		}
		return numRec;
	}
	
	
	///////////////
	//  private  //
	///////////////
	
	/**  prepare this object to be serialized. */
	private void prepForSerialization() {
		numSus = new int[susVec.size()];
//		numExp = new int[expVec.size()];
//		numInf = new int[infVec.size()];
		numContagious = new int[contagiousVec.size()];
		numNonContagious = new int[nonContagiousVec.size()];
		numSymptomatic = new int[symptomaticVec.size()];
		numNonSymptomatic = new int[nonSymptomaticVec.size()];
		numRec = new int[recVec.size()];
		
		for(int i = 0 ; i < numSus.length ; i++) {
			numSus[i] = susVec.get(i);
//			numExp[i] = expVec.get(i);
//			numInf[i] = infVec.get(i);
			numContagious[i] = contagiousVec.get(i);
			numNonContagious[i] = nonContagiousVec.get(i);
			numSymptomatic[i] = symptomaticVec.get(i);
			numNonSymptomatic[i] = nonSymptomaticVec.get(i);
			numRec[i] = recVec.get(i);
		}

		susVec = null;
//		expVec = null;
//		infVec = null;
		contagiousVec = null;
		nonContagiousVec = null;
		symptomaticVec = null;
		nonSymptomaticVec = null;
		recVec = null;
	}
	
	
	//////////////////////
	//  Externalizable  //
	//////////////////////
	
	/**  Manually written serialization method. */
	public void writeExternal(ObjectOutput out) throws IOException {
		
		this.prepForSerialization();
		out.writeInt(placeIndex);
		out.writeObject(numSus);
//		out.writeObject(numExp);
//		out.writeObject(numInf);
		out.writeObject(numContagious);
		out.writeObject(numNonContagious);
		out.writeObject(numSymptomatic);
		out.writeObject(numNonSymptomatic);
		out.writeObject(numRec);
		out.writeInt(threadNum);
	}
	
	
	/**  Manually written deserialization method. */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		
		placeIndex = in.readInt();
		numSus = (int[]) in.readObject();
//		numExp = (int[]) in.readObject();
//		numInf = (int[]) in.readObject();
		numContagious = (int[]) in.readObject();
		numNonContagious = (int[]) in.readObject();
		numSymptomatic = (int[]) in.readObject();
		numNonSymptomatic = (int[]) in.readObject();
		numRec = (int[]) in.readObject();
		threadNum = in.readInt();
	}
}