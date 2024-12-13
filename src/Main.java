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
        // Create a new Menu instance and display the main menu to the user
        Menu menu = new Menu();
        menu.displayMenu();
    }
}
