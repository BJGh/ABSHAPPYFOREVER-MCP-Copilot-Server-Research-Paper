package tree;
import tokens.Lexeme;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    private NodeType type;
    private List <TreeNode> nodes;
    private List <Lexeme> leafs;

    public TreeNode(NodeType type) {

        this.type = type;
        this.leafs = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public TreeNode(NodeType type, List <TreeNode> nodes, List <Lexeme> leafs) {

        this.type = type;
        this.leafs = leafs;
        this.nodes = nodes;
    }

    public void addLeaf (Lexeme leaf) {

        leafs.add(leaf);
    }

    public void addNode (TreeNode node) {

        nodes.add(node);
    }

    public void print () {

        this.printNext("", "", "", true);
    }

    private void printNext (String format, String format2, String format3, boolean last) {

        if (last)
            System.out.printf(format2);
        else
            System.out.printf(format3);

        System.out.println("╥" + type);

        for (int i = 0; i < leafs.size(); i++) {

            if (i == leafs.size() - 1 && this.nodes.size()==0) {

                System.out.printf(format + "╙────");
                System.out.println(leafs.get(i).getType() + (leafs.get(i).get_value() == null ? "" :(" (" + leafs.get(i).get_value() + ")")));
            } else {

                System.out.printf(format + "╟────");
                System.out.println(leafs.get(i).getType() + (leafs.get(i).get_value() == null ? "" :(" (" + leafs.get(i).get_value() + ")")));
            }
        }

        for (int i = 0; i < nodes.size(); i++) {

            if (i == nodes.size()-1)
                nodes.get(i).printNext(format + "     ", format + "╟────", format + "╙────", false);
            else
                nodes.get(i).printNext(format + "║    ", format + "╟────", format + "╙────", true);
        }
    }

    public NodeType getType() {

        return type;
    }

    public List <TreeNode> getNodes () {

        return nodes;
    }

    public List <Lexeme> getLeafs () {

        return leafs;
    }
}
