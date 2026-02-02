//DID: ПОМЕНЯЛСЯ API С Stable Video Diffusion на Runway API (он может быть только платным)
//DID: ИЗ HOMESCREEN ПЕРЕДАТЬ ТЕКСТ И ФОТО В ВИДЕ DATA_URI
//TODO: КАК ВИДЕО СГЕНЕРИЛОСЬ, НУЖНО ДОБАВИТЬ - ОЗВУЧКА, МУЗЫКА, ТЕКСТ
//INFO: MVC ПАТТЕРН.
import 'dart:io';
import 'package:advideos/utils/error.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:advideos/utils/data_uri_maker.dart';
import 'package:appwrite/appwrite.dart'; // Import Appwrite
import 'package:advideos/utils/text_censor.dart';
import 'package:safe_text/safe_text.dart';
// Import the file where VideoGenerator is defined
import 'package:advideos/api/runway_video.dart';
import 'package:path_provider/path_provider.dart';
import 'package:advideos/api/nsfw_detector.dart';
// Your Appwrite Function ID needs to be accessible here.
// You can define it as a constant or pass it down.
//TODO убрать проверку на NSFW
const YOUR_APPWRITE_FUNCTION_ID = '685bd464e3ba43420047'; // Replace with your actual function ID
const NSFW_FUNC_ID='68a7da550023b77eb3d4';

// MyApp doesn't need changes, but it's good practice to pass the client down
// from where it's initialized (e.g., main.dart or after login).
// For this example, we'll assume the client is passed to MyHomePage.
class MyApp extends StatelessWidget {
  final Client appwriteClient;

  const MyApp({super.key, required this.appwriteClient});

  @override
  Widget build(BuildContext context) => MaterialApp(
    home: MyHomePage(
      appwriteClient: appwriteClient, // Pass the client here
      functionId: YOUR_APPWRITE_FUNCTION_ID,
    ),
    debugShowCheckedModeBanner: false,
  );
}

class MyHomePage extends StatefulWidget {
  // Add these fields to accept the client and function ID
  final Client appwriteClient;
  final String functionId;

  const MyHomePage({
    super.key,
    required this.appwriteClient,
    required this.functionId,
  });

  @override
  // ignore: library_private_types_in_public_api
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final ImagePicker _picker = ImagePicker();
  XFile? _image;
  bool isLoading = false;
  final textController = TextEditingController();
///HERE
  /// MODIFIED: This function now picks AND validates the image.
  Future<void> _pickImage() async {
    // Prevent picking if another operation is running.
    if (isLoading) return;

    final pickedFile = await _picker.pickImage(source: ImageSource.gallery);
    if (pickedFile == null) {
      return; // User cancelled the picker
    }

    // Set loading state BEFORE the async check begins.
    setState(() {
      isLoading = true;
      _image = null; // Clear previous image while checking new one
    });

    try {

      final base64Image = await getBase64FromImage(pickedFile);
      if (base64Image == null) {
        showErrorDialog("Cannot encode image!", context);// handle error
        return;
      }// Directly pass the asset name. The plugin will handle finding it.
      final nsfwScore = await checkNSFWWithAppwrite(
        appwriteClient: widget.appwriteClient,
        functionId: NSFW_FUNC_ID,
        base64Image: base64Image,
      );

      if (nsfwScore == null) {
        await showErrorDialogFuture("Could not check image for NSFW content.", context);
        return;
      }

      if (nsfwScore > 0.6) {
        await showErrorDialogFuture("This image appears to be inappropriate. Please select another.", context);
      } else {
        // If safe, update the state to display it.
        setState(() {
          _image = pickedFile;
        });
      }
    } catch (e) {
      // Handle potential errors during the check
      debugPrint("Error during NSFW check: $e");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Error checking image: $e")),
      );
    } finally {
      // Always turn off the loading indicator
      if (mounted) {
        setState(() {
          isLoading = false;
        });
      }
    }
  }
///HERE
  ///HERE
  // This is the new, correct async function to handle the button press.

  Future<void> _sendImageAndGenerateVideo() async {
    if (_image == null) {
      // Show a snackbar or alert if no image is selected
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select an image first.')),
      );
      return;
    }

    // Set loading state before starting async work
    setState(() {
      isLoading = true;
    });

    try {
      // 1. Get the data URI and user prompt
      final dataUri = await getDataUriFromImage(_image);
      final userPrompt = textController.text.trim();
      //my own textCensor
 /*     final textCheck = TextSafety.check(userPrompt);
      if (!textCheck.ok) {
        showErrorDialog("Wrong prompt!", context);
        return;
      }*/

// Use textCheck.cleaned as the sanitized prompt
      final safePrompt  = SafeText.filterText(
        text: userPrompt,
        useDefaultWords: true,
        fullMode: true,
        obscureSymbol: "*",
      );
      if (safePrompt != userPrompt) {
        showErrorDialog("Wrong prompt!", context);
        return;
      }


      if (dataUri == null) {
        throw Exception("Failed to convert image to Data URI.");
      }

      // 2. Navigate to the VideoGenerator screen, passing all required props
      //    The `await` here means we'll wait until the user comes back from the generator screen.
      await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => VideoGenerator(
            data_uri_image: dataUri,
            user_prompt: safePrompt,
            appwriteClient: widget.appwriteClient, // Pass the client from the widget
            functionId: widget.functionId,       // Pass the functionId from the widget
          ),
        ),
      );
    } catch (e) {
      // Handle any errors that might occur during data URI generation
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('An error occurred: $e')),
      );
    } finally {
      // 3. Set loading back to false when the process is done or failed
      if (mounted) { // Check if the widget is still in the tree
        setState(() {
          isLoading = false;
        });
      }
    }
  }
  /// CORRECTED VERSION


  @override
  Widget build(BuildContext context) => SafeArea(
    child: Scaffold(
      backgroundColor: const Color(0XFF27374D),
      body: Padding(
        padding: const EdgeInsets.all(15.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: textController,
              decoration: InputDecoration(
                hintText: 'Enter your prompt',
                fillColor: Colors.white,
                filled: true,
                contentPadding: const EdgeInsets.all(16),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10.0),
                ),
                labelStyle: const TextStyle(color: Colors.red),
              ),
            ),
            ElevatedButton(
              onPressed: _pickImage,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0XFF9DB2BF),
              ),
              child: const Text('Upload Image',
                  style: TextStyle(color: Colors.black)),
            ),
            const SizedBox(height: 20),
            _image != null
                ? Image.file(File(_image!.path), height: 200)
                : const Text('No image selected',
                style: TextStyle(color: Colors.white)),
            const SizedBox(height: 30),
            SizedBox(
              width: 150,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0XFF9DB2BF),
                ),
                // Call the new async function here
                onPressed: isLoading ? null : _sendImageAndGenerateVideo,
                child: isLoading
                    ? const SizedBox(
                  height: 15,
                  width: 15,
                  child:
                  CircularProgressIndicator(color: Colors.black),
                )
                    : const Text('Send Image',
                    style: TextStyle(color: Colors.black)),
              ),
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    ),
  );
}