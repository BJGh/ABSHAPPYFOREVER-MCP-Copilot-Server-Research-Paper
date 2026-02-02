// lib/screens/quiz_finished.dart
import 'package:flutter/material.dart';
import 'package:sportquiz/models/question.dart';
import 'package:sportquiz/screens/check_answers.dart';
// Добавляем наши новые импорты:
import 'package:sportquiz/services/appwrite_service.dart';
import 'server_details_screen.dart'; 


class QuizFinishedPage extends StatefulWidget {
  final List<Question> questions;
  final Map<int, dynamic> answers;

  const QuizFinishedPage({Key? key, required this.questions, required this.answers})
      : super(key: key);

  @override
  _QuizFinishedPageState createState() => _QuizFinishedPageState();
}

// lib/screens/quiz_finished.dart

class _QuizFinishedPageState extends State<QuizFinishedPage> {
  int? correctAnswers;
  bool _isLoading = true; // Добавляем состояние загрузки
  int correct = 0;

  @override
  void initState() {
    super.initState();
    // Сразу начинаем процесс получения сервера при инициализации
    _fetchServerAndRedirect();
  }

  Future<void> _fetchServerAndRedirect() async {
    widget.answers.forEach((index, value) {
      if (widget.questions[index].correctAnswer == value) correct++;
    });

    // Можешь добавить условие, например, если правильных ответов > 60%
    if (correct / widget.questions.length >= 0.0) { // Например, всегда перенаправляем, если ответил хотя бы на 1 вопрос правильно
        try {
          final serverData = await AppwriteService.getRandomServer();
          
          if (!mounted) return;

          // Перенаправляем на экран деталей сервера и убираем этот экран из стека
          Navigator.of(context).pushReplacement(MaterialPageRoute(
              builder: (_) => ServerDetailsScreen(
                ipAddress: serverData['ip_address'] ?? 'N/A',
                port: serverData['port'] ?? 0,
                gameName: serverData['name'] ?? 'Игра',
              )));
        } catch (e) {
          // Если ошибка, остаемся на экране результатов и показываем ошибку
          if (!mounted) return;
          setState(() {
            _isLoading = false;
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(
              content: Text("Ошибка получения сервера из Appwrite: $e"),
            ));
          });
        }
    } else {
      // Если не прошел тест, остаемся на экране результатов
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    // ... (Остальной код build метода без изменений)
    
    // Заменяем вывод результатов на экран загрузки, пока идет запрос к Appwrite
    if (_isLoading) {
        return const Scaffold(
          body: Center(
            child: CircularProgressIndicator(
              valueColor: AlwaysStoppedAnimation<Color>(Colors.blue),
            ),
          ),
        );
    }
    
    // ... (Остальной код build метода, если нужно показать результаты в случае ошибки)
    // Но по твоей логике, _isLoading всегда будет true до редиректа или ошибки.

    // Этот код ниже можно удалить, так как мы либо редиректим, либо показываем ошибку загрузки
    // Но для резерва оставим его на случай отладки:

    const TextStyle titleStyle = TextStyle(/*...*/) ;
    const TextStyle trailingStyle = TextStyle(/*...*/) ;
    
    return const Scaffold(
      // ... (Весь остальной Scaffold UI с карточками результатов)
    );
  }
}
