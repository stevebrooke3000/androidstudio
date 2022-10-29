package ca.eonsound.esm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.core.view.GestureDetectorCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

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
            double dMaxX = getSeries().get(0).getHighestValueX();
            double dMinX = getSeries().get(0).getLowestValueX();
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

    int vSetOptimal() {

        Viewport viewport = getViewport();
        if (viewport == null)
            return -1;

        if (getSeries().get(0).isEmpty()) {
            viewport.setMinX(0);
            viewport.setMaxX(10);
            viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
            viewport.setMaxY(Settings.getInstance().getUnitsMax());

            viewport.setYAxisBoundsManual(true);

            onDataChanged(true, false);
            return 0;
        }

        double dMaxY = getSeries().get(0).getHighestValueY() * 1.05;
        double dMinY = getSeries().get(0).getLowestValueY() * 0.95;
        double dMaxX = getSeries().get(0).getHighestValueX();
        double dMinX = getSeries().get(0).getLowestValueX();

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

        return 1;
    }

    void vSetDefault() {
        Viewport viewport = getViewport();

        double dMaxX = getSeries().get(0).getHighestValueX();

        viewport.setMinX(dMaxX - 10);
        viewport.setMaxX(dMaxX);
        viewport.setMinY(0); // max Y is determined by the scale, see vUpdateScale
        viewport.setMaxY(Settings.getInstance().getUnitsMax());

    }
    private GestureDetectorCompat mDetector;

}
