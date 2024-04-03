import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'internal_audio_recorder_platform_interface.dart';

/// An implementation of [InternalAudioRecorderPlatform] that uses method channels.
class MethodChannelInternalAudioRecorder extends InternalAudioRecorderPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('internal_audio_recorder');

  /// Stream controller to manage the data stream.
  late StreamController<List<double>> _dataStreamController;

  MethodChannelInternalAudioRecorder() {
    // Initialize the stream controller
    _dataStreamController = StreamController<List<double>>();
    // Listen for events from the platform side and add them to the stream
    methodChannel.setMethodCallHandler((call) async {
      if (call.method == 'onData') {
        // Extract data from method call arguments and add it to the stream
        List<dynamic> data = call.arguments;
        debugPrint("CHunk: $data");
        List<double> doubleData = data.cast<double>();
        _dataStreamController.add(doubleData);
      }
    });
  }
  @override
  Future<String?> startCapturing(
      String outputPath, int encoding, int sampleRate, int delay) async {
    return await methodChannel.invokeMethod<String?>('startCapturing', {
      "outputPath": outputPath,
      "encoding": encoding,
      "sampleRate": sampleRate,
      "delay": delay
    });
  }

  @override
  Future<String?> stopCapturing() async {
    var responce = await methodChannel.invokeMethod('stopCapturing');
    return responce;
  }

  @override
  Stream<List<double>> listen() {
    return _dataStreamController.stream;
  }
}
