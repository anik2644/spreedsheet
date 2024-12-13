package ui;

import model.*;
import io.FileManager;

import java.io.IOException;
import java.util.Scanner;

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

    /**
     * Displays the main menu and processes user input.
     */
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
                case "5":
                    exit = true;
                    System.out.println("Exiting the program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Adds or modifies a cell in the spreadsheet.
     */
    private void addOrModifyCell() {
        System.out.print("Enter cell coordinate (e.g., A1): ");
        String coordinate = scanner.nextLine().toUpperCase();

        System.out.print("Enter cell content (text, number, or formula starting with '='): ");
        String contentInput = scanner.nextLine();

        Content content;
        if (contentInput.startsWith("=")) {
            content = new FormulaContent(contentInput);
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

    /**
     * Displays the entire spreadsheet.
     */
    private void displaySpreadsheet() {
        System.out.println("\nCurrent Spreadsheet:");
        spreadsheet.displaySpreadsheet();
    }

    /**
     * Saves the spreadsheet to a file.
     */
    private void saveSpreadsheet() {
        System.out.print("Enter file path to save the spreadsheet (e.g., spreadsheet.s2v): ");
        String filePath = scanner.nextLine();

        try {
            fileManager.saveSpreadsheet(filePath, spreadsheet);
            System.out.println("Spreadsheet saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving spreadsheet: " + e.getMessage());
        }
    }

    /**
     * Loads a spreadsheet from a file.
     */
    private void loadSpreadsheet() {
        System.out.print("Enter file path to load the spreadsheet (e.g., spreadsheet.s2v): ");
        String filePath = scanner.nextLine();

        try {
            this.spreadsheet = fileManager.loadSpreadsheet(filePath);
            System.out.println("Spreadsheet loaded successfully from " + filePath);
        } catch (IOException e) {
            System.err.println("Error loading spreadsheet: " + e.getMessage());
        }
    }
}
