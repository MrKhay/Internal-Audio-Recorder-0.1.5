import 'internal_audio_recorder_platform_interface.dart';

class InternalAudioRecorder {
  /// Before a capture session can be started, permission is checked
  /// and called if not granted requestRecordAudioPermission().
  /// Start capturing audio
  /// Delay before audio start been captured in seconds
  /// Returns value when something goes wrong
  /// Returns null when nothing went wrong
  Future<String?> startCapturing(
      String outputPath, int encoding, int sampleRate,
      {int delay = 0}) async {
    return InternalAudioRecorderPlatform.instance
        .startCapturing(outputPath, encoding, sampleRate, delay);
  }

  // Stop recording
  /// Returns value when something goes wrong
  ///
  /// Returns null when nothing went wrong
  Future<String?> stopCapturing() async {
    return InternalAudioRecorderPlatform.instance.stopCapturing();
  }

  Stream<List<int>> listen() {
    return InternalAudioRecorderPlatform.instance.listen();
  }
}
