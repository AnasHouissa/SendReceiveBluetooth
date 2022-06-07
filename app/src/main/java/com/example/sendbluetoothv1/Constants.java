/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sendbluetoothv1;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;


    int REQUEST_CODE_LOCATION_PERMISSION = 10;
    int REQUEST_CONNECT_DEVICE_SECURE = 20;
    int REQUEST_ENABLE_BT = 30;
    int REQUEST_ENABLE_DISCOVERABLE = 40;

    // Constants that indicate the current connection state
    int STATE_NONE = 100;       // we're doing nothing
    int STATE_LISTEN = 200;     // now listening for incoming connections
    int STATE_CONNECTING = 300; // now initiating an outgoing connection
    int STATE_CONNECTED = 400;  // now connected to a remote device



    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

}
