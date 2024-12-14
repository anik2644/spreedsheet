import ui.*;

import io.FileManager;
import utils.Evaluator;

import java.io.IOException;
import java.util.*;

/**
 * The Main class handles the user interface and integrates all the spreadsheet functionality.
 */
public class Main {

    public static void main(String[] args) {

        // System.out.println("Creating an instance of the Menu class...");
        Menu menu = new Menu();


        // System.out.println("Calling the displayMenu method to show the menu...");
        System.out.println("Displaying the menu...");
        menu.displayMenu();


        // System.out.println("The displayMenu method has completed execution...");


        // System.out.println("Program execution is about to complete...");
        System.out.println("Menu displayed successfully. Program execution complete.");
    }
}
