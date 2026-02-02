// TODO: Создать профиль пользователя, добавить биллинг (тариф).
import 'package:advideos/screens/HomeScreen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:advideos/utils/auth_util.dart';
import 'package:appwrite/appwrite.dart';

// Set your Appwrite Function ID (the router-based Python function)
const String kFunctionId = '685bcfbf00303847c1e7'; // TODO: hide it from prod

class SignInPage extends StatefulWidget {
  const SignInPage({Key? key}) : super(key: key);

  @override
  State<SignInPage> createState() => _SignInPageState();
}

class _SignInPageState extends State<SignInPage> with WidgetsBindingObserver {
  bool circular = false;
  final AuthClass authClass = AuthClass();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkForSession();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if (state == AppLifecycleState.resumed) {
      _checkForSession();
      if (mounted) setState(() => circular = false);
    }
  }

  Future<void> _checkForSession() async {
    try {
      final token = await authClass.getToken();
      if (token != null && mounted) {
        _navigateToHome();
      }
    } on AppwriteException catch (e) {
      // Optional: show a snackbar or log
      debugPrint("Session check failed: ${e.message}");
      if (mounted) setState(() => circular = false);
    }
  }

  void _navigateToHome() {
    final Client client = authClass.client; // Use the same client from AuthClass
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (_) => MyHomePage(
          appwriteClient: client,   // REQUIRED
          functionId: kFunctionId,  // REQUIRED
        ),
      ),
          (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height,
        color: Colors.black,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text(
              "Welcome to BizVidsAI!",
              style: TextStyle(
                fontSize: 35,
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 30),
            if (circular)
              const CircularProgressIndicator()
            else
              buttonItem(
                "assets/google.svg",
                "Continue with Google",
                25,
                    () async {
                  setState(() => circular = true);
                  await authClass.googleSignIn(context);
                  // App will bounce to browser and back; resume handler will run _checkForSession.
                },
              ),
          ],
        ),
      ),
    );
  }

  Widget buttonItem(
      String imagePath,
      String buttonName,
      double size,
      Function() onTap,
      ) {
    return InkWell(
      onTap: onTap,
      child: SizedBox(
        width: MediaQuery.of(context).size.width - 60,
        height: 60,
        child: Card(
          elevation: 8,
          color: Colors.black,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
            side: const BorderSide(width: 1, color: Colors.grey),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              SvgPicture.asset(imagePath, height: size, width: size),
              const SizedBox(width: 15),
              Text(
                buttonName,
                style: const TextStyle(color: Colors.white, fontSize: 17),
              ),
            ],
          ),
        ),
      ),
    );
  }
}