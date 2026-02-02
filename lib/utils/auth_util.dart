// In auth_util.dart
import 'package:appwrite/appwrite.dart';
import 'package:appwrite/enums.dart';
import 'package:advideos/utils/error.dart';

class AuthClass {
  final Client client = Client();
  late Account account;

  AuthClass() {
    client
        .setEndpoint('https://nyc.cloud.appwrite.io/v1') // Your Appwrite endpoint
        .setProject('685a7d03001331bd52a2'); // Your Appwrite project ID
    account = Account(client);
  }

  Future<void> googleSignIn(context) async {
    try {
      await account.createOAuth2Session(provider: OAuthProvider.google);
      await Future.delayed(const Duration(microseconds: 400));
      // On return, you should check session or token and redirect
    } on Exception {
      showErrorDialog("Failed login or register", context);
    }
  }

  Future<String?> getToken() async {
    try {
      final session = await account.getSession(sessionId: 'current');
      return session.userId;
    } on Exception {
      return null;
    }
  }
}