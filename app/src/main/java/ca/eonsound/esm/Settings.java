package ca.eonsound.esm;

import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class Settings {
    // singleton methods
    private static volatile Settings ourInstance = null;
    private Settings() { }

    public static Settings getInstance() {
        if (ourInstance == null) {
            synchronized (Settings.class) {
                if (ourInstance == null)
                    ourInstance = new Settings();
            }
        }
        return ourInstance;
    }


    enum EUnits {INCH_H2O, MM_HG, PSI, KPA};

    // these are the values maintained by shared preferences
    protected int iThold_sbu; // seekbar units (0..100)
    protected int iDelay_sbu; // seekbar units (0.100)
    protected  int iSmooth_sbu;
    protected  boolean bAutoScan;
    protected EUnits eUnits;
    private String strDeviceAddress;
    private int iNextFileIdx;

    private ArrayList<CScore> aScore;

    // need a singleton place to store these. The parameters do not need to be saved to the shared preferences
    private String strManufacturer = "eonsound";
    private String strModel = "ESM1";
    private String strFirmware = "V1.0.0";
    private String strHardware = "REV B";
    private float fSamplePeriod_s = 0.1f;
    private int lBattery = 100;
    private boolean bUse_mmHg = false;

    /*
    access methods follow
    */

    public void vAddScore(CScore score) { aScore.add(0, score); }
    public void vClearScore() { aScore.clear();}
    public void vDeleteScore(CScore score) { aScore.remove(score); }
    public ArrayList<CScore> getScore() { return aScore; }
    public String getNextFname() {
        String strFname = "esm" + String.format(Locale.US,"%04d", iNextFileIdx) + ".dat";
        if (++iNextFileIdx >= 10000)
            iNextFileIdx = 0;
        return strFname;
    }

    void Restore(SharedPreferences sharedprefs) {
        // Restore preferences
        strDeviceAddress = sharedprefs.getString("devaddr", null);
        iThold_sbu = sharedprefs.getInt("threshold", 10);
        iDelay_sbu = sharedprefs.getInt("delay", 0);
        iSmooth_sbu = sharedprefs.getInt("smooth", 0);
        bAutoScan = sharedprefs.getBoolean("autoscan", false);
        iNextFileIdx = sharedprefs.getInt("fileidx", 0);

        eUnits = EUnits.INCH_H2O;
        int iUnits = sharedprefs.getInt("units", 0);
        for (EUnits unit : EUnits.values())
            if (unit.ordinal() == iUnits) {
                eUnits = unit;
                break;
            }

        Gson gson = new Gson();
        String json = sharedprefs.getString("score", null);
        Type type = new TypeToken<ArrayList<CScore>>() {}.getType();
        aScore = gson.fromJson(json, type);
        if (aScore == null)
            aScore = new ArrayList<CScore>();
    }

    void Save(SharedPreferences sharedprefs) {
        SharedPreferences.Editor editor = sharedprefs.edit();
        editor.putString("devaddr", strDeviceAddress);
        editor.putInt("threshold",iThold_sbu);
        editor.putInt("delay",iDelay_sbu);
        editor.putInt("smooth", iSmooth_sbu);
        editor.putBoolean("autoscan", bAutoScan);
        editor.putInt("units", eUnits.ordinal());
        editor.putInt("fileidx", iNextFileIdx);

        Gson gson = new Gson();
        String json = gson.toJson(aScore);
        editor.putString("score", json);

        editor.commit();
    }

    // set
    public void setDevAddr(String _strDevAddr) { strDeviceAddress = _strDevAddr; }
    // get/set with raw units
    public void setThold(int _iThold_sbu) { iThold_sbu = _iThold_sbu; }
    public void setDelay(int _iDelay_sbu) { iDelay_sbu = _iDelay_sbu; }
    public void setSmooth(int _iSmooth_sbu) { iSmooth_sbu = _iSmooth_sbu; }
    public void setAutoScan(boolean _bAutoScan) {bAutoScan = _bAutoScan; }
    public void setUnits(EUnits _eUnits) { eUnits = _eUnits; }

    public String getDevAddr() {
        if (strDeviceAddress == null)
            return null;

        if (strDeviceAddress.length() < 1 )
            return null;
        else
            return strDeviceAddress;
    }
    public int getThreshold_sbu() { return iThold_sbu; }
    public int getDelay_sbu() { return iDelay_sbu; }
    public int getSmooth_sbu() { return iSmooth_sbu; }
    public boolean getAutoScan() { return bAutoScan; }
    public EUnits getUnits() { return eUnits; }

    // conversion factors
    private static final double kmmHg_per_sbu = 1.0;
    public static final double kPsi_per_mmHg = 0.019336777496394;
    public static final double kInchH2O_per_mmHg = 0.53524;
    public static final double kPa_per_mmHg =  7.5006157584566;

    private static final double kmmHg_min = 5 / kInchH2O_per_mmHg;

    // get with unit conversion
    public double getThreshold_units() { return (iThold_sbu * kmmHg_per_sbu + kmmHg_min) * getUnitsConversion(); }
    public double getDelay_s() { return getDelay_sbu() * 0.04; }
    public String getDelay_str() { return String.format(Locale.US, "%.1f", getDelay_s()); }

    public String getThold_str() {
        double units = getThreshold_units();
        switch(eUnits) {
            default:
            case INCH_H2O:
            case MM_HG:
            case KPA:
                return String.format(Locale.US, "%.0f", units);
            case PSI:
                return String.format(Locale.US, "%.2f", units);
        }
    }

    public double getUnitsConversion() {
        String strUnits;
        double dMaxY = 1;
        switch (Settings.getInstance().getUnits()) {
            default:
            case INCH_H2O: return kInchH2O_per_mmHg;
            case MM_HG: return 1.0;
            case KPA: return kPa_per_mmHg;
            case PSI: return kPsi_per_mmHg;
        }
    }

    String getUnitsName() {
        switch (Settings.getInstance().getUnits()) {
            default:
            case INCH_H2O: return "inches H2O";
            case MM_HG: return "mm Hg";
            case KPA: return "Pascals";
            case PSI: return "PSI";
        }
    }

    double getUnitsMax() {
        switch (Settings.getInstance().getUnits()) {
            default:
            case INCH_H2O: return 50;
            case MM_HG: return 100;
            case KPA: return 1000;
            case PSI: return 2;
        }
    }

    // smoothing
    public double getSmooth_s() { return getSmooth_sbu() * 0.02; }
    public String getSmooth_str() { return String.format(Locale.US, "%.2f", getSmooth_s()); }
    /*
    TBD?
    private static final double kTs_max = 10;
    private static final double kAs = Math.log(kTs_max + 1) / 100;
    public double getSmooth_s() { return Math.exp(getSmooth_sbu() * kAs) - 1; }

     */

    // ESM1 device info
    public void setManufacturer(String _strManufacturer) {
        strManufacturer = _strManufacturer;
    }
    public String getManufacturer() {
        return strManufacturer;
    }

    public void setModel(String _strModel) { strModel = _strModel; }
    public String getModel() { return strModel; }

    public void setFirmware(String _strFirmware) {
        strFirmware = _strFirmware;
        bUse_mmHg = versionCompare("V1.0.2", strFirmware) >= 0;
    }
    public String getFirmware() { return strFirmware; }

    public int cmpFirmwareVersion(String version) {
        return versionCompare(version, strFirmware);
    }
    public void setHardware(String _strHardware) { strHardware = _strHardware; }
    public String getHardware() { return strHardware; }

    public void setSamplePeriod(float _fSamplePeriod_s) { fSamplePeriod_s = _fSamplePeriod_s; }
    public float getSamplePeriod_s() { return fSamplePeriod_s; }

    public void setBattery(int _lBattery) { lBattery = _lBattery; }
    public int getBattery() { return lBattery; }

    public boolean bIsUse_mmHg() { return bUse_mmHg; }


    // Method to compare two versions.
    // Returns 1 if v1 > v2, -1 if v1 < v2 is smaller, 0 if v1 = v2
    static int versionCompare(String v1, String v2) {
        // vnum stores each numeric part of version
        v1 = v1.substring(1);
        v2 = v2.substring(1);

        int vnum1 = 0, vnum2 = 0;

        // loop until both String are processed
        for (int i = 0, j = 0; (i < v1.length() || j < v2.length());) {
            // Storing numeric part of version 1 in vnum1
            while (i < v1.length() && v1.charAt(i) != '.') {
                vnum1 = vnum1 * 10 + (v1.charAt(i) - '0');
                i++;
            }

            // storing numeric part of version 2 in vnum2
            while (j < v2.length() && v2.charAt(j) != '.') {
                vnum2 = vnum2 * 10 + (v2.charAt(j) - '0');
                j++;
            }

            if (vnum1 > vnum2)
                return 1;
            if (vnum2 > vnum1)
                return -1;

            // if equal, reset variables and
            // go for next numeric part
            vnum1 = vnum2 = 0;
            i++;
            j++;
        }
        return 0;
    }



}
