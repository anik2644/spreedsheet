package model;
import java.util.*;
public class FormulaTreeParser {

    public static TreeNode parseFormula(String formula) {
        formula = formula.trim();
        if (formula.isEmpty()) return null;

        // Remove the initial '=' if present
        if (formula.startsWith("=")) {
            formula = formula.substring(1);
        }

        return parseExpression(formula);
    }

    private static TreeNode parseExpression(String expr) {
        Stack<TreeNode> stack = new Stack<>();
        StringBuilder currentToken = new StringBuilder();
        TreeNode root = null;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '(') {
                // Push the current token as an operator node
                if (currentToken.length() > 0) {
                    TreeNode node = new TreeNode(currentToken.toString().trim());
                    stack.push(node);
                    currentToken.setLength(0);
                }
            } else if (c == ')') {
                // Complete the current node and pop stack
                if (currentToken.length() > 0) {
                    TreeNode child = new TreeNode(currentToken.toString().trim());
                    stack.peek().addChild(child);
                    currentToken.setLength(0);
                }
                TreeNode completedNode = stack.pop();
                if (stack.isEmpty()) {
                    root = completedNode;
                } else {
                    stack.peek().addChild(completedNode);
                }
            } else if (c == ',' || c == ';') {
                // Add the current token as a child to the top of the stack
                if (currentToken.length() > 0) {
                    TreeNode child = new TreeNode(currentToken.toString().trim());
                    stack.peek().addChild(child);
                    currentToken.setLength(0);
                }
            } else {
                // Append characters to the current token
                currentToken.append(c);
            }
        }

        // Handle root-level expressions without parentheses
        if (currentToken.length() > 0 && root == null) {
            root = new TreeNode(currentToken.toString().trim());
        }

        return root;
    }


}