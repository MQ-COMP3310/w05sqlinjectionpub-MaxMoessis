package workshop05code;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//Import for logging exercise
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

// imported for input checking
import java.util.regex.*;

public class SQLiteConnectionManager {
  // Start code logging exercise
  static {
    // must set before the Logger
    // loads logging.properties from the classpath
    try {// resources\logging.properties
      LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
    } catch (SecurityException | IOException e1) {
      // e1.printStackTrace(); 
      System.err.println("An error occurred while trying to manage the logger");
    }
  }

  private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());
  // End code logging exercise

  private String databaseURL = "";

  private static final String WORDLE_DROP_TABLE_STRING = "DROP TABLE IF EXISTS wordlist;";
  private static final String WORDLE_CREATE_STRING = "CREATE TABLE wordlist (\n"
      + " id integer PRIMARY KEY,\n"
      + " word text NOT NULL\n"
      + ");";

  private static final String VALID_WORDS_DROP_TABLE_STRING = "DROP TABLE IF EXISTS validWords;";
  private static final String VALID_WORDS_CREATE_STRING = "CREATE TABLE validWords (\n"
      + " id integer PRIMARY KEY,\n"
      + " word text NOT NULL\n"
      + ");";

  /**
   * Set the database file name in the sqlite project to use
   *
   * @param fileName the database file name
   */
  public SQLiteConnectionManager(String filename) {
    databaseURL = "jdbc:sqlite:sqlite/" + filename;

  }

  /**
   * Connect to a sample database
   *
   * @param fileName the database file name
   */
  public void createNewDatabase(String fileName) {

    try (Connection conn = DriverManager.getConnection(databaseURL)) {
      if (conn != null) {
        DatabaseMetaData meta = conn.getMetaData();
        logger.log(Level.INFO, "The driver name is " +meta.getDriverName());
        logger.log(Level.INFO, "A new database has been created.");
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Connection error", e);
    }
  }

  /**
   * Check that the file has been cr3eated
   *
   * @return true if the file exists in the correct location, false otherwise. If
   *         no url defined, also false.
   */
  public boolean checkIfConnectionDefined() {
    if (databaseURL.equals("")) {
      return false;
    } else {
      try (Connection conn = DriverManager.getConnection(databaseURL)) {
        if (conn != null) {
          return true;
        }
      } catch (SQLException e) {
        logger.log(Level.WARNING, "Connection error", e);
        return false;
      }
    }
    return false;
  }

  /**
   * Create the table structures (2 tables, wordle words and valid words)
   *
   * @return true if the table structures have been created.
   */
  public boolean createWordleTables() {
    if (databaseURL.equals("")) {
      return false;
    } else {
      try (Connection conn = DriverManager.getConnection(databaseURL);
          Statement stmt = conn.createStatement()) {
        stmt.execute(WORDLE_DROP_TABLE_STRING);
        stmt.execute(WORDLE_CREATE_STRING);
        stmt.execute(VALID_WORDS_DROP_TABLE_STRING);
        stmt.execute(VALID_WORDS_CREATE_STRING);
        return true;

      } catch (SQLException e) {
        logger.log(Level.WARNING, "Connection error", e);
        return false;
      }
    }
  }

  /**
   * Take an id and a word and store the pair in the valid words
   * 
   * @param id   the unique id for the word
   * @param word the word to store
   */
  public void addValidWord(int id, String word) {

    String sql = "INSERT INTO validWords(id,word) VALUES(?,?)";

    try (Connection conn = DriverManager.getConnection(databaseURL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.setString(2, word);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Connection error", e);
    }

  }

  /**
   * Possible weakness here?
   * 
   * @param guess the string to check if it is a valid word.
   * @return 2 if word isn't valid
   * @return 1 if the word is valid and matches
   * @return 0 if the word is valid but doesn't match.
   */
  public int isValidWord(String guess) {
    // String sql = "SELECT count(id) as total FROM validWords WHERE word like '" + guess + "';";   // weak 
    String sql = "SELECT count(id) as total FROM validWords WHERE word like ?";   // secure

    try (Connection conn = DriverManager.getConnection(databaseURL);
        PreparedStatement stmt = conn.prepareStatement(sql)) {

        if (!guess.matches("^[a-z]{4}$")) {
            return 2;
        }

      stmt.setString(1, guess);   // security issue fix
      ResultSet resultRows = stmt.executeQuery();
      if (resultRows.next()) {
        int result = resultRows.getInt("total");

        if (result >= 1) {
          return 1;
        } else {
          return 0;
        }

      }

      return 0;

    } catch (SQLException e) {
      logger.log(Level.WARNING, "Connection error", e);
      return 0;
    }
  }
}
