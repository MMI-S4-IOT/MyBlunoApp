package fr.usmb.mmi.s4.myblunoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SCAN_LE = 2;

    private String blunoAddr = "D0:39:72:A0:CC:15";
    private MyBlunoCallback callback;
    private BluetoothGatt gatt;
    private Button toggleLedButton;
    private boolean requestEnableBluetooth = true;

    private EditText editCommand;
    private TextView textView;
    private Switch connectionStatus;
    private Switch ledStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleLedButton = this.findViewById(R.id.toggleLedButton);
        toggleLedButton.setOnClickListener((v)->{write("LED1\n");});
        Button scanButton = this.findViewById(R.id.scanButton);
        Intent scanIntent = new Intent(this, ScanActivity.class);
        scanButton.setOnClickListener((v)->{
            this.disconnectBluno();
            startActivityForResult(scanIntent, REQUEST_SCAN_LE);
        });
        textView = this.findViewById(R.id.textView);
        editCommand = this.findViewById(R.id.editCommand);
        Button sendCommandButton = this.findViewById(R.id.sendCommand);
        sendCommandButton.setOnClickListener((v)->{
            String s = editCommand.getText().toString() + "\n";
            write(s);
        });

        connectionStatus = this.findViewById(R.id.connectionStatus);
        ledStatus = this.findViewById(R.id.ledStatus);
    }


    public void setConnectionStatus(boolean status){
        this.runOnUiThread(() -> connectionStatus.setChecked(status));
    }

    public void setLedStatus(boolean status){
        this.runOnUiThread(() -> ledStatus.setChecked(status));
    }

    public void write(String command) {
        if ((callback != null)  && (callback.isConnected()) && (callback.isWriteReady())) {
            callback.write(command);
        } else {
            Toast toast = Toast.makeText(this, "Peripherique bluetooth non disponible", Toast.LENGTH_SHORT) ;
            toast.show();
        }
    }

    public String data = "";

    public void onNewData(String s) {
        data = data + s;

        while (data.contains("\n")) {
            String[] splitString = data.split("\n", 2);
            final String command = splitString[0];
            data = splitString[1];
            if ("LED1:on".equals(command)) {
                this.setLedStatus(true);
            }
            else if (("LED1:off".equals(command))) {
                this.setLedStatus(false);
            }
            this.runOnUiThread(() -> {
                textView.append(command + "\n");
            });
        }
    }

    private void connectBluno(){
        BluetoothAdapter bluetoothApdater = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothApdater == null ) {
            Log.e(LOG_TAG, "Ce peripherique ne supporte pas bluetooth");
            Toast.makeText(this, "Ce peripherique ne supporte pas bluetooth", Toast.LENGTH_LONG).show();
        } else if (!bluetoothApdater.isEnabled()) {
            Log.e(LOG_TAG, "Bluetooth non active");
            Toast.makeText(this, "Bluetooth non active", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (requestEnableBluetooth) startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            BluetoothDevice blunoDevice = bluetoothApdater.getRemoteDevice(blunoAddr);
            callback = new MyBlunoCallback(this);
            gatt = blunoDevice.connectGatt(this, false, callback);
        }
    }

    private void disconnectBluno(){
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    @Override
    protected void onStart() {
        requestEnableBluetooth = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.connectBluno();
        textView.setText(blunoAddr);
        textView.append("\n");
    }

    @Override
    protected void onPause() {
        this.disconnectBluno();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            //le bluetooth a ete active
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
            requestEnableBluetooth = false;
            Log.d(LOG_TAG, "L'unyilisateur n'a pas active l'adaptateur bluetooh");
        } else if (requestCode == REQUEST_SCAN_LE && resultCode == RESULT_OK){
            String newAdr = data.getStringExtra("deviceAddress");
            blunoAddr = newAdr;
        } else if (requestCode == REQUEST_SCAN_LE && resultCode == RESULT_OK){
            // on garde l'ancienne adrese
        }
    }
}
