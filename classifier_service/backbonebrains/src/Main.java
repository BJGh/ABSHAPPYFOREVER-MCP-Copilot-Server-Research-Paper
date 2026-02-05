// Main.jsh (J# compatible)
public class Main {
    public static void main(String[] args) {
        TextDriver text = new TextDriver("SourceFiles\\Example_1.txt");
        Lexer lexer = new Lexer();
        try {
            lexer.analysis(text.get_source());
        } catch (Exception e) {
            e.printStackTrace();
        }
        lexer.printLexemeList();
        Parser parser = new Parser(lexer.getLexemes());
        parser.analysis();
        parser.AST.print();
        TranslatorRPN RPN = new TranslatorRPN(parser.getTree());
        RPN.translate();
        RPN.print();
        StackMachine machine = new StackMachine(RPN.getRPN());
        machine.run();
        machine.print();
    }
}
