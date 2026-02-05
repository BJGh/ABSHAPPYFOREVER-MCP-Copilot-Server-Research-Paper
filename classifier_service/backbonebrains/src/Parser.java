import tokens.LexType;
import tokens.Lexeme;
import tree.TreeNode;

import java.util.List;

import static tokens.LexType.*;
import static tree.NodeType.*;

public class Parser {

    private List<Lexeme> lexemes;
    private int position;

    TreeNode AST;

    public Parser (List <Lexeme> lexemes) {

        this.lexemes = lexemes;
        this.position = 0;
    }

    public void analysis () {

        long time_analysis = System.nanoTime();

        AST = new TreeNode(Language);

        while (currentLexeme() != END) {

            try {

                languageConst(AST);
            } catch (Exception e) {

                //System.out.println("Синтаксическая ошибка: " + position + currentLexeme());
                e.printStackTrace();
                break;
            }
        }

        System.out.println("[Parser] time analysis: " + (System.nanoTime() - time_analysis) / 1_000_000.0 + "ms");
    }

    private void languageConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(LanguageConst);
        astTop.addNode(astNext);

        if (currentLexeme() == NAME) {

            if (nextLexeme() == OP_DOT) {

                callFuncConst(astNext);
            } else {

                assignConst(astNext);
            }
        } else if (currentLexeme() == WHILE) {

            whileConst(astNext);
        } else if (currentLexeme() == IF) {

            ifConst(astNext);
        } else if (currentLexeme() == DO) {

            doWhileConst(astNext);
        } else if (currentLexeme() == FOR) {

            forConst(astNext);
        } else if (currentLexeme() == PRINT) {

            printConst(astNext);
        } else if (currentLexeme() == LIST) {

            listConst(astNext);
        } else if (currentLexeme() == SET) {

            setConst(astNext);
        }else {

            throw new Exception();
        }
    }

    private void callFuncConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(CallFuncConst);
        astTop.addNode(astNext);

        if (currentLexeme() == NAME) {
            astNext.addLeaf(LexCheck(NAME));
            astNext.addLeaf(LexCheck(OP_DOT));
            astNext.addLeaf(new Lexeme (FUNC, LexCheck(NAME).get_value()));
            astNext.addLeaf(LexCheck(L_BRACKET));

            if (currentLexeme() != R_BRACKET) {
                expression(astNext);
                while (currentLexeme() == COMMA) {
                    astNext.addLeaf(LexCheck(COMMA));
                    expression(astNext);
                }
            }
            astNext.addLeaf(LexCheck(R_BRACKET));
            astNext.addLeaf(LexCheck(SEMICOLON));
        }
        else
            throw new Exception();
    }

    private void listConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(ListConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(LIST));
        astNext.addLeaf(LexCheck(NAME));
        astNext.addLeaf(LexCheck(SEMICOLON));
    }

    private void setConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(SetConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(SET));
        astNext.addLeaf(LexCheck(NAME));
        astNext.addLeaf(LexCheck(SEMICOLON));
    }

    private void printConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(PrintConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(PRINT));
        expression(astNext);
        astNext.addLeaf(LexCheck(SEMICOLON));
    }



    private void doWhileConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(DoWhileConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(DO));
        block(astNext);
        astNext.addLeaf(LexCheck(WHILE));
        astNext.addLeaf(LexCheck(L_BRACKET));
        expression(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
        astNext.addLeaf(LexCheck(SEMICOLON));
    }

    private void forConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(ForConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(FOR));
        astNext.addLeaf(LexCheck(L_BRACKET));
        assignConstFor(astNext);
        astNext.addLeaf(LexCheck(SEMICOLON));
        expression(astNext);
        astNext.addLeaf(LexCheck(SEMICOLON));
        assignConstFor(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
        block(astNext);
    }

    private void assignConstFor(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(AssignConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(NAME));
        astNext.addLeaf(LexCheck(OP_ASSIGN));
        expression(astNext);
    }

    private void ifConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(IfConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(IF));
        astNext.addLeaf(LexCheck(L_BRACKET));
        expression(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
        block(astNext);

        while (currentLexeme() == ELIF)
            elifConst(astNext);

        if (currentLexeme() == ELSE)
            elseConst(astNext);
    }

    private void elifConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(ElifConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(ELIF));
        astNext.addLeaf(LexCheck(L_BRACKET));
        expression(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
        block(astNext);
    }

    private void elseConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(ElseConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(ELSE));
        block(astNext);
    }

    private void whileConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(WhileConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(WHILE));
        astNext.addLeaf(LexCheck(L_BRACKET));
        expression(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
        block(astNext);
    }

    private void block(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(Block);
        astTop.addNode(astNext);

        if (currentLexeme() == L_BRACE) {

            astNext.addLeaf(LexCheck(L_BRACE));

            while (currentLexeme() != R_BRACE)
                languageConst(astNext);

            astNext.addLeaf(LexCheck(R_BRACE));

        } else {

            languageConst(astNext);
        }
    }

    private void assignConst(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(AssignConst);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(NAME));
        astNext.addLeaf(LexCheck(OP_ASSIGN));
        expression(astNext);
        astNext.addLeaf(LexCheck(SEMICOLON));
    }

    private boolean isOp (LexType type) {

        return type == OP_ADD || type == OP_SUB || type == OP_DIV || type == OP_MUL ||
                type == OP_EQUAL || type == OP_LESS || type == OP_LESS_EQUAL || type == OP_MORE || type == OP_MORE_EQUAL || type == OP_NOT_EQUAL;
    }

    private void expression(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(Expression);
        astTop.addNode(astNext);

        if (currentLexeme() == NUM || currentLexeme() == NAME) {

            if (currentLexeme() == NAME && nextLexeme() == OP_DOT) {

                memberCont(astNext);
            } else {
                member(astNext);
            }
        } else if (currentLexeme() == L_BRACKET) {

            memberBracket(astNext);
        } else {

            throw new Exception();
        }

        while (isOp(currentLexeme())) {

            op(astNext);
            expression(astNext);
        }
    }

    private void memberCont(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(MemberCont);
        astTop.addNode(astNext);

        if (currentLexeme() == NAME) {
            astNext.addLeaf(LexCheck(NAME));
            astNext.addLeaf(LexCheck(OP_DOT));
            astNext.addLeaf(new Lexeme (FUNC, LexCheck(NAME).get_value()));
            astNext.addLeaf(LexCheck(L_BRACKET));

            if (currentLexeme() != R_BRACKET) {
                expression(astNext);
                while (currentLexeme() == COMMA) {
                    astNext.addLeaf(LexCheck(COMMA));
                    expression(astNext);
                }
            }
            astNext.addLeaf(LexCheck(R_BRACKET));
        }
        else
            throw new Exception();
    }


    private void op(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(Op);
        astTop.addNode(astNext);

        if (isOp(currentLexeme()))
            astNext.addLeaf(getNextLexeme());
        else
            throw new Exception();
    }

    private void memberBracket(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(BracketMember);
        astTop.addNode(astNext);

        astNext.addLeaf(LexCheck(L_BRACKET));
        expression(astNext);
        astNext.addLeaf(LexCheck(R_BRACKET));
    }

    private void member(TreeNode astTop) throws Exception {

        TreeNode astNext = new TreeNode(Member);
        astTop.addNode(astNext);

        if (currentLexeme() == NUM || currentLexeme() == NAME)
            astNext.addLeaf(getNextLexeme());
        else
            throw new Exception();
    }

    private Lexeme LexCheck(LexType type) throws Exception {

        if (currentLexeme() != type)
            throw new Exception();
        else
            return lexemes.get(position++);
    }

    private Lexeme getNextLexeme () {

        return lexemes.get(position++);
    }

    private LexType nextLexeme () {

        return lexemes.get(position + 1).getType();
    }

    private LexType currentLexeme () {

        return lexemes.get(position).getType();
    }

    public TreeNode getTree() {

        return AST;
    }
}
