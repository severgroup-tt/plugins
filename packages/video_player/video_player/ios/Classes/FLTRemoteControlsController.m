//
//  FLTRemoteControlsController.m
//  video_player
//
//  Created by a.govyashov on 21.10.2020.
//

#import "FLTRemoteControlsController.h"
#import <Flutter/Flutter.h>

@implementation FLTRemoteControlsController

+ (instancetype)sharedController {
    static FLTRemoteControlsController *sharedController = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedController = [[self alloc] init];
    });
    return sharedController;
}

- (instancetype)init {
    if (self = [super init]) {
        [self registerRemoteControlCallbacks];
        [self registerVideoPlayerPlatformChannel];
    }
    
    return self;
}

- (void)registerRemoteControlCallbacks {
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
    
    MPRemoteCommandCenter *remoteCommandCenter = [MPRemoteCommandCenter sharedCommandCenter];
    remoteCommandCenter.playCommand.enabled = YES;
    remoteCommandCenter.pauseCommand.enabled = YES;
    
    if (@available(iOS 9.1, *)) {
        remoteCommandCenter.changePlaybackPositionCommand.enabled = NO;
    }
    
    [remoteCommandCenter.playCommand addTarget:self action:@selector(onTogglePlaybackTap)];
    [remoteCommandCenter.pauseCommand addTarget:self action:@selector(onTogglePlaybackTap)];
    [remoteCommandCenter.nextTrackCommand addTarget:self action:@selector(onNextTap)];
    [remoteCommandCenter.previousTrackCommand addTarget:self action:@selector(onPreviousTap)];
}

- (void)registerVideoPlayerPlatformChannel {
    id<FlutterBinaryMessenger> flutterViewController = (id <FlutterBinaryMessenger>)UIApplication.sharedApplication.keyWindow.rootViewController;
    
    if ([flutterViewController conformsToProtocol:@protocol(FlutterBinaryMessenger)]) {
        
        
        _platformChannel = [FlutterMethodChannel methodChannelWithName:@"flutter.io/videoPlayer/callback"
                                                       binaryMessenger:flutterViewController];
        __weak __typeof(self)weakSelf = self;
        [_platformChannel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
            __strong typeof(self)strongSelf = weakSelf;
            
            if ([call.method isEqualToString:@"setTrackMeta"]) {
                [strongSelf onSetTrackMeta:call.arguments];
            }
            
            result(@YES);
        }];
    }
}

- (void)setPlaybackRate:(double)playbackRate {
    _playbackRate = playbackRate;
    
    NSMutableDictionary *info = [[MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo mutableCopy];
    info[MPNowPlayingInfoPropertyPlaybackRate] = @(_playbackRate);
    [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = [info copy];
}

- (void)setPosition:(NSInteger)position duration:(NSInteger)duration {
    MPNowPlayingInfoCenter *nowPlayingCenter = [MPNowPlayingInfoCenter defaultCenter];
    
    NSMutableDictionary *info = [nowPlayingCenter.nowPlayingInfo mutableCopy];
    info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = @(position / 1000);
    info[MPMediaItemPropertyPlaybackDuration] = @(duration / 1000);
    nowPlayingCenter.nowPlayingInfo = [info copy];
}

- (void)onSetTrackMeta:(NSDictionary *)meta {
    MPRemoteCommandCenter *remoteCommandCenter = [MPRemoteCommandCenter sharedCommandCenter];
    MPNowPlayingInfoCenter *nowPlayingCenter = [MPNowPlayingInfoCenter defaultCenter];
    
    NSDictionary *arguments = meta;
    
    BOOL hasNext = ((NSNumber *)arguments[@"has_next"]).boolValue;
    remoteCommandCenter.nextTrackCommand.enabled = hasNext;
    
    BOOL hasPrevious = ((NSNumber *)arguments[@"has_previous"]).boolValue;
    remoteCommandCenter.previousTrackCommand.enabled = hasPrevious;
    
    NSString *title = arguments[@"title"];
    NSString *albumTitle = arguments[@"album_title"];
    NSNumber *position = arguments[@"position"];
    NSNumber *duration = arguments[@"duration"];
    
    NSDictionary *info = @{
        MPNowPlayingInfoPropertyPlaybackRate: @(self.playbackRate),
        MPMediaItemPropertyTitle: title,
        MPMediaItemPropertyAlbumTitle: albumTitle,
        MPNowPlayingInfoPropertyElapsedPlaybackTime: position,
        MPMediaItemPropertyPlaybackDuration: duration,
    };
    
    nowPlayingCenter.nowPlayingInfo = info;
}

- (MPRemoteCommandHandlerStatus)onTogglePlaybackTap {
    return self.onTogglePlaybackBlock();
}

- (MPRemoteCommandHandlerStatus)onPreviousTap {
    [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = NULL;
    [_platformChannel invokeMethod:@"onPreviousTap" arguments:nil];
    
    return MPRemoteCommandHandlerStatusSuccess;
}

- (MPRemoteCommandHandlerStatus)onNextTap {
    [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = NULL;
    [_platformChannel invokeMethod:@"onNextTap" arguments:nil];
    
    return MPRemoteCommandHandlerStatusSuccess;
}

@end
