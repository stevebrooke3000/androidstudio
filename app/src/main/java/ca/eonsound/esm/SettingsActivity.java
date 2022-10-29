package ca.eonsound.esm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup rgUnits;
    SeekBar sbThreshold, sbDelay, sbSmooth;
    CheckBox cbAutoScan;
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

        // back action
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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
        cbAutoScan = findViewById(R.id.checkboxAutoScan);

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
        cbAutoScan.setChecked(settings.getAutoScan());

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

        cbAutoScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                settings.setAutoScan(cbAutoScan.isChecked());
            }

        });

        rgUnits.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int buttonId = rgUnits.getCheckedRadioButtonId();
                if (buttonId == R.id.rbInchesH2O)
                    settings.setUnits(Settings.EUnits.INCH_H2O);
                else if (buttonId == R.id.rbKPa)
                    settings.setUnits(Settings.EUnits.KPA);
                else if (buttonId == R.id.rbmmHg)
                    settings.setUnits(Settings.EUnits.MM_HG);
                else
                    settings.setUnits(Settings.EUnits.PSI);
                
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

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void vUpdateTextViews() {
        tvThold.setText("Start Threshold: " + settings.getThold_str());
        tvDelay.setText("Start Delay: " + settings.getDelay_str());
        tvSmooth.setText("Smoothing: " + settings.getSmooth_str());
    }
}
