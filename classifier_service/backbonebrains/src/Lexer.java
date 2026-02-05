import java.util.ArrayList;
import tokens.LexType;
import tokens.Lexeme;
import tokens.Terminal;

public class Lexer {

    private ArrayList <Lexeme> lexemes;
    private ArrayList <Terminal> terminals;

    public Lexer () {

        lexemes = new ArrayList<Lexeme>();

        terminals = new ArrayList<Terminal>();

        terminals.add(new Terminal("while", LexType.WHILE,1));
        terminals.add(new Terminal("if", LexType.IF,1));
        terminals.add(new Terminal("elif", LexType.ELIF,1));
        terminals.add(new Terminal("else", LexType.ELSE,1));
        terminals.add(new Terminal("for", LexType.FOR,1));
        terminals.add(new Terminal("do", LexType.DO,1));
        terminals.add(new Terminal("print", LexType.PRINT,1));
        terminals.add(new Terminal("list", LexType.LIST,1));
        terminals.add(new Terminal("set", LexType.SET,1));
        terminals.add(new Terminal("[a-zA-Z]+", LexType.NAME));
        terminals.add(new Terminal("0|([1-9][0-9]*)", LexType.NUM));
        terminals.add(new Terminal("[;]", LexType.SEMICOLON));
        terminals.add(new Terminal("[.]", LexType.OP_DOT));
        terminals.add(new Terminal("[,]", LexType.COMMA));
        terminals.add(new Terminal("[=]", LexType.OP_ASSIGN));
        terminals.add(new Terminal("[*]", LexType.OP_MUL));
        terminals.add(new Terminal("[/]", LexType.OP_DIV));
        terminals.add(new Terminal("[+]", LexType.OP_ADD));
        terminals.add(new Terminal("[-]", LexType.OP_SUB));
        terminals.add(new Terminal("[(]", LexType.L_BRACKET));
        terminals.add(new Terminal("[)]", LexType.R_BRACKET));
        terminals.add(new Terminal("[{]", LexType.L_BRACE));
        terminals.add(new Terminal("[}]", LexType.R_BRACE));
        terminals.add(new Terminal("==", LexType.OP_EQUAL));
        terminals.add(new Terminal("<>", LexType.OP_NOT_EQUAL));
        terminals.add(new Terminal(">", LexType.OP_MORE));
        terminals.add(new Terminal("<", LexType.OP_LESS));
        terminals.add(new Terminal(">=", LexType.OP_MORE_EQUAL));
        terminals.add(new Terminal("<=", LexType.OP_LESS_EQUAL));
    }

    public void analysis (String source) throws Exception {

        long time_analysis = System.nanoTime();

        int position = 0;
        StringBuilder buffer = null;

        while (position != source.length()) {

            if ((source.charAt(position) == ' ' || source.charAt(position) == '\n') && buffer == null) {
                position++;
                continue;
            }

            if (buffer == null)
                buffer = new StringBuilder();

            buffer.append(source.charAt(position++));

            Terminal terminal = lookTerminal(buffer);

            if (terminal == null) {

                if (buffer.length() == 1)
                    throw new Exception();

                buffer.deleteCharAt(buffer.length() - 1);
                position--;
                terminal = lookTerminal(buffer);
                lexemes.add(new Lexeme(terminal.get_type(), buffer.toString()));

                buffer = null;
                continue;
            }
        }

        lexemes.add(new Lexeme(LexType.END, ""));

        System.out.println("[Lexer] time analysis: " + (System.nanoTime() - time_analysis) / 1_000_000.0 + "ms");
    }

    /*public void analysis (String source) {

        double time_analysis = System.nanoTime();

        int position = 0;

        Matcher matcher;

        do {

            while (source.charAt(position) == ' ' || source.charAt(position) == '\n') {
                position++;
                continue;
            }

            for(int i = 0; i < terminals.size(); i++) {

                matcher = terminals.get(i).get_pattern().matcher(source);

                if (matcher.find(position) && matcher.start() == position) {

                    position = matcher.end();

                    lexemes.add(new Lexeme(terminals.get(i).get_type(), source.substring(matcher.start(), matcher.end())));
                    break;
                }
            }
        } while (position != source.length() - 1);

        lexemes.add(new Lexeme(LexType.END, ""));

        System.out.println("[Lexer] time analysis: " + (System.nanoTime() - time_analysis) / 1_000_000_000.0 + "ms");
    }*/

    private Terminal lookTerminal(StringBuilder string) {

        Terminal found_terminal = null;

        for (Terminal terminal: terminals) {

            if (terminal.matches(string)) {
                found_terminal = terminal.compare_priority(found_terminal);
            }
        }

        return found_terminal;
    }

    public ArrayList<Lexeme> getLexemes () {

        return lexemes;
    }

    public void printLexemeList() {

        System.out.println("[Lexer] table lexemes: ");
        System.out.printf("%-20s%-20s\n", "Name lexeme", "Value");

        for (Lexeme lexeme: lexemes) {

            lexeme.println();
        }
    }
}
