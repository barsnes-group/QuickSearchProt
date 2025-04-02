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
public class LineChartView {


    JFreeChart chart;

    public JFreeChart getChart() {
        return chart;
    }

    public LineChartView(String title, String[] labels, Map<String, Double[]> dataMap) {  
        CategoryDataset dataset = createDataset(labels, dataMap);
        String updatedTitle = title;
        // Create chart
        chart = createChart(dataset, updatedTitle);
        // Customize the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        chart.setBackgroundPaint(Color.WHITE);
        // Remove bar shadows
        plot.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Times New Roman", Font.PLAIN, 14));

    }

    private CategoryDataset createDataset(String[] labels, Map<String, Double[]> dataMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i=0;i<labels.length;i++) {
            String label = labels[i];
            for (String id : dataMap.keySet()) {
                    dataset.addValue(dataMap.get(id)[i], id, label);
            }
        }
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset, String title) {
        return ChartFactory.createLineChart(
                title, // Chart title
                "", // X-Axis Label
                "Id ratios %", // Y-Axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Orientation
                true, // Include legend
                true, // Tooltips
                false // URLs?
        );
    }

   
}
