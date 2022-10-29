package ca.eonsound.esm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;

public class DevInfoActivity extends AppCompatActivity {
    Settings settings;
    TextView tvBattery;
    TextView tvManufacturer;
    TextView tvModel;
    TextView tvFirmware;
    TextView tvHardware;
    TextView tvDevAddr;
    private String mDeviceAddress;
    Button btnForget;

    // calibration
    EditText edtCoeff0;
    EditText edtCoeff1;
    Button btnSetCoeff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_info);

        // back action
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        settings = Settings.getInstance();

        tvBattery = findViewById(R.id.textBatteryLevel);
        tvManufacturer = findViewById(R.id.textManufacturer);
        tvModel = findViewById(R.id.textModel);
        tvFirmware = findViewById(R.id.textFirmware);
        tvHardware = findViewById(R.id.textHardware);

        tvBattery.setText("Battery level: " + settings.getBattery() );
        tvManufacturer.setText("Manufacturer: " + settings.getManufacturer());
        tvModel.setText("Model: " + settings.getModel());
        tvFirmware.setText("F/W: " + settings.getFirmware());
        tvHardware.setText("H/W: " + settings.getHardware());

        tvDevAddr = findViewById(R.id.tvDeviceAddr);
        btnForget = findViewById(R.id.btnForget);

        String strDevAddr = getResources().getString(R.string.device_addr) + settings.getDevAddr();
        tvDevAddr.setText(strDevAddr);

        // Calibration
        edtCoeff0 = findViewById(R.id.edtCoeff0);
        edtCoeff1 = findViewById(R.id.edtCoeff1);
        btnSetCoeff = findViewById(R.id.btnSetCoeff);

        btnForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog mAlertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(DevInfoActivity.this);
                builder.setTitle("Clear the paired manometer");
                builder.setMessage("you will need to reconnect, are you sure?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK, clear the device
                        tvDevAddr.setText(getResources().getString(R.string.device_addr) + " ??:??:??:??:??:??");
                        settings.setDevAddr(null);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mAlertDialog = builder.create();
                mAlertDialog.show();


            }
        });
/*
        btnSetCoeff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                if (edtCoeff0.toString() == null)
                    intent.putExtra("Coeff0", 0.0);
                else
                    intent.putExtra("Coeff0", Float.parseFloat(edtCoeff0.getText().toString()));

                if (edtCoeff1.toString() == null)
                    intent.putExtra("Coeff1", 1.0);
                else
                    intent.putExtra("Coeff1", Float.parseFloat(edtCoeff1.getText().toString()));

                finish();
            }
        });
 */

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
}
