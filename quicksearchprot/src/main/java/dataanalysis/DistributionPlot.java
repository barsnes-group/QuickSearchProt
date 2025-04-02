/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataanalysis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;

/**
 *
 * @author yfa041
 */
public class DistributionPlot extends JFrame {

    public DistributionPlot(String title, Map<String, Double> referencePsmScores, Map<String, Double> testPsmScores) {
        super(title);

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String str : referencePsmScores.keySet()) {
            dataset.addValue(referencePsmScores.get(str), "Reference", str);
        }

        for (String str : testPsmScores.keySet()) {
//            dataset.addValue(testPsmScores.get(str), "Test", str);
        }

        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Data Distribution",
                "X-Axis",
                "Y-Axis",
                dataset, PlotOrientation.VERTICAL,
                true, // Include legend
                true, // Tooltips
                false // URLs?
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        chart.setBackgroundPaint(Color.WHITE);
        // Remove bar shadows
        plot.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Customize the chart (optional)
        // ...
        // Add chart to a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

        SVGGraphics2D sub_g2 = new SVGGraphics2D(1020, 1020);
        Rectangle r3 = new Rectangle(20, 20, 1000, 1000);
        chart.draw(sub_g2, r3);
        String datafolder = "D:\\test";
        File sub_f = new File(datafolder, "(" + title + ").svg");
        try {
            SVGUtils.writeToSVG(sub_f, sub_g2.getSVGElement());
        } catch (IOException ex) {
            Logger.getLogger(StackedBarChartView.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Create dataset
    }

    public static synchronized void generateDistributionPlot(String title, double[] referencePsmScores, double[] testPsmScores) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Reference", referencePsmScores, 100);
   

        HistogramDataset dataset2 = new HistogramDataset();
        dataset.addSeries("Test", testPsmScores, 150);

        // Create chart
        JFreeChart histogram = ChartFactory.createHistogram(
                "Score Distribution",
                "Score",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Customize the plot to add the second dataset
        XYPlot plot = (XYPlot) histogram.getPlot(); 
        XYBarRenderer renderer1 = new XYBarRenderer();
        XYBarRenderer renderer2 = new XYBarRenderer();

        // Set colors for the datasets
        renderer1.setSeriesPaint(0, Color.ORANGE);
        renderer1.setSeriesPaint(1, Color.BLACK);

        plot.setRenderer(0, renderer1);
        plot.setRenderer(1, renderer2);
        plot.setDataset(1, dataset2);

        // Customize the renderer to remove shadows (optional)
        renderer1.setShadowVisible(false);
        renderer2.setShadowVisible(false);

       
//        plot.setDataset(1, dataset2);

        // Customize the renderer to remove shadows (optional)
    

       
//        XYPlot plot = (XYPlot) histogram.getPlot();
        histogram.setBackgroundPaint(Color.WHITE);
        // Remove bar shadows
        // Customize the renderer to remove shadows
//        CategoryPlot cplot = histogram.getCategoryPlot();
//        BarRenderer renderer = (BarRenderer) cplot.getRenderer();
//        renderer.setShadowVisible(false);
//        cplot.setBackgroundPaint(Color.WHITE);
        histogram.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Customize the chart (optional)
        SVGGraphics2D sub_g2 = new SVGGraphics2D(1020, 1020);
        Rectangle r3 = new Rectangle(20, 20, 1000, 1000);
        histogram.draw(sub_g2, r3);
        String datafolder = "D:\\test";
        File sub_f = new File(datafolder, "(" + title + ").svg");
        try {
            SVGUtils.writeToSVG(sub_f, sub_g2.getSVGElement());
            System.out.println("done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public DistributionPlot(String title, double[] referencePsmScores, double[] testPsmScores) {
        super(title);

        // Create dataset
        HistogramDataset dataset = new HistogramDataset();
        double[] scores = {85, 90, 78, 92, 88, 76, 95, 89, 84, 91};
        dataset.addSeries("Reference", referencePsmScores, 100);
        dataset.addSeries("Test", testPsmScores, 100);

        // Create chart
        JFreeChart histogram = ChartFactory.createHistogram(
                "Score Distribution",
                "Score",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true, // Include legend
                true, // Tooltips
                false // URLs?
        );
//        XYPlot plot = (XYPlot) histogram.getPlot();
        histogram.setBackgroundPaint(Color.WHITE);
        // Remove bar shadows
        // Customize the renderer to remove shadows
        CategoryPlot cplot = histogram.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) cplot.getRenderer();
        renderer.setShadowVisible(false);
        cplot.setBackgroundPaint(Color.WHITE);
        histogram.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Customize the chart (optional)
        // ...
        // Add chart to a panel
        ChartPanel chartPanel = new ChartPanel(histogram);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

        SVGGraphics2D sub_g2 = new SVGGraphics2D(1020, 1020);
        Rectangle r3 = new Rectangle(20, 20, 1000, 1000);
        histogram.draw(sub_g2, r3);
        String datafolder = "D:\\test";
        File sub_f = new File(datafolder, "(" + title + ").svg");
        try {
            SVGUtils.writeToSVG(sub_f, sub_g2.getSVGElement());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Create dataset
    }

    public static void main(String[] args) {
//        DistributionPlot example = new DistributionPlot("Histogram Example");
//        example.setSize(800, 600);
//        example.setLocationRelativeTo(null);
//        example.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        example.setVisible(true);
    }
}
