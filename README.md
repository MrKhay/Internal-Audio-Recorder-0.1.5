# Internal Audio Recorder Flutter Plugin

Flutter plugin for capturing internal audio on Android devices.

## Installation

Add the following to your `pubspec.yaml` file:

```yaml
dependencies:
  internal_audio_recorder: ^1.0.0
```

## Usage

```dart
import 'package:internal_audio_recorder/internal_audio_recorder.dart';

InternalAudioRecorder internalAudioRecorder InternalAudioRecorde();

  void startRecording() async { 
    File pcmOutputFile ('path/to/output.pcm');
    await pcmOutputFile.create();

   String result = await
   internalAudioRecorder.startCapturing(outputPath: pcmOutputFile.path, encoding:
   AudioFormat.encodingPCM16BIT, sampleRate:44100);
}

void stopRecording() { 
  internalAudioRecorder.stopCapturing();
  }
```

