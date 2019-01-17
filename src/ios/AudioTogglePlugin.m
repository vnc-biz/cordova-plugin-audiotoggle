#import "AudioTogglePlugin.h"
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>


@interface AudioTogglePlugin()
@property (nonatomic, strong) NSMutableDictionary *eventCallbacks;

@end

@implementation AudioTogglePlugin

- (void) pluginInitialize {
	self.eventCallbacks = [NSMutableDictionary dictionaryWithCapacity:1];
	[self.eventCallbacks setValue:[NSMutableArray array] forKey:@"speaker"];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleAudioRouteChange:) name:AVAudioSessionRouteChangeNotification object:nil];
}

- (void)setAudioMode:(CDVInvokedUrlCommand *)command
{
	NSError* __autoreleasing err = nil;
	NSString* mode = [[NSString stringWithFormat:@"%@", [command.arguments objectAtIndex:0]] lowercaseString];
	
	AVAudioSession *session = [AVAudioSession sharedInstance];
	
	if ([mode isEqualToString:@"earpiece"]) {
		[session setCategory:AVAudioSessionCategoryPlayAndRecord error:&err];
		[session overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&err];
		
	} else if ([mode isEqualToString:@"speaker"] || [mode isEqualToString:@"ringtone"]) {
		[session setCategory:AVAudioSessionCategoryPlayAndRecord error:&err];
		[session overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&err];
		
	} else if ([mode isEqualToString:@"normal"]) {
		[session setCategory:AVAudioSessionCategorySoloAmbient error:&err];
	}
}

- (void) registerListener:(CDVInvokedUrlCommand *)command {
	NSString* eventName = command.arguments.firstObject;
	if(self.eventCallbacks[eventName] != nil) {
		[self.eventCallbacks[eventName] addObject:command.callbackId];
	}
}

- (void)handleAudioRouteChange:(NSNotification *) notification
{
	AVAudioSessionRouteChangeReason reasonValue = [notification.userInfo[@"AVAudioSessionRouteChangeReasonKey"] unsignedIntegerValue];
	AVAudioSessionRouteDescription* currentRoute = [AVAudioSession sharedInstance].currentRoute;
	if([currentRoute.outputs count] > 0 && (reasonValue == AVAudioSessionRouteChangeReasonOverride || reasonValue == AVAudioSessionRouteChangeReasonCategoryChange)) {
		AVAudioSessionPortDescription *output = currentRoute.outputs.firstObject;
		
		for (id callbackId in self.eventCallbacks[@"speaker"]) {
			CDVPluginResult* pluginResult = nil;
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[output.portType isEqual: @"Speaker"]];
			[pluginResult setKeepCallbackAsBool:YES];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
		}
	}
}

@end
