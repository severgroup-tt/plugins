package io.flutter.plugins.videoplayer;

public class TrackMeta {
    final boolean hasNext;
    final boolean hasPrevious;
    final String title;
    final String albumTitle;
    final double duration;
    final double position;

    public TrackMeta(boolean hasNext, boolean hasPrevious, String title, String albumTitle, double duration, double position) {
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.title = title;
        this.albumTitle = albumTitle;
        this.duration = duration;
        this.position = position;
    }
}
