package ca.eonsound.esm;

import android.content.SharedPreferences;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class Settings {
    enum EUnits {INCH_H2O, MM_HG, PSI, KPA};

    // these are the values maintianed by shared preferences
    protected int iThold_sbu; // seekbar units (0..100)
    protected int iDelay_sbu; // seekbar units (0.100)
    protected  int iSmooth_sbu;
    protected EUnits eUnits;
    private String strDeviceAddress;
    private int iNextFileIdx;

    private ArrayList<CScore> aScore;


    public void vAddScore(CScore score) { aScore.add(0, score); }
    public void vClearScore() { aScore.clear();}
    public ArrayList<CScore> getScore() { return aScore; }
    public String getNextFname() {
        String strFname = "esm" + String.format("%04d", iNextFileIdx) + ".dat";
        if (++iNextFileIdx >= 10000)
            iNextFileIdx = 0;
        return strFname;
    }

    private static final Settings ourInstance = new Settings();

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    void Restore(SharedPreferences sharedprefs) {
        // Restore preferences
        strDeviceAddress = sharedprefs.getString("devaddr", null);
        iThold_sbu = sharedprefs.getInt("threshold", 0);
        iDelay_sbu = sharedprefs.getInt("delay", 0);
        iSmooth_sbu = sharedprefs.getInt("smooth", 0);
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
        editor.putInt("units", eUnits.ordinal());
        editor.putInt("fileidx", iNextFileIdx);

        Gson gson = new Gson();
        String json = gson.toJson(aScore);
        editor.putString("score", json);

        editor.commit();
    }

    public void setDevAddr(String _strDevAddr) { strDeviceAddress = _strDevAddr; }
    // get/set with raw units
    public void setThold(int _iThold_sbu) { iThold_sbu = _iThold_sbu; }
    public void setDelay(int _iDelay_sbu) { iDelay_sbu = _iDelay_sbu; }
    public void setSmooth(int _iSmooth_sbu) { iSmooth_sbu = _iSmooth_sbu; }
    public void setUnits(EUnits _eUnits) { eUnits = _eUnits; };

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
    public int getSmooth_sbu() { return iSmooth_sbu; };
    public EUnits getUnits() { return eUnits; };

    // conversion factors
    private static final double kmmHg_per_sbu = 1.0;
    public static final double kPsi_per_mmHg = 0.019336777496394;
    public static final double kInchH2O_per_mmHg = 0.53524;
    public static final double kPa_per_mmHg =  7.5006157584566;

    // get with unit conversion
    public double getThreshold_units() { return iThold_sbu *kmmHg_per_sbu * getUnitsConversion(); }
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
    /*
    TBD?
    private static final double kTs_max = 10;
    private static final double kAs = Math.log(kTs_max + 1) / 100;
    public double getSmooth_s() { return Math.exp(getSmooth_sbu() * kAs) - 1; }

     */
    public double getSmooth_s() { return getSmooth_sbu() * 0.02; }
    public String getSmooth_str() { return String.format(Locale.US, "%.2f", getSmooth_s()); }

    // need a singleton place to store these. The parameters do not need to be saved to the shared preferences
    private String strManufacturer;
    public void setManufacturer(String _strManufacturer) {
        strManufacturer = _strManufacturer;
    }
    public String getManufacturer() {
        return strManufacturer;
    }

    private String strModel;
    public void setModel(String _strModel) { strModel = _strModel; }
    public String getModel() { return strModel; }

    private String strFirmware;
    public void setFirmware(String _strFirmware) { strFirmware = _strFirmware; }
    public String getFirmware() { return strFirmware; }

    private String strHardware;
    public void setHardware(String _strHardware) { strHardware = _strHardware; }
    public String getHardware() { return strHardware; }

    private float fSamplePeriod_s;
    public void setSamplePeriod(float _fSamplePeriod_s) { fSamplePeriod_s = _fSamplePeriod_s; }
    public float getSamplePeriod_s() { return fSamplePeriod_s; }

    private int lBattery;
    public void setBattery(int _lBattery) { lBattery = _lBattery; }
    public int getBattery() { return lBattery; }
}
