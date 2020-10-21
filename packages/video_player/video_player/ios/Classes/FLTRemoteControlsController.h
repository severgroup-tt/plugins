//
//  FLTRemoteControlsController.h
//  video_player
//
//  Created by a.govyashov on 21.10.2020.
//

#import <Foundation/Foundation.h>
#import <MediaPlayer/MediaPlayer.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

/// We need this singleton because remote controls handlers must be added only once.
/// The FLTVideoPlayer is being recreated for each new item.
/// We can't remove existing handlers on its init and add new ones, because in case when playback is on pause and we switch the track, the control center disappears.
/// Also there is no api to check whether the handlers have already been added.
@interface FLTRemoteControlsController : NSObject

@property (nonatomic) double playbackRate;
@property (nonatomic) FlutterMethodChannel* platformChannel;
@property (nonatomic, copy) MPRemoteCommandHandlerStatus (^onTogglePlaybackBlock)(void);

+ (instancetype)sharedController;
- (void)setPosition:(NSInteger)position duration:(NSInteger)duration;

@end

NS_ASSUME_NONNULL_END
