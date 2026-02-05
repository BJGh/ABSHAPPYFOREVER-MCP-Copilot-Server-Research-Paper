import tokens.Lexeme;
import tree.TreeNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static tokens.LexType.*;
import static tree.NodeType.*;

public class TranslatorRPN {

    private TreeNode AST;

    private LinkedList <Lexeme> texas;
    private ArrayList <Lexeme> RPN;

    private HashSet<String> variables;
    private HashSet<String> variablesList;
    private HashSet<String> variablesSet;

    public TranslatorRPN (TreeNode AST) {

        this.AST = AST;

        this.texas = new LinkedList<>();
        this.RPN = new ArrayList<>();
        this.variables = new HashSet<>();
        this.variablesList = new HashSet<>();
        this.variablesSet = new HashSet<>();
    }

    public void translate () {

        long time_analysis = System.nanoTime();

        try {

            for (TreeNode node : AST.getNodes()) {

                languageConst(node);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println("[TranslatorRPN] time translate: " + (System.nanoTime() - time_analysis) / 1_000_000.0 + "ms");
    }

    private void languageConst(TreeNode node) throws Exception {

        TreeNode nodeConst = node.getNodes().get(0);

        if (nodeConst.getType() == AssignConst) {

            assignConst(nodeConst);
        } else if (nodeConst.getType() == IfConst) {

            ifConst(nodeConst);
        } else if (nodeConst.getType() == WhileConst) {

            whileConst(nodeConst);
        } else if (nodeConst.getType() == DoWhileConst) {

            doWhileConst(nodeConst);
        } else if (nodeConst.getType() == ForConst) {

            forConst(nodeConst);
        } else if (nodeConst.getType() == PrintConst) {

            printConst(nodeConst);
        } else if (nodeConst.getType() == ListConst) {

            listConst(nodeConst);
        } else if (nodeConst.getType() == CallFuncConst) {

            callFuncConst(nodeConst);
        } else if (nodeConst.getType() == SetConst) {

            setConst(nodeConst);
        }
    }

    private void setConst(TreeNode nodeConst) {

        addOprand(nodeConst.getLeafs().get(1));
        addOprand(nodeConst.getLeafs().get(0));

        variables.add(nodeConst.getLeafs().get(1).get_value());
        variablesSet.add(nodeConst.getLeafs().get(1).get_value());
    }

    private void callFuncConst(TreeNode node) throws Exception {

        if (!variables.contains(node.getLeafs().get(0).get_value()) && (!variablesList.contains(node.getLeafs().get(0).get_value()) || !variablesSet.contains(node.getLeafs().get(0).get_value()))) {

            throw new Exception ();
        }

        if (variablesList.contains(node.getLeafs().get(0).get_value())) {

            if (node.getLeafs().get(2).get_value().equals("add")) {

                if (node.getNodes().size() == 2) {

                    expression(node.getNodes().get(0));
                    expression(node.getNodes().get(1));

                } else {

                    throw new Exception();
                }
            } else if (node.getLeafs().get(2).get_value().equals("printList")) {

                if (node.getNodes().size() == 0) {

                } else {

                    throw new Exception();
                }
            } else  {

                throw new Exception();
            }
        } else if (variablesSet.contains(node.getLeafs().get(0).get_value())) {

            if (node.getLeafs().get(2).get_value().equals("add")) {

                if (node.getNodes().size() == 1) {

                    expression(node.getNodes().get(0));

                } else {

                    throw new Exception();
                }
            } else if (node.getLeafs().get(2).get_value().equals("delete")) {

                if (node.getNodes().size() == 1) {

                    expression(node.getNodes().get(0));


                } else {

                    throw new Exception();
                }
            } else  if (node.getLeafs().get(2).get_value().equals("printSet")) {

                if (node.getNodes().size() == 0) {

                } else {

                    throw new Exception();
                }
            } else {

                throw new Exception();
            }
        }

        addOprand(node.getLeafs().get(0));
        addOprand(node.getLeafs().get(2));
    }

    private void listConst(TreeNode nodeConst) throws Exception {

        addOprand(nodeConst.getLeafs().get(1));
        addOprand(nodeConst.getLeafs().get(0));

        variables.add(nodeConst.getLeafs().get(1).get_value());
        variablesList.add(nodeConst.getLeafs().get(1).get_value());
    }

    private void printConst(TreeNode nodeConst) throws Exception {

        expression(nodeConst.getNodes().get(0));
        flushTexas();
        addOprand(nodeConst.getLeafs().get(0));
    }

    private void forConst(TreeNode nodeConst) throws Exception {

        assignConst(nodeConst.getNodes().get(0));

        int start = RPN.size();

        expression(nodeConst.getNodes().get(1));
        flushTexas();

        Lexeme point = new Lexeme(JMP_VALUE);
        addOprand(point);
        addOprand(nodeConst.getLeafs().get(0));

        block(nodeConst.getNodes().get(3));
        assignConst(nodeConst.getNodes().get(2));

        Lexeme endPoint = new Lexeme(JMP_VALUE);
        addOprand(endPoint);
        addOprand(new Lexeme (JMP));

        point.set_value(Integer.toString(RPN.size()));
        endPoint.set_value(Integer.toString(start));
    }

    private void doWhileConst(TreeNode nodeConst) throws Exception {

        int start = RPN.size();

        block(nodeConst.getNodes().get(0));

        expression(nodeConst.getNodes().get(1));
        flushTexas();

        Lexeme point = new Lexeme(JMP_VALUE);
        addOprand(point);

        addOprand(nodeConst.getLeafs().get(0));

        point.set_value(Integer.toString(start));
    }

    private void whileConst(TreeNode nodeConst) throws Exception {

        int start = RPN.size();

        expression(nodeConst.getNodes().get(0));
        flushTexas();

        Lexeme point = new Lexeme(JMP_VALUE);
        addOprand(point);

        addOprand(nodeConst.getLeafs().get(0));
        block(nodeConst.getNodes().get(1));

        Lexeme endPoint = new Lexeme(JMP_VALUE);
        addOprand(endPoint);
        addOprand(new Lexeme (JMP));

        point.set_value(Integer.toString(RPN.size()));
        endPoint.set_value(Integer.toString(start));
    }

    private void ifConst(TreeNode nodeConst) throws Exception {

        expression(nodeConst.getNodes().get(0));
        flushTexas();

        Lexeme point = new Lexeme(JMP_VALUE);
        addOprand(point);

        addOprand(nodeConst.getLeafs().get(0));
        block(nodeConst.getNodes().get(1));

        Lexeme endPoint = new Lexeme(JMP_VALUE);
        addOprand(endPoint);
        addOprand(new Lexeme (JMP));

        point.set_value(Integer.toString(RPN.size()));

        for (int i = 2; i < nodeConst.getNodes().size(); i++) {

            if (nodeConst.getNodes().get(i).getType() == ElifConst) {

                elifConst(nodeConst.getNodes().get(i), endPoint);
            } else if (nodeConst.getNodes().get(i).getType() == ElseConst) {

                elseConst(nodeConst.getNodes().get(i));
            }
        }

        endPoint.set_value(Integer.toString(RPN.size()));
    }

    private void elifConst(TreeNode nodeConst, Lexeme endPoint) throws Exception {

        expression(nodeConst.getNodes().get(0));
        flushTexas();

        Lexeme point = new Lexeme(JMP_VALUE);
        addOprand(point);

        addOprand(nodeConst.getLeafs().get(0));
        block(nodeConst.getNodes().get(1));

        addOprand(endPoint);
        addOprand(new Lexeme (JMP));

        point.set_value(Integer.toString(RPN.size()));
    }

    private void elseConst(TreeNode nodeConst) throws Exception {

        block(nodeConst.getNodes().get(0));
    }

    private void block(TreeNode topNode) throws Exception {

        for(TreeNode node: topNode.getNodes()) {

            languageConst(node);
        }
    }

    private void assignConst(TreeNode nodeConst) throws Exception {

        addOprand(nodeConst.getLeafs().get(0));
        addOperator(nodeConst.getLeafs().get(1));
        expression(nodeConst.getNodes().get(0));
        flushTexas();

        variables.add(nodeConst.getLeafs().get(0).get_value());
    }

    private void expression(TreeNode topNode) throws Exception {

        List<TreeNode> nextNodes = topNode.getNodes();

        if (nextNodes.get(0).getType() == Member) {

            member(nextNodes.get(0));
        } else if (nextNodes.get(0).getType() == BracketMember) {

            bracketMember(nextNodes.get(0));
        } else if (nextNodes.get(0).getType() == MemberCont) {

            memberCont(nextNodes.get(0));
        }
        if (nextNodes.size()>1) {
            op(nextNodes.get(1));
            expression(nextNodes.get(2));
        }
    }

    private void memberCont(TreeNode node) throws Exception {

        if (!variables.contains(node.getLeafs().get(0).get_value()) && (!variablesList.contains(node.getLeafs().get(0).get_value()) || !variablesSet.contains(node.getLeafs().get(0).get_value()))) {

            throw new Exception ();
        }

        if (variablesList.contains(node.getLeafs().get(0).get_value())) {

            if (node.getLeafs().get(2).get_value().equals("get")) {

                if (node.getNodes().size() == 1) {

                    expression(node.getNodes().get(0));

                } else {

                    throw new Exception();
                }
            } else if (node.getLeafs().get(2).get_value().equals("peek")) {

                if (node.getNodes().size() == 1) {

                    expression(node.getNodes().get(0));

                } else {

                    throw new Exception();
                }
            } else {

                throw new Exception();
            }
        } else if (variablesSet.contains(node.getLeafs().get(0).get_value())) {

            if (node.getLeafs().get(2).get_value().equals("contains")) {

                if (node.getNodes().size() == 1) {

                    expression(node.getNodes().get(0));

                } else {

                    throw new Exception();
                }
            } else {

                throw new Exception();
            }
        }

        addOprand(node.getLeafs().get(0));
        addOprand(node.getLeafs().get(2));
    }

    private void bracketMember(TreeNode node) throws Exception {

        addOperator(node.getLeafs().get(0));
        expression(node.getNodes().get(0));
        addOperator(node.getLeafs().get(1));
    }

    private void op(TreeNode node) {

        addOperator(node.getLeafs().get(0));
    }

    private void member(TreeNode node) throws Exception {

        if (node.getLeafs().get(0).getType() == NAME && !variables.contains(node.getLeafs().get(0).get_value()) && (variablesList.contains(node.getLeafs().get(0).get_value()) || variablesSet.contains(node.getLeafs().get(0).get_value()))) {

            throw new Exception ();
        }

        addOprand(node.getLeafs().get(0));
    }

    private int opPriority (Lexeme op) {

        int priority;

        if (op.getType() == OP_ASSIGN)
            priority = 0;
        else if (op.getType() == OP_LESS)
            priority = 5;
        else if (op.getType() == OP_MORE)
            priority = 5;
        else if (op.getType() == OP_LESS_EQUAL)
            priority = 5;
        else if (op.getType() == OP_MORE_EQUAL)
            priority = 5;
        else if (op.getType() == OP_NOT_EQUAL)
            priority = 5;
        else if (op.getType() == OP_EQUAL)
            priority = 5;
        else if (op.getType() == OP_SUB)
            priority = 9;
        else if (op.getType() == OP_MUL)
            priority = 10;
        else if (op.getType() == OP_DIV)
            priority = 10;
        else if (op.getType() == OP_ADD)
            priority = 9;
        else
            priority = 0;

        return priority;
    }

    private void addOprand (Lexeme lexeme) {

        RPN.add(lexeme);
    }

    private void addOperator (Lexeme lexeme) {

        if (lexeme.getType() == L_BRACKET) {
            texas.addFirst(lexeme);
            return;
        }

        if (lexeme.getType() == R_BRACKET) {

            while (texas.peek().getType() != L_BRACKET) {

                RPN.add(texas.removeFirst());
            }

            texas.removeFirst();
            return;
        }

        while (true) {

            if (texas.peek()!=null && opPriority(texas.peek()) > opPriority(lexeme))
                RPN.add(texas.removeFirst());
            else {

                texas.addFirst(lexeme);
                return;
            }
        }
    }

    private void flushTexas () {

        int size = texas.size();

        for (int i = 0; i < size; i++) {

            RPN.add(texas.removeFirst());
        }
    }

    public void print () {

        System.out.println("[TranslatorRPN] reverse polish notation: ");
        System.out.printf("%-4s%-20s%-20s\n","№", "Name lexeme", "Value");

        int i = 0;

        for (Lexeme lexeme: RPN) {
            System.out.printf("%-4s", i++);
            lexeme.println();
        }
    }

    public ArrayList<Lexeme> getRPN() {

        return RPN;
    }
}
