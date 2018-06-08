package com.example.jimbo.bluetootha2dp;

public class A2dpSinkHelper {

    // These are all copied from BluetoothA2dpSink in the Android bluetooth package
    // since they are hidden from use
    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED";

    public static final String ACTION_PLAYING_STATE_CHANGED =
            "android.bluetooth.a2dp-sink.profile.action.PLAYING_STATE_CHANGED";

    public static final int STATE_PLAYING   =  10;
    public static final int STATE_NOT_PLAYING   =  11;

    public static final int A2DP_SINK_PROFILE = 11;
    public static final int AVRCP_CONTROLLER_PROFILE = 12;
}
