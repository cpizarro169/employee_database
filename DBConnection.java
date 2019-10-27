package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.sqlite.SQLiteDataSource;

public class DBConnection {

    private static Connection conn;

    public static Connection get() throws SQLException, FileNotFoundException {
        if (conn == null) {
            // Create database Employees if not exists.
            SQLiteDataSource DS = new SQLiteDataSource();
            DS.setUrl("jdbc:sqlite:Employees.db");
            conn = DS.getConnection();
            try (Statement stmt = conn.createStatement()) {

                // Read file employees.sql to create tables and insert initial data.
                try (Scanner sc = new Scanner(new File("C:/Users/cpiza/IdeaProjects/Assignment4/src/employees.sql"))) { //source is whereever employee sql code is in
                    sc.useDelimiter(";");
                    while (sc.hasNext()) {
                        try {
                            String line = sc.next();
                            stmt.execute(line);
                        } catch (SQLException exc) {

                        }
                    }
                }
                System.out.println("Database created.");
            }
        }
        return conn;
    }

}
