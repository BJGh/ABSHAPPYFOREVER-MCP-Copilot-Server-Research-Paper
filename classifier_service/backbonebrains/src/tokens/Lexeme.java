package tokens;

public class Lexeme {

    private LexType type;
    private String value;

    public Lexeme(LexType type, String value) {

        this.type = type;
        this.value = value;
    }

    public Lexeme(LexType type, int value) {

        this.type = type;
        this.value = Integer.toString(value);
    }

    public Lexeme(LexType type) {

        this(type, null);
    }

    public LexType getType() {

        return type;
    }

    public String get_value () {

        return value;
    }

    public void set_value (String s) {

        this.value = s;
    }

    public void print () {

        System.out.printf("%-20s%-20s", type, value == null ? " " : value);
    }

    public void println () {

        System.out.printf("%-20s%-20s\n", type, value == null ? " " : value);
    }
}


