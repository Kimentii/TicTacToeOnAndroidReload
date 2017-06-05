import com.mysql.fabric.jdbc.FabricMySQLDriver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/game_info";
    private static final String USSERNAME = "root";
    private static final String PASSWORD = "12345";

    private static final String ADD_PLAYER = "insert into players (login,password,wins,draws,losses) values(?,?,?,?,?)";
    private static final String GET_INFO = "select login, password, wins, draws, losses from players";
    private static final String GET_PLAYER_INFO = "select login, password, wins, draws, losses from players where login = ?";

    private static final int PORT = 6666;
    private static ServerSocket serverSocket;
    private static Socket firstClientSocket;
    private static DataInputStream firstClientInputStream;
    private static DataOutputStream firstClientOutputStream;
    private static Socket secondClientSocket;
    private static DataInputStream secondClientInputStream;
    private static DataOutputStream secondClientOutputStream;

    public static void firstClientAuthorization() {
        try {
            System.out.println("Waiting for a client...");
            firstClientSocket = serverSocket.accept();
            System.out.println("Got first client.");
            firstClientInputStream = new DataInputStream(firstClientSocket.getInputStream());
            firstClientOutputStream = new DataOutputStream(firstClientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void secondClientAuthorization() {
        try {
            System.out.println("Waiting for a client...");
            secondClientSocket = serverSocket.accept();
            System.out.println("Got second client.");
            secondClientInputStream = new DataInputStream(secondClientSocket.getInputStream());
            secondClientOutputStream = new DataOutputStream(secondClientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            Driver driver = new FabricMySQLDriver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USSERNAME, PASSWORD);
            if (connection.isClosed()) {
                System.out.println("Error with SQL connection.");
                return;
            } else {
                System.out.println("SQL connected.");
            }
            serverSocket = new ServerSocket(PORT);
            firstClientAuthorization();
            LoginThread firstClientLoginThread = new LoginThread(firstClientInputStream, firstClientOutputStream, connection);
            firstClientLoginThread.start();

            secondClientAuthorization();
            LoginThread secondClientLoginThread = new LoginThread(secondClientInputStream, secondClientOutputStream, connection);
            secondClientLoginThread.start();

            firstClientLoginThread.join();
            secondClientLoginThread.join();
            System.out.println("All users logged in");



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                System.out.println("SQL closing error.");
                e.printStackTrace();
            }
        }
    }
}
