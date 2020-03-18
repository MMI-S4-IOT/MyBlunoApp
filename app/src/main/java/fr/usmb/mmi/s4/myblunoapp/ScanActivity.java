package fr.usmb.mmi.s4.myblunoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static String LOG_TAG = "ScanActivity";

    private ArrayAdapter devicesAdapter;
    private List<BluetoothDevice> devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        devicesList = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.device_view, devicesList){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null){
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_view, parent, false);
                }
                BluetoothDevice dev = getItem(position);
                TextView devName = convertView.findViewById(R.id.devName);
                TextView devAdr = convertView.findViewById(R.id.devAdr);
                String name = dev.getName();
                if (name == null) name = "Inconnu";
                devName.setText(name);
                devAdr.setText(dev.getAddress());
                return  convertView;
            }
        };
        ListView v = this.findViewById(R.id.devicesList);
        v.setAdapter(devicesAdapter);
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice dev = devicesList.get(position);
                Intent result = new Intent().putExtra("deviceAddress", dev.getAddress()) ;
                ScanActivity.this.setResult(RESULT_OK, result);
                ScanActivity.this.finish();
            }
        });

        callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice dev = result.getDevice();
                if (! devicesList.contains(dev)){
                    //devicesList.add(dev);
                    //devicesAdapter.notifyDataSetChanged();
                    devicesAdapter.add(dev);
                }
                Log.d(LOG_TAG, "bluetooth detecte " + dev.getName() + " " + dev.getAddress());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Toast.makeText(ScanActivity.this, "Erreur du scan bluetooth", Toast.LENGTH_LONG).show();
                ScanActivity.this.setResult(Activity.RESULT_CANCELED);
                ScanActivity.this.finish();
                Log.e(LOG_TAG, "echac du scan bluetooth");
            }
        };
    }

    private BluetoothLeScanner scanner ;
    private ScanCallback callback;

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ((bluetoothAdapter != null) && (bluetoothAdapter.isEnabled())){
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(callback);
        }
    }

    @Override
    protected void onStop() {
        if (scanner != null) scanner.stopScan(callback);
        Log.d(LOG_TAG, "Scan bluetooth LE termine");
        super.onStop();
    }
}
