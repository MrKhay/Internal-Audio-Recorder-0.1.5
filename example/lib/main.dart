import 'dart:io';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:internal_audio_recorder/internal_audio_recorder.dart';
import 'package:internal_audio_recorder/model/audioformat.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _internalAudioRecorderPlugin = InternalAudioRecorder();

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              MaterialButton(
                color: Colors.green,
                onPressed: () async {
                  var document = await getTemporaryDirectory();
                  var storagePath = "${document.path}/opm.pcm";
                  var file = File(storagePath);
                  await file.create();

                  var responce =
                      await _internalAudioRecorderPlugin.startCapturing(
                          file.path, AudioFormat.encodingPCM16BIT, 44200,
                          delay: 2);

                  _internalAudioRecorderPlugin.listen().listen((event) {
                    debugPrint('Stream: $event');
                  });
                },
                child: const Text("Start Recording"),
              ),
              MaterialButton(
                color: Colors.green,
                onPressed: () async {
                  var responce =
                      await _internalAudioRecorderPlugin.stopCapturing();

                  await _internalAudioRecorderPlugin.listen().drain();
                  print("Responce: $responce");
                },
                child: const Text("Stop Recording"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
