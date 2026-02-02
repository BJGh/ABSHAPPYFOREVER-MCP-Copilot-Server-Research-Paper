import 'package:flutter/material.dart';
import 'package:sportquiz/models/category.dart';
import 'package:sportquiz/models/question.dart';
import 'package:sportquiz/screens/quiz_finished.dart';
import 'package:html_unescape/html_unescape.dart';
import 'package:flutter/painting.dart';

class QuizPage extends StatefulWidget {
  final List<Question> questions;
  final Category? category;

  const QuizPage({Key? key, required this.questions, this.category})
      : super(key: key);

  @override
  _QuizPageState createState() => _QuizPageState();
}

class _QuizPageState extends State<QuizPage> {
  const TextStyle _questionStyle = TextStyle(
      fontSize: 18.0, fontWeight: FontWeight.w500, color: Colors.white);

  int _currentIndex = 0;
  final Map<int, dynamic> _answers = {};
  final GlobalKey<ScaffoldState> _key = GlobalKey<ScaffoldState>();

  @override
  Widget build(BuildContext context) {
    Question question = widget.questions[_currentIndex];
    final List<dynamic> options = question.incorrectAnswers!;
    if (!options.contains(question.correctAnswer)) {
      options.add(question.correctAnswer);
      options.shuffle();
    }

    return WillPopScope(
        onWillPop: _onWillPop,
        child: Scaffold(
          key: _key,
          appBar: AppBar(
            title: Text(widget.category!.name),
            elevation: 0,
            backgroundColor: Colors.amber,
          ),
          body: Container(
            decoration: const BoxDecoration(
              image: DecorationImage(
                image: AssetImage("assets/images/background.png"),
                fit: BoxFit.cover,
              ),
            ),
            child: Stack(
              children: <Widget>[
                ClipPath(
                  child: Container(
                    height: 200,
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: <Widget>[
                      Row(
                        children: <Widget>[
                          CircleAvatar(
                            backgroundColor: Colors.white70,
                            child: Text("${_currentIndex + 1}"),
                          ),
                          const SizedBox(width: 16.0),
                          Expanded(
                            child: Text(
                              HtmlUnescape().convert(
                                  widget.questions[_currentIndex].question!),
                              softWrap: true,
                              style: MediaQuery.of(context).size.width > 800
                                  ? _questionStyle.copyWith(fontSize: 30.0)
                                  : _questionStyle,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 20.0),
                      Card(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: <Widget>[
                            ...options.map((option) => RadioListTile(
                                  title: Text(
                                    HtmlUnescape().convert("$option"),
                                    style:
                                        MediaQuery.of(context).size.width > 800
                                            ? const TextStyle(fontSize: 30.0)
                                            : null,
                                  ),
                                  groupValue: _answers[_currentIndex],
                                  value: option,
                                  onChanged: (dynamic value) {
                                    setState(() {
                                      _answers[_currentIndex] = option;
                                    });
                                  },
                                )),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Container(
                          alignment: Alignment.bottomCenter,
                          child: ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              padding: MediaQuery.of(context).size.width > 800
                                  ? const EdgeInsets.symmetric(
                                      vertical: 20.0, horizontal: 64.0)
                                  : null,
                            ),
                            onPressed: _nextSubmit,
                            child: Text(
                              _currentIndex == (widget.questions.length - 1)
                                  ? "Submit"
                                  : "Next",
                              style: MediaQuery.of(context).size.width > 800
                                  ? const TextStyle(fontSize: 30.0)
                                  : null,
                            ),
                          ),
                        ),
                      )
                    ],
                  ),
                )
              ],
            ),
          ),
        ));
  }

  void _nextSubmit() {
    if (_answers[_currentIndex] == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text("You must select an answer to continue."),
      ));
      return;
    }
    if (_currentIndex < (widget.questions.length - 1)) {
      setState(() {
        _currentIndex++;
      });
    } 
  }

  Future<bool> _onWillPop() async {
    final resp = await showDialog<bool>(
        context: context,
        builder: (_) {
          return AlertDialog(
            content: const Text(
                "Are you sure you want to quit the quiz? All your progress will be lost."),
            title: const Text("Warning!"),
            actions: <Widget>[
              TextButton(
                child: const Text("Yes"),
                onPressed: () {
                  Navigator.pop(context, true);
                },
              ),
              TextButton(
                child: const Text("No"),
                onPressed: () {
                  Navigator.pop(context, false);
                },
              ),
            ],
          );
        });
    return resp ?? false;
  }
}
