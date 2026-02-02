import 'dart:convert';
import 'package:appwrite/appwrite.dart';

Future<double?> checkNSFWWithAppwrite({
  required Client appwriteClient,
  required String functionId,
  required String base64Image,
}) async {
  final functions = Functions(appwriteClient);

  try {
    final execution = await functions.createExecution(
      functionId: functionId,
      body: jsonEncode({'image': base64Image}),
      xasync: false,
    );
    final result = jsonDecode(execution.responseBody);
    if (result['nsfw_score'] != null) {
      return (result['nsfw_score'] as num).toDouble();
    }
    return null;
  } catch (e) {
    print('Appwrite NSFW check error: $e');
    return null;
  }
}
