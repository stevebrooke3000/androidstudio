package ca.eonsound.esm;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.core.view.GestureDetectorCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.Series;

public class MyGraphView extends GraphView  {
    public MyGraphView(Context context) {
        super(context);
        vInit();
    }
    public MyGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        vInit();
    }
    public MyGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        vInit();
    }

    void vInit() {
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());

        Viewport viewport = getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        viewport.setScrollableY(false);
        viewport.setScalableY(false);
        viewport.setMaxXAxisSize(6000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        MyGestureListener() {
        }

          @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            vSetOptimal();
            return true; //super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            vSetOptimal();
            return true; //super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            java.util.List<Series> 	listSeries = getSeries();
            Series series = listSeries.get(0);
            double dMaxX = series.getHighestValueX();
            double dMinX = series.getLowestValueX();
            if (dMinX < dMaxX - 3000)
                dMinX = dMaxX - 3000;

            Viewport viewport = getViewport();
            viewport.setMinX(dMinX);
            viewport.setMaxX(dMaxX);
            viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(Settings.getInstance().getUnitsMax());
            onDataChanged(true, false);
        }
    }

    void vSetOptimal() {

        Viewport viewport = getViewport();

        java.util.List<Series> 	listSeries = getSeries();
        Series series = listSeries.get(0);

        if (series.isEmpty()) {
            viewport.setMinX(0);
            viewport.setMaxX(10);
            viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(Settings.getInstance().getUnitsMax());

            viewport.setYAxisBoundsManual(true);

            onDataChanged(true, false);
            return;
        }

        double dMaxY = series.getHighestValueY() * 1.05;
        double dMinY = series.getLowestValueY() * 0.95;
        double dMaxX = series.getHighestValueX();
        double dMinX = series.getLowestValueX();

        double dTime = dMaxX - dMinX;

        if (dTime < 1.0) {
            viewport.setMinX(0);
            viewport.setMaxX(10);
            viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(Settings.getInstance().getUnitsMax());
        }
        else if (dTime < 10.0) {
            viewport.setMinX(dMaxX - 10);
            viewport.setMaxX(dMaxX);
            viewport.setMinY(dMinY); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(dMaxY);
        }
        else {
            viewport.setMinX(dMinX);
            viewport.setMaxX(dMaxX);
            viewport.setMinY(dMinY); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(dMaxY);
        }

//        viewport.setYAxisBoundsManual(false);
        onDataChanged(true, false);


    }

    void vSetDefault() {
        Viewport viewport = getViewport();

        java.util.List<Series> 	listSeries = getSeries();
        Series series = listSeries.get(0);
        double dMaxX = series.getHighestValueX();

        viewport.setMinX(dMaxX - 10);
        viewport.setMaxX(dMaxX);
        viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
        viewport.setMaxY(Settings.getInstance().getUnitsMax());

    }
    private GestureDetectorCompat mDetector;

}
