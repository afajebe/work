package model.gui;


import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;


/**
 *
 * @author  jparker
 */
public class RunSummaryPanel extends javax.swing.JPanel {

	int numCases;

	int numDays;

	int[] casesPerDay;

	int[] compTimes;

	int[][] casesPerDayByNode;

	int[][] compTimesByNode;


	/** Creates new form RunSummaryPanel */
	public RunSummaryPanel(int numCases, int numDays, int[] casesPerDay, int[] compTimes, int[][] casesPerDayByNode, int[][] compTimesByNode) {
		this.numCases = numCases;
		this.numDays = numDays;
		this.casesPerDay = casesPerDay;
		this.compTimes = compTimes;
		this.casesPerDayByNode = casesPerDayByNode;
		this.compTimesByNode = compTimesByNode;
		initComponents();
	}


	/** Create a graph that lists cases per day and compute time. */
	private JPanel buildRunSummaryChart() {

		//build casesPerDay dataset
		double[] y1 = new double[numDays];
		double[] y2 = new double[numDays];
		double[] xCoord = new double[numDays];
		for (int i = 0; i < xCoord.length; i++) {
			y1[i] = casesPerDay[i];
			y2[i] = compTimes[i];
			xCoord[i] = i;
			
//			System.out.println(casesPerDay[i]);
		}
		DefaultXYDataset casesPerDayData = new DefaultXYDataset();
		casesPerDayData.addSeries(
			(Comparable) "Num Sick", new double[][]{xCoord, y1});

		//build inital chart that uses above dataset
		JFreeChart chart = ChartFactory.createXYLineChart(
			"Cases Per Day and Compute Time", //title
			"Day", //x axis label
			"Cases", //y axis label
			casesPerDayData, //data
			PlotOrientation.VERTICAL,
			true, //show legend
			false, //show tooltips
			false //show urls
			);

		//get direct access to the plot - so we can add additional info
		XYPlot plot = chart.getXYPlot();

		//add 2nd Axis
		NumberAxis compTimeAxis = new NumberAxis("Compute Time (sec/10)");
		plot.setRangeAxis(1, compTimeAxis);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);	//put axis on right side

		//build the compute time dataset
		DefaultXYDataset compTimeData = new DefaultXYDataset();
		compTimeData.addSeries(
			(Comparable) "Compute Time", new double[][]{xCoord, y2});

		//add the 2nd dataset
		plot.setDataset(1, compTimeData);
		plot.mapDatasetToRangeAxis(1, 1);	//tell the 2nd dataset to use the 2nd axis

		//add a 2nd renderer - so series are drawn differently
		XYItemRenderer rend = new XYLineAndShapeRenderer(true, false);
		plot.setRenderer(1, rend);

		return new ChartPanel(chart);
	}


//	/** Create a graph that lists cases by node and compute time by node. */
//	private JPanel buildNodeSummaryChart() {
//		
//		int numNodes = casesPerDayByNode.length;
//		
//		//build casesPerDayByNode dataset
//		DefaultXYDataset casesPerDayByNodeData = new DefaultXYDataset();
//		//build xCoord
//		double[] xCoord = new double[numDays];
//		for (int i = 0; i < xCoord.length; i++) {
//			xCoord[i] = i;
//		}
//		//build many y coords
//		for (int i = 0; i < numNodes; i++) {
//			double[] yTemp = new double[numDays];
//			
//			for (int j = 0; j < yTemp.length; j++) {
//				yTemp[j] = casesPerDayByNode[i][j];
//			}			
//			
//			casesPerDayByNodeData.addSeries(
//				(Comparable) ("Num Sick - " + i) , new double[][]{xCoord, yTemp}
//			);
//		}
//		
//				
//		//build inital chart that uses above dataset
//		JFreeChart chart = ChartFactory.createXYLineChart(
//			"Cases Per Day - By Node" ,		//title
//			"Day" ,					//x axis label
//			"Cases" ,				//y axis label
//			casesPerDayByNodeData ,			//data
//			PlotOrientation.VERTICAL ,
//			false ,					//show legend
//			false ,					//show tooltips
//			false					//show urls
//			);
//		
//		//get direct access to the plot - so we can add additional info
//		XYPlot plot = chart.getXYPlot();
//		
//		//add 2nd Axis
//		NumberAxis compTimeAxis = new NumberAxis("Compute Time (sec/10)");
//		plot.setRangeAxis(1, compTimeAxis);
//		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);	//put axis on right side
//				
//		//build compTimesByNodeData dataset
//		DefaultXYDataset compTimesByNodeData = new DefaultXYDataset();	
//		//build many y coords
//		for (int i = 0; i < numNodes; i++) {
//			double[] yTemp = new double[numDays];
//			
//			for (int j = 0; j < yTemp.length; j++) {
//				yTemp[j] = compTimesByNode[i][j];
//			}			
//			
//			compTimesByNodeData.addSeries(
//				(Comparable) ("Compute Time - " + i) , new double[][]{xCoord, yTemp}
//			);
//		}
//		
//		//add the 2nd dataset
//		plot.setDataset(1, compTimesByNodeData);
//		plot.mapDatasetToRangeAxis(1, 1);	//tell the 2nd dataset to use the 2nd axis
//		
//		//add a 2nd renderer - so series are drawn differently
//		XYItemRenderer rend = new XYLineAndShapeRenderer(true , false);
//		plot.setRenderer(1, rend);
//		
//		return new ChartPanel(chart);
//	}
	/** Create a graph that display's each node's sick population vs time. */
	private JPanel buildNodeSickChart() {

		int numNodes = casesPerDayByNode.length;

		//build casesPerDayByNode dataset
		DefaultXYDataset casesPerDayByNodeData = new DefaultXYDataset();
		//build xCoord
		double[] xCoord = new double[numDays];
		for (int i = 0; i < xCoord.length; i++) {
			xCoord[i] = i;
		}
		//build many y coords
		for (int i = 0; i < numNodes; i++) {
			double[] yTemp = new double[numDays];

			for (int j = 0; j < yTemp.length; j++) {
				yTemp[j] = casesPerDayByNode[i][j];
			}

			casesPerDayByNodeData.addSeries(
				(Comparable) ("Num Sick - " + i), new double[][]{xCoord, yTemp});
		}


		//build inital chart that uses above dataset
		JFreeChart chart = ChartFactory.createXYLineChart(
			"Cases Per Day - By Node", //title
			"Day", //x axis label
			"Cases", //y axis label
			casesPerDayByNodeData, //data
			PlotOrientation.VERTICAL,
			false, //show legend
			false, //show tooltips
			false //show urls
			);

		return new ChartPanel(chart);
	}


	/** Create a graph that display's each node's compute time. */
	private JPanel buildNodeTimeChart() {

		int numNodes = casesPerDayByNode.length;

		//build compTimesByNodeData dataset
		DefaultXYDataset compTimesByNodeData = new DefaultXYDataset();
		//build xCoord
		double[] xCoord = new double[numDays];
		for (int i = 0; i < xCoord.length; i++) {
			xCoord[i] = i;
		}
		//build many y coords
		for (int i = 0; i < numNodes; i++) {
			double[] yTemp = new double[numDays];

			for (int j = 0; j < yTemp.length; j++) {
				yTemp[j] = compTimesByNode[i][j];
			}

			compTimesByNodeData.addSeries(
				(Comparable) ("Compute Time - " + i), new double[][]{xCoord, yTemp});
		}

		//build inital chart that uses above dataset
		JFreeChart chart = ChartFactory.createXYLineChart(
			"Compute Time - By Node", //title
			"Day", //x axis label
			"Compute Time (sec/10)", //y axis label
			compTimesByNodeData, //data
			PlotOrientation.VERTICAL,
			false, //show legend
			false, //show tooltips
			false //show urls
			);

		return new ChartPanel(chart);
	}


	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        totalCasesLabel = new javax.swing.JLabel();
        numDaysLabel = new javax.swing.JLabel();
        modelChart = this.buildRunSummaryChart();
        jPanel1 = new javax.swing.JPanel();
        nodeSickChart = this.buildNodeSickChart();
        nodeSpeedChart = this.buildNodeTimeChart();

        setPreferredSize(new java.awt.Dimension(800, 800));

        jLabel1.setText("Run Summary:");

        totalCasesLabel.setText("Total Cases: " + numCases);

        numDaysLabel.setText("Number Days: " + numDays);

        modelChart.setBorder(javax.swing.BorderFactory.createTitledBorder("Core Run Data:"));

        javax.swing.GroupLayout modelChartLayout = new javax.swing.GroupLayout(modelChart);
        modelChart.setLayout(modelChartLayout);
        modelChartLayout.setHorizontalGroup(
            modelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 768, Short.MAX_VALUE)
        );
        modelChartLayout.setVerticalGroup(
            modelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 295, Short.MAX_VALUE)
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Core Node Data\n"));

        nodeSickChart.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout nodeSickChartLayout = new javax.swing.GroupLayout(nodeSickChart);
        nodeSickChart.setLayout(nodeSickChartLayout);
        nodeSickChartLayout.setHorizontalGroup(
            nodeSickChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 369, Short.MAX_VALUE)
        );
        nodeSickChartLayout.setVerticalGroup(
            nodeSickChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );

        nodeSpeedChart.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout nodeSpeedChartLayout = new javax.swing.GroupLayout(nodeSpeedChart);
        nodeSpeedChart.setLayout(nodeSpeedChartLayout);
        nodeSpeedChartLayout.setHorizontalGroup(
            nodeSpeedChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
        );
        nodeSpeedChartLayout.setVerticalGroup(
            nodeSpeedChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nodeSickChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeSpeedChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(nodeSickChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nodeSpeedChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalCasesLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(numDaysLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modelChart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalCasesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numDaysLabel)
                .addGap(18, 18, 18)
                .addComponent(modelChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel modelChart;
    private javax.swing.JPanel nodeSickChart;
    private javax.swing.JPanel nodeSpeedChart;
    private javax.swing.JLabel numDaysLabel;
    private javax.swing.JLabel totalCasesLabel;
    // End of variables declaration//GEN-END:variables
}
