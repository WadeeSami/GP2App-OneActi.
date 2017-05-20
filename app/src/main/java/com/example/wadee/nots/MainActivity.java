package com.example.wadee.nots;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private String TAG = "GradPro";
    private TextView txtView;
    private NotificationReceiver nReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread dataSendingThread;
    private BluetoothDevice mDevice = null;
    private boolean b = false;

    private Enum currentMode = Mode.NOTIFICATIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.wadee.nots.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver, filter);

        //handle bluetooth
        this.intiateBluetoothConfig();

        Log.i(TAG, "Start getting available devices");
        this.getAvailableDevicesAndStartConnection();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }


    public void buttonClicked(View v) {

        if (v.getId() == R.id.gameModeBtn) {
            if (this.currentMode == Mode.SNAKE) {
                Toast.makeText(this, "You are already in the Snake mode", Toast.LENGTH_LONG).show();
            } else {
                this.currentMode = Mode.SNAKE;
            }

            if (b) {
                this.dataSendingThread.write("facebook");
            } else {
                this.dataSendingThread.write("whatsapp");
            }
            b = !b;
        }

        if (v.getId() == R.id.musicModeBtn) {

            if (this.currentMode == Mode.MUSIC) {
                Toast.makeText(this, "You are already in the Music mode", Toast.LENGTH_LONG).show();
            } else {
                this.currentMode = Mode.MUSIC;
            }
            Log.i(TAG, "Music btn clicked");

            if (b) {
                this.dataSendingThread.write("music");
            }
        }

        if (v.getId() != R.id.notificationModeBtn) {

            if (this.currentMode == Mode.NOTIFICATIONS) {
                Toast.makeText(this, "You are already in the Notification mode", Toast.LENGTH_LONG).show();
            } else {
                this.currentMode = Mode.NOTIFICATIONS;
            }
            Log.i(TAG, "Music btn clicked");

            if (b) {
                this.dataSendingThread.write("music");
            }
        }

        //handle music buttons
        if(v.getId() == R.id.play) {
            if(this.currentMode == Mode.MUSIC) {
                this.dataSendingThread.write("Play");
            }else{
                Toast.makeText(this, "You are not in Music mode", Toast.LENGTH_LONG).show();
            }
        }

        if(v.getId() == R.id.next) {
            if(this.currentMode == Mode.MUSIC) {
                this.dataSendingThread.write("Next");
            }else{
                Toast.makeText(this, "You are not in Music mode", Toast.LENGTH_LONG).show();
            }
        }

        if(v.getId() == R.id.pause) {
            if(this.currentMode == Mode.MUSIC) {
                this.dataSendingThread.write("Pause");
            }else{
                Toast.makeText(this, "You are not in Music mode", Toast.LENGTH_LONG).show();
            }
        }

        if(v.getId() == R.id.previous) {
            if(this.currentMode == Mode.MUSIC) {
                this.dataSendingThread.write("Previous");
            }else{
                Toast.makeText(this, "You are not in Music mode", Toast.LENGTH_LONG).show();
            }
        }

        ///////////handle game mode buttons
        if(v.getId() == R.id.left) {
            if(this.currentMode == Mode.SNAKE) {
                this.dataSendingThread.write("Left");
            }else{
                Toast.makeText(this, "You are not in Game mode", Toast.LENGTH_LONG).show();
            }
        }

        if(v.getId() == R.id.right) {
            if(this.currentMode == Mode.SNAKE) {
                this.dataSendingThread.write("Right");
            }else{
                Toast.makeText(this, "You are not in Game mode", Toast.LENGTH_LONG).show();
            }
        }


        if(v.getId() == R.id.down) {
            if(this.currentMode == Mode.SNAKE) {
                this.dataSendingThread.write("Down");
            }else{
                Toast.makeText(this, "You are not in Game mode", Toast.LENGTH_LONG).show();
            }
        }

        if(v.getId() == R.id.up) {
            if(this.currentMode == Mode.SNAKE) {
                this.dataSendingThread.write("Up");
            }else{
                Toast.makeText(this, "You are not in Game mode", Toast.LENGTH_LONG).show();
            }
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event") + "\n";//+ txtView.getText();
            //send data here to arduino

            if(MainActivity.this.currentMode == Mode.NOTIFICATIONS) {
                txtView.setText(temp);
                String notType = "";
                if(temp.contains("whatsapp")){
                    Toast.makeText(MainActivity.this,"Whatsapp not", Toast.LENGTH_LONG).show();
                    notType = "whatsapp";
                }

                if(temp.contains("facebook")){
                    Toast.makeText(MainActivity.this,"Facebook not", Toast.LENGTH_LONG).show();
                    notType = "facebook";
                }
                MainActivity.this.dataSendingThread.write(notType);
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.i(TAG, "Connected to device successfuly");
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    connectException.printStackTrace();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }

            MainActivity.this.dataSendingThread = new ConnectedThread(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.i(TAG, "GEtting input and output streams");
        }

        public void run() {
//            byte[] buffer = new byte[1024];
//            int begin = 0;
//            int bytes = 0;
//            while (true) {
//                try {
//                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
//                    for(int i = begin; i < bytes; i++) {
//                        if(buffer[i] == "#".getBytes()[0]) {
//                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
//                            begin = i + 1;
//                            if(i == bytes - 1) {
//                                bytes = 0;
//                                begin = 0;
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//                    break;
//                }
//            }
        }

        public void write(String bytes) {
            try {
                bytes += "\n";
                mmOutStream.write(bytes.getBytes());
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private void intiateBluetoothConfig() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Sorry, Your Wretched device does not support Bluetooth", Toast.LENGTH_LONG).show();
        }
        //check if the bluetoooth is not enabled and show some UI to open Bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

    }

    private void getAvailableDevicesAndStartConnection() {
        Set<BluetoothDevice> pairedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().toString().equals(Config.CHOSEN_DEVICE)) {
                    mDevice = device;
                    Log.i(TAG, "This is the chosen device" + mDevice.getName());
                }

            }
        }

        Log.i(TAG, "Start Connection operation");
        //start the connection thread
        mConnectThread = new ConnectThread(mDevice);
        mConnectThread.start();
    }

    private enum Mode {MUSIC, NOTIFICATIONS, SNAKE}
}
