/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataanalysis;

import com.compomics.util.experiment.identification.Advocate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import org.jfree.text.TextBlock;
import org.jfree.text.TextLine;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author yfa041
 */
public class FASTAComparisonMirrorTimeFastaSize {

    private final JPanel contentPan = new JPanel(null);
    private final Map<Integer, int[]> bottomCategoriesLocation = new LinkedHashMap<>();
    private final Map<Integer, int[]> topCategoriesLocation = new LinkedHashMap<>();

    private double bottomAccessW = 0;
    private double topAccessW = 0;
    private JFreeChart combinedBottomChart;

    public FASTAComparisonMirrorTimeFastaSize(String updatedTitle, String dataFolder, Map<String, ArrayList<Double>> bottomChartDataTypeValueMap, Map<String, ArrayList<Double>> topChartDataTypeValueMap, String SE,int figNumber) {
        JFreeChart combinedTopChart = createTopChart(topChartDataTypeValueMap);
        combinedBottomChart = createBottomChart(bottomChartDataTypeValueMap, SE);
        combinedBottomChart.getLegend().setVisible(false);
        combinedTopChart.getLegend().setVisible(false);
        combinedBottomChart.setBackgroundPaint(Color.WHITE);
        // Create and display the chart in a panel
        ChartPanel bottomChartPanel = new ChartPanel(combinedBottomChart);
        bottomChartPanel.setSize(new Dimension(650, 300));
        bottomChartPanel.setLocation(5, 279);
        combinedBottomChart.setPadding(RectangleInsets.ZERO_INSETS);
        combinedBottomChart.setBackgroundImageAlpha(0);
        ChartPanel topChartPanel = new ChartPanel(combinedTopChart);
        topChartPanel.setSize(new Dimension(650, 300));
        topChartPanel.setLocation(5, 5);
        topChartPanel.setBackground(Color.WHITE);

        JPanel legend = createLegend();

//        legend.setLocation(174, 610);
//        contentPan.setSize(new Dimension(1500, 1000));
//        contentPan.add(bottomChartPanel, 0);
//        contentPan.add(topChartPanel, 1);
//        contentPan.setBackground(Color.WHITE);
//        contentPan.add(legend, 1);
////
//        JFrame frame = new JFrame(updatedTitle);
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setSize(1000, 1000);
//        frame.setContentPane(contentPan);
//        frame.setVisible(true);
//        frame.repaint();
        SVGGraphics2D g2 = new SVGGraphics2D(1000, 1000);
        Rectangle r1 = new Rectangle(5, 5, 650, 300);//new Rectangle(435, 20, 500, 400);
        Rectangle r2 = new Rectangle(5, 279, 650, 300);
//        Rectangle r3 = new Rectangle(405, 20, 200, 400);//new Rectangle(935, 20, 200, 400);

//        combinedChart3.draw(g2, r3); 
        combinedBottomChart.draw(g2, r2);
        combinedTopChart.draw(g2, r1); 
        combinedBottomChart.draw(g2, r2);

//        combinedBottomChart.setBackgroundPaint(Color.WHITE);
//        combinedTopChart.draw(g2, r1);
//       
        g2.translate(241, 589);// three item174
//        g2.translate(31, 86);
        legend.paint(g2);
        File sub_f = new File(DataAnalysisHandler.Export_Data_Folder, "Figure" + "_" + figNumber + ".svg");
        try {
            SVGUtils.writeToSVG(sub_f, g2.getSVGElement());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private JFreeChart createTopChart(Map<String, ArrayList<Double>> dataTypeValueMap) {
        CategoryDataset dataset2;
        // Second dataset (inverted)
        dataset2 = createTopChartDataset(dataTypeValueMap);
        // Create a bar chart with the first dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, // Chart title
                null, // X-axis Label
                null, // Y-axis Label
                dataset2 // Data
                ,
                 PlotOrientation.VERTICAL, true, true, true
        );
        // Get the plot object to customize it
        CategoryPlot plot = chart.getCategoryPlot();
        // Create a Y-axis for categories (shared axis in the middle)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.4); // Adjust to make room for both datasets
        domainAxis.setVisible(true);
//        domainAxis.setLabelPaint(Color.BLUE);
        domainAxis.setTickLabelPaint(new Color(0, 0, 0, 0));

        // First X-axis (right side, normal)s
        NumberAxis xAxis1 = new NumberAxis("# Protein sequences") {
            boolean dirty = true;
            AxisState as;

            @Override
            public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
                if (dirty) {
                    System.out.println("dirty?");
                    bottomAccessW = dataArea.getMinX() - plotArea.getMinX();
                    as = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
                    dirty = false;
                    combinedBottomChart.setPadding(new RectangleInsets(0, (bottomAccessW - topAccessW), 0, 0));
                    chart.fireChartChanged();

                } else {
                    as = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
                    System.out.println("need to repaint");
                    contentPan.repaint();
                }
                return as;
            }

        };
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);
        xAxis1.setTickMarksVisible(false);  // Set tick mark thickness

//        chart.setPadding(new RectangleInsets(0, 27, 0, 0));
        // Renderer for the first dataset (right-side bars)
        // Customize the chart
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        // Renderer for the first dataset (right-side bars)
        BarRenderer renderer = new BarRenderer();
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(1, new Color(124, 185, 232));
        renderer.setSeriesPaint(0, new Color(0, 48, 143));
//        renderer.setMaximumBarWidth(0.9);
        renderer.setItemMargin(0.05);
        plot.setRenderer(0, renderer);

        renderer.setBaseItemLabelsVisible(false);  // Make sure item labels are visible
        // Optional: Adjust label font and visibility
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 11)); // Set label font
        renderer.setBaseItemLabelPaint(Color.BLACK);

        // Remove bar shadows
        renderer.setShadowVisible(false);

        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setRenderer(renderer);
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        chart.getLegend().setItemPaint(new Color(0, 0, 0, 0));
        chart.getLegend().setBorder(0, 0, 0, 0);
        LegendTitle l = chart.getLegend();
        l.getItemContainer().setWidth(0);

//        chart.setTitle(new TextTitle(SE, new Font("SansSerif", Font.PLAIN, 11), Color.GRAY, RectangleEdge.BOTTOM, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, RectangleInsets.ZERO_INSETS));
//        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));
        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
//        BarRenderer renderer1 = new BarRenderer() {
//
//        };
//        plot.setRenderer(0, renderer1);
//        // Customize appearance (colors, layout, etc.)
//        renderer1.setSeriesPaint(0, new java.awt.Color(0, 100, 200));
//        renderer1.setShadowVisible(false);
//        plot.getDomainAxis().setLabelPaint(Color.RED);
//        // Customize the chart
////        CategoryPlot plot = (CategoryPlot) chart.getPlot();
//        StackedBarRenderer renderer = new StackedBarRenderer();
//
//        chart.setBackgroundPaint(Color.WHITE);
//        plot.setRenderer(renderer);
//
//        // Customize renderer        
//        renderer.setSeriesPaint(0, new Color(0, 127, 255));
////        renderer.setSeriesPaint(1, new Color(124, 185, 232));
//        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
//            @Override
//            public String generateLabel(CategoryDataset dataset, int row, int column) {
//                String label = super.generateLabel(dataset, row, column);
//                return label + "%";
//            }
//
//        });
//        renderer.setBaseItemLabelsVisible(true);  // Make sure item labels are visible
//        // Optional: Adjust label font and visibility
//        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
//        renderer.setBaseItemLabelPaint(Color.WHITE);
//
//        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
//        // Remove bar shadows
//        renderer.setShadowVisible(false);
//        plot.setBackgroundPaint(Color.WHITE);
//        plot.setOutlineVisible(false);
////        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));
//
//        // Remove any reflections or glossy effects by making sure no effects are applied
//        renderer.setBarPainter(new StandardBarPainter());
        return chart;
    }

    private CategoryDataset createTopChartDataset(Map<String, ArrayList<Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Adding data for Series 2

        int typeCounter = 0;
        for (String Type : data.keySet()) {

            typeCounter++;

            ArrayList<Double> values = data.get(Type);
            int counter = 1;
            for (double i : values) {
                dataset.addValue(i, Type, "Dataset " + counter);
                counter++;
            }
            if (typeCounter == 2) {
                break;
            }

        }
        return dataset;
    }

    private JFreeChart createBottomChart(Map<String, ArrayList<Double>> dataTypeValueMap, String SE) {
        // Second dataset (inverted)
        final CategoryDataset dataset = createBottomChartDataset(dataTypeValueMap);
        bottomCategoriesLocation.clear();
        // Create a bar chart with the first dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, // Chart title
                null, // X-axis Label
                null, // Y-axis Label
                dataset // Data
                ,
                 PlotOrientation.VERTICAL, true, true, true
        );
        // Get the plot object to customize it
        final CategoryPlot plot = chart.getCategoryPlot();
        // First X-axis (right side, normal)
        String axLabel = "Time in minutes (" + SE + ")";
        NumberAxis xAxis1 = new NumberAxis(axLabel) {

            @Override
            public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
                topAccessW = dataArea.getMinX() - plotArea.getMinX();
                return super.draw(g2, cursor, plotArea, dataArea, edge, plotState);

            }

        };
//         xAxis1.setTickMarkStroke(new BasicStroke(2.0f));    // Set tick mark thickness
        xAxis1.setTickMarksVisible(false);              // Length of tick marks (outside)
//        xAxis1.setTickMarkInsideLength(5.0f);
        chart.setPadding(new RectangleInsets(0, 0, 0, 0));
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);
        plot.getRangeAxis().setInverted(true);
        plot.getRangeAxis().setAutoRange(true);
        final Font f = new Font("SansSerif", Font.PLAIN, 12);

        // Create a Y-axis for categories (shared axis in the middle)
        CategoryAxis domainAxis = new CategoryAxis() {
            private final Color green = new Color(62, 132, 49);

            @Override
            protected TextBlock createLabel(Comparable category, float width, RectangleEdge edge, Graphics2D g2) {
                String catAsString = category.toString();
                g2.setFont(f);
                TextBlock tb1 = super.createLabel(catAsString.split("\n")[0].trim(), width, edge, g2);
                tb1.addLine(new TextLine(catAsString.split("\n")[1], f, green));
                return tb1;
            }
        };
        domainAxis.setTickLabelFont(f);

//                plot.getDomainAxis();
        plot.setDomainAxis(domainAxis);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        domainAxis.setCategoryMargin(0.4); // Adjust to make room for both datasets
        domainAxis.setVisible(true);
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4) // 45 degrees
        );// Loop through each category and dataset

        domainAxis.setMaximumCategoryLabelWidthRatio(0.6f); // Allow space for multi-line labels
        domainAxis.setMaximumCategoryLabelLines(2);
        BarRenderer renderer = new BarRenderer();
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(1, new Color(124, 185, 232));
        renderer.setSeriesPaint(0, new Color(0, 48, 143));
//        renderer.setMaximumBarWidth(0.9);
        renderer.setItemMargin(0.05);
        plot.setRenderer(0, renderer);

        renderer.setBaseItemLabelFont(f); // Set label font
        renderer.setBaseItemLabelPaint(Color.WHITE);

        // Remove bar shadows
        renderer.setShadowVisible(false);

        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setRenderer(renderer);
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

//        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));
        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter() {
            double minx = 0;
            double maxx = 0;

            @Override
            public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base) {
                super.paintBar(g2, renderer, row, column, bar, base); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
                double w = bar.getMaxX() - bar.getMinX();
                if (row == 0) {
                    minx = (w / -2.0) + bar.getCenterX();
//                    System.out.println("bar width  " + w + "    "+bar.getCenterX()+"  "+row+"  "+column+"   "+minX);
                } else {
                    maxx = (w / 2.0) + bar.getCenterX();
                    int barrWidth = (int) Math.round(maxx - minx + 4);
                    int[] values = new int[]{(int) Math.round(minx + 3), barrWidth};
                    bottomCategoriesLocation.put(column, values);            //     
                    System.out.println("<<<<>>>>>>bar width  " + barrWidth + "  x " + Math.round(minx + 3) + "   " + column + "   " + bottomCategoriesLocation.size() + "   " + values[0] + "   " + values[1]);
                }
                if (column == 5) {
//                    initSimilartyLabels();

                }

            }

        });
//        RectangleInsets margin = chart.getLegend().getMargin();
//        chart.getLegend().setMargin(margin.getTop(),300,margin.getBottom(),margin.getRight());

        return chart;
    }

    private CategoryDataset createBottomChartDataset(Map<String, ArrayList<Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Adding data for Series 2
        for (String Type : data.keySet()) {
            if (Type.equalsIgnoreCase("Similarty")) {
                continue;
            }
            ArrayList<Double> values = data.get(Type);
            int counter = 1;
            for (double i : values) {
                dataset.addValue(i, Type, "Dataset " + counter + "\n" + (data.get("Similarty").get(counter - 1).intValue()) + "%");
                counter++;
            }

        }
        return dataset;
    }

    private JPanel createLegend() {
        JPanel container = new JPanel(null);
        container.setSize(new Dimension(217, 30));//312, 30 (3 items
        container.setBorder(new LineBorder(Color.GRAY));
        container.setBackground(Color.WHITE);

        JPanel iconII = new JPanel();
        iconII.setSize(12, 12);
        iconII.setBackground(new Color(0, 48, 143));
        iconII.setLocation(10, 9);
//        iconII.setLocation(124, 9);
        container.add(iconII);
        JLabel itemII = new JLabel("Full FASTA");
//        itemII.setLocation(141, 5);
        itemII.setLocation(27, 5);
        itemII.setSize(65, 20);
        container.add(itemII);

        JPanel iconI = new JPanel();
        iconI.setSize(12, 12);
        iconI.setBackground(new Color(124, 185, 232));
        iconI.setLocation(102, 9);
        container.add(iconI);

        JLabel itemI = new JLabel("Filtered FASTA");
        itemI.setLocation(119, 5);
        itemI.setSize(88, 20);
        container.add(itemI);
//         itemII.setBorder(new LineBorder(Color.BLUE));
//        itemI.setBorder(new LineBorder(Color.BLUE));

//        JPanel iconIII = new JPanel();
//        iconIII.setSize(12, 12);
//        iconIII.setLocation(217, 9);
//        iconIII.setBackground(new Color(0, 145, 121));
//        container.add(iconIII);
//        JLabel itemIII = new JLabel("Similarity %");
//        itemIII.setLocation(234, 5);
//        itemIII.setSize(68, 20);
//        container.add(itemIII);
        return container;
    }

    public static void compareSubFASTAPerformance(String parentDataFolderURL) {
        try {
            Map<String, ArrayList<Double>> sageDsFullFASTASizeMap = new LinkedHashMap<>();
            sageDsFullFASTASizeMap.put("Full-Fasta-File", new ArrayList<>());
            sageDsFullFASTASizeMap.put("Filtered-Fasta-File", new ArrayList<>());
            Map<String, ArrayList<Double>> xtandDsFullFASTASizeMap = new LinkedHashMap<>();

            xtandDsFullFASTASizeMap.put("Full-Fasta-File", new ArrayList<>());
            xtandDsFullFASTASizeMap.put("Filtered-Fasta-File", new ArrayList<>());

            Map<String, ArrayList<Double>> sageDsFullFASTATimeMap = new LinkedHashMap<>();

            sageDsFullFASTATimeMap.put("Full-Fasta-File", new ArrayList<>());
            sageDsFullFASTATimeMap.put("Filtered-Fasta-File", new ArrayList<>());
            sageDsFullFASTATimeMap.put("Similarty", new ArrayList<>());

            Map<String, ArrayList<Double>> xtandDsFullFASTATimeMap = new LinkedHashMap<>();

            xtandDsFullFASTATimeMap.put("Full-Fasta-File", new ArrayList<>());
            xtandDsFullFASTATimeMap.put("Filtered-Fasta-File", new ArrayList<>());
            xtandDsFullFASTATimeMap.put("Similarty", new ArrayList<>());
            File timeValueFile = new File(parentDataFolderURL, "FastaSubFasta.txt");
            try (Scanner myReader = new Scanner(timeValueFile)) {
                boolean xtand = false;
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();

                    if (data.startsWith("Xtand")) {
                        xtand = true;
                        continue;
                    } else if (data.startsWith("Sage")) {
                        xtand = false;
                        continue;
                    }
                    String[] values = data.split("\\t");
                    if (xtand) {

                        xtandDsFullFASTATimeMap.get("Filtered-Fasta-File").add(Double.valueOf(values[0]));
                        xtandDsFullFASTATimeMap.get("Full-Fasta-File").add(Double.valueOf(values[1]));
                        xtandDsFullFASTATimeMap.get("Similarty").add(Double.valueOf(values[2]));
                        xtandDsFullFASTASizeMap.get("Filtered-Fasta-File").add(Double.valueOf(values[3]));
                        xtandDsFullFASTASizeMap.get("Full-Fasta-File").add(Double.valueOf(values[4]));
                    } else {
                        sageDsFullFASTATimeMap.get("Filtered-Fasta-File").add(Double.valueOf(values[0]));
                        sageDsFullFASTATimeMap.get("Full-Fasta-File").add(Double.valueOf(values[1]));
                        sageDsFullFASTATimeMap.get("Similarty").add(Double.valueOf(values[2]));

                        sageDsFullFASTASizeMap.get("Filtered-Fasta-File").add(Double.valueOf(values[3]));
                        sageDsFullFASTASizeMap.get("Full-Fasta-File").add(Double.valueOf(values[4]));
                    }
                }
            }
            FASTAComparisonMirrorTimeFastaSize XtandFastaComparisonMirrorChart = new FASTAComparisonMirrorTimeFastaSize("FASTA size based search", parentDataFolderURL, xtandDsFullFASTATimeMap, xtandDsFullFASTASizeMap, Advocate.xtandem.getName(),10);
            FASTAComparisonMirrorTimeFastaSize SageFastaComparisonMirrorChart = new FASTAComparisonMirrorTimeFastaSize("FASTA size based search", parentDataFolderURL, sageDsFullFASTATimeMap, sageDsFullFASTASizeMap, Advocate.sage.getName(),11);

        } catch (IOException ex) {
            Logger.getLogger(DataAnalysisHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        compareSubFASTAPerformance("D:\\Manuscripts\\manuscript 2024\\SearchTagsResults\\sub-ds");
    }

}
