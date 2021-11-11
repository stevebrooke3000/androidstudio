package ca.eonsound.esm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup rgUnits;
    SeekBar sbThreshold, sbDelay, sbSmooth;
    Button btnOK;
    TextView tvThold, tvDelay, tvSmooth;
    private String mDeviceAddress;


    Settings settings;

    // old parameters to restore if OK button not pressed
    int oldThold, oldDelay, oldSmooth; // parameters in seek bar units (sbu)
    Settings.EUnits oldUnits;

    boolean bCommitSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // restore activity state from settings
        settings = Settings.getInstance();
        oldThold = settings.getThreshold_sbu();
        oldDelay = settings.getDelay_sbu();
        oldSmooth = settings.getSmooth_sbu();
        oldUnits = settings.getUnits();

        // set UI controls
        rgUnits = findViewById(R.id.radioGroup);
        tvThold = findViewById(R.id.tvThold);
        sbThreshold = findViewById(R.id.seekbarThold);
        tvDelay = findViewById(R.id.tvDelay);
        sbDelay = findViewById(R.id.seekbarDelay);
        tvSmooth = findViewById(R.id.tvSmooth);
        sbSmooth = findViewById(R.id.seekbarSmooth);
        btnOK = findViewById(R.id.btnOK);

        // restore UI states
        RadioButton radio;
        switch (oldUnits) {
            default:
            case INCH_H2O: radio = findViewById(R.id.rbInchesH2O); break;
            case KPA:      radio = findViewById(R.id.rbKPa); break;
            case MM_HG:    radio = findViewById(R.id.rbmmHg); break;
            case PSI:      radio = findViewById(R.id.rbPSI); break;
        }
        radio.setChecked(true);

        sbThreshold.setProgress(oldThold);
        sbDelay.setProgress(oldDelay);
        sbSmooth.setProgress(oldSmooth);
        vUpdateTextViews();


        /*************************
        the listeners
        *************************/
        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {}
           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {
               settings.setThold(seekBar.getProgress());
               vUpdateTextViews();
           }
        });

        sbDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settings.setDelay(seekBar.getProgress());
                vUpdateTextViews();
            }
        });

        sbSmooth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settings.setSmooth(seekBar.getProgress());
                vUpdateTextViews();
            }
        });

        rgUnits.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (rgUnits.getCheckedRadioButtonId()) {
                    case R.id.rbInchesH2O: settings.setUnits(Settings.EUnits.INCH_H2O); break;
                    case R.id.rbKPa:       settings.setUnits(Settings.EUnits.KPA); break;
                    case R.id.rbmmHg:      settings.setUnits(Settings.EUnits.MM_HG); break;
                    case R.id.rbPSI:       settings.setUnits(Settings.EUnits.PSI); break;
                }
                vUpdateTextViews();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bCommitSettings = true;
                final Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!bCommitSettings) {
            // restore old values
            settings.setThold(oldThold);
            settings.setDelay(oldDelay);
            settings.setSmooth(oldSmooth);
            settings.setUnits(oldUnits);
        }
    }

    void vUpdateTextViews() {
        tvThold.setText("Start Threshold: " + settings.getThold_str());
        tvDelay.setText("Start Delay: " + settings.getDelay_str());
        tvSmooth.setText("Smoothing: " + settings.getSmooth_str());
    }
}
