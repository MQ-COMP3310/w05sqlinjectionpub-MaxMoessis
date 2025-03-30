package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
//Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.util.regex.*;

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    // Start code for logging exercise
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            // e1.printStackTrace();
            System.err.println("Error occurred while trying to manage the logger"); // using syserr rather than printing the stack trace. 
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    // End code for logging exercise
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.log(Level.INFO,"Wordle created and connected.");
        } else {
            logger.log(Level.WARNING, "Unable to connect");
            System.out.println("Unable to connect");      // logger.info() was not printing to console 
            return;                                         // so it seemed better to do this rather than 
        }                                                   // make a console handler. 
        if (wordleDatabaseConnection.createWordleTables()) {
            logger.log(Level.INFO, "World structure in place");
        } else {
            logger.log(Level.WARNING, "Unable to launch.");
            System.out.println("Unable to create word table :(");
            return;
        }

        // let's add some words to valid 4 letter words from the data.txt file

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (wordleDatabaseConnection.isValidWord(line) == 2) {
                    logger.log(Level.SEVERE, "'"+line+"'" + " is an INVALID word, NOT added to database");
                } else {
                    logger.log(Level.INFO, "-- "+line+" --");
                    wordleDatabaseConnection.addValidWord(i, line);
                }
                i++;
            }

        } catch (IOException e) {
            logger.log(Level.INFO, "Unable to load!", e);
            return;
        }

        // let's get them to enter a word

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {

                System.out.println("You've guessed '" +guess+ "'.");

                if (wordleDatabaseConnection.isValidWord(guess) == 1) { 
                    System.out.println("Success! It is in the the list.");
                } else if (wordleDatabaseConnection.isValidWord(guess) == 0) {
                    System.out.println("Sorry. This word is NOT in the the list.");
                } else if (wordleDatabaseConnection.isValidWord(guess) == 2) {
                    System.out.println("!!! INPUT INVALID !!!");
                    logger.log(Level.INFO, "Invalid word entered: " + guess);
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: " );
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            // e.printStackTrace();
            logger.log(Level.WARNING, "An error occurred while trying to let you guess");
        }

    }
}