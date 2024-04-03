import 'package:flutter_test/flutter_test.dart';
import 'package:internal_audio_recorder/internal_audio_recorder_method_channel.dart';
import 'package:internal_audio_recorder/internal_audio_recorder_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockInternalAudioRecorderPlatform
    with MockPlatformInterfaceMixin
    implements InternalAudioRecorderPlatform {
  @override
  Future<String?> stopCapturing() {
    // TODO: implement stopCapturing
    throw UnimplementedError();
  }

  @override
  Future<String?> startCapturing(
      String outputPath, int encoding, int sampleRate, int delay) {
    // TODO: implement startCapturing
    throw UnimplementedError();
  }

  @override
  Stream<List<int>> listen() {
    // TODO: implement listen
    throw UnimplementedError();
  }
}

void main() {
  final InternalAudioRecorderPlatform initialPlatform =
      InternalAudioRecorderPlatform.instance;

  test('$MethodChannelInternalAudioRecorder is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelInternalAudioRecorder>());
  });
}
