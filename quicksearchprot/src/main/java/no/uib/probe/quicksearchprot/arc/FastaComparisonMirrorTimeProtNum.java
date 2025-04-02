/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.arc;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import dataanalysis.DataAnalysisHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author yfa041
 */
public class FastaComparisonMirrorTimeProtNum {

    public FastaComparisonMirrorTimeProtNum(String updatedTitle, String dataFolder, Map<String, ArrayList<Integer>> leftDataTypeValueMap, Map<String, ArrayList<Double>> rightDataTypeValueMap, String SE) {

        JFreeChart combinedChart1 = createLeftChart(leftDataTypeValueMap);
        JFreeChart combinedChart2 = createRightChart(rightDataTypeValueMap,SE);

        JFreeChart combinedChart3 = createFarRightChart(rightDataTypeValueMap);

        combinedChart1.getLegend().setVisible(false);
        combinedChart2.getLegend().setVisible(false);
        combinedChart3.getLegend().setVisible(false);

        // Create and display the chart in a panel
        ChartPanel chartPanel = new ChartPanel(combinedChart1);
        chartPanel.setSize(new Dimension(400, 600));
        chartPanel.setLocation(5, 5);
        combinedChart1.setPadding(RectangleInsets.ZERO_INSETS);
        combinedChart1.setBackgroundImageAlpha(0);

        ChartPanel chartPanel2 = new ChartPanel(combinedChart2);
        chartPanel2.setSize(new Dimension(400, 600));
        chartPanel2.setLocation(330, 5);

        ChartPanel chartPanel3 = new ChartPanel(combinedChart3);
        chartPanel3.setSize(new Dimension(100, 600));
        chartPanel3.setLocation(720, 5);

        JPanel legend = createLegend();
        legend.setLocation(266, 610);

        JPanel contentPan = new JPanel(null);
        contentPan.setSize(new Dimension(1000, 1000));
        contentPan.add(chartPanel);
        contentPan.add(chartPanel2);
        contentPan.add(chartPanel3);
        contentPan.add(legend);

//
        JFrame frame = new JFrame(updatedTitle);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setContentPane(contentPan);
        frame.setVisible(true);
        frame.repaint();
//        SVGGraphics2D g2 = new SVGGraphics2D(1029, 480);
//        Rectangle r1 = new Rectangle(0, 20, 500, 400);
//
//        Rectangle r2 = new Rectangle(519, 20, 500, 400);//new Rectangle(435, 20, 500, 400);
//
//        Rectangle r3 = new Rectangle(405, 20, 200, 400);//new Rectangle(935, 20, 200, 400);
//
////      
//        combinedChart3.draw(g2, r3);
//        combinedChart1.draw(g2, r1);
//        combinedChart2.draw(g2, r2);
////        g2.translate(390, 425);
//        g2.translate(31, 365);
//        legend.paint(g2);
//
//        File sub_f = new File(dataFolder, "PerformaceComparison_BasedOn_FASTA_FILESIZE" + "_" + SE + ".svg");
//        try {
//            SVGUtils.writeToSVG(sub_f, g2.getSVGElement());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }

    private JFreeChart createFarRightChart(Map<String, ArrayList<Double>> dataTypeValueMap) {
        CategoryDataset dataset2;
        // Second dataset (inverted)
        dataset2 = createFarRightSideDataset(dataTypeValueMap);
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
        domainAxis.setVisible(false);
        domainAxis.setMaximumCategoryLabelLines(2);

        //to remove
        // First X-axis (right side, normal)s
        NumberAxis xAxis1 = new NumberAxis(" ");// new NumberAxis("Output Similarity Rate %");
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);

        xAxis1.setTickMarkPaint(Color.WHITE);
        xAxis1.setTickLabelPaint(Color.WHITE);
        xAxis1.setAxisLinePaint(Color.WHITE);

        // Renderer for the first dataset (right-side bars)
        // Customize the chart
        StackedBarRenderer renderer = new StackedBarRenderer();


        renderer.setShadowVisible(false);
//        renderer.setSeriesPaint(0, new Color(0, 145, 121));
        renderer.setSeriesPaint(0, new Color(0, 0, 0, 0));
  
        renderer.setItemMargin(0.05);
        plot.setRenderer(0, renderer);

        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
            int counter = 0;

            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                int sim = dataTypeValueMap.get("Similarty").get(counter++).intValue();
                return "Dataset " + counter + " (Similarity " + sim + " %)";
            }

        });
        renderer.setBaseItemLabelsVisible(true);  // Make sure item labels are visible

        // Optional: Adjust label font and visibility
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
        renderer.setBaseItemLabelPaint(Color.DARK_GRAY);

        // Remove bar shadows
        renderer.setShadowVisible(false);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setRenderer(renderer);
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setOutlineVisible(false);
        chart.setBorderVisible(false);
        chart.getLegend().setItemPaint(new Color(0, 0, 0, 0));
        chart.getLegend().setBorder(0, 0, 0, 0);
        LegendTitle l = chart.getLegend();
        l.getItemContainer().setWidth(0);

        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());
        return chart;
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
        // First X-axis (right side, normal)
        String axLabel = "# Proteins in input FASTA file";
        NumberAxis xAxis1 = new NumberAxis(axLabel);
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0, 0);
        plot.getRangeAxis().setInverted(true);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);

        // Create a Y-axis for categories (shared axis in the middle)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.4); // Adjust to make room for both datasets
        domainAxis.setVisible(true);
//        domainAxis.setLabelPaint(Color.BLUE);
        domainAxis.setCategoryLabelPositionOffset(20);
        domainAxis.setTickLabelPaint(new Color(0, 0, 0, 0));

        // Renderer for the first dataset (right-side bars)
        BarRenderer renderer = new BarRenderer();

        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(124, 185, 232));
        renderer.setSeriesPaint(1, new Color(0, 48, 143));
//        renderer.setMaximumBarWidth(0.9);
        renderer.setItemMargin(0.05);

        plot.setRenderer(0, renderer);

        // Customize the chart
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        // Adjust as needed for more centered appearance
        // Customize renderer        
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

        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setRenderer(renderer);
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

//        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));
        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());
//        RectangleInsets margin = chart.getLegend().getMargin();
//        chart.getLegend().setMargin(margin.getTop(),300,margin.getBottom(),margin.getRight());

        return chart;
    }

    private JFreeChart createRightChart(Map<String, ArrayList<Double>> dataTypeValueMap,String SE) {
        CategoryDataset dataset2;
        // Second dataset (inverted)
        dataset2 = createRightSideDataset(dataTypeValueMap);
        // Create a bar chart with the first dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, // Chart title
                null, // X-axis Label
                null, // Y-axis Label
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
//        domainAxis.setLabelPaint(Color.BLUE);
        domainAxis.setTickLabelPaint(new Color(0, 0, 0, 0));

        // First X-axis (right side, normal)s
        NumberAxis xAxis1 = new NumberAxis("Pick-up parameters processing time in minutes ("+SE+")");
        plot.setRangeAxis(0, xAxis1); // Right-side axis for dataset1
        plot.mapDatasetToRangeAxis(0,0);
        // Renderer for the first dataset (right-side bars)

        // Customize the chart
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        // Renderer for the first dataset (right-side bars)
        BarRenderer renderer = new BarRenderer();

        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(124, 185, 232));
        renderer.setSeriesPaint(1, new Color(0, 48, 143));
//        renderer.setMaximumBarWidth(0.9);
        renderer.setItemMargin(0.05);

        plot.setRenderer(0, renderer);
//        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
//            int counter = 0;
//            @Override
//            public String generateLabel(CategoryDataset dataset, int row, int column) {
//
//                if (row == 0) {
//                    return "";
//                }
//                int sim = dataTypeValueMap.get("Similarty").get(counter++).intValue();
//                System.out.println("row " + row + "  colum " + column + "  " + dataset.getValue(row, column));
//
//                return "      Similarity " + sim + " %";
//            }
//
//        });
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

    private CategoryDataset createRightSideDataset(Map<String, ArrayList<Double>> data) {
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

    private CategoryDataset createFarRightSideDataset(Map<String, ArrayList<Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Adding data for Series 2

        int typeCounter = 0;
        for (String Type : data.keySet()) {
            if (typeCounter < 2) {
                typeCounter++;
                continue;
            }

            ArrayList<Double> values = data.get(Type);
            int counter = 1;
            for (double i : values) {
                dataset.addValue(100.0, Type, "Dataset " + counter);
                counter++;
            }

        }
        return dataset;
    }

    private JPanel createLegend() {
        JPanel container = new JPanel(null);
        container.setSize(new Dimension(217, 30));//372
        container.setBorder(new LineBorder(Color.GRAY));
        container.setBackground(Color.WHITE);
        JPanel iconI = new JPanel();
        iconI.setSize(12, 12);
        iconI.setBackground(new Color(124, 185, 232));
        iconI.setLocation(10, 9);
        container.add(iconI);

        JLabel itemI = new JLabel("Filtered FASTA");
        itemI.setLocation(27, 5);
        itemI.setSize(87, 20);
        container.add(itemI);

        JPanel iconII = new JPanel();
        iconII.setSize(12, 12);
        iconII.setBackground(new Color(0, 48, 143));
        iconII.setLocation(124, 9);
        container.add(iconII);
        JLabel itemII = new JLabel("Full FASTA");
        itemII.setLocation(141, 5);
        itemII.setSize(66, 20);
        container.add(itemII);

//        JPanel iconIII = new JPanel();
//        iconIII.setSize(12, 12);
//        iconIII.setLocation(217, 9);
//        iconIII.setBackground(new Color(0, 145, 121));
//        container.add(iconIII);
//
//        JLabel itemIII = new JLabel("Output Similarity Rate");
//        itemIII.setLocation(234, 5);
//        itemIII.setSize(128, 20);
//        container.add(itemIII);
//        itemIII.setBorder(new LineBorder(Color.BLUE));
//        itemII.setBorder(new LineBorder(Color.BLUE));
//        itemI.setBorder(new LineBorder(Color.BLUE));

        return container;
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

    public static void compareSubFastaPerformance(String parentDataFolderURL) {
        try {
            File parentDataFolder = new File(parentDataFolderURL);
            Set<String> datasetName = new LinkedHashSet<>();
            datasetName.add("DS1-optsearch_results_TA_qExactive01819");    //1
            datasetName.add("DS2-optsearch_results_TA_Adult_CD8Tcells_Gel_Elite_44_f24");    //2
            datasetName.add("DS3-optsearch_results_TA_b1948_293T_proteinID_12B_QE3_122212");          //3
            datasetName.add("DS4-optsearch_results_TA_Nor_NTC_2");        //4
            datasetName.add("DS5-optsearch_results_TA_JurkatShotgunProteomicsReplicate3");        //5
            datasetName.add("DS6-optsearch_results_TA_20140711_EXQ00_KiSh_SA_Brain_4");        //6

//            
            Map<String, ArrayList<Integer>> sageDsFullFastaSizeMap = new LinkedHashMap<>();
//            List<Integer> xtandRatios = new ArrayList<>();
            sageDsFullFastaSizeMap.put("Filtered FASTA File", new ArrayList<>());
            sageDsFullFastaSizeMap.put("Whole FASTA File", new ArrayList<>());

            Map<String, ArrayList<Integer>> xtandDsFullFastaSizeMap = new LinkedHashMap<>();
//            List<Integer> xtandRatios = new ArrayList<>();
            xtandDsFullFastaSizeMap.put("Filtered FASTA File", new ArrayList<>());
            xtandDsFullFastaSizeMap.put("Whole FASTA File", new ArrayList<>());

            for (String datasetfId : datasetName) {
                String datasetId = datasetfId.split("-")[1];
                File dataFolder = new File(parentDataFolder, datasetId);
                /**
                 * *FASTA files *
                 */
                File fastaFull = new File(dataFolder, datasetId + "_full.fasta");
                File fastaXtandSub = new File(dataFolder, datasetId + "_xtand.fasta");

                FastaIterator fastaIterator = new FastaIterator(fastaFull);
                int counter = 0;
                while ((fastaIterator.getNextProtein()) != null) {
                    counter++;
                }
                System.out.println(datasetId+"-->fasta full size " + counter);
                xtandDsFullFastaSizeMap.get("Whole FASTA File").add(counter / 2);
                fastaIterator = new FastaIterator(fastaXtandSub);
                counter = 0;
                while ((fastaIterator.getNextProtein()) != null) {
                    counter++;
                }
                System.out.println(datasetId+" fasta sub size " + counter);
                xtandDsFullFastaSizeMap.get("Filtered FASTA File").add(counter);

                /* sage space */
                /**
                 * *FASTA files *
                 */
                File fastaSageSub = new File(dataFolder, datasetId + "_sage.fasta");

                fastaIterator = new FastaIterator(fastaFull);
                counter = 0;
                while ((fastaIterator.getNextProtein()) != null) {
                    counter++;
                }
                System.out.println(datasetId+"--->fasta full size " + counter);
                sageDsFullFastaSizeMap.get("Whole FASTA File").add(counter / 2);
                fastaIterator = new FastaIterator(fastaSageSub);
                counter = 0;
                while ((fastaIterator.getNextProtein()) != null) {
                    counter++;
                }
                System.out.println(datasetId+" fasta sub size " + counter);
                sageDsFullFastaSizeMap.get("Filtered FASTA File").add(counter / 2);
//             
            }

            Map<String, ArrayList<Double>> sageDsFullFastaTimeMap = new LinkedHashMap<>();
//            List<Integer> xtandRatios = new ArrayList<>();
            sageDsFullFastaTimeMap.put("Filtered FASTA File", new ArrayList<>());
            sageDsFullFastaTimeMap.put("Whole FASTA File", new ArrayList<>());
            sageDsFullFastaTimeMap.put("Similarty", new ArrayList<>());

            Map<String, ArrayList<Double>> xtandDsFullFastaTimeMap = new LinkedHashMap<>();
//            List<Integer> xtandRatios = new ArrayList<>();
            xtandDsFullFastaTimeMap.put("Filtered FASTA File", new ArrayList<>());
            xtandDsFullFastaTimeMap.put("Whole FASTA File", new ArrayList<>());
            xtandDsFullFastaTimeMap.put("Similarty", new ArrayList<>());
            System.out.println("data sets names are : " + datasetName);
            File timeValueFile = new File(parentDataFolderURL, "FastaSubFasta.txt");
            System.out.println("fasta number exist " + timeValueFile.exists());
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
                        xtandDsFullFastaTimeMap.get("Filtered FASTA File").add(Double.valueOf(values[0]));
                        xtandDsFullFastaTimeMap.get("Whole FASTA File").add(Double.valueOf(values[1]));
                        xtandDsFullFastaTimeMap.get("Similarty").add(Double.valueOf(values[2]));
                    } else {
                        sageDsFullFastaTimeMap.get("Filtered FASTA File").add(Double.valueOf(values[0]));
                        sageDsFullFastaTimeMap.get("Whole FASTA File").add(Double.valueOf(values[1]));
                        sageDsFullFastaTimeMap.get("Similarty").add(Double.valueOf(values[2]));
                    }
                }
            }
            FastaComparisonMirrorTimeProtNum XtandFastaComparisonMirrorChart = new FastaComparisonMirrorTimeProtNum("Fasta based search", parentDataFolderURL, xtandDsFullFastaSizeMap, xtandDsFullFastaTimeMap, Advocate.xtandem.getName());
            FastaComparisonMirrorTimeProtNum SageFastaComparisonMirrorChart = new FastaComparisonMirrorTimeProtNum("Fasta based search", parentDataFolderURL, sageDsFullFastaSizeMap, sageDsFullFastaTimeMap, Advocate.sage.getName());

        } catch (IOException ex) {
            Logger.getLogger(DataAnalysisHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        compareSubFastaPerformance("D:\\Manuscripts\\manuscript 2024\\SearchTagsResults\\sub-ds");
    }

}
