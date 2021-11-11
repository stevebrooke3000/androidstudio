package ca.eonsound.esm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import java.io.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// BT
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "ca.eonsound.esm.prefs";

    final int REQUEST_CODE_SCAN = 42;
    final int REQUEST_CODE_SETTINGS = 43;
    final int REQUEST_CODE_SCORE = 44;

    private CViewModelMain viewModelMain;

    private boolean mConnected = false;
    private final static String TAG = MainActivity.class.getSimpleName();

    private BluetoothLeService mBluetoothLeService;

    public final static UUID UUID_DEVICE_INFO = UUID.fromString(SampleGattAttributes.DEVICE_INFO_UUID);
    public final static UUID UUID_BATTERY_SERVICE = UUID.fromString(SampleGattAttributes.strGattUuidService_Battery);
    public final static UUID uuidCharacteristicBattry = UUID.fromString(SampleGattAttributes.strGattUuidCharacteristic_Battery);
    public final static UUID UUID_GATT_OBJ_MODEL_NUMBER_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_MODEL_NUMBER_STR_UUID);
    public final static UUID UUID_GATT_OBJ_SERIAL_NUMBER_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_SERIAL_NUMBER_STR_UUID);
    public final static UUID UUID_GATT_OBJ_FIRMWARE_REV_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_FIRMWARE_REV_STR_UUID);
    public final static UUID UUID_GATT_OBJ_HARDWARE_REV_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_HARDWARE_REV_STR_UUID);
    public final static UUID UUID_GATT_OBJ_SOFTWARE_REV_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_SOFTWARE_REV_STR_UUID);
    public final static UUID UUID_GATT_OBJ_MANUFACTURER_NAME_STR = UUID.fromString(SampleGattAttributes.GATT_OBJ_MANUFACTURER_NAME_STR_UUID);

    public final static UUID UUID_BAGPIPE_MANOMETER_SRVC = UUID.fromString(SampleGattAttributes.BAGPIPE_MANOMETER_SERVICE);
    public final static UUID UUID_CHR_PRESSURE = UUID.fromString(SampleGattAttributes.CHARACTERISTIC_PRESSURE);

    ProgressBar pbBluetooth;
    TextView textData;

    // line plot graph for pressure history display
    MyGraphView graphLine; // the line plot graph
    GraphView graphBar; // bar plot graph for instantaneous pressure display
    //GraphView graphHist; // histogramplot

    // data items
    Settings settings;

    //private AlertDialog dlgScore;
    private CDialogScore dlgScore;
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textData = findViewById(R.id.textData);
        pbBluetooth = findViewById(R.id.pbBluetooth);
        pbBluetooth.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

        // Restore preferences
        settings = Settings.getInstance();
        settings.Restore(getSharedPreferences(PREFS_NAME, 0));

        // plots
        graphLine = (MyGraphView) findViewById(R.id.graphLine);
        graphBar = (GraphView) findViewById(R.id.graphBar);
        //graphHist = (GraphView) findViewById(R.id.graphHist);

        viewModelMain = ViewModelProviders.of(this).get(CViewModelMain.class);

        vClearPlots();


        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.ble_not_supported, Toast.LENGTH_LONG);
            errorToast.show();
            finish();
        }

        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

     }

    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            setContentView(R.layout.activity_main);

        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main);
    }


    @Override
    protected void onResume() {
        super.onResume();

         registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(settings.getDevAddr());
            Log.d(TAG, "Connect request result=" + result);
        }
        vClearPlots();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            String strName = intent.getStringExtra("name");
            String strAddr = intent.getStringExtra("addr");
            settings.setDevAddr(strAddr);
        }

        else if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            viewModelMain.vClearLines();
            vClearPlots();
            graphLine.vSetOptimal();
        }

        else if (requestCode == REQUEST_CODE_SCORE && resultCode == RESULT_OK) {
            viewModelMain.vReadFromFile(intent.getStringExtra("Fname"), getApplicationContext());
            vClearPlots();
            graphLine.vSetOptimal();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        settings.Save(getSharedPreferences(PREFS_NAME, 0));
        if (mBluetoothLeService != null) {
            Log.d(TAG, "disconnect bluetooth service");
            mBluetoothLeService.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_score) {
            Intent intent = new Intent(this, ScoreActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCORE);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            return true;
        }
        else if (id == R.id.action_scan) {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
            return true;
        }

        else if (id == R.id.action_clear) {
            // clear the graph data
            viewModelMain.vClearLines();
            vClearPlots();
        }

        else if (id == R.id.action_manual) {
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.eonsound.ca/esm/manual.html"));
            startActivity(intent);
        }

        else if (id == R.id.action_devinfo) {
            //Intent intent = new Intent(this, ScoreActivity.class);
            Intent intent = new Intent(this, DevInfoActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.action_exit)
            finish();

        return super.onOptionsItemSelected(item);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(Settings.getInstance().getDevAddr());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }

            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String strUuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                if (strUuid.equals(SampleGattAttributes.strGattUuidCharacteristic_Battery)) {
                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_BYTES);
                    settings.setBattery(data[0]);
                }
                else
                    displayData(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0));


            }


            else {
                String strUuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                String strCharacteristic = intent.getStringExtra(BluetoothLeService.EXTRA_STRING);
                Log.d(TAG, "uuid: " + strUuid);
                Log.d(TAG, "characteristic: " + strCharacteristic);

                if (strUuid.equals(SampleGattAttributes.GATT_OBJ_MANUFACTURER_NAME_STR_UUID))
                     settings.setManufacturer(strCharacteristic);

                else if (strUuid.equals(SampleGattAttributes.GATT_OBJ_MODEL_NUMBER_STR_UUID))
                    settings.setModel(strCharacteristic);

                else if (strUuid.equals(SampleGattAttributes.GATT_OBJ_FIRMWARE_REV_STR_UUID))
                    settings.setFirmware(strCharacteristic);

                else if (strUuid.equals(SampleGattAttributes.GATT_OBJ_HARDWARE_REV_STR_UUID))
                    settings.setHardware(strCharacteristic);

                else if (strUuid.equals(SampleGattAttributes.GATT_CHR_UUID_MEASUREMENT_INTERVAL)) {
                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_BYTES);
                    int nSmpPerSec = data[0] + (data[1] << 8);
                    float fSamplePeriod_s = 1.0f / nSmpPerSec;
                    settings.setSamplePeriod(fSamplePeriod_s);
                }

                else if (strUuid.equals(SampleGattAttributes.strGattUuidCharacteristic_Battery)) {
                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_BYTES);
                    settings.setBattery(data[0]);
                }
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        //runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
        //        mConnectionState.setText(resourceId);
        //    }
        //});
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        for (BluetoothGattService gattService : gattServices) {
            UUID uuidService = gattService.getUuid();
            if (uuidService.compareTo(UUID_DEVICE_INFO) == 0) {
                Log.d(TAG, "deviceinfo");

                List<BluetoothGattCharacteristic> gattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
                gattCharacteristics = gattService.getCharacteristics();

                // read all device info characteristics
                for(BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    mBluetoothLeService.vReadCharacteristic(characteristic);
                }
            }

            if (uuidService.compareTo(UUID_BATTERY_SERVICE) == 0) {
                Log.d(TAG, "battery level");

                List<BluetoothGattCharacteristic> gattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
                gattCharacteristics = gattService.getCharacteristics();

                // read all battery level characteristics
                for(BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    if ( characteristic.getUuid().equals(uuidCharacteristicBattry) )
                        mBluetoothLeService.vSetCharacNotification(characteristic);
                    else
                        mBluetoothLeService.vReadCharacteristic(characteristic);
                }
            }

            if (uuidService.compareTo(UUID_BAGPIPE_MANOMETER_SRVC) == 0) {
                // found ESM manometer device. enable characteristic update notifications.
                // causes BroadcastReceiver::onReceive(ACTION_DATA_AVAILABLE) to be called
                //mBluetoothLeService.setCharacteristicNotification(mycharacteristic, true);
                Log.d(TAG, "bagpipe manometer service");

                List<BluetoothGattCharacteristic> gattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
                gattCharacteristics = gattService.getCharacteristics();

                // read all device info characteristics
                for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    if ( characteristic.getUuid().equals(UUID_CHR_PRESSURE) )
                        mBluetoothLeService.vSetCharacNotification(characteristic);
                    else
                        mBluetoothLeService.vReadCharacteristic(characteristic);
                }
            }
        }
/*
        // Loops through available GATT Services.
        boolean bFoundManometer = false;
        for (BluetoothGattService gattService : gattServices) {
            UUID uuidService = gattService.getUuid();

            if (uuidService.compareTo(UUID_BAGPIPE_MANOMETER_SRVC) != 0)
                continue;

            BluetoothGattCharacteristic mycharacteristic = gattService.getCharacteristic(UUID_CHR_PRESSURE);

            if (mycharacteristic == null)
                continue;

            //BluetoothGattCharacteristic characDevInfo = gattService.getCharacteristic(())
            // found ESM manometer device. enable characteristic update notifications.
            // causes BroadcastReceiver::onReceive(ACTION_DATA_AVAILABLE) to be called
            //mBluetoothLeService.setCharacteristicNotification(mycharacteristic, true);
            mBluetoothLeService.vSetCharacNotification(mycharacteristic);

            break;
        }
*/
        mBluetoothLeService.vRequestCharacteristics();
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_CHARAC_AVAILABLE);
        return intentFilter;
    }

    /*******************************************************
    Data plotting functions
    *******************************************************/
    private void displayData(int lPressure_raw) {
        // update the bluetooth progress bar to show that bluetooth is working
        int prog = pbBluetooth.getProgress() + 10;
        if (prog > 100)
            prog = 0;
        pbBluetooth.setProgress(prog);

        // log the raw data for debugging, the text view will be invisible for production
        textData.setText(String.valueOf(lPressure_raw));

        CViewModelMain.EState eState = viewModelMain.appendData(lPressure_raw);

        if (eState == CViewModelMain.EState.kHistRdy) {
            vShowScore();
        }
        else if (eState == CViewModelMain.EState.kStart) {
            if (mAlertDialog != null)
                mAlertDialog.dismiss();
            viewModelMain.vClearLines();
            vClearPlots();
        }
    }

    private void vShowScore() {

        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(1);
        CScore score = new CScore();

        score.strScore = fmt.format(viewModelMain.getSeriesHist().dGetScore());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat fmtTstamp = new SimpleDateFormat("hh:mm:ss   MMM d, yyyy");
        score.strTstamp = fmtTstamp.format(calendar.getTime());

        score.strPlaytime = fmt.format(viewModelMain.getSeriesHist().dGetTime()) + " sec";
        score.strFname = settings.getNextFname();

        settings.vAddScore(score);
        viewModelMain.vSaveToFile(score.strFname, getApplicationContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Score");
        builder.setMessage("score: " + score.strScore + ",  playtime: " + score.strPlaytime);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();

    }

    private void vClearPlots() {
        // line graph
        graphLine.removeAllSeries();

        graphLine.addSeries(viewModelMain.getSeriesLine());
        graphLine.addSeries(viewModelMain.getSeriesMax());
        graphLine.addSeries(viewModelMain.getSeriesMin());

        String strUnits = settings.getUnitsName();
        double dMaxY = settings.getUnitsMax();

        // setup the line plot viewport
        graphLine.vSetDefault();

        graphLine.getGridLabelRenderer().setHighlightZeroLines(false);
        graphLine.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        graphLine.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        graphLine.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graphLine.getGridLabelRenderer().setNumVerticalLabels(10);

        // bar graph
        graphBar.addSeries(viewModelMain.getSeriesBar());
        graphBar.getViewport().setYAxisBoundsManual(true);
        graphBar.getViewport().setXAxisBoundsManual(true);
        graphBar.getViewport().setMinY(0);
        graphBar.getViewport().setMinX(0);
        graphBar.getViewport().setMaxX(1);
        graphBar.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        graphBar.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graphBar.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphBar.getGridLabelRenderer().setNumVerticalLabels(10);
        graphBar.getGridLabelRenderer().setNumHorizontalLabels(1);
        graphBar.getGridLabelRenderer().setHorizontalAxisTitle(" ");

        graphLine.getGridLabelRenderer().setVerticalAxisTitle(strUnits);
        graphLine.onDataChanged(false, false);
        graphLine.getGridLabelRenderer().setVerticalAxisTitle(strUnits);
        graphLine.onDataChanged(false, false);

        graphBar.getViewport().setMaxY(dMaxY);
        graphBar.onDataChanged(false, false);
        graphBar.getViewport().setMaxY(dMaxY);
        graphBar.onDataChanged(false, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
