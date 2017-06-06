package server;

import com.mysql.fabric.jdbc.FabricMySQLDriver;
import game.Game;
import game.Player;
import server.LoginThread;

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

            firstClientInputStream.readUTF();
            secondClientInputStream.readUTF();
            firstClientOutputStream.writeUTF("start_game");
            secondClientOutputStream.writeUTF("start_game");
            firstClientOutputStream.writeUTF("X");
            secondClientOutputStream.writeUTF("O");
            String line;
            int x, y;
            Player winner;
            Game game = new Game();
            game.start();
            try {
                while (true) {
                    while (true) {
                        line = firstClientInputStream.readUTF();
                        if (line.equals("closed")) {
                            firstClientOutputStream.writeUTF("closed");
                            secondClientOutputStream.writeUTF("closed");
                            throw new java.io.EOFException();
                        }
                        System.out.println("X: " + line);
                        x = Integer.parseInt(line);
                        line = firstClientInputStream.readUTF();
                        System.out.println("X: " + line);
                        y = Integer.parseInt(line);
                        if (game.makeTurn(x, y)) {
                            secondClientOutputStream.writeUTF(((Integer) x).toString());
                            secondClientOutputStream.writeUTF(((Integer) y).toString());
                            break;
                        }
                    }
                    winner = game.checkWinner();
                    if (winner != null) {
                        firstClientOutputStream.writeUTF("winner");
                        firstClientOutputStream.writeUTF(winner.getName());
                        secondClientOutputStream.writeUTF("winner");
                        secondClientOutputStream.writeUTF(winner.getName());
                        System.out.println(winner.getName() + " wins.");
                        game.reset();
                        continue;
                    }
                    if (game.isFieldFilled() && winner == null) {
                        firstClientOutputStream.writeUTF("draw.");
                        secondClientOutputStream.writeUTF("draw");
                        System.out.println("draw");
                        game.reset();
                        continue;
                    }
                    System.out.println("O is going to go");
                    while (true) {
                        line = secondClientInputStream.readUTF();
                        if (line.equals("closed")) {
                            firstClientOutputStream.writeUTF("closed");
                            secondClientOutputStream.writeUTF("closed");
                            throw new java.io.EOFException();
                        }
                        System.out.println("O: " + line);
                        x = Integer.parseInt(line);
                        line = secondClientInputStream.readUTF();
                        System.out.println("O: " + line);
                        y = Integer.parseInt(line);
                        if (game.makeTurn(x, y)) {
                            firstClientOutputStream.writeUTF(((Integer) x).toString());
                            firstClientOutputStream.writeUTF(((Integer) y).toString());
                            break;
                        }
                    }
                    System.out.println("O ended its step");
                    winner = game.checkWinner();
                    if (winner != null) {
                        firstClientOutputStream.writeUTF(winner.getName() + " wins.");
                        secondClientOutputStream.writeUTF(winner.getName() + " wins.");
                        System.out.println(winner.getName() + " wins.");
                        game.reset();
                        continue;
                    }
                    if (game.isFieldFilled() && winner == null) {
                        firstClientOutputStream.writeUTF("draw.");
                        secondClientOutputStream.writeUTF("draw");
                        System.out.println("draw");
                        game.reset();
                        continue;
                    }
                }
            } catch (IOException e) {
                System.out.println("game ended");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                System.out.println("SQL closing error.");
                e.printStackTrace();
            }
        }
    }
}
