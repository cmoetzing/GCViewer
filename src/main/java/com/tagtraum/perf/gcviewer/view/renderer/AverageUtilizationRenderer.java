package com.tagtraum.perf.gcviewer.view.renderer;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

import java.awt.Paint;
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AverageUtilizationRenderer extends PolygonChartRenderer {
    private static final double AVERAGING_TIME_FRAME = 60.0;

    public static final double MAGNIFICATION_FACTOR = 4.0;
    public static final Paint DEFAULT_LINE_PAINT;
    public static final Paint DEFAULT_FILL_PAINT;

    static {
        DEFAULT_LINE_PAINT = new Color(0, 64, 0);
        DEFAULT_FILL_PAINT = new Color(0, 128, 0);
    }

    public AverageUtilizationRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setFillPaint(DEFAULT_FILL_PAINT);
        setLinePaint(DEFAULT_LINE_PAINT);
        setDrawPolygon(true);
        setDrawLine(true);
    }

    @Override
    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        polygon.addPoint(0.0d, 0.0d);

        int chartMaxY = model.getHeapAllocatedSizes().getMax();

        List<AbstractGCEvent<?>> averagedEvents = new ArrayList<>();
        double utilization = 0.0;
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext();) {
            AbstractGCEvent<?> event = i.next();
            averagedEvents.add(event);
            
            double x = event.getTimestamp() - model.getFirstPauseTimeStamp();
            double firstValidTimestamp = event.getTimestamp() - AVERAGING_TIME_FRAME;
            double average = getAverageUtilization(averagedEvents, firstValidTimestamp);
            utilization = chartMaxY * average * MAGNIFICATION_FACTOR;
            if (polygon.npoints == 1) {
                polygon.addPoint(0, utilization);
            }
            polygon.addPoint(x, utilization);

            if(!i.hasNext()) {
                polygon.addPointNotOptimised(model.getRunningTime(), utilization);
            }
        }
        polygon.addPointNotOptimised(model.getRunningTime(), 0.0d);

        return polygon;
    }

    private double getAverageUtilization(List<AbstractGCEvent<?>> events, double firstValidTimestamp) {
        Double pause = 0.0;
        for (Iterator<AbstractGCEvent<?>> i = events.iterator(); i.hasNext();) {
            AbstractGCEvent<?> event = i.next();
            if(event.getTimestamp() < firstValidTimestamp) {
                i.remove();
            } else {
                pause += event.getPause();
            }
        }
        return pause / AVERAGING_TIME_FRAME;
    }

}
