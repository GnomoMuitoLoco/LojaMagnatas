package br.com.magnatasoriginal.lojaMagnatas;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection connection;
    private File dbFile;

    public DatabaseManager(File dataFolder) {
        dbFile = new File(dataFolder, "lojas.db");
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Tabela de lojas
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS lojas (" +
                    "playerName TEXT PRIMARY KEY, " +
                    "world TEXT, " +
                    "x REAL, " +
                    "y REAL, " +
                    "z REAL, " +
                    "pitch REAL, " +
                    "yaw REAL, " +
                    "visitCount INTEGER DEFAULT 0)");

            // Tabela de logs
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "playerName TEXT, " +
                    "lojaOwner TEXT, " +
                    "timestamp TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
