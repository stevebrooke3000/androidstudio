package ca.eonsound.esm;

import java.io.Serializable;

public class CPressureFile implements Serializable {
    public int nSamples;
    public int[] alPressure;

    public CPressureFile(CVecRawPressure vecRawPressure) {
        nSamples = vecRawPressure.nSamples;
        alPressure = new int[nSamples];
        for (int i=0; i<nSamples; i++) {
            alPressure[i] = vecRawPressure.alPressure[i];
        }
    }

    public void vGetData(CVecRawPressure vecRawPressure) {
        vecRawPressure.nSamples = nSamples;
        for (int i=0; i<nSamples; i++) {
            vecRawPressure.alPressure[i] = alPressure[i];
        }
    }
}
