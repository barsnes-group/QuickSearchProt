/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataanalysis;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author yfa041
 */
public class BarChartView {

    private Map<String, Double> confidentScores;

    private final Double[] values;
    JFreeChart chart;

    public JFreeChart getChart() {
        return chart;
    }

    public BarChartView(String title, Map<String, double[]> xtandData, Map<String, double[]> sageData) {
        this.values = new Double[4];
//        try {
        CategoryDataset dataset = createDataset(xtandData, sageData);
        String updatedTitle = title;
        // Create chart
        chart = createChart(dataset, updatedTitle);
        // Customize the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        // Customize renderer        
        plot.getRenderer().setSeriesPaint(0, new Color(124, 185, 232));
        plot.getRenderer().setSeriesPaint(2, new Color(0, 48, 143));

        plot.getRenderer().setSeriesPaint(1, Color.lightGray);
        plot.getRenderer().setSeriesPaint(3, Color.lightGray);
                
         plot.getRenderer().setSeriesOutlinePaint(10, Color.darkGray);
         plot.getRenderer().setSeriesOutlinePaint(1, Color.darkGray);
         plot.getRenderer().setSeriesOutlinePaint(2, Color.darkGray);
         plot.getRenderer().setSeriesOutlinePaint(3, Color.darkGray);
      
        // Remove bar shadows
        plot.setBackgroundPaint(Color.WHITE);

        // Create Panel
//        ChartPanel panel = new ChartPanel(chart);
//        JFrame frame = new JFrame(updatedTitle);
//        frame.setSize(1000, 1000);
//        frame.setContentPane(panel);
//        frame.setVisible(true);
//        frame.repaint();

//            });
//            t.start();
//            t.join();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(StackedBarChartView.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private CategoryDataset createDataset(Map<String, double[]> xtandData, Map<String, double[]> sageData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String id : xtandData.keySet()) {
            double[] subXtanData = xtandData.get(id);
            double[] subSageData = sageData.get(id);
            dataset.addValue(subXtanData[0], "Identified-X!Tandem", id.split("-")[0]);
            dataset.addValue(subXtanData[1], "UnIdentified-X!Tandem", id.split("-")[0]);
            dataset.addValue(subSageData[0], "Identified-Sage", id.split("-")[0]);
            dataset.addValue(subSageData[1], "UnIdentified-Sage", id.split("-")[0]);
        }
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset, String title) {
         chart = ChartFactory.createStackedBarChart(
                title, // Chart title
                "", // X-Axis Label
                "Identification Ratio (%)", // Y-Axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Orientation
                true, // Include legend
                true, // Tooltips
                false // URLs?
        );
        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
        KeyToGroupMap map = new KeyToGroupMap("G1");
        map.mapKeyToGroup("Identified-X!Tandem", "G1");
        map.mapKeyToGroup("UnIdentified-X!Tandem", "G1");
        map.mapKeyToGroup("Identified-Sage", "G2");
        map.mapKeyToGroup("UnIdentified-Sage", "G2");

        renderer.setSeriesToGroupMap(map);      

        renderer.setItemMargin(0.02);

//        renderer.setSeriesPaint(7, p4);
//        renderer.setSeriesPaint(11, p4);
//        renderer.setGradientPaintTransformer(
//                new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL)
//        );

        SubCategoryAxis domainAxis = new SubCategoryAxis("");
        domainAxis.setCategoryMargin(0.4);
        domainAxis.addSubCategory(" ");
        domainAxis.addSubCategory("  ");
        
        renderer.setShadowVisible(false);
        
        renderer.setBarPainter(new StandardBarPainter());
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainAxis(domainAxis);
        
        //plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        plot.setRenderer(renderer);
        plot.setFixedLegendItems(createLegendItems());
        chart.getLegend().setItemFont((DataAnalysisHandler.Default_Font));
        plot.getRangeAxis().setLabelFont((DataAnalysisHandler.Default_Font));
        domainAxis.setLabelFont(DataAnalysisHandler.Default_Font);

        return chart;
    }
 /**
     * Creates the legend items for the chart.  In this case, we set them manually because we
     * only want legend items for a subset of the data series.
     * 
     * @return The legend items.
     */
    private LegendItemCollection createLegendItems() {
        LegendItemCollection result = new LegendItemCollection();        
        LegendItem item1 = new LegendItem("Identified (X!Tandem)", new Color(124, 185, 232));
        LegendItem item2 = new LegendItem("Identified (Sage)", new Color(0, 48, 143));
        LegendItem item3 = new LegendItem("UnIdentified Confident tag", Color.lightGray);
        result.add(item1);
        result.add(item2);
        result.add(item3);
     
        return result;
    }
    public Double[] getValues() {
        return values;
    }

    public Map<String, Double> getConfidentScores() {
        return confidentScores;
    }

    public void setConfidentScores(Map<String, Double> confidentScores) {
        this.confidentScores = confidentScores;
    }
}
