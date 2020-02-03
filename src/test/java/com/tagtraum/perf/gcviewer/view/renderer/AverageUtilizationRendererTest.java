package com.tagtraum.perf.gcviewer.view.renderer;

import com.tagtraum.perf.gcviewer.math.IntData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AverageUtilizationRendererTest {
    private AverageUtilizationRenderer cut;

    @Mock
    private ModelChartImpl modelChart;

    @Mock
    private GCModel gcModel;

    @Before
    public void setUp() {
        cut = new AverageUtilizationRenderer(modelChart);
        cut.setSize(80, 100);

        when(modelChart.getScaleFactor()).thenReturn(1.0);
        when(modelChart.getFootprint()).thenReturn(100L);
        when(gcModel.getRunningTime()).thenReturn(80.0);

        IntData heap = new IntData();
        heap.add(100);
        when(gcModel.getHeapAllocatedSizes()).thenReturn(heap);
    }

    @Test
    public void whenNoEvents_polygonHasOnlyStartAndEndPoints() {
        List<AbstractGCEvent<?>> events = new ArrayList<>();
        when(gcModel.getStopTheWorldEvents()).thenReturn(events.iterator());

        Polygon polygon = cut.computePolygon(modelChart, gcModel);

        assertThat(polygon.npoints, is(2));
        assertPoints(polygon, p(0, 100), p(80,100));
    }

    @Test
    public void whenOneEvent_polygonHasValueAndOptimizationPoints() {
        List<AbstractGCEvent<?>> events = new ArrayList<>();
        events.add(createEvent(1.0, 6));
        when(gcModel.getStopTheWorldEvents()).thenReturn(events.iterator());


        Polygon polygon = cut.computePolygon(modelChart, gcModel);

        assertThat(polygon.npoints, is(5));
        int h = 60;
        assertPoints(polygon, p(0, 100), p(0, h),p(1, h),p(80, h), p(80,100));
    }

    @Test
    public void whenMultipleEvents_polygonHasValueAndOptimizationPoints() {
        List<AbstractGCEvent<?>> events = new ArrayList<>();
        events.add(createEvent(1.0, 3));
        events.add(createEvent(4.0, 3));
        events.add(createEvent(42.0, 3));
        events.add(createEvent(62.0, 3));
        when(gcModel.getStopTheWorldEvents()).thenReturn(events.iterator());

        Polygon polygon = cut.computePolygon(modelChart, gcModel);

        assertThat(polygon.npoints, is(8));
        assertPoints(polygon, p(0, 100), p(0, 80),p(1, 80),p(4,60), p(42, 40), p(62, 40),p(80, 40), p(80,100));
    }

    private static GCEvent createEvent(double timestamp, int pause) {
        GCEvent event = new GCEvent();
        event.setPause(pause);
        event.setTimestamp(timestamp);
        return event;
    }

    private void assertPoints(Polygon polygon, Point... points) {
        for(int i = 0; i < polygon.npoints; i++) {
            int actualX = polygon.xpoints[i];
            int actualY = polygon.ypoints[i];
            int expectedX = points[i].x;
            int expectedY = points[i].y;

            assertThat("x of point " + i, actualX, is(expectedX));
            assertThat("y of point " + i, actualY, is(expectedY));
        }
    }

    private static Point p(int x, int y) {
        return new Point(x, y);
    }
}
