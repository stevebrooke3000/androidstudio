package ca.eonsound.esm;

public class CScore {
    String strScore;
    String strTstamp;
    String strPlaytime;
    String strFname;
    boolean bLocked;

    void vToggleLock() {
        bLocked = !bLocked;
    }

    boolean bIsLocked() {
        return bLocked;
    }
}

