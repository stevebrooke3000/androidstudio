package ca.eonsound.esm;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.Series;

import java.util.Iterator;

public class CHistogramSeries extends BarGraphSeries {

    protected double dMax, dMin, dScale;
    protected int nBins;
    protected int[] aiCount;
    protected int nTotalCounts;
    protected double dMean, dDev, dTime;

    private static final double kS1 = 100;
    private static final double kS2 = 75;
    private static final double kE1 = 0.01;
    private static final double kE2 = 0.06;
    private static final double kA1 = (kS1 - kS2) / Math.log(kE1 / kE2);
    private static final double kA2 = Math.exp(kS1/kA1) / kE1;


    void vReset(int _nBins, double _dMin, double _dMax) {
        nBins = _nBins;
        dMin = _dMin;
        dMax = _dMax;
        dScale = nBins / (dMax - dMin);
        //nTotalCounts = 0;

        aiCount = new int[nBins];
        for (int i=0; i<nBins; i++)
            aiCount[i] = 0;
    }

    int iMapToBin(double dIn) {
        int iBin = (int)((dIn - dMin) * dScale + 0.5);
        iBin = Math.min(Math.max(iBin, 0), nBins-1);
        return iBin;
    }

    double dBinVal(int iBin) {
        return iBin / dScale + dMin;
    }

    void vPut(int _nBins, Series<DataPoint> series, double dStartTime) {
        vReset(_nBins, series.getLowestValueY(), series.getHighestValueY());
        double dSum = 0;
        int nTotalCounts = 0;

        Iterator<DataPoint> it = series.getValues(dStartTime, series.getHighestValueX());
        while(it.hasNext()) {
            double dIn =it.next().getY();

            //aiCount[iMapToBin(dIn)]++;

            dSum += dIn;
            nTotalCounts++;
        }

        if (nTotalCounts == 0) {
            dMean = 1.0;
            dDev = 1.0;
            return;
        }

        // now compute variance
        dMean = dSum / nTotalCounts;
        double dVarSum = 0;

        it = series.getValues(dStartTime, series.getHighestValueX());
        while(it.hasNext()) {
            double dIn = it.next().getY();
            dVarSum += Math.abs(dIn - dMean);
        }

        dDev = dVarSum / nTotalCounts;
        dTime = series.getHighestValueX() - dStartTime;

        // now update the histogram plot
        resetData(new DataPoint[] {}); // clear old histogram data

        for (int i=0; i<nBins; i++)
           appendData(new DataPoint(dBinVal(i), aiCount[i]), false, nBins, true);
    }

    double dGetScore() {
        double E = dDev / dMean;

        double dScore = kA1 * Math.log(kA2 * E);

        if (dScore < 0)
             return 0;
        else if (dScore > 100)
            return 100;
        else
            return dScore;
    }

    double dGetTime() {
        return dTime;
    }

    double dGetMode() {
        int iMode = 0;
        int nMaxCount = 0;
        for (int i=0; i<nBins; i++) {
            if (aiCount[i] > nMaxCount) {
                nMaxCount = aiCount[i];
                iMode = i;
            }
        }
        return dBinVal(iMode);
    }

    /*
    void vGetStats() {
        int iMode = 0;
        int nMaxCount = 0;
        for (int i=0; i<nBins; i++) {
            if (aiCount[i] > nMaxCount) {
                nMaxCount = aiCount[i];
                iMode = i;
            }
        }

        int nTgtCount = (int)((double)nTotalCounts * 0.05 / 2);
        int nCount = aiCount[iMode];
        int iPosDev, iNegDev;
        for (iNegDev = 0; iNegDev < nBins; iNegDev++) {
            nCount += aiCount[iNegDev];
            if (nCount >= nTgtCount)
                break;
        }
        nCount = 0;
        for (iPosDev=nBins-1; iPosDev>=0; iPosDev--) {
            nCount += aiCount[iPosDev];
            if (nCount >= nTgtCount)
                break;
        }
    }

     */
}
