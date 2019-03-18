import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import weka.core.Instances;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstration of a box-and-whisker chart using a {@link CategoryPlot}.
 *
 * @author David Browning
 */
public class BoxPlot{

    private ChartPanel chartPanel;

    public BoxPlot(Instances instances) {

        BoxAndWhiskerCategoryDataset dataset = createDataset(instances);
        CategoryAxis xAxis = new CategoryAxis("Numeric Attributes");
        NumberAxis yAxis = new NumberAxis("Values");
        yAxis.setAutoRangeIncludesZero(true);
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.gray);

        renderer.setSeriesPaint(0, Color.pink);
        renderer.setSeriesOutlinePaint(0, Color.red);
        renderer.setLegendTextFont(0, new Font("SansSerif", Font.PLAIN, 15));
        renderer.setFillBox(true);
        //renderer.setBaseOutlinePaint(Color.orange);
        renderer.setUseOutlinePaintForWhiskers(true);
        //renderer.setBaseFillPaint(Color.yellow);
        renderer.setMeanVisible(false);

        JFreeChart chart = new JFreeChart("Boxplots", new Font("SansSerif", Font.PLAIN, 14), plot, true);
        chart.setBackgroundPaint(Color.white);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(680, 350));
    }

    private BoxAndWhiskerCategoryDataset createDataset(Instances instances) {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        for (int att_indx = 0; att_indx < instances.numAttributes(); att_indx++) {
            if (!instances.attribute(att_indx).isDate() && instances.attribute(att_indx).isNumeric()) {
                List list = new ArrayList();
                for (int inst_indx = 0; inst_indx < instances.numInstances(); inst_indx++) {
                    list.add(instances.get(inst_indx).value(att_indx));
                }
                dataset.add(list, instances.relationName(), instances.attribute(att_indx).name());
            }
        }

        return dataset;
    }

    public ChartPanel getChart() {
        return chartPanel;
    }
}