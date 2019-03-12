#import <Cordova/CDVPlugin.h>

FOUNDATION_EXPORT NSString *const SpeakerSelectedNotification;
FOUNDATION_EXPORT NSString *const AudioOptionsAvailable;

@interface AudioTogglePlugin : CDVPlugin
- (void)setAudioMode:(CDVInvokedUrlCommand*)command;
@end
