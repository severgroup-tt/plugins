package io.flutter.plugins.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.List;

public class VideoPlayerService extends Service {
    public static final int PLAYER_NOTIFICATION_ID = 1;

    private final LongSparseArray<VideoPlayer> videoPlayers = new LongSparseArray<>();
    private long lastTextureId;

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    RemoteButtonsApi remoteButtonsApi;

    private PlayerNotificationManager notificationManager;

    private VideoPlayer getLastPlayer() {
        return videoPlayers.get(lastTextureId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new VideoPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(
                PLAYER_NOTIFICATION_ID,
                new NotificationCompat
                        .Builder(this, getString(R.string.player_notification_channel_id))
                        .build()
        );

        notificationManager = new PlayerNotificationManager(
                this,
                getString(R.string.player_notification_channel_id),
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
                return true;
            }

            @Override
            public boolean dispatchNext(Player player) {
                remoteButtonsApi.onNextTap();
                return true;
            }
        });

        mediaSession = new MediaSessionCompat(this, getPackageName());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);

        createNotificationChannel(
                this,
                getString(R.string.player_notification_channel_id),
                getString(R.string.player_notification_channel_name)
        );

        notificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        return START_STICKY;
    }

    void initWithPlayer(Player player) {
        final Player proxyPlayer = new PlayerDelegate(
                player,
                () -> remoteButtonsApi.getTrackMeta() != null && remoteButtonsApi.getTrackMeta().hasNext,
                () -> remoteButtonsApi.getTrackMeta() != null && remoteButtonsApi.getTrackMeta().hasPrevious
        );

        mediaSessionConnector.setPlayer(proxyPlayer);
        mediaSession.setActive(true);
        notificationManager.setPlayer(proxyPlayer);
    }
    
    public class VideoPlayerBinder extends Binder {
        public VideoPlayerService getService() {
            return VideoPlayerService.this;
        }
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
}

