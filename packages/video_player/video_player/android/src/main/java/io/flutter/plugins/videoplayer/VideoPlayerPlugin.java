// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.videoplayer.Messages.CreateMessage;
import io.flutter.plugins.videoplayer.Messages.LoopingMessage;
import io.flutter.plugins.videoplayer.Messages.MixWithOthersMessage;
import io.flutter.plugins.videoplayer.Messages.PlaybackSpeedMessage;
import io.flutter.plugins.videoplayer.Messages.PositionMessage;
import io.flutter.plugins.videoplayer.Messages.TextureMessage;
import io.flutter.plugins.videoplayer.Messages.VideoPlayerApi;
import io.flutter.plugins.videoplayer.Messages.VolumeMessage;
import io.flutter.view.TextureRegistry;

/**
 * Android platform implementation of the VideoPlayerPlugin.
 */
public class VideoPlayerPlugin implements FlutterPlugin, VideoPlayerApi {
    public static final int PLAYER_NOTIFICATION_ID = 1;

    private static final String TAG = "VideoPlayerPlugin";
    private FlutterState flutterState;
    private VideoPlayerOptions options = new VideoPlayerOptions();

    private RemoteButtonsApi remoteButtonsApi;

    private VideoPlayerService service;
    private boolean isServiceBound = false;

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    private PlayerNotificationManager notificationManager;

    /**
     * Register this with the v2 embedding for the plugin to respond to lifecycle callbacks.
     */
    public VideoPlayerPlugin() {
    }

    @SuppressWarnings("deprecation")
    private VideoPlayerPlugin(io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
        this.flutterState =
                new FlutterState(
                        registrar.context(),
                        registrar.messenger(),
                        registrar::lookupKeyForAsset,
                        registrar::lookupKeyForAsset,
                        registrar.textures());
        flutterState.startListening(this, registrar.messenger());
    }

    /**
     * Registers this with the stable v1 embedding. Will not respond to lifecycle events.
     */
    @SuppressWarnings("deprecation")
    public static void registerWith(io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
        final VideoPlayerPlugin plugin = new VideoPlayerPlugin(registrar);
        registrar.addViewDestroyListener(
                view -> {
                    plugin.onDestroy();
                    return false; // We are not interested in assuming ownership of the NativeView.
                });
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                HttpsURLConnection.setDefaultSSLSocketFactory(new CustomSSLSocketFactory());
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                Log.w(
                        TAG,
                        "Failed to enable TLSv1.1 and TLSv1.2 Protocols for API level 19 and below.\n"
                                + "For more information about Socket Security, please consult the following link:\n"
                                + "https://developer.android.com/reference/javax/net/ssl/SSLSocket",
                        e);
            }
        }

        @SuppressWarnings("deprecation") final FlutterLoader flutterLoader = FlutterLoader.getInstance();
        this.flutterState =
                new FlutterState(
                        binding.getApplicationContext(),
                        binding.getBinaryMessenger(),
                        flutterLoader::getLookupKeyForAsset,
                        flutterLoader::getLookupKeyForAsset,
                        binding.getTextureRegistry());
        flutterState.startListening(this, binding.getBinaryMessenger());

        this.remoteButtonsApi = new RemoteButtonsApi(flutterState.binaryMessenger);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        if (flutterState == null) {
            Log.wtf(TAG, "Detached from the engine before registering to it.");
        }
        flutterState.stopListening(binding.getBinaryMessenger());
        flutterState = null;
    }

    private void disposeAllPlayers() {
        if (service != null) {
            service.disposeAllPlayers();
        }
    }

    private void onDestroy() {
        // The whole FlutterView is being destroyed. Here we release resources acquired for all
        // instances
        // of VideoPlayer. Once https://github.com/flutter/flutter/issues/19358 is resolved this may
        // be replaced with just asserting that videoPlayers.isEmpty().
        // https://github.com/flutter/flutter/issues/20989 tracks this.
        disposeAllPlayers();

        if (isServiceBound) {
            service.stopSelf();
        }
    }

    public void initialize() {
        disposeAllPlayers();
    }

    void initService(Context context) {
        mediaSession = new MediaSessionCompat(context, context.getPackageName());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);

        if (!isServiceBound) {
            Intent intent = new Intent(context, VideoPlayerService.class);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

        createNotificationChannel(
                context,
                context.getString(R.string.player_notification_channel_id),
                context.getString(R.string.player_notification_channel_name)
        );
    }

    private void createNotificationChannel(Context context, String channelId, String channelName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
    }

    public TextureMessage create(CreateMessage arg) {
        service.startForeground(
                PLAYER_NOTIFICATION_ID,
                new NotificationCompat
                        .Builder(service, service.getString(R.string.player_notification_channel_id))
                        .build()
        );

        TextureRegistry.SurfaceTextureEntry handle =
                flutterState.textureRegistry.createSurfaceTexture();
        EventChannel eventChannel =
                new EventChannel(
                        flutterState.binaryMessenger, "flutter.io/videoPlayer/videoEvents" + handle.id());

        VideoPlayer player;
        if (arg.getAsset() != null) {
            String assetLookupKey;
            if (arg.getPackageName() != null) {
                assetLookupKey =
                        flutterState.keyForAssetAndPackageName.get(arg.getAsset(), arg.getPackageName());
            } else {
                assetLookupKey = flutterState.keyForAsset.get(arg.getAsset());
            }
            player =
                    new VideoPlayer(
                            flutterState.applicationContext,
                            eventChannel,
                            handle,
                            "asset:///" + assetLookupKey,
                            null,
                            options);
        } else {
            player =
                    new VideoPlayer(
                            flutterState.applicationContext,
                            eventChannel,
                            handle,
                            arg.getUri(),
                            arg.getFormatHint(),
                            options);
        }
        service.putPlayer(handle.id(), player, arg.getTitle());
        mediaSessionConnector.setPlayer(player.exoPlayer);
        mediaSession.setActive(true);

        notificationManager.setPlayer(player.exoPlayer);

        TextureMessage result = new TextureMessage();
        result.setTextureId(handle.id());
        return result;
    }

    public void dispose(TextureMessage arg) {
        service.stopForeground(true);
        service.dispose(arg.getTextureId());
    }

    public void setLooping(LoopingMessage arg) {
        service.setLooping(arg.getTextureId(), arg.getIsLooping());
    }

    public void setVolume(VolumeMessage arg) {
        service.setVolume(arg.getTextureId(), arg.getVolume());
    }

    public void setPlaybackSpeed(PlaybackSpeedMessage arg) {
        service.setPlaybackSpeed(arg.getTextureId(), arg.getSpeed());
    }

    public void play(TextureMessage arg) {
        service.play(arg.getTextureId());
    }

    public PositionMessage position(TextureMessage arg) {
        return service.position(arg.getTextureId());
    }

    public void seekTo(PositionMessage arg) {
        service.seekTo(arg.getTextureId(), arg.getPosition().intValue());
    }

    public void pause(TextureMessage arg) {
        service.pause(arg.getTextureId());
    }

    @Override
    public void setMixWithOthers(MixWithOthersMessage arg) {
        options.mixWithOthers = arg.getMixWithOthers();
    }

    private interface KeyForAssetFn {
        String get(String asset);
    }

    private interface KeyForAssetAndPackageName {
        String get(String asset, String packageName);
    }

    private static final class FlutterState {
        private final Context applicationContext;
        private final BinaryMessenger binaryMessenger;
        private final KeyForAssetFn keyForAsset;
        private final KeyForAssetAndPackageName keyForAssetAndPackageName;
        private final TextureRegistry textureRegistry;

        FlutterState(
                Context applicationContext,
                BinaryMessenger messenger,
                KeyForAssetFn keyForAsset,
                KeyForAssetAndPackageName keyForAssetAndPackageName,
                TextureRegistry textureRegistry) {
            this.applicationContext = applicationContext;
            this.binaryMessenger = messenger;
            this.keyForAsset = keyForAsset;
            this.keyForAssetAndPackageName = keyForAssetAndPackageName;
            this.textureRegistry = textureRegistry;
        }

        void startListening(VideoPlayerPlugin methodCallHandler, BinaryMessenger messenger) {
            methodCallHandler.initService(applicationContext);
            VideoPlayerApi.setup(applicationContext, messenger, methodCallHandler);
        }

        void stopListening(BinaryMessenger messenger) {
            VideoPlayerApi.setup(applicationContext, messenger, null);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("VideoPlayerPlugin", "[VideoPlayerApi] onServiceConnected");

            service = ((VideoPlayerService.VideoPlayerBinder) iBinder).getService();
            isServiceBound = true;
            notificationManager = new PlayerNotificationManager(
                    service,
                    service.getString(R.string.player_notification_channel_id),
                    PLAYER_NOTIFICATION_ID,
                    new PlayerNotificationManager.MediaDescriptionAdapter() {

                        @Override
                        public CharSequence getCurrentContentTitle(Player player) {
                            final TrackMeta meta = remoteButtonsApi.getTrackMeta();
                            return meta != null ? meta.title : null;
                        }

                        @Override
                        public PendingIntent createCurrentContentIntent(Player player) {
                            return null;
                        }

                        @Override
                        public CharSequence getCurrentContentText(Player player) {
                            final TrackMeta meta = remoteButtonsApi.getTrackMeta();
                            return meta != null ? meta.albumTitle : null;
                        }

                        @Override
                        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                            return null;
                        }
                    }
            );
            notificationManager.setControlDispatcher(new DefaultControlDispatcher(0, 0) {
                @Override
                public boolean dispatchPrevious(Player player) {
                    remoteButtonsApi.onPreviousTap();
                    return super.dispatchPrevious(player);
                }

                @Override
                public boolean dispatchNext(Player player) {
                    remoteButtonsApi.onNextTap();
                    return super.dispatchNext(player);
                }
            });
            notificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };
}
