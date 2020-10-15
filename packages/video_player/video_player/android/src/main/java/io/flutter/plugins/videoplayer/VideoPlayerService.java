package io.flutter.plugins.videoplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class VideoPlayerService extends Service {

    private final LongSparseArray<VideoPlayer> videoPlayers = new LongSparseArray<>();
    private long lastTextureId;
    private String title;

    private VideoPlayer getLastPlayer() {
        return videoPlayers.get(lastTextureId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new VideoPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public String getTitle() {
        return title;
    }

    public void putPlayer(Long textureId, VideoPlayer player, String title) {
        videoPlayers.put(textureId, player);
        lastTextureId = textureId;
        this.title = title;
    }

    public void play(Long textureId) {
        videoPlayers.get(textureId).play();
    }

    public void play() {
        getLastPlayer().play();
    }

    public void pause(Long textureId) {
        videoPlayers.get(textureId).pause();
    }

    public void pause() {
        getLastPlayer().pause();
    }

    public void setLooping(Long textureId, boolean isLooping) {
        videoPlayers.get(textureId).setLooping(isLooping);
    }

    public void setVolume(Long textureId, double volume) {
        videoPlayers.get(textureId).setVolume(volume);
    }

    public void setPlaybackSpeed(Long textureId, double speed) {
        videoPlayers.get(textureId).setPlaybackSpeed(speed);
    }

    public void dispose(Long textureId) {
        videoPlayers.get(textureId).dispose();
        videoPlayers.remove(textureId);
    }

    public Messages.PositionMessage position(Long textureId) {
        VideoPlayer player = videoPlayers.get(textureId);
        Messages.PositionMessage result = new Messages.PositionMessage();
        result.setPosition(player.getPosition());
        player.sendBufferingUpdate();
        return result;
    }

    public void seekTo(Long textureId, int position) {
        videoPlayers.get(textureId).seekTo(position);
    }

    public void disposeAllPlayers() {
        for (int i = 0; i < videoPlayers.size(); i++) {
            videoPlayers.valueAt(i).dispose();
        }
        videoPlayers.clear();
    }

    public class VideoPlayerBinder extends Binder {
        public VideoPlayerService getService() {
            return VideoPlayerService.this;
        }
    }

}

