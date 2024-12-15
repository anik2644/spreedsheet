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
    private static TreeNode refineTree(TreeNode node) {
        if (node == null) {
            return null;
        }

        // Recursively refine children
        List<TreeNode> refinedChildren = new ArrayList<>();
        for (TreeNode child : node.getChildren()) {
            TreeNode refinedChild = refineTree(child);
            if (refinedChild != null) {
                refinedChildren.add(refinedChild);
            }
        }

        // Replace the current children list with the refined one
        node.getChildren().clear();
        node.getChildren().addAll(refinedChildren);

        // Remove "SubExpr" nodes by promoting their children
        if ("SubExpr".equals(node.getValue())) {
            if (node.getChildren().size() == 1) {
                return node.getChildren().get(0); // Replace with its single child
            } else if (!node.getChildren().isEmpty()) {
                // Promote all children to the parent level
                TreeNode parentReplacement = new TreeNode("");
                parentReplacement.getChildren().addAll(node.getChildren());
                return parentReplacement;
            } else {
                return null; // Remove empty SubExpr node
            }
        }

        // Safely remove void nodes (empty nodes with no value and no children)
        if (node.getValue().isEmpty() && node.getChildren().isEmpty()) {
            return null; // Remove empty nodes
        }

        // Remove nodes that do not contain valid content (cells, operators, ranges)
        if (!isValidNode(node)) {
            return null; // Remove nodes that don't have valid content
        }

        return node;
    }

    private static boolean isValidNode(TreeNode node) {
        String value = node.getValue().toUpperCase(); // Normalize to uppercase for easy comparison

        // Valid values for operators (e.g., +, -, *, /)
        String[] validOperators = {"+", "-", "*", "/","%"};

        // Check if the value is a valid operator
        for (String operator : validOperators) {
            if (value.equals(operator)) {
                return true; // Valid operator
            }
        }

        // Check if the value is a valid function (e.g., SUMA, MIN, MAX, PROMEDIO)
        String[] validFunctions = {"SUMA", "MIN", "MAX", "PROMEDIO"};
        for (String function : validFunctions) {
            if (value.equals(function)) {
                return true; // Valid function (e.g., SUMA, MIN, MAX, PROMEDIO)
            }
        }

        // Check if the value is a valid cell reference (e.g., A1, B2, etc.)
        if (value.matches("[A-Z]+[0-9]+")) {
            return true; // Valid cell reference (e.g., A1, B2, etc.)
        }

        // Check if the value is a valid range (e.g., B1:C2)
        if (value.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+")) {
            return true; // Valid range (e.g., B1:C2)
        }

        // If the node's value does not match any valid operator, function, cell, or range, it's considered invalid
        return false;
    }


    private static TreeNode parseExpression(String expr) {
        Stack<TreeNode> stack = new Stack<>();
        StringBuilder currentToken = new StringBuilder();
        TreeNode root = null;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '(') {
                // Start of a sub-expression
                if (currentToken.length() > 0) {
                    TreeNode node = new TreeNode(currentToken.toString().trim());
                    stack.push(node);
                    currentToken.setLength(0);
                }
                stack.push(new TreeNode("("));
            } else if (c == ')') {
                // End of a sub-expression
                if (currentToken.length() > 0) {
                    TreeNode child = new TreeNode(currentToken.toString().trim());
                    stack.push(child);
                    currentToken.setLength(0);
                }

                // Gather all nodes in the current sub-expression
                List<TreeNode> subExprNodes = new ArrayList<>();
                while (!stack.isEmpty() && !"(".equals(stack.peek().getValue())) {
                    subExprNodes.add(0, stack.pop());
                }
                stack.pop(); // Remove the "("

                // Create a sub-expression node
                TreeNode subExprNode = new TreeNode("SubExpr");
                for (TreeNode node : subExprNodes) {
                    subExprNode.addChild(node);
                }
                stack.push(subExprNode);
            } else if (c == ',' || c == ';') {
                // Parameter separator
                if (currentToken.length() > 0) {
                    TreeNode node = new TreeNode(currentToken.toString().trim());
                    stack.push(node);
                    currentToken.setLength(0);
                }
            } else if (isOperatorChar(c)) {
                // Handle operators
                if (currentToken.length() > 0) {
                    TreeNode node = new TreeNode(currentToken.toString().trim());
                    stack.push(node);
                    currentToken.setLength(0);
                }

                TreeNode operatorNode = new TreeNode(String.valueOf(c));

                // If there's already a root, attach it as the left child
                if (root != null) {
                    operatorNode.addChild(root);
                }

                // Pop the right operand from the stack
                if (!stack.isEmpty()) {
                    TreeNode rightOperand = stack.pop();
                    operatorNode.addChild(rightOperand);
                }

                // Update the root to the operator
                root = operatorNode;
            } else {
                // Append characters to the current token
                currentToken.append(c);
            }
        }

        // Finalize the last token
        if (currentToken.length() > 0) {
            TreeNode node = new TreeNode(currentToken.toString().trim());
            if (root != null) {
                root.addChild(node);
            } else {
                stack.push(node);
            }
        }

        // Final tree construction
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            if (root == null) {
                root = node;
            } else {
                root.addChild(node);
            }
        }

        root = refineTree(root);
        return root;
    }






    private static void printTree(TreeNode node, int level) {
        if (node == null) return;

        // Indentation for visual structure
        String indent = "  ".repeat(level);
        System.out.println(indent + node.getValue());

        for (TreeNode child : node.getChildren()) {
            printTree(child, level + 1);
        }
    }



    // Helper method to identify operators
    private static boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    // Helper method to print the tree

}
