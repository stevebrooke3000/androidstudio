package ca.eonsound.esm;

class CVecRawPressure {
    private final int nMaxSamples = 20 * 60 * 10;
    public int nSamples;
    public int[] alPressure = new int[nMaxSamples];

    public CVecRawPressure() {
        nSamples = 0;
    }

    void vPut(int lPressure) {
        alPressure[nSamples] = lPressure;
        nSamples++;

        // TBD stop
        if (nSamples >= nMaxSamples)
            nSamples = nMaxSamples - 1;
    }

    void vReset() {
        nSamples = 0;
    }

}
