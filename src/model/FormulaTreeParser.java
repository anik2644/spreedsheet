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
        boolean insideFunction = false;

        // Process the expression character by character
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '(') {
                // If we encounter a '(', we may need to process a function
                if (currentToken.length() > 0) {
                    TreeNode node = new TreeNode(currentToken.toString().trim());
                    stack.push(node);
                    currentToken.setLength(0);
                }
                insideFunction = true;
            } else if (c == ')') {
                // Closing a function, finish the current argument
                if (currentToken.length() > 0) {
                    TreeNode child = new TreeNode(currentToken.toString().trim());
                    if (!stack.isEmpty()) {
                        stack.peek().addChild(child);
                    }
                    currentToken.setLength(0);
                }

                // Pop the function node and connect to the parent
                if (!stack.isEmpty()) {
                    TreeNode completedNode = stack.pop();
                    if (stack.isEmpty()) {
                        root = completedNode;
                    } else {
                        stack.peek().addChild(completedNode);
                    }
                }
                insideFunction = false;
            } else if (c == ',' || c == ';') {
                // Handle separator for function arguments
                if (currentToken.length() > 0) {
                    TreeNode child = new TreeNode(currentToken.toString().trim());
                    if (!stack.isEmpty()) {
                        stack.peek().addChild(child);
                    }
                    currentToken.setLength(0);
                }
            } else {
                // Append characters to the current token
                currentToken.append(c);
            }
        }

        // Handle the last token after parsing the expression
        if (currentToken.length() > 0 && root == null) {
            root = new TreeNode(currentToken.toString().trim());
        }

        return root;
    }
}
