package ca.eonsound.esm;

public class CFilter {
    public CFilter(int _nLen) {
        vReset(_nLen);
    }

    void vReset(int _nLen) {
        nLen = _nLen;
        adBuffer = new double[nLen];
        vClear();
    }

    public void vClear() {
        for (int i=0; i<nLen; i++)
            adBuffer[i] = 0;

        iHead = 0;
        dLastSum = 0;
    }

    double dNext(double dIn) {
        if ( nLen <=1 )
            return dIn;

        dLastSum -= adBuffer[iHead];
        dLastSum += dIn;

        adBuffer[iHead] = dIn;

        iHead++;
        if (iHead >= nLen)
            iHead = 0;
/*
        double dSum = 0;
        for (int i=0; i<nLen; i++)
            dSum += adBuffer[i];

        return dSum / nLen;
 */
        return dLastSum / nLen;
    }

    private double[] adBuffer;
    int nLen;
    int iHead;
    double dLastSum;
}
