import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'internal_audio_recorder_method_channel.dart';

abstract class InternalAudioRecorderPlatform extends PlatformInterface {
  /// Constructs a InternalAudioRecorderPlatform.
  InternalAudioRecorderPlatform() : super(token: _token);

  static final Object _token = Object();

  static InternalAudioRecorderPlatform _instance =
      MethodChannelInternalAudioRecorder();

  /// The default instance of [InternalAudioRecorderPlatform] to use.
  ///
  /// Defaults to [MethodChannelInternalAudioRecorder].
  static InternalAudioRecorderPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [InternalAudioRecorderPlatform] when
  /// they register themselves.
  static set instance(InternalAudioRecorderPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// Before a capture session can be started, permission is checked
  /// and called if not granted requestRecordAudioPermission().
  /// Start capturing audio
  /// Delay before audio start been captured in seconds
  Future<String?> startCapturing(
      String outputPath, int encoding, int sampleRate, int delay) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// Stop capture audio and save's file
  Future<String?> stopCapturing() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  // listen to audio chunks are they are produced
  Stream<List<double>> listen() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
