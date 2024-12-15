package ui;

import model.*;
import io.FileManager;


import java.util.*;
import java.io.IOException;

import static model.FormulaTreeParser.parseFormula;


/**
 * The Menu class provides a user interface for interacting with the Spreadsheet.
 */
public class Menu {
    private Spreadsheet spreadsheet;
    private FileManager fileManager;
    private Scanner scanner;

    // Constructor
    public Menu() {
        this.spreadsheet = new Spreadsheet();
        this.fileManager = new FileManager();
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== Spreadsheet Menu ===");
            System.out.println("1. Add or Modify Cell");
            System.out.println("2. Display Spreadsheet");
            System.out.println("3. Save Spreadsheet to File");
            System.out.println("4. Load Spreadsheet from File");
            System.out.println("5. Exit");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addOrModifyCell();
                    break;
                case "2":
                    displaySpreadsheet();
                    break;
                case "3":
                    saveSpreadsheet();
                    break;
                case "4":
                    loadSpreadsheet();
                    break;
                case "6":
                    tocheck();
                    break;
                case "5":
                    exit = true;
                    System.out.println("Exiting the program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private boolean isValidCoordinate(String coordinate) {
        // Regular expression to match cell coordinates like A1, B2, AA10, etc.
        return coordinate.matches("[A-Z]+\\d+");
    }

    private boolean isValidFormula(String formula) {
        if (!formula.startsWith("=")) {
            // System.out.println("Formula does not start with '='.");
            return false;
        }

        formula = formula.substring(1).trim();

        String cellRef = "[A-Z]+\\d+";

        String cellRange = cellRef + ":" + cellRef;

        String number = "\\d+(\\.\\d+)?";

        String function = "(SUMA|MIN|MAX|PROMEDIO)\\(([^()]*)\\)";

        String validToken = String.format("(%s|%s|%s|%s)", function, cellRange, cellRef, number);

        String formulaRegex = String.format("^(%s|[+\\-*/();\\s])+?$", validToken);

        // System.out.println("Validating formula: " + formula);
        // System.out.println("Regex used: " + formulaRegex);

        boolean result = formula.matches(formulaRegex);

        // System.out.println("Validation result: " + result);

        return result;
    }


    public static int findOperatorIndex(String formula) {
        // Define the list of operators
        String operators = "[+\\-*/%]";

        // Loop through the formula and check each character
        for (int i = 0; i < formula.length(); i++) {
            if (String.valueOf(formula.charAt(i)).matches(operators)) {
                return i;  // Return the index of the first operator found
            }
        }

        return -1;  // Return -1 if no operator is found
    }

    public boolean ifNested(String formula) {
        // Check if the formula contains a function call with parentheses
        // and looks for another function call inside
        int openParenthesesCount = 0;
        boolean hasNestedFunction = false;

        // Loop through the formula character by character
        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);

            // When an opening parenthesis is found
            if (ch == '(') {
                openParenthesesCount++;
            }
            // When a closing parenthesis is found
            else if (ch == ')') {
                openParenthesesCount--;
            }

            // If there are nested parentheses and we find another function call
            if (openParenthesesCount > 1 && (ch == ';'||ch == ':'||ch == ',')) {

           //     System.out.println(openParenthesesCount + "  "+ ch);
                hasNestedFunction = true;
                break;
            }
        }

        return hasNestedFunction;
    }



    // Visit the tree and calculate the final result, handling both functions and operators
    public String visitTree(TreeNode node, String coordinate) {
        if (node == null) return null;

        // Base case: If the node has no children, it's a leaf or operand
        if (node.children.isEmpty()) {
            return node.value; // Return the value of the leaf node
        }

        // Recursive case: Visit children first to process deeper levels
        for (TreeNode child : node.children) {
            visitTree(child, coordinate);
        }

        // Process the current node after its children (post-order traversal)
        if (isFunction(node.value)) {
            // Collect operands (children of the function) for the formula
            StringBuilder formula = new StringBuilder("=" + node.value + "(");
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode child = node.children.get(i);
                if (i > 0) formula.append("; ");
                if (isRange(child.value)) {
                    // Treat ranges as a single operand
                    formula.append(child.value); // Append the range as it is
                } else {
                    formula.append(child.value);
                }
            }
            formula.append(")");

            // Print the most independent formula
            // System.out.println("Formula: " + formula);

            // Create formula content and update the spreadsheet
            FormulaContent formulaContent = new FormulaContent(formula.toString());
            spreadsheet.addOrModifyCell(coordinate, formulaContent);

            // Retrieve the calculated value from the spreadsheet
            Cell cell = spreadsheet.getCell(coordinate);
            String value = (cell != null) ? cell.getValueAsString() : "Cell is empty";
            // System.out.println("Value of: " + coordinate + "  : " + value);

            // Replace this formula with the calculated value
            node.value = value;
            node.children.clear(); // Clear children to make this node a leaf

            // Return the calculated value
            return value;
        }

        // Now handle operators at the current level (e.g., multiplication)
        if (isOperator(node.value)) {
            // Process operands based on the operator
            double leftOperand = Double.parseDouble(node.children.get(0).value);
            double rightOperand = Double.parseDouble(node.children.get(1).value);

            double result = 0;
            switch (node.value) {
                case "+":
                    result = leftOperand + rightOperand;
                    break;
                case "-":
                    result = leftOperand - rightOperand;
                    break;
                case "*":
                    result = leftOperand * rightOperand;
                    break;
                case "/":
                    result = leftOperand / rightOperand;
                    break;
                case "%":
                    result = leftOperand % rightOperand;
                    break;
            }

            // Update the node value with the result of the operation
            node.value = String.valueOf(result);

            // Return the calculated value
            return node.value;
        }

        // If the node is not a function or operator, return its value
        return node.value;
    }

    // Helper method to identify operators
    private static boolean isOperator(String value) {
        return value.equals("+") || value.equals("-") || value.equals("*") || value.equals("/") || value.equals("%");
    }

    // Helper function to identify ranges
    private boolean isRange(String value) {
        return value.matches("[A-Za-z]+\\d+:[A-Za-z]+\\d+");
    }



    // Check if the value is a function (like SUMA, MIN, etc.)
    private static boolean isFunction(String value) {
        return value.matches("SUMA|MIN|MAX|PROMEDIO|\\+|\\-|\\*|\\/|%");
    }



    public String complexFormula(String finalString,String formula, String coordinate) {

        String value;
        int operatorIndex = findOperatorIndex(formula);
       // System.out.println("Operator Index: " + operatorIndex);

        // Split the formula if an operator is found
        if (operatorIndex != -1) {

            String part1 = cleanString(formula.substring(0, operatorIndex).trim());
            String part2 = "="+ cleanString(formula.substring(operatorIndex + 1).trim());


            if(this.ifNested(part1)) {
                TreeNode root = parseFormula(part1);
                value = visitTree(root, coordinate);
            }
            else{
                FormulaContent formulaContent = new FormulaContent(part1);
                spreadsheet.addOrModifyCell(coordinate, formulaContent);
                // Retrieve the calculated value from the spreadsheet
                Cell cell = spreadsheet.getCell(coordinate);
                value = (cell != null) ? cell.getValueAsString() : "Cell is empty";

            }

           // System.out.println("First part: " + part1);
          // System.out.println("Second part: " + part2);




            finalString = finalString + value + " "+ formula.charAt(operatorIndex) + " ";;
           // System.out.println("cur fin " + finalString);

            finalString = finalString + complexFormula("",part2,coordinate);

        } else {
            if(this.ifNested(formula)) {
                TreeNode root = parseFormula(formula);
                value = visitTree(root, coordinate);
            }
            else{
                FormulaContent formulaContent = new FormulaContent(formula);
                spreadsheet.addOrModifyCell(coordinate, formulaContent);
                // Retrieve the calculated value from the spreadsheet
                Cell cell = spreadsheet.getCell(coordinate);
                value = (cell != null) ? cell.getValueAsString() : "Cell is empty";

            }

            finalString = finalString +" "+ value ;
          //  System.out.println("cur fin " + finalString);


        }

        return finalString;

    }
    public static String cleanString(String input) {
        // Remove all spaces
        input = input.replaceAll("\\s+", "");

        while (input.startsWith("(") && input.endsWith(")")) {
            input = input.substring(1);
            input = input.substring(0, input.length() - 1);
        }
            input = keepValidParentheses(input);
        return input;
    }

    private static String keepValidParentheses(String input) {
        StringBuilder result = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        // Process the string to keep only valid parentheses
        for (char c : input.toCharArray()) {
            if (c == '(') {
                stack.push(c);  // Push opening parenthesis onto the stack
                result.append(c);  // Append to the result
            } else if (c == ')') {
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop();  // Remove matching opening parenthesis from the stack
                    result.append(c);  // Append to the result
                }
                // If no matching opening parenthesis, ignore this closing parenthesis
            } else {
                result.append(c);  // Append non-parenthesis characters
            }
        }

        // Remove unmatched opening parentheses from the result
        while (!stack.isEmpty()) {
            int index = result.lastIndexOf("(");
            if (index != -1) {
                result.deleteCharAt(index);
                stack.pop();
            }
        }

        return result.toString();
    }


    private void addOrModifyCell() {
        String coordinate;
        while (true) {
            System.out.print("Enter cell coordinate (e.g., A1): ");
            coordinate = scanner.nextLine().toUpperCase().trim();


            if (isValidCoordinate(coordinate)) {
                break;
            } else {
                System.out.println("Invalid cell coordinate format! Please enter a valid coordinate (e.g., A1, B2, AA10).");
            }
        }


        System.out.print("Enter cell content (text, number, or formula starting with '='): ");
        String contentInput = scanner.nextLine();

        Content content;
        if (contentInput.startsWith("=")) {
             Spreadsheet.coordinateMap.put(coordinate, contentInput);
//            Map<String, Cell> cells = new HashMap<>();
//            Cell cell = cells.getOrDefault(coordinate, new Cell(coordinate));
//            cell.setContent(new FormulaContent(contentInput.toString()));
//            cells.put(coordinate, cell);

            contentInput = contentInput.toUpperCase();

//            // Example formulas to check
//            String formula1 = "=MAX(A1;SUMA(B1;C2);5)";
//            String formula2 = "=SUMA(B1:C2)";
//            String formula3 = "=MIN(A1;SUMA(B1;C2);5)";
//            String formula4 = "=MAX(A1;B1;C2)";
//            String formula5 = "=a1+a2";
//
//            System.out.println("Formula 1 has nested functions: " + this.ifNested(formula1)); // true
//            System.out.println("Formula 2 has nested functions: " + this.ifNested(formula2)); // false
//            System.out.println("Formula 3 has nested functions: " + this.ifNested(formula3)); // true
//            System.out.println("Formula 4 has nested functions: " + this.ifNested(formula4)); // false
//            System.out.println("Formula 4 has nested functions: " + this.ifNested(formula5)); // false
//


            if(this.ifNested(contentInput))
            {
              //  System.out.println("Formula has nested functions: " + contentInput);

                // String formula = "= SUMA(B1:C2) * SUMA(A1;SUMA(B1:C2)";


                int operatorIndex = findOperatorIndex(contentInput);
             //   System.out.println("Operator Index: " + operatorIndex);

                // Split the formula if an operator is found
                if (operatorIndex != -1) {
                    String finalString = "=";
                    contentInput =  contentInput.replaceAll("\\s+", "");
                    contentInput = cleanString(contentInput);
                  //  System.out.println(contentInput);
                    finalString =  complexFormula(finalString,contentInput,coordinate);
                    finalString = finalString.replaceAll("\\s+", "");
                    finalString = cleanString(finalString);
                    System.out.println("Final String: " + finalString);



                    FormulaContent formulaContent = new FormulaContent(finalString);
                    spreadsheet.addOrModifyCell(coordinate, formulaContent);
                    System.out.println("Cell " + coordinate + " updated successfully.");
//                    System.out.println("Final String: " + finalString);
                } else {
                    TreeNode root = parseFormula(contentInput);
                    // System.out.println(root);


                    String result = visitTree(root, coordinate);
                    // System.out.println("Final Value: " + result);


                    TextContent formu = new TextContent(result);
                    spreadsheet.addOrModifyCell(coordinate,formu );
                    System.out.println("Cell " + coordinate + " updated successfully.");
                   // System.out.println("No operator found.");
                }


                return;
                //contentInput = result;

//                String formula = "=MAX(A1;SUMA(B1;C2);5)";

                // Build the formula tree
//                FormulaNode treeRoot = buildFormulaTree(formula);

//                 Print the tree structure
//                System.out.println("Formula Tree:");
//                printFormulaTree(treeRoot, 0);

//                System.out.println("Leaf to Root traversal:");
//                postOrderTraversal(treeRoot);

/*
                String coordinatee = "A4";
                Cell cell = spreadsheet.getCell(coordinatee);
                String value = (cell != null) ? cell.getValueAsString() : "Cell A4 is empty";
                System.out.println("Value of A4: " + value);
*/



            }
            else if (!isValidFormula(contentInput)) {
                System.out.println("Invalid formula syntax! Please ensure the formula contains valid cell coordinates.");
                return;
            }


            FormulaContent formulaContent = new FormulaContent(contentInput);
            if (spreadsheet.hasCircularDependency(coordinate, formulaContent)) {
                System.out.println("Circular dependency detected! Cannot add this formula.");
                return;
            }

            content = formulaContent;
        } else {
            try {
                content = new NumericContent(Double.parseDouble(contentInput));
            } catch (NumberFormatException e) {
                content = new TextContent(contentInput);
            }
        }

        spreadsheet.addOrModifyCell(coordinate, content);
        System.out.println("Cell " + coordinate + " updated successfully.");
    }


    private void displaySpreadsheet() {
        System.out.println("\nCurrent Spreadsheet:");
        spreadsheet.displaySpreadsheet();
    }


    private void saveSpreadsheet() {
//        System.out.print("Enter file path to save the spreadsheet (e.g., spreadsheet.s2v): ");
//        String filePath = scanner.nextLine();
//
//        System.out.println("Generated Map: " + Spreadsheet.coordinateMap);
//
//        for (Map.Entry<String, String> entry : Spreadsheet.coordinateMap.entrySet()) {
//            String coordinate = entry.getKey();
//            String newContent = entry.getValue();
//
//            // Check if the cell exists, if not create a new one
//            Cell cell = spreadsheet.getCells().getOrDefault(coordinate, new Cell(coordinate));
//
//            // Update the cell's content with new content
//            // Assuming TextContent for simplicity. If it's a formula or number, modify this logic accordingly
//            Content cellContent = new FormulaContent(newContent);  // Assuming it's a formula content
//            cell.setContent(cellContent);
//
//            // Update the cell in the spreadsheet
//            spreadsheet.getCells().put(coordinate, cell);
//        }

        String filePath = "src/spreedshet.s2v";

        try {
            fileManager.saveSpreadsheet(filePath, spreadsheet);
            System.out.println("Spreadsheet saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving spreadsheet: " + e.getMessage());
        }
    }


    private void loadSpreadsheet() {
//        System.out.print("Enter file path to load the spreadsheet (e.g., spreadsheet.s2v): ");
//        String filePath = scanner.nextLine();
        String filePath = "src/spreedshet.s2v";
        try {
            this.spreadsheet = fileManager.loadSpreadsheet(filePath);
            System.out.println("Spreadsheet loaded successfully from " + filePath);
        } catch (IOException e) {
            System.err.println("Error loading spreadsheet: " + e.getMessage());
        }
    }

    private void tocheck(){
        System.out.println("nothing to do");
        return;
    }


    // Helper method to print the formula tree
    public static void printFormulaTree(FormulaNode node, int level) {
        // Print the current node's value (formula or argument)
        System.out.println("  ".repeat(level) + node.value);

        // Recursively print each child (inner formulas or arguments)
        for (FormulaNode child : node.children) {
            printFormulaTree(child, level + 1);
        }
    }

    public void postOrderTraversal(FormulaNode node) {
        // If the node is not null, process it
        if (node != null) {
            // First, recursively traverse all children (from leaf to root)
            for (FormulaNode child : node.children) {
                postOrderTraversal(child);
            }

            // After traversing the children, process the current node (print value)
            System.out.println("Node value: " + node.value); // Print the node's value (formula or argument)
        }
    }
    // Check if the value is an operator (e.g., +, -, *, /)
    public static FormulaNode buildFormulaTree(String formula) {
        // Remove the leading '=' symbol
        formula = formula.substring(1);

        // Find the root function (e.g., MAX, SUMA, etc.)
        String rootFunction = formula.substring(0, formula.indexOf('('));  // Get the function name
        FormulaNode rootNode = new FormulaNode(rootFunction);

        // Extract inner formulas and arguments recursively
        int openParenthesesCount = 0;
        StringBuilder currentFormula = new StringBuilder();
        List<FormulaNode> currentArguments = new ArrayList<>();

        // Process the formula
        for (int i = formula.indexOf('(') + 1; i < formula.length(); i++) {
            char ch = formula.charAt(i);

            // Handle opening parentheses
            if (ch == '(') {
                openParenthesesCount++;
                currentFormula.append(ch);  // Start a new formula inside parentheses
            }
            // Handle closing parentheses
            else if (ch == ')') {
                openParenthesesCount--;
                currentFormula.append(ch);  // End the current formula

                if (openParenthesesCount == 0) {
                    // We've finished processing an inner formula
                    FormulaNode childNode = new FormulaNode(currentFormula.toString());
                    currentArguments.add(childNode);
                    currentFormula.setLength(0);  // Clear the current formula for next argument
                }
            }
            // Handle semicolon separating arguments
            else if (ch == ';' && openParenthesesCount == 0) {
                // Add the argument if it is not an empty string
                if (currentFormula.length() > 0) {
                    FormulaNode childNode = new FormulaNode(currentFormula.toString());
                    currentArguments.add(childNode);
                    currentFormula.setLength(0);  // Reset the current formula for next argument
                }
            } else {
                currentFormula.append(ch);  // Add the character to the current argument/formula
            }
        }

        // Add the last argument if there is any remaining formula part after the loop
        if (currentFormula.length() > 0) {
            FormulaNode childNode = new FormulaNode(currentFormula.toString());
            currentArguments.add(childNode);
        }

        // Add all arguments as children of the root node
        for (FormulaNode child : currentArguments) {
            rootNode.addChild(child);
        }

        return rootNode;
    }
    // Check if the node value is a numeric operand (not a function/operator)
    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Check if the value is a cell reference
    private static boolean isCellReference(String value) {
        return value.matches("[A-Z]+\\d+");
    }

    // Formula Node class to represent a node in the formula tree
    static class FormulaNode {
        String value; // The value of the node (could be a function or an argument)
        List<FormulaNode> children; // List of child nodes (arguments or inner formulas)

        // Constructor
        public FormulaNode(String value) {
            this.value = value;
            this.children = new ArrayList<>();
        }

        // Add a child node
        public void addChild(FormulaNode child) {
            this.children.add(child);
        }
    }





}