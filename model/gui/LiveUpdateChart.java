package model.gui;


import java.util.LinkedList;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;


/**
 * A LiveUpdateChart is a class that uses the JFreeChart API to make charts that display live data 
 * easy to obtain.  This class can be thought of as a simple facade for a few JFreeChart commands.
 */
public class LiveUpdateChart {

	/** The Chart's title. */
	private String chartTitle;

	/** The Chart's x axis label. */
	private String xAxisLabel;

	/** The Chart's y axis label. */
	private String yAxisLabel;

	/** The ChartPanel produced by the JFreeChart API (an extension of JPanel). */
	private ChartPanel chartPanel;

	/** The Chart's internal data set. */
	private DefaultXYDataset xyData;

	private LinkedList<Double> xData;

	private LinkedList<Double> yData;


	public LiveUpdateChart(String chartTitle, String xAxisLabel, String yAxisLabel) {
		this.chartTitle = chartTitle;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		this.chartPanel = null;
	}


	public synchronized void addDataPoint(double x, double y) {
		xData.add(x);
		yData.add(y);

		double[] xCoord = new double[xData.size()];
		for (int i = 0; i < xCoord.length; i++) {
			xCoord[i] = xData.get(i);
		}

		double[] yCoord = new double[yData.size()];
		for (int i = 0; i < yCoord.length; i++) {
			yCoord[i] = yData.get(i);

		}

		//replace existing data
		xyData.addSeries(chartTitle, new double[][]{xCoord, yCoord});
	}


	public synchronized void addDataPoint(double y) {
		xData.add(new Double(xData.size()));
		yData.add(y);

		double[] xCoord = new double[xData.size()];
		for (int i = 0; i < xCoord.length; i++) {
			xCoord[i] = xData.get(i);
		}

		double[] yCoord = new double[yData.size()];
		for (int i = 0; i < yCoord.length; i++) {
			yCoord[i] = yData.get(i);

		}

		//replace existing data
		xyData.addSeries(chartTitle, new double[][]{xCoord, yCoord});
	}


	/** Strip out the X,Y data from the lists. */
	private double[][] extractData() {
		double[] xCoord = new double[xData.size()];
		for (int i = 0; i < xCoord.length; i++) {
			xCoord[i] = xData.get(i);
		}

		double[] yCoord = new double[yData.size()];
		for (int i = 0; i < yCoord.length; i++) {
			yCoord[i] = yData.get(i);

		}

		return new double[][]{xCoord, yCoord};
	}


	/** Build a chart that will automatically update when new XY data is added. */
	public JPanel getChart() {

		if (this.chartPanel == null) {

			this.xyData = new DefaultXYDataset();
			this.xData = new LinkedList<Double>();
			this.yData = new LinkedList<Double>();
			xyData.addSeries(chartTitle, extractData());

			//build inital chart that uses above dataset
			JFreeChart chart = ChartFactory.createXYLineChart(
					chartTitle,
					xAxisLabel,
					yAxisLabel,
					xyData, //data
					PlotOrientation.VERTICAL,
					false, //show legend
					false, //show tooltips
					false //show urls
					);

			this.chartPanel = new ChartPanel(chart);
			return chartPanel;
		} else {
			return chartPanel;
		}
	}
}
