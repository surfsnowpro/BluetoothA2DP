package com.example.jimbo.bluetootha2dp;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String BT_ADVERTISED_NAME = "SharkExp#1";

    public static final int REQUEST_DISCOVERABLE = 2;

    private static final int DISCOVERABLE_DURATION = 120;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothProfile a2dpProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_discover);
        button.setOnClickListener(view -> {
            // This seems to hand on the onBind() method in the BluetoothA2dpSink.java class
            // No errors are being thrown.
            bluetoothAdapter.getProfileProxy(this, profileServiceListener, A2dpSinkHelper.A2DP_SINK_PROFILE);
            // When using A2DP, we get a callback to the ServiceListener that we've received the proxy
//            bluetoothAdapter.getProfileProxy(this, profileServiceListener, BluetoothProfile.A2DP);
            Log.d(TAG, "onCreate: getting a2dp profile");
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName(BT_ADVERTISED_NAME);
        if (bluetoothAdapter == null) {
            Log.e(TAG, "onCreate: no bluetooth" );
            return;
        }
//        setupBTProfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);

        IntentFilter a2dpSinkStateFilter = new IntentFilter();
        a2dpSinkStateFilter.addAction(A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED);

        IntentFilter a2dpSinkPlayStateFilter = new IntentFilter();
        a2dpSinkPlayStateFilter.addAction(A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED);

        registerReceiver(a2dpSinkStateChangedReceiver, a2dpSinkStateFilter);
        registerReceiver(a2dpSinkPlayStateChangedReceiver, a2dpSinkPlayStateFilter);
        registerReceiver(btReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(btReceiver);
        unregisterReceiver(a2dpSinkStateChangedReceiver);
        unregisterReceiver(a2dpSinkPlayStateChangedReceiver);
    }


    private void makeDiscoverable() {
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        // This will automatically enable BT
        startActivityForResult(discoverIntent, REQUEST_DISCOVERABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode);
        switch (requestCode) {
            case REQUEST_DISCOVERABLE:
                if (resultCode == DISCOVERABLE_DURATION) {
                    Log.d(TAG, "onActivityResult: device now discoverable");
                }
                break;
        }
    }

    /**
     * This was used for trying out the Android Things API with no luck.
     * I tried adding the <uses-library android:name="com.google.android.things" /> in the
     * manifest, but it seems it is incompatible with regular android devices?
     */
//    public void setupBTProfiles() {
//        BluetoothProfileManager bluetoothProfileManager = BluetoothProfileManager.getInstance();
//        List<Integer> enabledProfiles = bluetoothProfileManager.getEnabledProfiles();
//        if (!enabledProfiles.contains(A2dpSinkHelper.A2DP_SINK_PROFILE)) {
//            Log.d(TAG, "setupBTProfiles: Enabling A2dp sink");
//            List<Integer> toDisable = Arrays.asList(BluetoothProfile.A2DP);
//            List<Integer> toEnable = Arrays.asList(
//                    A2dpSinkHelper.A2DP_SINK_PROFILE,
//                    A2dpSinkHelper.AVRCP_CONTROLLER_PROFILE
//            );
//            bluetoothProfileManager.enableAndDisableProfiles(toEnable, toDisable);
//        }
//        else {
//            Log.d(TAG, "setupBTProfiles: A2dp sink is enabled");
//        }
//    }

    BroadcastReceiver a2dpSinkStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: sink state");
//            if (intent.getAction().equals(A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED)) {
            if (intent.getAction().equals(A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                int prevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
                Log.d(TAG, "onReceive: sink state changed: state" + state + ", prevState: " + prevState);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (state == BluetoothAdapter.STATE_CONNECTED) {
                        Log.d(TAG, "onReceive: " + device.getName() + " is connected.");
                    }
                    else {
                        Log.d(TAG, "onReceive: " + device.getName() + " disconnected");
                    }
                }
            }
        }
    };

    BroadcastReceiver a2dpSinkPlayStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: play state");
        }
    };

    BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                        //  20: SCAN_MODE_NONE
                        //  21: SCAN_MODE_CONNECTABLE
                        //  23: SCAN_MODE_CONNECTABLE_DISCOVERABLE
                        Log.d(TAG, "onReceive: scan mode: " + intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1));
                        break;
                    case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                        // STATE
                        //   0: STATE_DISCONNECTED
                        //   1: STATE_CONNECTING
                        //   2: STATE_CONNECTED
                        //   3: STATE_DISCONNECTING
                        Log.d(TAG, "onReceive: adapter connection state changed: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1));
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            Log.d(TAG, "onReceive: device found: " + device.getName());
                        }
                        break;
                    case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                        // STATE
                        //   0: STATE_DISCONNECTED
                        //   1: STATE_CONNECTING
                        //   2: STATE_CONNECTED
                        //   3: STATE_DISCONNECTING
                        int currentState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                        int previousState = intent.getIntExtra(BluetoothA2dp.EXTRA_PREVIOUS_STATE, -1);
                        Log.d(TAG, "onReceive: a2dp connection state: " + currentState);
                        Log.d(TAG, "onReceive: a2dp previous connection state: " + previousState);
                        if (currentState == BluetoothA2dp.STATE_CONNECTED) {
                            BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            Log.d(TAG, "onReceive: device found" + device2.getName());
                        }
                        break;
//                    case BluetoothAdapter.ACTION_PLAYING_STATE_CHANGED:
//                        // STATE
//                        //   10: STATE_PLAYING
//                        //   11: STATE_NOT_PLAYING
//                        int currentPlayState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
//                        int previousPlayState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
//                        Log.d(TAG, "onReceive: playing state: " + currentPlayState);
//                        Log.d(TAG, "onReceive: previous playing state: " + previousPlayState);
//                        break;
                }
            }
        }
    };

    private BluetoothProfile.ServiceListener profileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG, "onServiceConnected: " + profile);
            if (profile == A2dpSinkHelper.A2DP_SINK_PROFILE) {
                a2dpProfile = proxy;
                makeDiscoverable();
                Log.d(TAG, "onServiceConnected: retrieved A2DP sink profile!");
                Log.d(TAG, "onServiceConnected: proxy devices: " + proxy.getConnectedDevices());
            }
            else if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = proxy;
                makeDiscoverable();
                Log.d(TAG, "onServiceConnected: received A2DP profile!");
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG, "onServiceDisconnected: profile: " + profile);
            if (profile == BluetoothProfile.A2DP) {
                Log.d(TAG, "onServiceDisconnected: A2DP disconnected");
            }
        }
    };
}
