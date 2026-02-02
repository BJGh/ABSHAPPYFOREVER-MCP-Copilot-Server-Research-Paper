/*import 'dart:io';
import 'dart:math';
import 'dart:async';

// Параметры игрового поля
const int BOARD_WIDTH = 40;
const int BOARD_HEIGHT = 20;
const Duration GAME_SPEED = Duration(milliseconds: 200);

class Coordinate {
  int x, y;
  Coordinate(this.x, this.y);
  @override
  bool operator ==(Object other) => other is Coordinate && other.x == x && other.y == y;
  @override
  int get hashCode => x.hashCode ^ y.hashCode;
}

enum Direction { up, down, left, right }

class SnakeGame {
  List<Coordinate> snake;
  Coordinate food;
  Direction direction;
  bool gameOver;
  int score;

  SnakeGame() : 
    snake = [Coordinate(BOARD_WIDTH ~/ 2, BOARD_HEIGHT ~/ 2)],
    direction = Direction.right,
    gameOver = false,
    score = 0 {
    _generateFood();
  }

  void _generateFood() {
    final random = Random();
    food = Coordinate(
      random.nextInt(BOARD_WIDTH - 2) + 1,
      random.nextInt(BOARD_HEIGHT - 2) + 1,
    );
    // Убедиться, что еда не на теле змейки
    if (snake.contains(food)) {
      _generateFood();
    }
  }

  void update() {
    if (gameOver) return;

    Coordinate newHead;
    switch (direction) {
      case Direction.up:
        newHead = Coordinate(snake.first.x, snake.first.y - 1);
        break;
      case Direction.down:
        newHead = Coordinate(snake.first.x, snake.first.y + 1);
        break;
      case Direction.left:
        newHead = Coordinate(snake.first.x - 1, snake.first.y);
        break;
      case Direction.right:
        newHead = Coordinate(snake.first.x + 1, snake.first.y);
        break;
    }

    // Проверка на столкновения со стенами
    if (newHead.x <= 0 || newHead.x >= BOARD_WIDTH - 1 || 
        newHead.y <= 0 || newHead.y >= BOARD_HEIGHT - 1) {
      gameOver = true;
      return;
    }

    // Проверка на столкновения с собой
    if (snake.contains(newHead)) {
      gameOver = true;
      return;
    }

    snake.insert(0, newHead);

    // Проверка на еду
    if (newHead == food) {
      score += 10;
      _generateFood();
    } else {
      snake.removeLast();
    }
  }

  void draw() {
    // Очистка экрана консоли (работает по-разному в разных терминалах)
    // В Linux/macOS:
    // stdout.write('\x1B[2J\x1B[0;0H'); 
    // Более простой способ:
    print('\n' * 50); 

    // Отрисовка поля и счета
    for (int y = 0; y < BOARD_HEIGHT; y++) {
      for (int x = 0; x < BOARD_WIDTH; x++) {
        Coordinate current = Coordinate(x, y);
        if (x == 0 || x == BOARD_WIDTH - 1 || y == 0 || y == BOARD_HEIGHT - 1) {
          stdout.write('!'); // Граница
        } else if (snake.contains(current)) {
          stdout.write('*'); // Тело змейки
        } else if (current == food) {
          stdout.write('F'); // Еда
        } else {
          stdout.write(' '); // Пустое место
        }
      }
      stdout.write('\n');
    }
    print("Счет: $score. Жизни: 3 (пока что бесконечные).");
    if (gameOver) {
      print("ИГРА ОКОНЧЕНА! ТЫ НЕ ИЗБРАННЫЙ.");
    }
  }

  void handleInput(String input) {
    if (gameOver) return;

    // В консольном Dart'е считывание getChar() сложно, используем stdin.readLineSync() или слушаем поток
    // Для простоты, этот пример использует stdin.listen для асинхронного ввода.
  }
}

// Главная функция, запускающая игру в консоли
void main() {
  var game = SnakeGame();
  
  // Включаем режим одиночного символа для ввода без Enter
  stdin.echoMode = false; 
  stdin.lineMode = false;

  // Слушаем ввод
  stdin.listen((List<int> codes) {
    if (game.gameOver) return;
    String input = String.fromCharCodes(codes);
    switch (input) {
      case 'w':
      case 'ц':
        if (game.direction != Direction.down) game.direction = Direction.up;
        break;
      case 's':
      case 'ы':
        if (game.direction != Direction.up) game.direction = Direction.down;
        break;
      case 'a':
      case 'ф':
        if (game.direction != Direction.right) game.direction = Direction.left;
        break;
      case 'd':
      case 'в':
        if (game.direction != Direction.left) game.direction = Direction.right;
        break;
    }
  });

  // Игровой цикл
  Timer.periodic(GAME_SPEED, (Timer t) {
    if (game.gameOver) {
      t.cancel();
      // Вернуть режим ввода в норму
      stdin.echoMode = true;
      stdin.lineMode = true;
      exit(0);
    }
    game.update();
    game.draw();
  });
}*/
