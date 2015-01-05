package util;


import java.util.Random;
import java.util.Vector;

/**
 * Provides a single access point to enable to distribution of a population across computing resources.
 */
public class Distribute {
	
	/** Disallow instantiation of this class */
	private Distribute() {
		throw new RuntimeException("Creating a \"Distribute\" object is not allowed");
	}
	
	
	/**
	 * Given a population, split it into groups.  This method fills in the entries of the
	 * zipOwners array in a manner that quasi-minimizes inter-group interaction.
	 *
	 * @param numGroups - The number of groups you want to divide this population into.  This
	 * argument has a core constraint place on it.  numGroups must be equal to (2^n)*(3*m) 
	 * where both n and m are both integers >= 0.  (Obviously n = m = 0 is not allowed).  This constraint
	 * allows the optimization scheme to obtain resulst quickly at each level of recursion.  
	 * Technically this restraint is not required, but imposing it allows the algorithm to run many times
	 * faster.
	 *    
	 * @param zipOwners - An array where zipOwners[i] = zipcode i's group number assignment
	 * @param zipPops - An array where zipPops[i] = the population of zipcode i.  These values
	 * are used to loosely balance the total populations of each group
	 * 
	 * @param matrix - A matrix where matrix[i][j] = the amount of interaction between 
	 * zipcode i and zipcode j 
	 */
	public static void optimizeDist(int numGroups , int[] zipOwners , int[] zipPops , double[][] matrix) {
		
		//check length
		if( !((zipOwners.length == zipPops.length) && (zipPops.length == matrix.length)) ) {
			throw new IllegalArgumentException("all input arrays must have the same length" +
				"\n zipOwners.length :: " + zipOwners.length +
				"\n zipPops.length :: " + zipPops.length +
				"\n matrix.length :: " + matrix.length
				);
		}
		int matrixSize = matrix.length;
		for (int i = 0 ; i < matrixSize ; i++) {
			if(matrix[i].length != matrixSize) {
				throw new IllegalArgumentException("input matrix is not square");
			}
			
		}
		//check validity of inital ownerships
		for(int i = 0 ; i < zipOwners.length ; i++) {
			if(zipOwners[i] >= numGroups) {
				throw new IllegalArgumentException("invalid zip owner argument at index " +
					i + " :: " + zipOwners[i]
					);
			}
		}
		
		//confirm the numGroups is a multiple of just 2s and 3s
		int tempNumGroups = numGroups;
		while(tempNumGroups % 2 == 0) {
			tempNumGroups = tempNumGroups / 2;
		}
		while(tempNumGroups % 3 == 0) {
			tempNumGroups = tempNumGroups / 3;
		}
		if(tempNumGroups != 1) {
			throw new IllegalArgumentException(
				"numGroups must be writeable as \"(2^n)*(3*m)\" this is not the case"
				);
		}		
		
		if(numGroups > 64) {
			throw new IllegalArgumentException("numGroups is too big :: " + numGroups +
				"cannot split population that many ways."
				);
		}
		
		System.out.println(
			"Optimizing the arrangement of " + zipOwners.length +
			" zipcodes into " + numGroups + " groups"
			);
		
		//do nothing if there is only 1 possible arrangement..and you are in it
		if(numGroups == 1) { return; }
		
		//attempt to optimize in stages
		if(numGroups >= 4) {
			
			//factor
			int factor = 2;
			boolean done = false;
			while(!done) {
				if(numGroups % factor == 0) {
					done = true;
				} else {
					factor++;
				}
			}
			
			if(factor != numGroups) {
				
				System.out.println("Recursing :: spliting working population into " + factor + " subgroups ");				
				
				//since zips were originally distributed amongst "numGroups" group adjust
				//zipOwners as if dist occured across "factor" groups
				for (int i = 0 ; i < zipOwners.length ; i++) {
					zipOwners[i] = zipOwners[i] % factor;
				}
								
				optimizeDist(factor , zipOwners , zipPops , matrix);
				
				int[][] zipsOwned = new int[factor][];
				int[][] subOwners = new int[factor][];
				int[][] subPops = new int[factor][];
				double[][][] subMatricies = new double[factor][][];
				
				
				extractSubGroupsForOptimization(
					factor ,		//how many groups exist
					numGroups / factor,	//how far is each group split
					zipOwners ,
					zipPops ,
					matrix ,
					subOwners ,
					subPops ,
					subMatricies ,
					zipsOwned
					);
				
				System.out.println("\nOptimizing subgroups now");
				for (int i = 0 ; i < factor ; i++) {
					System.out.println("Optimizing subgroup :: " + i);
					
					if(subOwners[i] == null) System.out.println("subOwners[ " + i + "] = null");
					if(subPops[i] == null) System.out.println("subPops[ " + i + "] = null");
					if(subMatricies[i] == null) System.out.println("subMatricies[ " + i + "] = null");
					
					optimizeDist(numGroups / factor , subOwners[i] , subPops[i] , subMatricies[i]);
				}
				
				//we now need to update the "zipOwners" array - originally it had number 0 to (factor - 1)
				//we need it to have numbers from 0 to (numGroups - 1)
				
				//correct the zipOwners array
				int zipNum;
				int threadNum;
				for (int i = 0 ; i < factor ; i++) {
					for (int j = 0 ; j < zipsOwned[i].length ; j++) {
						zipNum = zipsOwned[i][j];
						threadNum = zipOwners[zipNum] * (numGroups / factor) + subOwners[i][j];
						zipOwners[zipNum] = threadNum;
					}
				}
				
				return;
			}
		}
		
		directOptimization(numGroups , zipOwners , zipPops , matrix);	
	}
		
	
	/** 
	 * The "engine" if the optimization scheme.  This method is the final step in all recursions.
	 * This method assumes the numGroups will be small enough for the algorithm to perform well.
	 * 
	 * @param numGroups - The number of groups you want to divide this population into
	 * @param zipOwners - An array where zipOwners[i] = zipcode i's group number assignment
	 * @param zipPops - An array where zipPops[i] = the population of zipcode i.  These values
	 * are used to loosely balance the total populations of each group
	 * 
	 * @param matrix - A matrix where matrix[i][j] = the amount of interaction between 
	 * zipcode i and zipcode j 
	 */			 
	public static void directOptimization(int numGroups , int[] zipOwners , int[] zipPops , double[][] matrix) {
		
		//calculate and save group populations to speed calculations
		long[] groupPops = new long[numGroups];
		for (int i = 0 ; i < zipPops.length ; i++) {
			groupPops[zipOwners[i]] += zipPops[i];
		}
		
		//error check those calculations
		for(int i = 0 ; i < numGroups ; i++) {
			if(groupPops[i] <= 0) {
				throw new IllegalStateException("Groups must have positive populations :: " +
					i + " has pop " + groupPops[i]
					);
			}
		}		
		
//		//debug info
//		System.out.println("zipOwners.length :: " + zipOwners.length);
//		System.out.println("zipPops.length :: " + zipPops.length);
//		for(int i = 0 ; i < zipPops.length ; i++) {
//			System.out.println("zipOwners[" + i + "] :: " + zipOwners[i]);
//		}
//		for(int i = 0 ; i < numGroups ; i++) {
//			System.out.println(i + " :: " + groupPops[i]);
//		 
//			if(groupPops[i] == 0) {
//				throw new IllegalStateException("Groups cannot have zero populations");
//			}
//		}		
		
		long totalPop = 0;
		long fairShareOfPop;		
		
		//compute "fairShareOfPop" and totalPop
		for(int i = 0 ; i < zipPops.length ; i++) {
			totalPop += zipPops[i];
		}
		fairShareOfPop = totalPop / numGroups;
		
		/*
		 * Save the amount each zipcode i interacts with each group j.  This array is editted 
		 * as zipcodes are moved from group to group.  Its purpose is to allow us to quickly
		 * determine which group a given zipcode interacts most with
		 *
		 * an extra column has been added, this column contains the sum of all group interactions.  
		 */		
		double[][] zipGroupInt = new double[zipOwners.length][numGroups + 1];
		for (int zip = 0 ; zip < zipOwners.length ; zip++) {
			for (int zip2 = 0 ; zip2 < zipOwners.length ; zip2++) {
				if(zip != zip2) {
					zipGroupInt[zip][zipOwners[zip2]] += matrix[zip][zip2];
				}
			}
		}
		//compute total interactions
		for (int zip = 0 ; zip < zipOwners.length ; zip++) {
			double totalInteraction = 0.0;
			for (int group = 0 ; group < numGroups ; group++) {
				totalInteraction += zipGroupInt[zip][group];
			}
			zipGroupInt[zip][numGroups] = totalInteraction;
		}
		
		
		Random rand = new Random(17L);	//use a fixed seed so all dsitributions are reproducible		
		int currentGroup = 0;
		int lastGroupNum = 1;
		int counter = 0;
		long diff;
		double progress;
		int numIterations = numGroups * 25;
		while(counter < numIterations) {
			
			//starts at 1.0 goes to 0.0
			progress = (double)(numIterations - counter) / (double) numIterations;
			//amount above "fairShare"
			diff = (long)(fairShareOfPop * (numGroups - 1) * progress);
			
			
			//periodically provide an update
			if(counter % 5 == 0) {
				System.out.println(roundThree(progress) + " \t " + diff);
			}
			
			
			///////////////////////////
			//  Pick a Random Group  //
			///////////////////////////
			
			//pick a random group (no doubles)
			currentGroup = rand.nextInt(numGroups);
			while(currentGroup == lastGroupNum) {
				currentGroup = rand.nextInt(numGroups);
			}
			
			//////////////////////////////////////////////
			//  Grow a small group past a "fair share"  //
			//////////////////////////////////////////////
			growGroup(
				currentGroup ,
				groupPops ,
				fairShareOfPop + diff ,		//growth limit
				zipOwners ,
				zipGroupInt ,
				zipPops ,
				matrix ,
				rand.nextBoolean()//rand.nextBoolean() & rand.nextBoolean() & rand.nextBoolean()
				);
			
			///////////////////////////////////////
			//  Trim the group to a "fair share" //
			///////////////////////////////////////
			trimGroup(
				currentGroup ,
				groupPops ,
				fairShareOfPop ,		//trim limit
				zipOwners ,
				zipGroupInt ,
				zipPops ,
				matrix
				);
			
			//confirm that no group has a zero population
			for(int i = 0 ; i < numGroups ; i++) {
				if(groupPops[i] == 0) {
					throw new IllegalStateException("Groups cannot have zero populations");
				}
			}
			
			lastGroupNum = currentGroup;
			counter++;
		}
		
		for(int i = 0 ; i < numGroups ; i++) {
			System.out.println("group " + i + " has population " + groupPops[i]);
		}
	}
	
	
	
//	/**
//	 * Roughly optimize this population.  Do so by combining zipcodes into groups of 25 zipcodes.
//	 */
//	public static void fastOptimize(int numNodes, int[] zipNodeOwners, int[] zipcodePopulation, double[][] interactionMatrix) {
//		throw new UnsupportedOperationException("Not yet implemented");
//	}
	
	/**
	 * This method is used to help recursive optimization calls.  For instance, say you want to
	 * optimize the distribution of zipcode amongst 6 groups.  You could call "optimizeDist(6 ,
	 * zipOwners , zipPops , matrix)" or you could first optimize into 2 group, then optimize each
	 * of those 2 group into 3 subgroup, for a total of 6 groups.  This method help decompose the
	 * results from the 2 group optimization into data that can be used for the 3 subgroup optimization.
	 * Once this method has been called you can call optimizeDist(numSubGroups , subOwners[i] , subPops[i] ,
	 * subMatricies[i]) for each i where i is a an integer from 0 to (numGroups - i)
	 *
	 * @param numGroup - 2 in the example quoted
	 * @param numSubGroups - 3 in the example quoted
	 *
	 * @param zipOwners - An array where zipOwners[i] = the group number of zipcode i
	 * (0 or 1 in the example) (THIS IS NOT ALTERED)
	 *
	 * @param zipPops - An array where zipPops[i] = the population of zipcode i (THIS IS NOT ALTERED)
	 *
	 * @param matrix - An array where matrix[i][j] = the interaction between zipcode i and zipcode j
	 * (THIS IS NOT ALTERED)
	 *
	 * @param subOwners - An array where subOwners[i][j] = the subgroup number of the j_th zipcode on
	 * the i_th group (THIS IS CHANGED)
	 *
	 * @param subPops - An array where subPops[i][j] = the population of the j_th zipcode on the i_th
	 * group(THIS IS CHANGED)
	 *
	 * @param subMatricies - An array where subMatrixcies[i][j][k] = the interaction between zipcode j
	 * and zipcode k where both zip j and zip k are members of group i
	 *
	 * @param zipsOwned - An array where zipsOwned[i][j] = the zipcode id number of the j_th zipcode
	 * that group i owns (THIS IS CHANGED)
	 */
	private static void extractSubGroupsForOptimization(
		int numGroups , int numSubGroups , int[] zipOwners , int[] zipPops , double[][] matrix ,
		int[][] subOwners , int[][] subPops , double[][][] subMatricies , int[][] zipsOwned
		) {
		
		int numZips = zipOwners.length;
		
		//allocate the zips on a node to a thread
		//create the sub matricies
//		subMatricies = new double[numGroups][][];
		int[] zipCount = new int[numGroups];				//how many zips each node was given
//		subPops = new int[numGroups][];					//each group needs a list of its zips populations
//		subOwners = new int[numGroups][];				//each group needs a list of which thread owns its zips
//		zipsOwned = new int[numGroups][];
		for (int group = 0 ; group < numGroups ; group++) {
			
			//for each group - create a list of the zips it owns
			Vector<Integer> vec = new Vector<Integer>();
			for (int zip = 0 ; zip < numZips ; zip++) {
				if(zipOwners[zip] == group) {
					vec.add(zip);
				}
			}
			
			zipCount[group] = vec.size();				//save the number of zips this group has
			
			zipsOwned[group] = new int[vec.size()];			//create a list of the zips this group has
			for (int i = 0 ; i < zipsOwned[group].length ; i++) {
				zipsOwned[group][i] = vec.get(i);
			}
			
			subPops[group] = new int[vec.size()];			//create a list of the populations of those zips
			for (int i = 0 ; i < zipsOwned[group].length ; i++) {
				subPops[group][i] = zipPops[zipsOwned[group][i]];
			}
			
			subOwners[group] = new int[vec.size()];			//create a list of which thread owns each zip
			
			
			//allocate the zipss in a speckled fasion
			for (int i = 0 ; i < zipsOwned[group].length ; i++) {
				subOwners[group][i] = i % numSubGroups;		//allocate a zip to a thread
			}
			
			//create the smaller matrices
			double[][] smallMatrix = new double[zipsOwned[group].length][zipsOwned[group].length];
			for(int i = 0 ; i < zipsOwned[group].length ; i++) {
				for(int j = 0 ; j < zipsOwned[group].length ; j++) {
					smallMatrix[i][j] = matrix[zipsOwned[group][i]][zipsOwned[group][j]];
				}
			}
			subMatricies[group] = smallMatrix;
		}
	}
	
	
	
	/**
	 * Grow a group until its population reaches "growTo".
	 *
	 * @param currentGroup - The group to grow
	 * @param groupPops - An array where groupPops[i] = The population of each group i
	 * @param growTo - When the currentGroup reaches the population you stop absorbing zips
	 * @param zipOwners - An array where zipOwners[i] = the number of the group that owns zip i
	 * @param zipGroupInt - An array where zipGroupInt[i][j] = the interaction between zipcode i and group j
	 * @param zipPops - An array where zipPops[i] = the population of zipcode i
	 * @param matrix - An array where matrix[i][j] = the interaction between zipcode i and zipcode j
	 */
	private static void growGroup(int currentGroup , long[] groupPops , long growTo , int[] zipOwners , double[][] zipGroupInt , int[] zipPops , double[][] matrix , boolean pureInt) {
		
//		int took = 0;
//		int tookPop = 0;
		long prePop = groupPops[currentGroup];
		int bestZip;
		int oldOwner;
		while( groupPops[currentGroup] <= growTo) {
			
			bestZip = getBestZip(currentGroup , zipOwners , zipGroupInt , zipPops , pureInt);
			oldOwner = zipOwners[bestZip];
			
			//veto further changing if adding the next zipcode makes it worse
			if(groupPops[currentGroup] + zipPops[bestZip] > growTo + zipPops[bestZip] / 2) {
//				System.out.println("***SIZE VETO***");
				break;
			}
			//veto further changing if you'd zero out a group
			if(groupPops[oldOwner] - zipPops[bestZip] == 0) {
//				System.out.println("***ZERO VETO***");
				break;
			}
			
			moveZip(bestZip , currentGroup , zipOwners , zipPops , zipGroupInt , groupPops , matrix);
//			took++;
//			tookPop += zipPops[bestZip];
		}
		
//		System.out.print("\ntook :: " + took + "\t" + tookPop);
	}
	
	
	/**
	 * Trim a group until its population reaches "fairShare".
	 *
	 * @param currentGroup - The group to grow
	 * @param groupPops - An array where groupPops[i] = The population of each group i
	 * @param fairShare - When the currentGroup reaches this population you stop triming zips
	 * @param zipOwners - An array where zipOwners[i] = the number of the group that owns zip i
	 * @param zipGroupInt - An array where zipGroupInt[i][j] = the interaction between zipcode i and group j
	 * @param zipPops - An array where zipPops[i] = the population of zipcode i
	 * @param matrix - An array where matrix[i][j] = the interaction between zipcode i and zipcode j
	 */
	private static void trimGroup(int currentGroup , long[] groupPops , long fairShare , int[] zipOwners , double[][] zipGroupInt , int[] zipPops , double[][] matrix) {
		
		int bestNewOwner;
		int worstZip;
//		int gave = 0;
//		int gavePop = 0;
		while( groupPops[currentGroup] >= fairShare) {
			worstZip = getWorstZip(currentGroup , zipOwners , zipGroupInt);
			
			if( groupPops[currentGroup] - fairShare > zipPops[worstZip] / 2) {
				//implement change
				bestNewOwner = getBestGroup(worstZip , currentGroup , zipGroupInt);
				
				moveZip(worstZip , bestNewOwner , zipOwners , zipPops , zipGroupInt , groupPops , matrix);
//				gave++;
//				gavePop += zipPops[worstZip];
				
			} else {
				//stop because this losing this zip will introduce MORE error
				break;
			}
		}
		
//		System.out.print("\ngave:: " + gave + "\t" + gavePop);
	}
	
	
	/** Move a zipcode from one group to another, update helper variables. */
	private static void moveZip(int zip , int newOwner , int[] zipOwners , int[] zipPops , double[][] zipGroupInt , long[] groupPops , double[][] matrix) {
		
		int oldOwner = zipOwners[zip];
		
		//update interaction
		for (int i = 0 ; i < zipOwners.length ; i++) {
			
			if(i != zip) {
				zipGroupInt[i][oldOwner] -= matrix[i][zip];
				zipGroupInt[i][newOwner] += matrix[i][zip];
			}
		}
		//update pops
		groupPops[newOwner] += zipPops[zip];
		groupPops[oldOwner] -= zipPops[zip];
		zipOwners[zip] = newOwner;
	}
	
	
	/** Find which out-of-group zipcode interacts the most with a given group. */
	private static int getBestZip(int groupNum , int[] zipOwners , double[][] zipGroupInt , int[] zipPops , boolean useInteraction) {
		
		double tempInteraction;
		double mostInteraction = 0.0;
		int bestZip = -1;
		
		
		for(int i = 0 ; i < zipOwners.length ; i++) {
			
			//if you've found an out of group zip
			if(zipOwners[i] != groupNum) {
				
				//get potential in-group interaction
				if(useInteraction) {
					tempInteraction = zipGroupInt[i][groupNum];
				} else {
					double totalInteraction = 0.0;
					for (int j = 0 ; j < zipGroupInt[i].length - 1 ; j++) {
						totalInteraction += zipGroupInt[i][j];
					}
					
					tempInteraction = zipGroupInt[i][groupNum] / totalInteraction;
					//tempInteraction = zipGroupInt[i][groupNum] / ((double)zipPops[i]);
				}
				
				//save best
				if(tempInteraction >= mostInteraction) {
					mostInteraction = tempInteraction;
					bestZip = i;
				}
			}
		}
		
		return bestZip;
	}
	
	
	/** Find which in-group zipcode interacts least with its own group. */
	private static int getWorstZip(int groupNum , int[] zipOwners , double[][] zipGroupInt) {
		
		double tempInteraction;
		double leastInteraction = Double.MAX_VALUE;
		int worstZip = -1;
		
		//for each zip
		for(int i = 0 ; i < zipOwners.length ; i++) {
			
			//if you've found an in-group zip
			if(zipOwners[i] == groupNum) {
				
				//find the fraction of interaction that is in-group interaction
				tempInteraction = zipGroupInt[i][groupNum] / zipGroupInt[i][zipGroupInt[i].length - 1];
				
				//save best
				if(tempInteraction <= leastInteraction) {
					leastInteraction = tempInteraction;
					worstZip = i;
				}
			}
		}
		
		return worstZip;
	}
	
	
	/** Find which group (excluding one specific group) interacts the most with a given zipcode. */
	private static int getBestGroup(int zip , int notGroup , double[][] zipGroupInt) {
		
		int numGroups = zipGroupInt[0].length - 1;
		
		if(numGroups == 2) {
			return (1 - notGroup);
		}
		
		double bestInteraction = Double.NEGATIVE_INFINITY;
		int bestGroup = -1;
		double tempInteraction;
		for (int i = 0 ; i < numGroups ; i++) {
			
			if(i == notGroup) {
				//do nothing
			} else {
				tempInteraction = zipGroupInt[zip][i];
				
				if(tempInteraction >= bestInteraction) {
					bestInteraction = tempInteraction;
					bestGroup = i;
				}
			}
		}
		
		return bestGroup;
	}
	
	
	/** Small utility method that rounds a number to 3 places */
	private static double roundThree(double num) {
		double temp = num * 1000.0;
		temp = Math.floor(temp);
		return (temp / 1000.0);
	}	
}
