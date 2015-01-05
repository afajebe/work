/*
 * MemoryPanel.java
 *
 * Created on August 29, 2008, 2:06 PM
 */
package model.gui;


import java.util.Arrays;
import javax.swing.GroupLayout;
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
public class MemoryPanel extends javax.swing.JPanel {

    int numDays;

    long[] maxMem;

    long[] totalMem;

    long[] freeMem;

    int[] numONCEs;

    long[][] maxMemByNode;

    long[][] totalMemByNode;

    long[][] freeMemByNode;

    int numNodes;


    /** Creates new form MemoryPanel */
    public MemoryPanel(int numDays, long[] maxMem, long[] totalMem, long[] freeMem, int[] numONCEs, long[][] maxMemByNode, long[][] totalMemByNode, long[][] freeMemByNode) {
        this.numDays = numDays;
        this.maxMem = Arrays.copyOf(maxMem, maxMem.length);
        this.totalMem = Arrays.copyOf(totalMem, totalMem.length);
        this.freeMem = Arrays.copyOf(freeMem, freeMem.length);
        this.numONCEs = Arrays.copyOf(numONCEs, numONCEs.length);
        this.maxMemByNode = maxMemByNode;
        this.totalMemByNode = totalMemByNode;
        this.freeMemByNode = freeMemByNode;
        this.numNodes = freeMemByNode.length;
        initComponents();
    }


    /** Create a graph that lists cases per day and compute time. */
    private JPanel buildManagerChart() {

        //build casesPerDay dataset
        double[] yMaxMem = new double[numDays];
        double[] yTotalMem = new double[numDays];
        double[] yFreeMem = new double[numDays];
        double[] yNumOnces = new double[numDays];
        double[] xCoord = new double[numDays];
        for (int i = 0; i < xCoord.length; i++) {
            yMaxMem[i] = maxMem[i];
            yTotalMem[i] = totalMem[i];
            yFreeMem[i] = freeMem[i];
            yNumOnces[i] = numONCEs[i];
            xCoord[i] = i;
        }
        DefaultXYDataset memData = new DefaultXYDataset();
        memData.addSeries(
                (Comparable) "Max Mem",
                new double[][]{xCoord, yMaxMem});
        memData.addSeries(
                (Comparable) "Total Mem",
                new double[][]{xCoord, yTotalMem});
        memData.addSeries(
                (Comparable) "Free Mem",
                new double[][]{xCoord, yFreeMem});

        //build inital chart that uses above dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Memory and ONCEs Sent", //title
                "Day", //x axis label
                "Memory", //y axis label
                memData, //data
                PlotOrientation.VERTICAL,
                true, //show legend
                false, //show tooltips
                false //show urls
                );

        //get direct access to the plot - so we can add additional info
        XYPlot plot = chart.getXYPlot();

        //add 2nd Axis
        NumberAxis onceAxis = new NumberAxis("ONCE's Sent");
        plot.setRangeAxis(1, onceAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);	//put axis on right side

        //build the compute time dataset
        DefaultXYDataset onceData = new DefaultXYDataset();
        onceData.addSeries(
                (Comparable) "ONCEs Sent", new double[][]{xCoord, yNumOnces});

        //add the 2nd dataset
        plot.setDataset(1, onceData);
        plot.mapDatasetToRangeAxis(1, 1);	//tell the 2nd dataset to use the 2nd axis

        //add a 2nd renderer - so series are drawn differently
        XYItemRenderer rend = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(1, rend);

        return new ChartPanel(chart);
    }


    /** Create a Chart that displays memory variables of the various nodes. */
    private JPanel buildNodeChart(boolean drawFreeMem, boolean drawTotalMem, boolean drawMaxMem) {

        //gather xCoord
        double[] xCoord = new double[numDays];
        for (int i = 0; i < xCoord.length; i++) {
            xCoord[i] = i;
        }

        DefaultXYDataset memData = new DefaultXYDataset();

        if (drawFreeMem) {
            for (int i = 0; i < numNodes; i++) {

                double[] yTemp = new double[numDays];

                for (int j = 0; j < yTemp.length; j++) {
                    yTemp[j] = freeMemByNode[i][j];
                }

                memData.addSeries(
                        (Comparable) ("FreeMem_" + i), new double[][]{xCoord, yTemp});
            }
        }
        if (drawTotalMem) {
            for (int i = 0; i < numNodes; i++) {

                double[] yTemp = new double[numDays];

                for (int j = 0; j < yTemp.length; j++) {
                    yTemp[j] = totalMemByNode[i][j];
                }

                memData.addSeries(
                        (Comparable) ("TotalMem" + i), new double[][]{xCoord, yTemp});
            }
        }
        if (drawMaxMem) {
            for (int i = 0; i < numNodes; i++) {

                double[] yTemp = new double[numDays];

                for (int j = 0; j < yTemp.length; j++) {
                    yTemp[j] = maxMemByNode[i][j];
                }

                memData.addSeries(
                        (Comparable) ("MaxMem" + i), new double[][]{xCoord, yTemp});
            }
        }


        //build inital chart that uses above dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Memory", //title
                "Day", //x axis label
                "Memory", //y axis label
                memData, //data
                PlotOrientation.VERTICAL,
                true, //show legend
                false, //show tooltips
                false //show urls
                );

        //get direct access to the plot - so we can add additional info
        XYPlot plot = chart.getXYPlot();

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

        buttonGroup1 = new javax.swing.ButtonGroup();
        managerWrapper = this.buildManagerChart();
        nodeWrapper = new javax.swing.JPanel();
        displayWhichLabel = new javax.swing.JLabel();
        totalMemRB = new javax.swing.JRadioButton();
        maxMemRB = new javax.swing.JRadioButton();
        freeMemRB = new javax.swing.JRadioButton();
        nodeChart = this.buildNodeChart(true,false,false);

        managerWrapper.setBorder(javax.swing.BorderFactory.createTitledBorder("Model Manager Memory"));

        javax.swing.GroupLayout managerWrapperLayout = new javax.swing.GroupLayout(managerWrapper);
        managerWrapper.setLayout(managerWrapperLayout);
        managerWrapperLayout.setHorizontalGroup(
            managerWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 634, Short.MAX_VALUE)
        );
        managerWrapperLayout.setVerticalGroup(
            managerWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 257, Short.MAX_VALUE)
        );

        nodeWrapper.setBorder(javax.swing.BorderFactory.createTitledBorder("Node Memory"));

        displayWhichLabel.setText("Display Which?");

        buttonGroup1.add(totalMemRB);
        totalMemRB.setText("Total Mem");
        totalMemRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalMemRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(maxMemRB);
        maxMemRB.setText("Max Mem");
        maxMemRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxMemRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(freeMemRB);
        freeMemRB.setSelected(true);
        freeMemRB.setText("Free Mem");
        freeMemRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freeMemRBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout nodeChartLayout = new javax.swing.GroupLayout(nodeChart);
        nodeChart.setLayout(nodeChartLayout);
        nodeChartLayout.setHorizontalGroup(
            nodeChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 614, Short.MAX_VALUE)
        );
        nodeChartLayout.setVerticalGroup(
            nodeChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 222, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout nodeWrapperLayout = new javax.swing.GroupLayout(nodeWrapper);
        nodeWrapper.setLayout(nodeWrapperLayout);
        nodeWrapperLayout.setHorizontalGroup(
            nodeWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nodeWrapperLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nodeWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nodeChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(nodeWrapperLayout.createSequentialGroup()
                        .addComponent(displayWhichLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalMemRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxMemRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(freeMemRB)))
                .addContainerGap())
        );
        nodeWrapperLayout.setVerticalGroup(
            nodeWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nodeWrapperLayout.createSequentialGroup()
                .addGroup(nodeWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayWhichLabel)
                    .addComponent(totalMemRB)
                    .addComponent(maxMemRB)
                    .addComponent(freeMemRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(managerWrapper, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nodeWrapper, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(managerWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    /** Replace the noddeChart with the appropriate graph. */
private void totalMemRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalMemRBActionPerformed
    GroupLayout layout = (GroupLayout) nodeWrapper.getLayout();
    JPanel newChart = buildNodeChart(false, true, false);
    layout.replace(nodeChart, newChart);
    this.nodeChart = newChart;
}//GEN-LAST:event_totalMemRBActionPerformed


    /** Replace the noddeChart with the appropriate graph. */
private void maxMemRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxMemRBActionPerformed
    GroupLayout layout = (GroupLayout) nodeWrapper.getLayout();
    JPanel newChart = buildNodeChart(false, false, true);
    layout.replace(nodeChart, newChart);
    this.nodeChart = newChart;
}//GEN-LAST:event_maxMemRBActionPerformed


    /** Replace the noddeChart with the appropriate graph. */
private void freeMemRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freeMemRBActionPerformed
    GroupLayout layout = (GroupLayout) nodeWrapper.getLayout();
    JPanel newChart = buildNodeChart(true, false, false);
    layout.replace(nodeChart, newChart);
    this.nodeChart = newChart;
}//GEN-LAST:event_freeMemRBActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel displayWhichLabel;
    private javax.swing.JRadioButton freeMemRB;
    private javax.swing.JPanel managerWrapper;
    private javax.swing.JRadioButton maxMemRB;
    private javax.swing.JPanel nodeChart;
    private javax.swing.JPanel nodeWrapper;
    private javax.swing.JRadioButton totalMemRB;
    // End of variables declaration//GEN-END:variables
}
