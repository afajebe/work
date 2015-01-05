/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.gui;


import util.PeriodicLog;
import util.Utility;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 * This class defines a simple GUI that reads in log files from a model run
 * a then displays information about that run.
 */
public class RunInspectorApp extends JFrame {

	/** The individual tabs. */
	private JTabbedPane tabs;

	private JComponent runSummaryComp;

	private JComponent memoryInfoComp;


	public RunInspectorApp() {
		super("Run Inspector Application");
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		this.runSummaryComp = null;
		this.memoryInfoComp = null;
		this.tabs = buildTabbedPane();

		this.setContentPane(tabs);
		this.setVisible(true);
	}


	/** Bring the "Run Sammary" tab to the front. */
	public void showSummaryTab() {
		int index = tabs.indexOfTab("Run Summary");
		tabs.setSelectedIndex(index);
	}


	/** 
	 * Load a model run's summary file. 
	 * 
	 * @param logFile - A .slg file (Serialized Log File)
	 */
	protected void loadFile(File logFile) {
		this.loadRunData(logFile);
		this.loadMemoryData(logFile);
	}


	/** 
	 * Load and display basic infomation contained in a run's summary file.
	 * 
	 * @param logFile - A .slg file (Serialized Log File)
	 */
	private void loadRunData(File logFile) {

		//deseralize the file
		PeriodicLog log = null;
		try {
			log = (PeriodicLog) Utility.deserialize(logFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		//print variables - for fun
		String[] vars = log.listVariables();
		for (int i = 0; i < vars.length; i++) {
			System.out.println("Vars :: " + vars[i]);
		}

		int numNodes = Integer.parseInt(log.getProperty("numNodes"));
		

		//The following variables are required to build a RunSummaryPanel : 
		int numCases = 0;
		int numDays = Integer.MIN_VALUE;
		int[] casesPerDay = null;
		int[] compTimes = null;
		int[][] casesPerDayByNode = null;
		int[][] compTimesPerNode = null;

		//extract the data required to initalize those variables	
		numDays = Integer.parseInt(log.getProperty("numDays"));		
		String[] stringNumNewInfections = log.extract("numNewInfections");
		String[] stringCasesPerDay = log.extract("numSick");
		String[] stringCompTimes = log.extract("recTime");
		casesPerDay = new int[stringCasesPerDay.length];
		compTimes = new int[stringCompTimes.length];
		for (int i = 0; i < casesPerDay.length; i++) {
			System.out.println(Integer.parseInt(stringNumNewInfections[i]));
			numCases += Integer.parseInt(stringNumNewInfections[i]);
			casesPerDay[i] = Integer.parseInt(stringCasesPerDay[i]);
			compTimes[i] = Integer.parseInt(stringCompTimes[i]);
		}
		casesPerDayByNode = new int[numNodes][];
		compTimesPerNode = new int[numNodes][];
		for (int i = 0; i < numNodes; i++) {
			//extract the number sick each day on node i
			String[] tempCasesPerDay = log.extract("numSick_" + i);
			//extract how long node i takes to compute iteration j
			String[] tempCompTimes = log.extract("NodeCompTime_" + i);
			int[] nodeCasesPerDay = new int[tempCasesPerDay.length];
			int[] nodeCompTimes = new int[tempCompTimes.length];
			//parse each of these values into the new arrays
			for (int j = 0; j < tempCasesPerDay.length; j++) {
				nodeCasesPerDay[j] = Integer.parseInt(tempCasesPerDay[j]);
				nodeCompTimes[j] = Integer.parseInt(tempCompTimes[j]);
			}
			//save the results
			casesPerDayByNode[i] = nodeCasesPerDay;
			compTimesPerNode[i] = nodeCompTimes;
		}

		//test arrays to ensure proper length
		if (casesPerDay.length != numDays) {
			throw new IllegalStateException(
				"casesPerDay.length = " + casesPerDay.length +
				" when it should be " + numDays);
		}
		if (compTimes.length != numDays) {
			throw new IllegalStateException(
				"compTimes.length = " + compTimes.length +
				" when it should be " + numDays);
		}
		if (casesPerDayByNode[0].length != numDays) {
			throw new IllegalStateException(
				"casesPerDayByNode[0].length = " + casesPerDayByNode[0].length +
				" when it should be " + numDays);
		}


		this.runSummaryComp = new RunSummaryPanel(
			numCases,
			numDays,
			casesPerDay,
			compTimes,
			casesPerDayByNode,
			compTimesPerNode
			);

		int temp = tabs.indexOfTab("Run Summary");
		tabs.remove(temp);
		tabs.insertTab("Run Summary", null, runSummaryComp, "", temp);
	}


	/** 
	 * Load and display memory infomation contained in a run's summary file.
	 * 
	 * @param logFile - A .slg file (Serialized Log File)
	 */
	private void loadMemoryData(File logFile) {
		
		//deseralize the file
		PeriodicLog log = null;
		try {
			log = (PeriodicLog) Utility.deserialize(logFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}	

		int numNodes = Integer.parseInt(log.getProperty("numNodes"));
		
		//The following variables are required to build a MemmoryPanel : 		
		int numDays = Integer.MIN_VALUE;		
		long[] maxMem = null;
		long[] totalMem = null;
		long[] freeMem = null;		
		int[] numONCEs = null;
		long[][] totalMemByNode = null;
		long[][] maxMemByNode = null;
		long[][] freeMemByNode = null;


		//extract the data required to initalize those variables	
		numDays = Integer.parseInt(log.getProperty("numDays"));
		String[] stringMaxMem = log.extract("maxMem");
		String[] stringTotalMem = log.extract("totalMem");
		String[] stringFreeMem = log.extract("freeMem");
		String[] stringNumONCEs = log.extract("ONCE_all");
		
		maxMem = new long[stringMaxMem.length];
		totalMem = new long[stringTotalMem.length];
		freeMem = new long[stringFreeMem.length];
		numONCEs = new int[stringNumONCEs.length];
		for (int i = 0; i < numDays; i++) {
			maxMem[i] = Long.parseLong(stringMaxMem[i]);
			totalMem[i] = Long.parseLong(stringTotalMem[i]);
			freeMem[i] = Long.parseLong(stringFreeMem[i]);
			numONCEs[i] = Integer.parseInt(stringNumONCEs[i]);
		}
		
		maxMemByNode = new long[numNodes][];
		totalMemByNode = new long[numNodes][];
		freeMemByNode = new long[numNodes][];
		for (int i = 0; i < numNodes; i++) {
			//extract the memory variable for node i
			String[] tempMaxMem = log.extract("maxMem_" + i);
			String[] tempTotalMem = log.extract("totalMem_" + i);
			String[] tempFreeMem = log.extract("freeMem_" + i);
			
			long[] nodeMaxMem = new long[tempMaxMem.length];
			long[] nodeTotalMem = new long[tempTotalMem.length];
			long[] nodeFreeMem = new long[tempFreeMem.length];
			
			//parse each of these values into the new arrays
			for (int j = 0; j < tempMaxMem.length; j++) {
				nodeMaxMem[j] = Long.parseLong(tempMaxMem[j]);
				nodeTotalMem[j] = Long.parseLong(tempTotalMem[j]);
				nodeFreeMem[j] = Long.parseLong(tempFreeMem[j]);
			}
			//save the results
			maxMemByNode[i] = nodeMaxMem;
			totalMemByNode[i] = nodeTotalMem;
			freeMemByNode[i] = nodeFreeMem;
		}
				
		this.memoryInfoComp = new MemoryPanel(
			numDays ,
			maxMem ,
			totalMem ,
			freeMem ,
			numONCEs ,
			maxMemByNode ,
			totalMemByNode ,
			freeMemByNode
			);

		int temp = tabs.indexOfTab("Memory Info");
		tabs.remove(temp);
		tabs.insertTab("Memory Info", null, memoryInfoComp, "", temp);
	}


	/** Assemble all of the TabbedPanes that will display info about the run. */
	private JTabbedPane buildTabbedPane() {

		JTabbedPane returnMe = new JTabbedPane();

		returnMe.addTab("Introduction", new FileLoaderPanel(this));

		runSummaryComp = new JPanel();
		runSummaryComp.add(new JLabel("Please Load a File"));
		returnMe.addTab("Run Summary", runSummaryComp);

		memoryInfoComp = new JPanel();
		memoryInfoComp.add(new JLabel("Please Load a File"));
		returnMe.addTab("Memory Info", memoryInfoComp);

		return returnMe;
	}


	/** 
	 * Launch a RunInspectorApp. 
	 * 
	 * @param args - Ignored
	 */
	public static void main(String[] args) {
		RunInspectorApp app = new RunInspectorApp();
	}
}

