# Current Status:
Currently, pressing the "GET A2DP PROXY" button will retrieve the profile proxy associated with the A2DP profile. Since
A2DP Source is enabled by default, this returns what we ask for (but don't need, in this case).
Pressing the "GET A2DP SINK PROXY" button will attempt to retrieve the A2dp sink profile and proxy. There is a log 
statement which says the following when this is attempted:

`06-08 10:10:42.739 4481-4481/com.example.jimbo.bluetootha2dp D/BluetoothA2dpSink: doBind(): CallingUid(myUserHandle) = 0`

Since the BluetoothA2dpSink class is part of the hidden API, I created a small helper class (similar to the one in the 
resources below) to to register for the necessary broadcasts and to request the proper proxy.  The problem with the 
one in the resources is that it uses the BluetoothProfileManager from the Android Things library to disable A2DP and enable
A2DP_SINK.  The source code is not available for these libraries because it is still in the preview state, I believe.

It seems most the solutions I've seen in Stack Overflow have to deal with rooting the device and doing a bit of C/C++ work 
on the Bluetooth stack using BlueZ

# Resources
* [Android Things A2DP Sink](https://github.com/androidthings/sample-bluetooth-audio)
* [Android Docs - Bluetooth Overview](https://developer.android.com/guide/topics/connectivity/bluetooth)
* [Android Docs - BluetoothA2dp](https://developer.android.com/reference/android/bluetooth/BluetoothA2dp)

Stack Overflow
* [How to stream from one device to another](https://stackoverflow.com/questions/16789394/how-to-stream-audio-from-one-android-device-to-another-android-device-via-blueto)
  * [Receive audio via Bluetooth](https://stackoverflow.com/questions/15557933/receive-audio-via-bluetooth-in-android/15559902#15559902)
  * [How to stream audio](https://android.stackexchange.com/questions/40810/how-can-i-stream-audio-from-another-device-via-bluetooth/41486#41486)
