import 'package:flutter/material.dart';
import 'package:sportquiz/models/category.dart';
import 'package:sportquiz/screens/quiz_page.dart';
import 'package:sportquiz/models/question.dart';
import 'package:sportquiz/resources/api_provider.dart';
import 'package:flutter/cupertino.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/services.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  ConnectivityResult? _connectivityResult;
  bool? _isConnectionSuccessful;
  String selectedQuestionCount = '5'; // Default value
  String selectedDifficulty = 'easy';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
          child: Container(
            constraints: const BoxConstraints.expand(),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                  begin: Alignment.center,
                  end: Alignment.bottomCenter,
                  colors: [Colors.transparent, Colors.black12, Colors.black54]),
              image: DecorationImage(
                  image: AssetImage(
                      "assets/images/background.png"), // adjust the image path accordingly
                  fit: BoxFit.cover),
            ),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  const Text('Sports Quiz Game',
                      style: TextStyle(
                          color: Colors.blue,
                          fontWeight: FontWeight.bold,
                          fontSize: 40)),
                  const Text('Select number of questions',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.blue, fontSize: 25)),
                  Container(
                      color: Colors.white,
                      child: DropdownButton<String>(
                        isExpanded: true,
                        value: selectedQuestionCount,
                        items: ['5', '10', '15', '20'].map((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(value),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            selectedQuestionCount = newValue ?? '5';
                          });
                        },
                      )),
                  const SizedBox(height: 16),
                  const Text('Select difficulty',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.blue, fontSize: 25)),
                  Container(
                      color: Colors.white,
                      child: DropdownButton<String>(
                        isExpanded: true,
                        itemHeight: null,
                        value: selectedDifficulty,
                        items: ['easy', 'medium', 'hard'].map((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(value),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            selectedDifficulty = newValue ?? 'easy';
                          });
                        },
                      )),
                  SizedBox(
                      height: 50,
                      width: 100,
                      child: TextButton(
                        onPressed: () async {
                          await _checkConnectivityState(context);
                          List<Question> questions = await getQuestions(
                              Category(21, 'Sports'),
                              int.parse(selectedQuestionCount),
                              selectedDifficulty);
                          Navigator.push(
                              context,
                              CupertinoPageRoute(
                                  builder: (_) => QuizPage(
                                      questions: questions,
                                      category: Category(21, 'Sports'))));
                        },
                        child: const Text('Start Game',
                            style: TextStyle(
                                fontWeight: FontWeight.bold, fontSize: 30)),
                      )),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _checkConnectivityState(BuildContext context) async {
    final ConnectivityResult result = await Connectivity().checkConnectivity();

    if (result == ConnectivityResult.none) {
      print('func working, no internet');
      await showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('No Internet'),
          content: const Text('Please check your internet connection.'),
          actions: <Widget>[
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('OK'),
            ),
          ],
        ),
      );
      return;
    }

    setState(() {
      _connectivityResult = result;
    });
  }

  List<Question> generateQuestions(List<Map<String, dynamic>> data) {
    List<Question> questions = Question.fromData(data);
    return questions;
  }
}
