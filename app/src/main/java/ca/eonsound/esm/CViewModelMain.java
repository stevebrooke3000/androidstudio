package ca.eonsound.esm;

import android.content.Context;
import android.graphics.Color;
//import android.os.Environment;

import androidx.lifecycle.ViewModel;

import java.io.*;
//import java.io.Serializable;
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

public class CViewModelMain extends ViewModel {
    // MPR Transfer function "B" 2.5% to 22.5% of 2^24 counts
    private static final double kOmin = 0.025 * (double) (1 << 24);
    private static final double kOmax = 0.225 * (double) (1 << 24);
    // MPR Gauge: 0 mmHg to 300 mmHg
    private static final double kPmax = 300.0;
    private static final double kPmin = 0.0;

    //private String mDeviceAddress;

    private CVecRawPressure vecRawPressure;

    private LineGraphSeries<DataPoint> seriesLine; // the data series for the line plot
    private LineGraphSeries<DataPoint> seriesMax;
    private LineGraphSeries<DataPoint> seriesMin;
    private BarGraphSeries<DataPoint> seriesBar; // the data series for the instantaneous pressure display
    private CHistogramSeries seriesHist; // for the histogram analysis plot

    private CFilter filter = new CFilter(20);

    private double dDelayCountdown_s;
    //private boolean bModeAcq;
    private double dMaxPressure;
    private double dMinPressure;
    private double dTime = 0;
    private double dStartTime = 0;

    enum EState {kIdle, kAcq, kHistRdy, kStart};
    EState eState = EState.kIdle;


/*    public void setDeviceAddress(String _devAddr) {
        mDeviceAddress = _devAddr;
    }
    public String getDeviceAddress() {
        return mDeviceAddress;
    }

 */

    public CVecRawPressure getVecRawPressure() {
        if (vecRawPressure == null)
            vecRawPressure = new CVecRawPressure();
        return vecRawPressure;
    }
    public LineGraphSeries<DataPoint> getSeriesLine() {
        if (seriesLine == null) {
            seriesLine = new LineGraphSeries<>();
            seriesLine.setColor(Color.BLUE);
            seriesLine.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {

                }
            });
        }
        return seriesLine;
    }
    public LineGraphSeries<DataPoint> getSeriesMax() {
        if (seriesMax == null) {
            seriesMax = new LineGraphSeries<>();
            seriesMax.setColor(Color.RED);
            dMaxPressure = 0;
        }
        return seriesMax;
    }
    public LineGraphSeries<DataPoint> getSeriesMin() {
        if (seriesMin == null) {
            seriesMin = new LineGraphSeries<>();
            seriesMin.setColor(Color.RED);
            dMinPressure = 1000;
        }
        return seriesMin;
    }
    public BarGraphSeries<DataPoint> getSeriesBar() {
        if (seriesBar == null) {
            seriesBar = new BarGraphSeries<DataPoint>();
            seriesBar.setDrawValuesOnTop(true);

        }
        return seriesBar;
    }
    public CHistogramSeries getSeriesHist() {
        if (seriesHist == null) {
            seriesHist = new CHistogramSeries();
        }
        return seriesHist;
    }

    public double dConvertPressure(int lPressure_raw) {
        Settings settings = Settings.getInstance();

        // convert the raw data to mmHG
        double dPressure_raw = (double) lPressure_raw;
        double dPressure_mmHg;
        if (settings.bIsUse_mmHg())
            dPressure_mmHg = dPressure_raw * 0.001;
        else
            dPressure_mmHg = (dPressure_raw - kOmin) * (kPmax - kPmin) / (kOmax - kOmin) + kPmin;

        double dPressure = dPressure_mmHg * settings.getUnitsConversion();

        if (dPressure < 0)
            dPressure = 0;

        return dPressure;
    }

    public EState appendData(double dPressure, int lPressure_raw) {
        Settings settings = Settings.getInstance();

        // update bar plot
        getSeriesBar().resetData(new DataPoint[]{new DataPoint(0.5, dPressure)});
        double dAvg = filter.dNext(dPressure);

        switch (eState) {
            case kHistRdy:
                eState = EState.kIdle;

            case kIdle:
                if ( dAvg <= settings.getThreshold_units() ) {
                    // pressure below threhold, do nothing
                    dDelayCountdown_s = settings.getDelay_s();
                    getVecRawPressure().vReset();
                    break;
                }

                // pressure above threshold, count down
                if (dDelayCountdown_s > 0) {
                    // pressure is above threhold, count down timer
                    dDelayCountdown_s -= 0.1;
                    getVecRawPressure().vPut(lPressure_raw);
                    break;
                }
                // count down completed, go to next state
                eState = EState.kStart;
                break;

            case kStart:
                dStartTime = dTime;
                eState = EState.kAcq;
                // fall into next state

            case kAcq:
                if ( dPressure > settings.getThreshold_units() ) {
                    // pressure above threshold, continue acquisition
                    // average the pressure
                    getVecRawPressure().vPut(lPressure_raw);

                    getSeriesLine().appendData(new DataPoint(dTime, dAvg), true, 12000, false);

                    if (dAvg > dMaxPressure)
                        dMaxPressure = dAvg;
                    getSeriesMax().appendData(new DataPoint(dTime, dMaxPressure), true, 12000, false);

                    if (dAvg < dMinPressure)
                        dMinPressure = dAvg;
                    getSeriesMin().appendData(new DataPoint(dTime, dMinPressure), true, 12000, false);

                    dTime += 0.1;
                }
                else {
                    // pressure is below threshold
                        // we had been in acquire mode and now we are not. show the histogram.
                    getSeriesHist().vPut(50, getSeriesLine(), dStartTime);
                    filter.vClear(); // was acquiring, clear the filter for next time
                    eState = EState.kHistRdy;
                }
            }

        return eState;
    }

    public void vClearLines() {
        seriesLine = new LineGraphSeries<>();
        seriesMax = new LineGraphSeries<>();
        seriesMin = new LineGraphSeries<>();

        // set up the line plot colors
        seriesLine.setColor(Color.BLUE);
        seriesMax.setColor(Color.RED);
        seriesMin.setColor(Color.rgb(255,165,0)); // orange

        dMaxPressure = 0;
        dMinPressure = 1000;
        dTime = 0;

        dDelayCountdown_s = 0; //settings.getDelay_s();
        //bModeAcq = true;

        filter.vReset((int)(Settings.getInstance().getSmooth_s() * 10));
    }

    public void vResetFilter() {
        filter.vReset((int)(Settings.getInstance().getSmooth_s() * 10));
    }


    public void vSaveToFile(String strFname, Context context) {

        CPressureFile fileData = new CPressureFile(getVecRawPressure());
        //CVecRawPressure vecRawPressure = getVecRawPressure();
        try {
            //internal
            FileOutputStream f_out = context.openFileOutput(strFname, Context.MODE_PRIVATE);
            ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
            obj_out.writeObject(fileData);
            f_out.flush();
            f_out.close();
            }
        catch (Exception e) {
            System.out.println (e.toString ());
            System.exit (1);
        }
    }

    public void vReadFromFile(String strFname, Context context) {

        Object obj = new Object();
        try {
            FileInputStream f_in = context.openFileInput(strFname);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            obj = obj_in.readObject();
        }
        catch (Exception e) {
            System.out.println (e.toString ());
            System.exit (1);
        }

        if (obj instanceof CPressureFile) {
            CPressureFile filePressure = (CPressureFile) obj;

            vClearLines();
            for (int i = 0; i < filePressure.nSamples; i++) {
                int lPressure = filePressure.alPressure[i];
                double dPressure = dConvertPressure(lPressure);
                appendData(dPressure, lPressure);
            }

            eState = EState.kIdle;
        }
    }

}
