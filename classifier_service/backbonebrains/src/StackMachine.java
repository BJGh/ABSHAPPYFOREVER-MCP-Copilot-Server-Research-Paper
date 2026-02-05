import tokens.LexType;
import tokens.Lexeme;
import util.HashSetInt;
import util.LinkedListInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import static tokens.LexType.*;

public class StackMachine {

    private ArrayList <Lexeme> RPN;
    private int index;

    private HashMap <String, Integer> variables;
    private HashMap <String, LinkedListInt> variablesList;
    private HashMap <String, HashSetInt> variablesSet;
    private LinkedList <Lexeme> stack;

    public StackMachine (ArrayList <Lexeme> RPN) {

        this.RPN = RPN;
        this.variables = new HashMap<>();
        this.variablesList = new HashMap<>();
        this.variablesSet = new HashMap<>();
        this.stack = new LinkedList<>();
    }

    public void run () {

        long time_analysis = System.nanoTime();

        System.out.println("[StackMachine] Output:");

        for (index = 0; index < RPN.size(); index++) {

            switch (RPN.get(index).getType()) {
                case OP_ADD:
                    stack.addFirst(sum(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_SUB:
                    stack.addFirst(sub(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_MUL:
                    stack.addFirst(mul(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_DIV:
                    stack.addFirst(div(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_ASSIGN:
                    assign(stack.removeFirst(), stack.removeFirst());
                    break;
                case OP_EQUAL:
                    stack.addFirst(equal(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_NOT_EQUAL:
                    stack.addFirst(notEqual(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_MORE:
                    stack.addFirst(more(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_LESS:
                    stack.addFirst(less(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_MORE_EQUAL:
                    stack.addFirst(moreEqual(stack.removeFirst(), stack.removeFirst()));
                    break;
                case OP_LESS_EQUAL:
                    stack.addFirst(lessEqual(stack.removeFirst(), stack.removeFirst()));
                    break;
                case IF:
                    ifConst(stack.removeFirst(), stack.removeFirst());
                    break;
                case ELIF:
                    ifConst(stack.removeFirst(), stack.removeFirst());
                    break;
                case WHILE:
                    whileConst(stack.removeFirst(), stack.removeFirst());
                    break;
                case JMP:
                    index = Integer.parseInt(stack.removeFirst().get_value()) - 1;
                    break;
                case DO:
                    doWhileConst(stack.removeFirst(), stack.removeFirst());
                    break;
                case FOR:
                    ForConst(stack.removeFirst(), stack.removeFirst());
                    break;
                case FUNC:
                    funcConst(RPN.get(index));
                    break;
                case PRINT:
                    printConst(stack.removeFirst());
                    break;
                case LIST:
                    listConst(stack.removeFirst());
                    break;
                case SET:
                    setConst(stack.removeFirst());
                    break;
                default:
                    stack.addFirst(RPN.get(index));
            }
        }

        System.out.println("[StackMachine] Time run: " + (System.nanoTime() - time_analysis) / 1_000_000.0 + "ms");
    }

    private void setConst(Lexeme a) {

        if(variablesList.containsKey(a.get_value()) || variables.containsKey(a.get_value())) {

            //throw new Exeption ();
        } else {

            variablesSet.put(a.get_value(), new HashSetInt());
        }
    }

    private void listConst(Lexeme a) {

        if(variablesList.containsKey(a.get_value()) || variables.containsKey(a.get_value())) {

            //throw new Exeption ();
        } else {

            variablesList.put(a.get_value(), new LinkedListInt());
        }
    }

    private void funcConst(Lexeme lexeme) {

        Lexeme name = stack.removeFirst();

        if (variablesList.containsKey(name.get_value())) {


            if (lexeme.get_value().equals("add")) {

                Lexeme value = stack.removeFirst();
                Lexeme index = stack.removeFirst();


                int index_value = index.getType() == NAME ? variables.get(index.get_value()) : Integer.parseInt(index.get_value());
                int value_value = value.getType() == NAME ? variables.get(value.get_value()) : Integer.parseInt(value.get_value());

                variablesList.get(name.get_value()).add(index_value, value_value);
            } else if (lexeme.get_value().equals("peek")) {

                Lexeme index = stack.removeFirst();

                int index_value = index.getType() == NAME ? variables.get(index.get_value()) : Integer.parseInt(index.get_value());

                stack.addFirst(new Lexeme(LexType.NUM, variablesList.get(name.get_value()).peek(index_value)));
            } else if (lexeme.get_value().equals("get")) {

                Lexeme index = stack.removeFirst();

                int index_value = index.getType() == NAME ? variables.get(index.get_value()) : Integer.parseInt(index.get_value());

                stack.addFirst(new Lexeme(LexType.NUM, variablesList.get(name.get_value()).get(index_value)));
            } else if (lexeme.get_value().equals("printList")) {
                variablesList.get(name.get_value()).printList();
                System.out.println();
            }
        } else if (variablesSet.containsKey(name.get_value())) {


            if (lexeme.get_value().equals("add")) {

                Lexeme value = stack.removeFirst();

                int value_value = value.getType() == NAME ? variables.get(value.get_value()) : Integer.parseInt(value.get_value());

                variablesSet.get(name.get_value()).add(value_value);
            } else if (lexeme.get_value().equals("contains")) {

                Lexeme index = stack.removeFirst();

                int value_value = index.getType() == NAME ? variables.get(index.get_value()) : Integer.parseInt(index.get_value());

                stack.addFirst(new Lexeme(BOOLEAN, variablesSet.get(name.get_value()).contains(value_value) == -1 ? Boolean.toString(false) : Boolean.toString(true)));
            } else if (lexeme.get_value().equals("delete")) {

                Lexeme index = stack.removeFirst();

                int value_value = index.getType() == NAME ? variables.get(index.get_value()) : Integer.parseInt(index.get_value());

                variablesSet.get(name.get_value()).delete(value_value);
            } else if (lexeme.get_value().equals("printSet")) {

                variablesSet.get(name.get_value()).print();
                System.out.println();
            }
        }
    }

    private void printConst(Lexeme a) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        System.out.println(a_value);
    }

    private void ForConst(Lexeme jmp, Lexeme bool) {

        int jmp_value = Integer.parseInt(jmp.get_value());
        boolean bool_value = Boolean.parseBoolean(bool.get_value());

        if (!bool_value)
            index = jmp_value - 1;
    }

    private void doWhileConst(Lexeme jmp, Lexeme bool) {

        int jmp_value = Integer.parseInt(jmp.get_value());
        boolean bool_value = Boolean.parseBoolean(bool.get_value());

        if (bool_value)
            index = jmp_value - 1;
    }

    private void whileConst(Lexeme jmp, Lexeme bool) {

        int jmp_value = Integer.parseInt(jmp.get_value());
        boolean bool_value = Boolean.parseBoolean(bool.get_value());

        if (!bool_value)
            index = jmp_value - 1;
    }

    private Lexeme notEqual(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value != a_value));
    }

    private Lexeme lessEqual(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value <= a_value));
    }

    private Lexeme moreEqual(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value >= a_value));
    }

    private Lexeme less(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value < a_value));
    }

    private Lexeme more(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value > a_value));
    }

    private void ifConst(Lexeme jmp, Lexeme bool) {

        int jmp_value = Integer.parseInt(jmp.get_value());
        boolean bool_value = Boolean.parseBoolean(bool.get_value());

        if (!bool_value)
            index = jmp_value - 1;
    }

    private Lexeme equal(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(BOOLEAN, Boolean.toString(b_value == a_value));
    }

    private Lexeme div(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(NUM, Integer.toString(b_value / a_value));
    }

    private Lexeme mul(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(NUM, Integer.toString(b_value * a_value));
    }

    private Lexeme sub(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(NUM, Integer.toString(b_value - a_value));
    }

    private void assign(Lexeme a, Lexeme b) {

        variables.put(b.get_value(), Integer.parseInt(a.get_value()));
    }

    private Lexeme sum(Lexeme a, Lexeme b) {

        int a_value = a.getType() == NAME ? variables.get(a.get_value()) : Integer.parseInt(a.get_value());
        int b_value = b.getType() == NAME ? variables.get(b.get_value()) : Integer.parseInt(b.get_value());

        return new Lexeme(NUM, Integer.toString(a_value+b_value));
    }

    private boolean isOp(Lexeme lexeme) {

        LexType type = lexeme.getType();

        return type == OP_ADD || type == OP_SUB || type == OP_DIV || type == OP_MUL ||
                type == OP_EQUAL || type == OP_LESS || type == OP_LESS_EQUAL || type == OP_MORE || type == OP_MORE_EQUAL || type == OP_NOT_EQUAL || type == OP_ASSIGN;
    }

    public void print () {

        System.out.println("[StackMachine] Variable values:");

        Set <String> keys = variables.keySet();

        for (String name: keys) {

            System.out.println(name + " " + variables.get(name));
        }
    }
}
