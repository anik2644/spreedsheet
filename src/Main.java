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
///     =SUMA(A1;SUMA(B1:C2);suma(5 ,A2),min(a1:a3))
// =SUMA(B1:C2) +  SUMA(A1;SUMA(B1:C2);suma(5 ,A2),min(a1:a3)) +5 +min(a1:a3)
//  =SUMA(B1:C2)
//1 + A1*((SUMA(A2:B2;PROMEDIO(B1:b3);C1;27)/4)+(a1-a2))
/// =max(a1;suma(b1:c1);5))