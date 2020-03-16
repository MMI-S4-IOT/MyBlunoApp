package fr.usmb.mmi.s4.myblunoapp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

class MyBlunoCallback extends BluetoothGattCallback {

    private final String LOG_TAG = "BluetoothGattCallback";

    private MainActivity activity;
    private boolean connected = false;
    private BluetoothGatt gatt;
    private UUID SerialPortUUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private BluetoothGattCharacteristic serialCharacteristic;
    private boolean writing = false;


    public MyBlunoCallback(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        switch(newState){
            case BluetoothProfile.STATE_CONNECTED :
                this.gatt = gatt;
                this.setConnected(true);
                Log.d(LOG_TAG, "connecte au peripheique bluetooth");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_CONNECTING :
            case BluetoothProfile.STATE_DISCONNECTING :
            case BluetoothProfile.STATE_DISCONNECTED :
            default:
                Log.d(LOG_TAG, "deconnecte du peripheique bluetooth");
                this.setConnected(false);
                break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(LOG_TAG, "Services du peripheique bluetooth decouverts : OK");
            List<BluetoothGattService> services = gatt.getServices();
            Iterator<BluetoothGattService> it = services.iterator();
            while (it.hasNext() && serialCharacteristic == null){
                serialCharacteristic = it.next().getCharacteristic(SerialPortUUID);
            }
            if (serialCharacteristic != null) {
                Log.d(LOG_TAG, "Acces au port serie bluno: OK");
            } else {
                Log.d(LOG_TAG, "Acces au port serie bluno: OK");
            }
        } else {
            Log.d(LOG_TAG, "Echec de la recuperation de la liste des services");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            writing = false;
            Log.d(LOG_TAG, "Reussite envoie de la commande: ok" + new String(characteristic.getValue()));
        } else {
            Log.w(LOG_TAG, "Echec envoie de la commande: KO" + new String(characteristic.getValue()));
        }
    }

    public boolean isWriteReady(){
        return (connected && ! writing && serialCharacteristic != null);
    }

    public void write(String command){
        if (! writing && serialCharacteristic != null) {
            serialCharacteristic.setValue(command);
            if (gatt.writeCharacteristic(serialCharacteristic)) {
                writing = true;
                Log.d(LOG_TAG, "Envoie commande au peripherique bluetooth : OK");
            }
            else {
                Log.d(LOG_TAG, "Envoie commande au peripherique bluetooth : KO");
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }
}
