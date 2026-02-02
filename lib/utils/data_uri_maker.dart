import 'dart:convert';
import 'dart:io';
import 'package:mime/mime.dart';
import 'package:image_picker/image_picker.dart';

Future<String?> getDataUriFromImage(XFile? xfile) async {
  if (xfile == null) return null;
  // Определяем mime-тип
  final mimeType = lookupMimeType(xfile.path) ?? 'image/jpeg';
  // Читаем файл
  final bytes = await File(xfile.path).readAsBytes();
  final base64Image = base64Encode(bytes);
  // Формируем Data URI
  return 'data:$mimeType;base64,$base64Image';
}
Future<String?> getBase64FromImage(XFile? xfile) async {
  if (xfile == null) return null;
  final bytes = await File(xfile.path).readAsBytes();
  return base64Encode(bytes);
}