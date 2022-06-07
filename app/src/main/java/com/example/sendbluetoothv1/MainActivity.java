package com.example.sendbluetoothv1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    private BluetoothChatService mChatService = null;
    /**
     * Array adapter for the conversation thread
     */
    private String mConnectedDeviceName = null;


    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private EditText et_name;
    private TextView tv_state,msg_tv;
    private Switch onOff_switch;
    private Button discoverable_btn,btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onOff_switch = findViewById(R.id.onOff_switch);
        discoverable_btn = findViewById(R.id.discoverable_btn);
        msg_tv = findViewById(R.id.msg_tv);
        btn_send = findViewById(R.id.btn_send);
        et_name = findViewById(R.id.et_name);

        //check if device supports bluetooth; if it does, then check bluetooth on/off and set switch checked/unchecked
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            //check bt state in bg
            checkBTStateBG();


            if (bluetoothAdapter.isEnabled()) {
                onOff_switch.setChecked(true);
                onOff_switch.setText("Turn off Bluetooth");
            } else {
                if(mChatService!=null){
                    mChatService.stop();
                }
                onOff_switch.setChecked(false);
                onOff_switch.setText("Turn on Bluetooth");
            }
            // all activity's click listeners
            clickListenersSetUp();
        }

    }

    private void checkBTStateBG() {
        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothAdapter.isEnabled() && !onOff_switch.isChecked()) {
                            onOff_switch.setChecked(true);
                            onOff_switch.setText("Turn off Bluetooth");
                        } else if (!bluetoothAdapter.isEnabled() && onOff_switch.isChecked()) {
                            if(mChatService!=null){
                                mChatService.stop();
                            }
                            onOff_switch.setChecked(false);
                            onOff_switch.setText("Turn on Bluetooth");
                        }
                    }
                });
            }
        };
        timer.schedule(myTask, 1000, 1000);
    }

    private void clickListenersSetUp() {
        //check state and turn on/off bluetooth
        onOff_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onOff_switch.isChecked()) {
                    turnOnBT();
                } else {
                    turnOffBT();
                }
            }
        });

        //enable discover mode
        discoverable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDiscoverable();
                mChatService=new BluetoothChatService(getApplicationContext(),mHandler); //here
                //AccpetThread accpetThread = new AccpetThread();
                //accpetThread.start();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                // check if device supports bluetooth
                if (bluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
                } else {

                    //check if bluetooth is enabled
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //display bluetooth permission
                        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);

                    } else {
                        // app location permission granted but not turned on
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.
                                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission
                                    .ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION
                            );
                        } else {
                            //check if location is on
                            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                buildAlertMessageNoGps();
                            } else {

                                if (mChatService != null && mChatService.getState() == Constants.STATE_CONNECTED) {
                                    if(!et_name.getText().toString().isEmpty()){
                                        //send text to remote device
                                        String dataToSend = et_name.getText().toString();
                                        sendData(dataToSend);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Can't send empty data", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else {
                                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                                    startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE_SECURE);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void turnOffBT() {
        if(mChatService!=null){
            mChatService.stop();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.disable();
        }
        Toast.makeText(MainActivity.this, "Bluetooth off", Toast.LENGTH_SHORT).show();
        onOff_switch.setText("Turn on Bluetooth");
    }

    private void turnOnBT() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //display bluetooth permission
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
    }



    private void makeDiscoverable() {
        onOff_switch.setChecked(true);
        onOff_switch.setText("Turn off Bluetooth");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (!bluetoothAdapter.isDiscovering()) {
                startActivityForResult(discoverableIntent, Constants.REQUEST_ENABLE_DISCOVERABLE);
            }
        }

    }

    //if permission granted or not nresult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request permission code is same as already declared 12

        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            //check if bluetooth granted ( if reslt code = 0 then no )
            if (resultCode ==Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.
                        permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission
                            .ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {

                    if (mChatService != null && mChatService.getState() == Constants.STATE_CONNECTED) {
                        //send text to remote device
                        String dataToSend = et_name.getText().toString();
                        sendData(dataToSend);
                    } else {
                        //check if location is on
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        }
                    }
                }

            } else {
                Toast.makeText(this, "The app requires bluetooth permission", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_CONNECT_DEVICE_SECURE) {
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(getApplicationContext(), mHandler);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer();

        }


        // Get the device MAC address
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        Log.d("dev", device.toString());
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        bluetoothAdapter.disable();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            tv_state = findViewById(R.id.tv_state);
            Context activity = getApplicationContext();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            // set state text
                            tv_state.setText("State : Connected to " + mConnectedDeviceName);
                            break;
                        case Constants.STATE_CONNECTING:
                            Toast.makeText(activity, "Connecting", Toast.LENGTH_SHORT).show();
                            // set state text
                            tv_state.setText("State : Connecting ");
                            break;
                        case Constants.STATE_LISTEN:
                            tv_state.setText("State : Listening ");
                        case Constants.STATE_NONE:
                            // set state text
                            tv_state.setText("State : Not connected ");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_tv.setText(tempMsg);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        //send text to remote device
                        String dataToSend = et_name.getText().toString();
                        sendData(dataToSend);
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                        tv_state.setText("State : Not connected ");

                    }
                    break;
            }
        }
    };

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have to enable your GPS!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendData(String message) {
        if (message.length() > 0) {

            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            et_name.setText("");
        }
    }

}