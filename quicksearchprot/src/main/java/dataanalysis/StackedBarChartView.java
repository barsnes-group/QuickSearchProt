/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataanalysis;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author yfa041
 */
public class StackedBarChartView {
 private  Map<String,Double> confidentScores;

   
    private final Double[] values;
    JFreeChart chart;

    public JFreeChart getChart() {
        return chart;
    }

    public StackedBarChartView(String title, Double[][] data) {
        this.values = new Double[4];
//        try {
        // Create dataset
//            Thread t = new Thread(() -> {

        List<String> ratios = calculateQuartileRatios(data);
        CategoryDataset dataset = createDataset(data, ratios);
        String updatedTitle = title;

        // Create chart
        chart = createChart(dataset, updatedTitle);

        // Customize the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StackedBarRenderer renderer = new StackedBarRenderer();
        chart.setBackgroundPaint(Color.WHITE);
        plot.setRenderer(renderer);

        // Customize renderer
        renderer.setSeriesPaint(1, Color.GRAY.brighter());
        renderer.setSeriesPaint(0, Color.GREEN.darker().darker());

        // Remove bar shadows
        renderer.setShadowVisible(false);
        plot.setBackgroundPaint(Color.WHITE);

        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Remove any reflections or glossy effects by making sure no effects are applied
        renderer.setBarPainter(new StandardBarPainter());
        // Create Panel
//                ChartPanel panel = new ChartPanel(chart);
//                JFrame frame = new JFrame(updatedTitle);
//                frame.setSize(1000, 1000);
//                frame.setContentPane(panel);
//                frame.setVisible(true);
//                frame.repaint();
      
     

//            });
//            t.start();
//            t.join();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(StackedBarChartView.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private List<String> calculateQuartileRatios(Double[][] data) {
        double totalId = 0;
        double totalCount = 0;
        List<String> quartileRatios = new ArrayList<>();
        int quartIndex = 1;
        for (Double[] qData : data) {
            totalCount += qData[0];
            totalCount += qData[1];
            totalId += qData[0];
            double ratio = qData[0] / (qData[0] + qData[1]);
            ratio = Math.round(ratio * 100.0);
            quartileRatios.add("Q " + quartIndex + " (" + ratio + "%)");
            values[quartIndex - 1] = ratio;
            quartIndex++;
        }
//        double quat_3_sum = 0;
//        for (int j = 0; j < quartileRatios.size() - 1; j++) {
//            double ratio = quartileRatios.get(j) / totalCount;
//            ratio = Math.round(ratio * 100.0) / 100.0;
//            quartileRatios.set(j, ratio);
//            quat_3_sum += ratio;
//        }
//        double ratio = 1.0 - quat_3_sum;
//        ratio = Math.round(ratio * 100.0) / 100.0;
//        quartileRatios.set(quartileRatios.size() - 1, ratio);
        double totalIdRatio = totalId / totalCount;
        totalIdRatio = Math.round(totalIdRatio * 100.0);
        quartileRatios.add(totalIdRatio + "");
        return quartileRatios;

    }

    private CategoryDataset createDataset(Double[][] data, List<String> labels) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int i = 0;
        for (Double[] qData : data) {
            dataset.addValue(qData[0], "Identified", labels.get(i));
            dataset.addValue(qData[1], "UnIdentified", labels.get(i));
            i++;
        }
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset, String title) {
        return ChartFactory.createStackedBarChart(
                title, // Chart title
                "", // X-Axis Label
                "#Spectra", // Y-Axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Orientation
                true, // Include legend
                true, // Tooltips
                false // URLs?
        );
    }

    public Double[] getValues() {
        return values;
    }

    public Map<String,Double> getConfidentScores() {
        return confidentScores;
    }

    public void setConfidentScores(Map<String,Double> confidentScores) {
        this.confidentScores = confidentScores;
    }
}
