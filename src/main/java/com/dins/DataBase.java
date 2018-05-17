package com.dins;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DataBase {
    public static Connection conn;
    public static Statement statement;
    public static ResultSet resSet;

    public static void conn() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:TEST1.s3db");
        System.out.println("Connected to database");
    }

    public static void createDB() throws ClassNotFoundException, SQLException {
        statement = conn.createStatement();
        statement.execute("CREATE TABLE if not exists 'metrics' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'CPU' REAL, 'RAM' REAL, 'DiskSpace' REAL);");

        System.out.println("Table created/already exists");
    }

    public static void writeDB(double CPU, double RAM, double DiskSpace) throws SQLException {
        statement.execute("INSERT INTO 'metrics' ('CPU', 'RAM', 'DiskSpace') VALUES ('" + String.valueOf(CPU) + "','" + String.valueOf(RAM) + "','" +String.valueOf(DiskSpace) + "'); ");
    }

    public static String getLastNRows(int N) throws SQLException {
        resSet = statement.executeQuery("SELECT * FROM metrics LIMIT " +  N + " OFFSET (SELECT COUNT(*) FROM metrics)-" + N + ";");
        StringBuilder result = new StringBuilder();
        while (resSet.next()) {
            result.append("CPU: ").append(resSet.getDouble("CPU")).append("%, ");
            result.append("RAM: ").append(resSet.getDouble("RAM")).append("%, ");
            result.append("Disk: ").append(resSet.getDouble("DiskSpace")).append("%");
            result.append("\n");
        }
        return result.toString();
    }

    public static void closeDB() throws ClassNotFoundException, SQLException {
        conn.close();
        statement.close();
        resSet.close();
        System.out.println("Database closed");
    }

}
