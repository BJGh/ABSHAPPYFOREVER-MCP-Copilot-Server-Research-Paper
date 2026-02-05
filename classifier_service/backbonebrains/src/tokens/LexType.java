package tokens;

public enum LexType {

    NAME,

    //ключевые слова
    WHILE,
    IF,
    ELIF,
    ELSE,
    FOR,
    DO,
    PRINT,
    LIST,
    SET,


    //литералы
    NUM,
    BOOLEAN,

    //операторы
    OP_ASSIGN,
    SEMICOLON,
    L_BRACKET,
    R_BRACKET,
    L_BRACE,
    R_BRACE,
    OP_DOT,
    COMMA,

    //Арифметичекие операторы
    OP_MUL,
    OP_DIV,
    OP_ADD,
    OP_SUB,

    //Логические операторы
    OP_EQUAL,
    OP_MORE,
    OP_LESS,
    OP_MORE_EQUAL,
    OP_LESS_EQUAL,
    OP_NOT_EQUAL,

    //Служебная лексема перехода
    JMP_VALUE,
    JMP,

    FUNC,

    ERROR,

    //Лексема окончания кода
    END,
}
