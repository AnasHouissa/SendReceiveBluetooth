package com.example.sendbluetoothv1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int REQUEST_ENABLE_BT = 12;

        EditText et_name = findViewById(R.id.et_name);
        Button btn_send = findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                // check if device supports bluetooth
                if (bluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
                } else {
                    //check if bluetooth is enabled
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //display bluetooth permission
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                    } else {
                        Toast.makeText(MainActivity.this, "show availaible ppl", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    //if permission granted or not nresult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request permission code is same as already declared 12

        if (requestCode == 12) {
            //check if bluetooth granted ( if reslt code = 0 then no )
            if (resultCode != 0) {
                Toast.makeText(this, "show availaible ppl", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "The app requires bluetooth permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}