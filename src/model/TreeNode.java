package model;
import java.util.*;



// Node class to represent the tree structure
public class TreeNode {
    public String value; // Operator or Operand
    public List<TreeNode> children;

    public TreeNode(String value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringHelper(this, sb, 0);
        return sb.toString();
    }

    private void toStringHelper(TreeNode node, StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) sb.append("  ");
        sb.append(node.value).append("\n");
        for (TreeNode child : node.children) {
            toStringHelper(child, sb, depth + 1);
        }
    }
}
