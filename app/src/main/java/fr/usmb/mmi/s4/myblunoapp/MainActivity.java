package fr.usmb.mmi.s4.myblunoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private String blunoAddr = "D0:39:72:A0:CC:15";
    private MyBlunoCallback callback;
    private BluetoothGatt gatt;
    private Button toggleLedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleLedButton = this.findViewById(R.id.toggleLedButton);
        toggleLedButton.setOnClickListener((v)->{write("LED1\n");});
    }

    public void write(String command) {
        if ((callback != null)  && (callback.isConnected()) && (callback.isWriteReady())) {
            callback.write(command);
        } else {
            Toast toast = Toast.makeText(this, "Peripherique bluetooth non disponible", Toast.LENGTH_SHORT) ;
            toast.show();
        }
    }

    private void connectBluno(){
        BluetoothAdapter bluetoothApdater = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice blunoDevice = bluetoothApdater.getRemoteDevice(blunoAddr);
        callback = new MyBlunoCallback(this);
        gatt = blunoDevice.connectGatt(this, false, callback);
    }

    private void disconnectBluno(){
        gatt.disconnect();
        gatt.close();
    }

    @Override
    protected void onStart() {
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
    }

    @Override
    protected void onPause() {
        this.disconnectBluno();
        super.onPause();
    }
}
