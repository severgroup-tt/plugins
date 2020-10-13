package io.flutter.plugins.videoplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.LongSparseArray;

public class VideoPlayerService extends Service {

    private final LongSparseArray<VideoPlayer> videoPlayers = new LongSparseArray<>();

    @Override
    public IBinder onBind(Intent intent) {
        return new VideoPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void putPlayer(Long textureId, VideoPlayer player) {
        videoPlayers.put(textureId, player);
    }

    public void play(Long textureId) {
        videoPlayers.get(textureId).play();
    }

    public void pause(Long textureId) {
        videoPlayers.get(textureId).pause();
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

