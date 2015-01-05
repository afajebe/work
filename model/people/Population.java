package model.people;


//package flu.people;
//
//import flu.core.HospitalModelPlaceSeed;
//import flu.core.ModelManager;
//import flu.core.Node;
//import flu.utility.Distribute;
//import flu.utility.Distribution;
//import flu.utility.ImageUtility;
//import gridBasedData.GridExtraction;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
//import java.io.Serializable;
//import java.util.Arrays;
//import java.util.Properties;
//import java.util.Vector;
//import java.util.concurrent.CountDownLatch;
//import javax.imageio.ImageIO;
//
//
//public class Population implements Serializable{
//
//	private final static String		populationPath = "pops\\";
//
//	/////////////////////////
//	//  instance variables //
//	/////////////////////////
//
//	public		PopulationSkeleton		skel;
//
//	/** The original grid based data. */
//	public final GridExtraction extraction;
//
//	/** The total population. */
//	public final long totalPopulation;
//
//	public		int				numZips;
//	public		double[][]			interactionMatrix;	//we keep this to facilitate population splitting
//	public		Distribution[]			pdfs;
//
//	public		int[]				zipcodePopulation;
//
//	/**
//	 * Each entry in this array indicates the node that owns that ModelPlace.
//	 * For Instance, if this array was nothing but zeros then it would indicate
//	 * that this model will only be run with one node (ie node 0).  If this array
//	 * was made up of nothing but 0s, 1s, and 2s, then you would assume 3 nodes
//	 * are running this model.
//	 */
//	public		int[]				zipNodeOwners;
//	public		int[]				zipThreadOwners;
//
//	public final int gridWidth;
//	public final int gridHeight;
//
//
//
//	static {
//		findOrBuildPops();
//	}
//
//
//	/**
//	 * Create a NewPopulation that only stores the information required for a single node.
//	 *
//	 * @param nodeIndex - The index of the node that will use this population.  If this value
//	 * is "-1" then the ModelManger will be using this population, therefore it needs no
//	 * interactionMatrix or pdfs.
//	 */
//	public static Population loadPopulation(int nodeIndex) {
//
//		PopulationSkeleton popSkel = new PopulationSkeleton();
//
//		//deserialize the desire populationFile
//		String fileName = popSkel.getPartialFileName(nodeIndex);
//
//		File f = new File(fileName);
//		if( f.exists()) {
//			System.out.println("deserializing a Population");
//			Population deserializedPop = null;
//
//			try {
//				FileInputStream fis = new FileInputStream(
//					new File(fileName)
//					);
//
//				ObjectInputStream ois = new ObjectInputStream(fis);
//				deserializedPop = (Population) ois.readObject();
//
//				ois.close();
//				fis.close();
//
//				if(deserializedPop == null) {
//					throw new IllegalStateException("deserializedPop == null");
//				}
//
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				System.exit(0);
//			}
//
//			return deserializedPop;
//		} else {
//			System.out.println("Cannot load population file for node :: " + nodeIndex);
//			System.exit(0);
//		}
//
//		throw new IllegalStateException(
//			"Should never get here, should always " +
//			"return a NewPopulation piece " +
//			" OR shutdown JVM"
//			);
//	}
//
//
//	///////////////
//	//  private  //
//	///////////////
//
//	/**  This constructor is private, it forces you to use Population.load() */
//	private Population(PopulationSkeleton popSkel) {
//
//		System.out.println("Constructing a Population");
//
//		//save basic population data
//		this.skel = popSkel;
//
//		//get the correct data from the data provider
//		this.extraction = GridExtraction.loadData(skel.dataset, skel.approxNumPlaces, skel.scale);
//		this.gridWidth = extraction.mapWidth;
//		this.gridHeight = extraction.mapHeight;
//		this.numZips = extraction.numZips;
//
//		this.zipcodePopulation = new int[numZips];
//		long sum = 0;
//		for (int i = 0; i < numZips; i++) {
//			zipcodePopulation[i] = extraction.gridPlaces[i].pop;
//			sum += zipcodePopulation[i];
//		}
//		this.totalPopulation = sum;
//
//		System.out.println("numZips = " + numZips);
//		System.out.println("population = " + totalPopulation);
//
//
//		this.interactionMatrix = extraction.computeInteractionMatrix();
//
//		/////////////////////////////////////////////
//		//  create inital population distribution  //
//		/////////////////////////////////////////////
//		zipNodeOwners = new int[numZips];
//		zipThreadOwners = new int[numZips];
//
//		//speckled - round robin dist
//		for (int i = 0 ; i < numZips ; i++) {
//			zipNodeOwners[i] = i % skel.numNodes;
//		}
////		//ordered - by population -- This is bad becasue it produces unbalanced nodes
////		for (int i = 0; i < numZips; i++) {
////			zipNodeOwners[i] = i * skel.numNodes / numZips;
////		}
//
//		//////////////////////////////////////
//		//  save a pic of the distribution  //
//		//////////////////////////////////////
//		drawModelPic("model_preNode.bmp");
//
//
////		//////////////////////////////////////////
////		//  optimize distribution across nodes  //
////		//////////////////////////////////////////
////		System.out.println("\n*** Distributing the zipcode amongst nodes *** ");
////		Distribute.optimizeDist(skel.numNodes , zipNodeOwners , zipcodePopulation , interactionMatrix);
////		System.out.println("*** DONE DIST ACROSS NODES ***\n");
//
//		drawModelPic("model_postNode.bmp");
//
//		////////////////////////////////////////////
//		//  prepare to distribute across threads  //
//		////////////////////////////////////////////
//		//calculate each node's population
//		long[] nodePops = new long[skel.numNodes];
//		for(int i = 0 ; i < numZips ; i++) {
//			nodePops[zipNodeOwners[i]] += zipcodePopulation[i];
//		}
//
//		//allocate the zips on a node to a thread
//		//create the sub matricies
//		final double[][][] nodeIntMatricies = new double[skel.numNodes][][];
//		int[] zipCount = new int[skel.numNodes];				//how many zips each node was given
//		final int[][] zipPops = new int[skel.numNodes][];			//each node needs a list of its zips populations
//		final int[][] threadOwners = new int[skel.numNodes][];			//each node needs a list of which thread owns its zips
//		int[][] zipsOwned = new int[skel.numNodes][];
//		for (int node = 0 ; node < skel.numNodes ; node++) {
//
//			//find all of the zips that this node owns
//			Vector<Integer> vec = new Vector<Integer>();
//			for (int zip = 0 ; zip < numZips ; zip++) {
//				if(zipNodeOwners[zip] == node) {
//					vec.add(zip);
//				}
//			}
//
//			zipCount[node] = vec.size();				//save the number of zips this node was given
//			zipsOwned[node] = new int[vec.size()];			//create a list of the zips this node was given
//			zipPops[node] = new int[vec.size()];			//create a list of the populations of those zips
//			threadOwners[node] = new int[vec.size()];		//create a list of which thread owns each zip
//
//			for (int i = 0 ; i < zipsOwned[node].length ; i++) {
//				zipsOwned[node][i] = vec.get(i);				//record which zips this node has
//			}
//			for (int i = 0 ; i < zipsOwned[node].length ; i++) {
//				zipPops[node][i] = zipcodePopulation[zipsOwned[node][i]];	//record those zips populations
//			}
//
//			//allocate zips to threads in a round-robin fashion
//			int tempThreadNum = 0;
//			for (int i = 0 ; i < zipsOwned[node].length ; i++) {
//				zipThreadOwners[zipsOwned[node][i]] = tempThreadNum;		//allocate a zip to a thread
//				threadOwners[node][i] = tempThreadNum;				//allocate a zip to a thread
//
//				tempThreadNum = (tempThreadNum + 1) % skel.threadsPerNode;
//			}
//
//			//create the smaller matrices
//			double[][] matrix = new double[zipsOwned[node].length][zipsOwned[node].length];
//			for(int i = 0 ; i < zipsOwned[node].length ; i++) {
//				for(int j = 0 ; j < zipsOwned[node].length ; j++) {
//					matrix[i][j] = interactionMatrix[zipsOwned[node][i]][zipsOwned[node][j]];
//				}
//			}
//			nodeIntMatricies[node] = matrix;
//		}
//
//		//draw a picture of each node
//		for (int i = 0 ; i < skel.numNodes ; i++) {
//			this.drawNodePic(i , "node_ " + i + "_preThread.bmp");
//		}
//
//		////////////////////////////////////////////
//		//  optimize distribution across threads  //
//		////////////////////////////////////////////
//		//@todo - MultiThread this
//		try {
//			final CountDownLatch latch = new CountDownLatch(skel.numNodes);
//
//			for(int i = 0 ; i < skel.numNodes ; i++) {
//
//				final int finalI = i;
//
//				Thread worker = new Thread() {
//					public void run() {
//						System.out.println("Launching thread to optimize node " + finalI + "...");
//
//						Distribute.optimizeDist(
//							Population.this.skel.threadsPerNode ,
//							threadOwners[finalI] ,
//							zipPops[finalI] ,
//							nodeIntMatricies[finalI]
//
//							);
//
//						System.out.println("node " + finalI + " optimized");
//						latch.countDown();
//					}
//				};
//				worker.start();
//			}
//			latch.await();
//		} catch (InterruptedException ex) {
//			ex.printStackTrace();
//		}
//
//		//record results
//		int[] counters = new int[skel.numNodes];
//		int tempNode;
//		for(int i = 0 ; i < numZips ; i++) {
//			tempNode = zipNodeOwners[i];
//			zipThreadOwners[i] = threadOwners[tempNode][counters[tempNode]];
//			counters[tempNode]++;
//		}
//
//		//draw a picture of each node
//		for (int i = 0 ; i < skel.numNodes ; i++) {
//			this.drawNodePic(i , "node_ " + i + "_postThread.bmp");
//		}
//
//
//		drawComplexModelPic("postThread.bmp");
//
//
//		//report amount of offNode communication
//		double[][] nodeComm = new double[skel.numNodes][skel.numNodes];
//		for (int i = 0 ; i < numZips ; i++) {
//			for (int j = 0 ; j < numZips ; j++) {
//				nodeComm[zipNodeOwners[i]][zipNodeOwners[j]] += interactionMatrix[i][j];
//			}
//		}
//		double[] nodeRowSums = new double[skel.numNodes];
//		for (int i = 0 ; i < skel.numNodes ; i++) {
//			for (int j = 0 ; j < skel.numNodes ; j++) {
//				nodeRowSums[i] += nodeComm[i][j];
//			}
//		}
//		System.out.println("\nBelow you will find the intra-node communication matrix\n");
//		for (int i = 0 ; i < skel.numNodes ; i++) {
//			for (int j = 0 ; j < skel.numNodes ; j++) {
//				System.out.print( roundThree(nodeComm[i][j] / nodeRowSums[i]) + "\t");
//			}
//			System.out.println();
//		}
//
//
//
//		//recast interaction matrix as a series of pdfs (1 per zip)
//		System.out.println("\ncalculating pdfs");
//		pdfs = new Distribution[numZips];
//		for(int i = 0 ; i < numZips ; i++) {
//			pdfs[i] = new Distribution(interactionMatrix[i] , .05);
//		}
//		interactionMatrix = null;
//		System.out.println("Done Building Complete Population");
//	}
//
//
//	/**
//	 * This is basically a clone method, however, it only copies the portions
//	 * of the pdf[] that a specific node will require.
//	 */
//	private Population(Population completePop , int nodeIndex) {
//		this.skel = completePop.skel;
//		this.extraction = completePop.extraction;
//
//		this.gridWidth = completePop.gridWidth;
//		this.gridHeight = completePop.gridHeight;
//
//		this.totalPopulation = completePop.totalPopulation;
//		this.numZips = completePop.numZips;
//		this.interactionMatrix = null;
//
//		this.zipcodePopulation = Arrays.copyOf(
//			completePop.zipcodePopulation ,
//			completePop.zipcodePopulation.length
//			);
//		this.zipNodeOwners = Arrays.copyOf(
//			completePop.zipNodeOwners ,
//			completePop.zipNodeOwners.length
//			);
//		this.zipThreadOwners = Arrays.copyOf(
//			completePop.zipThreadOwners ,
//			completePop.zipThreadOwners.length
//			);
//
//		this.pdfs = new Distribution[completePop.pdfs.length];
//		for (int i = 0 ; i < pdfs.length ; i++) {
//			if(zipNodeOwners[i] == nodeIndex) {
//				pdfs[i] = completePop.pdfs[i];
//			} else {
//				pdfs[i] = null;
//			}
//		}
//	}
//
//
//	/**
//	 * Given a persons resident zipcode, determine the home zipcode of a random contact.
//	 *
//	 * @param zip - A persons home zipcode.
//	 * @param randomDraw - A random number 0 - 1
//	 */
//	public int getRandomZipcode(int zip , double randomDraw) {
//		return pdfs[zip].getRandomIndex(randomDraw);
//	}
//
//
//	/** Ensure that all of the population pieces exist.  If they do not exist build them. */
//	private static void findOrBuildPops() {
//
//		PopulationSkeleton popSkel = new PopulationSkeleton();
//
//		//search for each nodes file, and the manager file
//		boolean fileMissing = false;
//		for (int i = -1 ; i < popSkel.numNodes ; i++) {
//			String fileName = popSkel.getPartialFileName(i);
//
//			File f = new File(fileName);
//			if( !f.exists()) {
//				System.out.println("Cannot find " + fileName);
//				fileMissing = true;
//			}
//		}
//
//		if(fileMissing) {
//
//			System.out.println("A Population piece was not found - building population from scratch");
//
//			//create core popultion
//			Population np = new Population(popSkel);
//
//			//create subpopulations
//			for (int i = -1 ; i < popSkel.numNodes ; i++) {
//
//				Population tempPop = new Population(np , i);
//
//				//serialize subpopulation
//				System.out.println("Serializing piece " + i + " of " + popSkel.numNodes + " a Population...");
//				try {
//					FileOutputStream fos = new FileOutputStream(
//						popSkel.getPartialFileName(i)
//						);
//
//					ObjectOutputStream out = new ObjectOutputStream(fos);
//					out.writeObject(tempPop);
//
//					out.close();
//					fos.close();
//				} catch (Exception ex) {
//					ex.printStackTrace();
//					System.exit(0);
//				}
//			}
//
//		} else {
//			System.out.println("All " + popSkel.numNodes + " Population pieces were found");
//			return;
//		}
//
//
//	}
//
//
//	/** Convert a number line 1,500,000 to "1500k" or 150,000,000 to "150Mill". */
//	private static String getDesiredPopulationString(long pop) {
//
//		if(pop % 1000000000 == 0) {
//			return pop / 1000000000 + "Bill";
//		} else if(pop % 1000000 == 0) {
//			//population is a multiple of 1Mill
//			return pop / 1000000 + "Mill";
//		} else if(pop % 100000 == 0) {
//			//population is a multiple of 100k
//			return pop / 1000 + "k";
//		} else {
//			throw new IllegalArgumentException("Simulate a larger population");
//		}
//	}
//
//
//	/** Write a file that lists information about the ModelPlaceSeeds. */
//	private void writePixelData(HospitalModelPlaceSeed[] sortedArray , String fileName) {
//
//		//create a file that has pixel data listed
//		try {
//			FileOutputStream fos = new FileOutputStream(fileName);
//			PrintWriter dout = new PrintWriter(fos);
//
//			dout.write(
//				"entry\t" + "population\t" + "latIndex\t" + "longIndex\t" +
//				"beds1\t" + "beds2\t" + "beds3\t" + "beds4\t" + "beds5\t" + "beds6\t\n"
//				);
//
//			for (int i = 0 ; i < sortedArray.length ; i++) {
//
//				//list - population , lat , long , hospitals
//				dout.write("sortedArray[" + i + "]" +
//					"\t" + sortedArray[i].population +
//					"\t" + sortedArray[i].latIndex +
//					"\t" + sortedArray[i].longIndex + "\t"
//					);
//
//				StringBuffer buffer = new StringBuffer("");
//				if(sortedArray[i].hospitalBeds != null) {
//					for (int j = 0 ; j < sortedArray[i].hospitalBeds.length ; j++) {
//						buffer.append(sortedArray[i].hospitalBeds[j] + "\t");
//					}
//					dout.write(buffer.toString() + "\n");
//				} else {
//					dout.write("\n");
//				}
//			}
//
//			dout.close();
//			fos.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.exit(0);
//		}
//
//	}
//
//
//	/** Draw a pricture that depicts the allocation of zips to nodes. */
//	private void drawModelPic(String fileName) {
//
//		BufferedImage image = new BufferedImage(gridWidth, gridHeight , BufferedImage.TYPE_3BYTE_BGR);
//
//		int x;
//		int y;
//		int red;
//		int green;
//		int blue;
//
//		//paint each pixel in the image
//		for (int i = 0 ; i < zipNodeOwners.length ; i++) {
//
//			x = this.extraction.gridPlaces[i].x;
//			y = this.extraction.gridPlaces[i].y;
//
//			red = 0;
//			green = 0;
//			blue = 0;
//
//			if(zipNodeOwners[i] % 8 == 0) {
//				red = 255;
//				green = 0;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 1) {
//				red = 0;
//				green = 255;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 2) {
//				red = 0;
//				green = 0;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 3) {
//				red = 255;
//				green = 255;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 4) {
//				red = 255;
//				green = 0;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 5) {
//				red = 0;
//				green = 255;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 6) {
//				red = 255;
//				green = 255;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 7) {
//				red = 127;
//				green = 127;
//				blue = 127;
//			}
//
//			image.setRGB(
//				x ,
//				y ,
//				ImageUtility.getRGB(red , green , blue)
//				);
//
//		}
//
//		//write the image
//		try {
//			ImageIO.write(image , "bmp" , new File(populationPath + fileName));
//		} catch(Exception ex) {
//			ex.printStackTrace();
//			System.exit(0);
//		}
//	}
//
//
//	/**  Draw a pic of the population split. */
//	private void drawComplexModelPic(String fileName) {
//
//		BufferedImage image = new BufferedImage(gridWidth, gridHeight , BufferedImage.TYPE_3BYTE_BGR);
//
//		int x;
//		int y;
//		int red;
//		int green;
//		int blue;
//
//		//paint each pixel in the image
//		for (int i = 0 ; i < zipNodeOwners.length ; i++) {
//			x = this.extraction.gridPlaces[i].x;
//			y = this.extraction.gridPlaces[i].y;
//
//			red = 0;
//			green = 0;
//			blue = 0;
//
//			if(zipNodeOwners[i] % 8 == 0) {
//				red = 255;
//				green = 0;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 1) {
//				red = 0;
//				green = 255;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 2) {
//				red = 0;
//				green = 0;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 3) {
//				red = 255;
//				green = 255;
//				blue = 0;
//			} else if(zipNodeOwners[i] % 8 == 4) {
//				red = 255;
//				green = 0;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 5) {
//				red = 0;
//				green = 255;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 6) {
//				red = 255;
//				green = 255;
//				blue = 255;
//			} else if(zipNodeOwners[i] % 8 == 7) {
//				red = 127;
//				green = 127;
//				blue = 127;
//			}
//
//
//			//adjust for thread num
//			//all colors are in the top half
//
//			double adjustment = ((double)zipThreadOwners[i]) / (skel.threadsPerNode );
//
//			if(adjustment > 1.0) {
//				System.out.println("zipThreadOwners[ " + i + "] :: " + zipThreadOwners[i] );
//			}
//
//			if(red == 0){
//				red = (int)(255 * adjustment);
//			}
//			if(green == 0) {
//				green = (int)(255 * adjustment);
//			}
//			if(blue == 0) {
//				blue = (int)(255 * adjustment);
//			}
//
//			image.setRGB(
//				x ,
//				y ,
//				ImageUtility.getRGB(red , green , blue)
//				);
//		}
//
//		//write the image
//		try {
//			ImageIO.write(image , "bmp" , new File(populationPath + fileName));
//		} catch(Exception ex) {
//			ex.printStackTrace();
//			System.exit(0);
//		}
//	}
//
//
//	/**
//	 * Draw a picture of all of the zipcode on a specific node.  Each
//	 * thread on that node is drawn a different color.
//	 */
//	public void drawNodePic(int nodeNum, String fileName) {
//
//		BufferedImage image = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_3BYTE_BGR);
//
//		int x;
//		int y;
//		int red;
//		int green;
//		int blue;
//
//		//paint each pixel in the image
//		for (int i = 0 ; i < zipNodeOwners.length ; i++) {
//
//			if (zipNodeOwners[i] == nodeNum) {
//
//				x = this.extraction.gridPlaces[i].x;
//				y = this.extraction.gridPlaces[i].y;
//
//				red = 0;
//				green = 0;
//				blue = 0;
//
//				if(zipThreadOwners[i] % 8 == 0) {
//					red = 255;
//					green = 0;
//					blue = 0;
//				} else if(zipThreadOwners[i] % 8 == 1) {
//					red = 0;
//					green = 255;
//					blue = 0;
//				} else if(zipThreadOwners[i] % 8 == 2) {
//					red = 0;
//					green = 0;
//					blue = 255;
//				} else if(zipThreadOwners[i] % 8 == 3) {
//					red = 255;
//					green = 255;
//					blue = 0;
//				} else if(zipThreadOwners[i] % 8 == 4) {
//					red = 255;
//					green = 0;
//					blue = 255;
//				} else if(zipThreadOwners[i] % 8 == 5) {
//					red = 0;
//					green = 255;
//					blue = 255;
//				} else if(zipThreadOwners[i] % 8 == 6) {
//					red = 255;
//					green = 255;
//					blue = 255;
//				} else if(zipThreadOwners[i] % 8 == 7) {
//					red = 127;
//					green = 127;
//					blue = 127;
//				}
//
//				image.setRGB(
//					x ,
//					y ,
//					ImageUtility.getRGB(red , green , blue)
//					);
//			}
//		}
//
//		//write the image
//		try {
//			ImageIO.write(image , "bmp" , new File(populationPath + fileName));
//		} catch(Exception ex) {
//			ex.printStackTrace();
//			System.exit(0);
//		}
//	}
//
//
//	/** Small utility method that rounds a number to 3 places */
//	private static double roundThree(double num) {
//		double temp = num * 1000.0;
//		temp = Math.floor(temp);
//		return (temp / 1000.0);
//	}
//
//
//	/** Empty Method, however the static initalizer is still run...creating a serialized pop. */
//	public static void main(String[] args) {}
//
//
//
//	/**
//	 * A PopulationSkeleton is built by looking in the properties file, and determining which
//	 * population should be modeled.
//	 *
//	 * This class looks at the following variables:
//	 * -"popDataSource"
//	 * -"scale"
//	 * -"approxNumZips"
//	 * -"numNodes"
//	 * -"threadsPerNode"
//	 */
//	static public class PopulationSkeleton implements Serializable {
//
//		//these variables contribute to the serialized filename
//
//		/** The grid based data that will be simulated. */
//		public final String dataset;
//
//		/** The fraction of the "correct" population you will model. */
//		public final double scale;
//
//		/** The approximate number of ModelPlaces you want to simulate. */
//		public final int approxNumPlaces;
//
//		/** The number of nodes. */
//		public final int numNodes;
//
//		/** The number of Threads Per node. */
//		public final int threadsPerNode;
//
//		/** Create a wrapper for a Population.  All required variables are read from a file. */
//		PopulationSkeleton() {
//
//			//read in variables required to create a population.
//			Properties props = Node.props;
//			if(props == null) {
//				props = ModelManager.manager.getProperties();
//			}
//			this.dataset = props.getProperty("popDataSource");
//			this.scale = Double.parseDouble(props.getProperty("scale"));
//			this.approxNumPlaces = Integer.parseInt(props.getProperty("approxNumZips"));
//			this.numNodes = Integer.parseInt(props.getProperty("numNodes"));
//			this.threadsPerNode = Integer.parseInt(props.getProperty("threadsPerNode"));
//		}
//
//
//		/**
//		 * Generate a file name for a portion of this Population.
//		 *
//		 * @param nodeIndex - If nodeIndex is
//		 */
//		public String getPartialFileName(int nodeIndex) {
//			String indexString;
//			if(nodeIndex >= 0) {
//				indexString = Integer.toString(nodeIndex);
//			} else {
//				indexString = "M";
//			}
//
//			return populationPath + getFilePrefix() + "-" + indexString + ".ser";
//		}
//
//
//		/**
//		 * Generate the file name for a Population.
//		 *
//		 * @return A string like "USZIP_10000_0.5_4_2"
//		 */
//		private String getFilePrefix() {
//			String result =
//				this.dataset + "_" +
//				this.approxNumPlaces + "_" +
//				this.scale + "_" +
//				this.numNodes + "_" +
//				this.threadsPerNode;
//
//			return result;
//		}
//	}
//}
