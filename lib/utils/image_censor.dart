/*
import 'dart:typed_data';
import 'dart:io';
import 'package:image/image.dart' as img;
import 'package:tflite_flutter/tflite_flutter.dart';

class ImageSafety {
  Interpreter? _interpreter;
  final int inputSize; // e.g., 224
  final double threshold; // e.g., 0.6

  ImageSafety({this.inputSize = 224, this.threshold = 0.6});

  Future<void> init() async {
    // Place your TFLite model at assets/models/nsfw.tflite and declare in pubspec.yaml
    _interpreter = await Interpreter.fromAsset('models/nsfw.tflite', options: InterpreterOptions()..threads = 2);
  }

  bool get ready => _interpreter != null;

  Future<double> nsfwScoreFromFile(String path) async {
    final bytes = await File(path).readAsBytes();
    final decoded = img.decodeImage(bytes);
    if (decoded == null) return 0.0;

    final resized = img.copyResize(decoded, width: inputSize, height: inputSize);
    final input = _imageToInput(resized); // Float32 normalized tensor [1,inputSize,inputSize,3]

    // Adjust output shape depending on your model
    final output = List.filled(1, 0.0).reshape([1, 1]); // for sigmoid single-prob model
    _interpreter!.run(input, output);

    final prob = output[0][0]; // NSFW probability
    return prob is double ? prob : (prob as num).toDouble();
  }

  // Convert image to Float32 input normalized to 0..1
  List<List<List<List<double>>>> _imageToInput(img.Image image) {
    final data = List.generate(1, (_) => List.generate(inputSize, (_) => List.generate(inputSize, (_) => List.filled(3, 0.0))));
    for (int y = 0; y < inputSize; y++) {
      for (int x = 0; x < inputSize; x++) {
        final p = image.getPixel(x, y);
        data[0][y][x][0] = (img.getRed(p)) / 255.0;
        data[0][y][x][1] = (img.getGreen(p)) / 255.0;
        data[0][y][x][2] = (img.getBlue(p)) / 255.0;
      }
    }
    return data;
  }

  Future<bool> isSafe(String path) async {
    final score = await nsfwScoreFromFile(path);
    return score < threshold;
  }
}*/