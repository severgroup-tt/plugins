package io.flutter.plugins.videoplayer;

import android.util.Log;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public class RemoteButtonsApi {
    private static final String TAG = "RemoteButtonsApi";

    private static final String CHANNEL_METHOD_NAME = "flutter.io/videoPlayer/callback";

    private static final String METHOD_ON_NEXT_TAP = "onNextTap";
    private static final String METHOD_ON_PREVIOUS_TAP = "onPreviousTap";
    private static final String METHOD_SET_TRACK_META = "setTrackMeta";

    private final MethodChannel callbackMethodChannel;

    private TrackMeta trackMeta;

    public RemoteButtonsApi(BinaryMessenger binaryMessenger) {
        callbackMethodChannel = new MethodChannel(binaryMessenger, CHANNEL_METHOD_NAME);
        callbackMethodChannel.setMethodCallHandler((call, result) -> {
            Log.d(TAG, "call " + call.method);

            if (call.method.equals(METHOD_SET_TRACK_META)) {
                final Map<String, Object> args = (Map<String, Object>) call.arguments;

                Log.d(TAG, "setTrackMeta() with arguments " + args.toString());

                trackMeta = new TrackMeta(
                        (boolean) args.get("has_next"),
                        (boolean) args.get("has_previous"),
                        (String) args.get("title"),
                        (String) args.get("album_title"),
                        (double) args.get("duration"),
                        (double) args.get("position")
                );

                result.success(null);
            }
        });
    }

    public TrackMeta getTrackMeta() {
        return trackMeta;
    }

    void onNextTap() {
        Log.d(TAG, "onNextTap()");
        callbackMethodChannel.invokeMethod(METHOD_ON_NEXT_TAP, null);
    }

    void onPreviousTap() {
        Log.d(TAG, "onPreviousTap()");
        callbackMethodChannel.invokeMethod(METHOD_ON_PREVIOUS_TAP, null);
    }
}
