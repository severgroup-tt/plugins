package io.flutter.plugins.videoplayer;

import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import java.util.List;

public class PlayerDelegate implements Player {

    private final Player player;
    private final BooleanFunction hasNext;
    private final BooleanFunction hasPrevious;

    public PlayerDelegate(Player player, BooleanFunction hasNext, BooleanFunction hasPrevious) {
        this.player = player;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    @Override
    @Nullable
    public AudioComponent getAudioComponent() {
        return player.getAudioComponent();
    }

    @Override
    @Nullable
    public VideoComponent getVideoComponent() {
        return player.getVideoComponent();
    }

    @Override
    @Nullable
    public TextComponent getTextComponent() {
        return player.getTextComponent();
    }

    @Override
    @Nullable
    public MetadataComponent getMetadataComponent() {
        return player.getMetadataComponent();
    }

    @Override
    @Nullable
    public DeviceComponent getDeviceComponent() {
        return player.getDeviceComponent();
    }

    @Override
    public Looper getApplicationLooper() {
        return player.getApplicationLooper();
    }

    @Override
    public void addListener(EventListener listener) {
        player.addListener(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        player.removeListener(listener);
    }

    @Override
    public void setMediaItems(List<MediaItem> mediaItems) {
        player.setMediaItems(mediaItems);
    }

    @Override
    public void setMediaItems(List<MediaItem> mediaItems, boolean resetPosition) {
        player.setMediaItems(mediaItems, resetPosition);
    }

    @Override
    public void setMediaItems(List<MediaItem> mediaItems, int startWindowIndex, long startPositionMs) {
        player.setMediaItems(mediaItems, startWindowIndex, startPositionMs);
    }

    @Override
    public void setMediaItem(MediaItem mediaItem) {
        player.setMediaItem(mediaItem);
    }

    @Override
    public void setMediaItem(MediaItem mediaItem, long startPositionMs) {
        player.setMediaItem(mediaItem, startPositionMs);
    }

    @Override
    public void setMediaItem(MediaItem mediaItem, boolean resetPosition) {
        player.setMediaItem(mediaItem, resetPosition);
    }

    @Override
    public void addMediaItem(MediaItem mediaItem) {
        player.addMediaItem(mediaItem);
    }

    @Override
    public void addMediaItem(int index, MediaItem mediaItem) {
        player.addMediaItem(index, mediaItem);
    }

    @Override
    public void addMediaItems(List<MediaItem> mediaItems) {
        player.addMediaItems(mediaItems);
    }

    @Override
    public void addMediaItems(int index, List<MediaItem> mediaItems) {
        player.addMediaItems(index, mediaItems);
    }

    @Override
    public void moveMediaItem(int currentIndex, int newIndex) {
        player.moveMediaItem(currentIndex, newIndex);
    }

    @Override
    public void moveMediaItems(int fromIndex, int toIndex, int newIndex) {
        player.moveMediaItems(fromIndex, toIndex, newIndex);
    }

    @Override
    public void removeMediaItem(int index) {
        player.removeMediaItem(index);
    }

    @Override
    public void removeMediaItems(int fromIndex, int toIndex) {
        player.removeMediaItems(fromIndex, toIndex);
    }

    @Override
    public void clearMediaItems() {
        player.clearMediaItems();
    }

    @Override
    public void prepare() {
        player.prepare();
    }

    @Override
    public int getPlaybackState() {
        return player.getPlaybackState();
    }

    @Override
    public int getPlaybackSuppressionReason() {
        return player.getPlaybackSuppressionReason();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    @Nullable
    public ExoPlaybackException getPlayerError() {
        return player.getPlayerError();
    }

    @Override
    @Nullable
    @Deprecated
    public ExoPlaybackException getPlaybackError() {
        return player.getPlaybackError();
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    @Override
    public boolean getPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    @Override
    public int getRepeatMode() {
        return player.getRepeatMode();
    }

    @Override
    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
        player.setShuffleModeEnabled(shuffleModeEnabled);
    }

    @Override
    public boolean getShuffleModeEnabled() {
        return player.getShuffleModeEnabled();
    }

    @Override
    public boolean isLoading() {
        return player.isLoading();
    }

    @Override
    public void seekToDefaultPosition() {
        player.seekToDefaultPosition();
    }

    @Override
    public void seekToDefaultPosition(int windowIndex) {
        player.seekToDefaultPosition(windowIndex);
    }

    @Override
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        player.seekTo(windowIndex, positionMs);
    }

    @Override
    public boolean hasPrevious() {
        return hasPrevious.invoke();
    }

    @Override
    public void previous() {
        player.previous();
    }

    @Override
    public boolean hasNext() {
        return hasNext.invoke();
    }

    @Override
    public void next() {
        player.next();
    }

    @Override
    public void setPlaybackParameters(@Nullable PlaybackParameters playbackParameters) {
        player.setPlaybackParameters(playbackParameters);
    }

    @Override
    public PlaybackParameters getPlaybackParameters() {
        return player.getPlaybackParameters();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void stop(boolean reset) {
        player.stop(reset);
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public int getRendererCount() {
        return player.getRendererCount();
    }

    @Override
    public int getRendererType(int index) {
        return player.getRendererType(index);
    }

    @Override
    @Nullable
    public TrackSelector getTrackSelector() {
        return player.getTrackSelector();
    }

    @Override
    public TrackGroupArray getCurrentTrackGroups() {
        return player.getCurrentTrackGroups();
    }

    @Override
    public TrackSelectionArray getCurrentTrackSelections() {
        return player.getCurrentTrackSelections();
    }

    @Override
    @Nullable
    public Object getCurrentManifest() {
        return player.getCurrentManifest();
    }

    @Override
    public Timeline getCurrentTimeline() {
        return player.getCurrentTimeline();
    }

    @Override
    public int getCurrentPeriodIndex() {
        return player.getCurrentPeriodIndex();
    }

    @Override
    public int getCurrentWindowIndex() {
        return player.getCurrentWindowIndex();
    }

    @Override
    public int getNextWindowIndex() {
        return player.getNextWindowIndex();
    }

    @Override
    public int getPreviousWindowIndex() {
        return player.getPreviousWindowIndex();
    }

    @Override
    @Nullable
    @Deprecated
    public Object getCurrentTag() {
        return player.getCurrentTag();
    }

    @Override
    @Nullable
    public MediaItem getCurrentMediaItem() {
        return player.getCurrentMediaItem();
    }

    @Override
    public int getMediaItemCount() {
        return player.getMediaItemCount();
    }

    @Override
    public MediaItem getMediaItemAt(int index) {
        return player.getMediaItemAt(index);
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    @Override
    public int getBufferedPercentage() {
        return player.getBufferedPercentage();
    }

    @Override
    public long getTotalBufferedDuration() {
        return player.getTotalBufferedDuration();
    }

    @Override
    public boolean isCurrentWindowDynamic() {
        return player.isCurrentWindowDynamic();
    }

    @Override
    public boolean isCurrentWindowLive() {
        return player.isCurrentWindowLive();
    }

    @Override
    public long getCurrentLiveOffset() {
        return player.getCurrentLiveOffset();
    }

    @Override
    public boolean isCurrentWindowSeekable() {
        return player.isCurrentWindowSeekable();
    }

    @Override
    public boolean isPlayingAd() {
        return player.isPlayingAd();
    }

    @Override
    public int getCurrentAdGroupIndex() {
        return player.getCurrentAdGroupIndex();
    }

    @Override
    public int getCurrentAdIndexInAdGroup() {
        return player.getCurrentAdIndexInAdGroup();
    }

    @Override
    public long getContentDuration() {
        return player.getContentDuration();
    }

    @Override
    public long getContentPosition() {
        return player.getContentPosition();
    }

    @Override
    public long getContentBufferedPosition() {
        return player.getContentBufferedPosition();
    }
}
