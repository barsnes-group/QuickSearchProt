/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.arc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author yfa041
 */
public class FastaComparisonMirrorChart {

    public  FastaComparisonMirrorChart(String updatedTitle, String dataFolder,Map<String, ArrayList<Integer>> dataTypeValueMap,List<Integer>ratios,String SE) {
//        CombinedHorizontalChartExample();
        JFreeChart combinedChart1 = createLeftChart(dataTypeValueMap);
        JFreeChart combinedChart2 = createRightChart(ratios);

        // Create and display the chart in a panel
        ChartPanel chartPanel = new ChartPanel(combinedChart1);
        chartPanel.setSize(new Dimension(400, 600));
        chartPanel.setLocation(5, 5);
        combinedChart1.setPadding(RectangleInsets.ZERO_INSETS);
        combinedChart1.setBackgroundImageAlpha(0);

        ChartPanel chartPanel2 = new ChartPanel(combinedChart2);
        chartPanel2.setSize(new Dimension(400, 600));
        chartPanel2.setLocation(330, 5);

        JPanel contentPan = new JPanel(null);
        contentPan.setSize(new Dimension(800, 600));
        contentPan.add(chartPanel);
        contentPan.add(chartPanel2);
//
        JFrame frame = new JFrame(updatedTitle);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setContentPane(contentPan);
        frame.setVisible(true);
        frame.repaint();

        SVGGraphics2D g2 = new SVGGraphics2D(1060, 480);
        Rectangle r1 = new Rectangle(0, 20, 500, 400);

        Rectangle r2 = new Rectangle(435, 20, 500, 400);
        combinedChart2.draw(g2, r2);
        combinedChart1.draw(g2, r1);
        File sub_f = new File(dataFolder, "Similarity rate_BasedOn_FASTA_FILESIZE"  + "_" + SE + ".svg");
        try {
            SVGUtils.writeToSVG(sub_f, g2.getSVGElement());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private JFreeChart createLeftChart(Map<String, ArrayList<Integer>> dataTypeValueMap) {
        // Second dataset (inverted)
        CategoryDataset dataset2;

        dataset2 = createLeftSideDataset(dataTypeValueMap);
        // Create a bar chart with the first dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, // Chart title
                null, // X-axis Label
                "Value", // Y-axis Label
                dataset2 // Data
                ,
                 PlotOrientation.HORIZONTAL, true, true, true
        );
        // Get the plot object to customize it
        CategoryPlot plot = chart.getCategoryPlot();

        // Create a Y-axis for categories (shared axis in the middle)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.4); // Adjust to make room for both datasets
        domainAxis.setCategoryLabelPositionOffset(20);
        // First X-axis (right side, normal)
        String axLabel = "# Proteinsin the input FASTA file";
       
        NumberAxis xAxis1 = new NumberAxis(axLabel);
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);
        // Renderer for the first dataset (right-side bars)
        BarRenderer renderer1 = new BarRenderer();
        plot.setRenderer(0, renderer1);
        renderer1.setShadowVisible(false);
        // Customize appearance (colors, layout, etc.)
        renderer1.setSeriesPaint(0, new java.awt.Color(0, 100, 200));
        plot.getRangeAxis().setInverted(true);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);

        // Customize the chart
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StackedBarRenderer renderer = new StackedBarRenderer();  // Set maximum bar width
        renderer.setMaximumBarWidth(0.4); // Adjust as needed for more centered appearance

        // Customize renderer        
        renderer.setSeriesPaint(1, new Color(124, 185, 232));
        renderer.setSeriesPaint(0, new Color(0, 48, 143));
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {

                if (row == 0) {
                    String labelI = super.generateLabel(dataset, row, column).replace(" ", "").replace(",", ".");
                    double v2 = Double.parseDouble(labelI);
                    String labelII = super.generateLabel(dataset, 1, column).replace(" ", "").replace(",", ".");
                    double v1 = Double.parseDouble(labelII);
                    int ratio = (int) Math.round(v2 / v1);

                    return "1:" + ratio;

                }

                return "";
            }

        });
        renderer.setBaseItemLabelsVisible(true);  // Make sure item labels are visible
        // Optional: Adjust label font and visibility
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
        renderer.setBaseItemLabelPaint(Color.WHITE);

        // Remove bar shadows
        renderer.setShadowVisible(false);

        chart.setBackgroundPaint(Color.WHITE);
        plot.setRenderer(renderer);
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

//        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));
        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());

        return chart;
    }

    private JFreeChart createRightChart(List<Integer>ratios) {
        CategoryDataset dataset2;
        // Second dataset (inverted)
            dataset2 = createRightSideDataset(ratios);
       

        // Create a bar chart with the first dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, // Chart title
                null, // X-axis Label
                "Value", // Y-axis Label
                dataset2 // Data
                ,
                 PlotOrientation.HORIZONTAL, true, true, true
        );
        // Get the plot object to customize it
        CategoryPlot plot = chart.getCategoryPlot();
        // Create a Y-axis for categories (shared axis in the middle)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.4); // Adjust to make room for both datasets
        domainAxis.setVisible(true);
        domainAxis.setLabelPaint(Color.BLUE);
        // First X-axis (right side, normal)
        NumberAxis xAxis1 = new NumberAxis("Output similarity rate for subset identifications");
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);
        // Renderer for the first dataset (right-side bars)
        BarRenderer renderer1 = new BarRenderer() {

        };

        plot.setRenderer(0, renderer1);
        // Customize appearance (colors, layout, etc.)
        renderer1.setSeriesPaint(0, new java.awt.Color(0, 100, 200));
        renderer1.setShadowVisible(false);
        plot.getDomainAxis().setLabelPaint(Color.RED);
        // Customize the chart
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StackedBarRenderer renderer = new StackedBarRenderer();

        chart.setBackgroundPaint(Color.WHITE);
        plot.setRenderer(renderer);

        // Customize renderer        
        renderer.setSeriesPaint(0, new Color(0, 127, 255));
//        renderer.setSeriesPaint(1, new Color(124, 185, 232));
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                String label = super.generateLabel(dataset, row, column);
                return label + "%";
            }

        });
        renderer.setBaseItemLabelsVisible(true);  // Make sure item labels are visible
        // Optional: Adjust label font and visibility
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
        renderer.setBaseItemLabelPaint(Color.WHITE);

        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        // Remove bar shadows
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
//        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());

        return chart;
    }

    private CategoryDataset createRightSideDataset(List<Integer>ratios) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int counter = 1;
        for(double ratio:ratios){
         dataset.addValue(ratio, "Similarity rate %", "Dataset "+counter);
         counter++;
        }
        
        return dataset;
    }

    private CategoryDataset createLeftSideDataset(Map<String, ArrayList<Integer>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Adding data for Series 2
        for (String Type : data.keySet()) {
            ArrayList<Integer> values = data.get(Type);
            int counter = 1;
            for (int i : values) {
                dataset.addValue(i, Type, "Dataset " + counter);
                counter++;
            }

        }
        return dataset;
    }

    public static void main(String[] args) {

    }

}
