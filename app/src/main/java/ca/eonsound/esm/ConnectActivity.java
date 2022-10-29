package ca.eonsound.esm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import android.app.Activity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
import android.widget.Button;
//import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {
    private final static String TAG = ConnectActivity.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean bScanning;
    private Handler mHandler;

    // Lollipop support
    private ScanSettings scanSetting;
    private List<ScanFilter> filterList;

    Button btnScan;
    ProgressBar progressScanning;
    TextView textviewScanning;
    ListView listviewDevices;

    //private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create");
        setContentView(R.layout.activity_connect);
        Toast.makeText(this, "select manometer device", Toast.LENGTH_LONG).show();

        // back action
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        bScanning = false;
        btnScan = findViewById(R.id.btnScan);
        progressScanning = findViewById(R.id.progressScan);
        textviewScanning = findViewById(R.id.textviewScanning);
        listviewDevices = findViewById(R.id.listviewDevices);

        ArrayList<BluetoothDevice> leList = new ArrayList<>();


        mHandler = new Handler();

        mLeDeviceListAdapter = new LeDeviceListAdapter(this, android.R.layout.simple_list_item_1, leList);
        listviewDevices.setAdapter(mLeDeviceListAdapter);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        scanSetting = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        filterList = new ArrayList<>();


        // Bluetooth requires location permission. Ask to enable if not already enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        /*
         the listeners
        */
        // user has selected the device
        listviewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                vReturnDevice(mLeDeviceListAdapter.getDevice(position));
            }
        });

        // handle scan/stop button actions
        btnScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!bScanning) {
                    progressScanning.setVisibility(View.VISIBLE);
                    textviewScanning.setVisibility((View.VISIBLE));
                    btnScan.setText("Stop");

                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                }
                else {
                    progressScanning.setVisibility(View.INVISIBLE);
                    textviewScanning.setVisibility((View.INVISIBLE));
                    btnScan.setText("Scan");

                    scanLeDevice(false);
                }
            }
        });

    }

    @Override
    protected  void onStart() {
        super.onStart();
        Log.d(TAG, "on start");

        // on startup, go directly to scanning for devices
        progressScanning.setVisibility(View.VISIBLE);
        btnScan.setText("Stop");
        mLeDeviceListAdapter.clear();
        scanLeDevice(true);


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


    // object classes
    // Adapter for holding devices found through scanning.
    private static class LeDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
        private ArrayList<BluetoothDevice> mLeDevices;

        private LeDeviceListAdapter(Context context, int textViewResourceId, ArrayList<BluetoothDevice> objects) {
            super(context, textViewResourceId, objects);
            mLeDevices = objects;
        }

        private void addDevice(BluetoothDevice device) {

            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        private BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                view = inflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback. bluetooth has found a device
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "scan result callbackType:" + callbackType);
            Log.i(TAG, "scan result:" + result.toString());

            BluetoothDevice device = result.getDevice();

            // if this is our paired device, then return it
            String strDevAddr = Settings.getInstance().getDevAddr();
            String strDevName = device.getName();
            Log.d(TAG, "found device address: " + strDevAddr + ", name: " + strDevName);

            if (strDevAddr != null) {
                if ( strDevAddr.equals(device.getAddress()) ) {
                    Log.d(TAG, "found paired device");
                    vReturnDevice((device));
                }

            }

            // add the device to the list
           if (strDevName != null) {
                if (strDevName.startsWith("eonsound")) {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i(TAG, "ScanResult - Results" + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed, Error Code: " + errorCode);
        }

    };

    private void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter.getBluetoothLeScanner() == null)
            return;

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    }
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            bScanning = true;
            // Check before call the function
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.getBluetoothLeScanner().startScan(filterList, scanSetting, mScanCallback);
            }
        } else {
            bScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }
        }
        invalidateOptionsMenu();
    }


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


    void vReturnDevice(BluetoothDevice device) {
        Log.d(TAG, "return device");

        if (device == null)
            return;

        final Intent intent = new Intent();
        intent.putExtra("name", device.getName());
        intent.putExtra("addr", device.getAddress());

        if (bScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            } //else {mBluetoothAdapter.stopLeScan(mLeScanCallback);}
            bScanning = false;
        }

        setResult(RESULT_OK, intent);
        finish();

    }

}
