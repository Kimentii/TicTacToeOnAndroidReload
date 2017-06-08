package server;

import com.mysql.fabric.jdbc.FabricMySQLDriver;
import game.Game;
import game.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/game_info";
    private static final String USERNAME = "root";
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

    public static boolean didFirstClientExit;
    public static boolean didSecondClientExit;

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

    public static boolean analyzeTheWinner(Game game) {
        try {
            Player winner = game.checkWinner();
            if (winner != null) {
                firstClientOutputStream.writeUTF("winner");
                firstClientOutputStream.writeUTF(winner.getName());
                secondClientOutputStream.writeUTF("winner");
                secondClientOutputStream.writeUTF(winner.getName());
                System.out.println(winner.getName() + " wins.");
                game.reset();
                return true;
            }
            if (game.isFieldFilled() && winner == null) {
                firstClientOutputStream.writeUTF("draw");
                secondClientOutputStream.writeUTF("draw");
                System.out.println("draw");
                game.reset();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        Connection connection = null;
        try {
            Driver driver = new FabricMySQLDriver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (connection.isClosed()) {
                System.out.println("Error with SQL connection.");
                return;
            } else {
                System.out.println("SQL connected.");
            }
            serverSocket = new ServerSocket(PORT);
            firstClientAuthorization();
            LoginAndMainMenuThread firstClientLoginAndMainMenuThread =
                    new LoginAndMainMenuThread(firstClientInputStream, firstClientOutputStream, connection, 1);
            firstClientLoginAndMainMenuThread.start();

            secondClientAuthorization();
            LoginAndMainMenuThread secondClientLoginAndMainMenuThread =
                    new LoginAndMainMenuThread(secondClientInputStream, secondClientOutputStream, connection, 2);
            secondClientLoginAndMainMenuThread.start();

            do {
                firstClientLoginAndMainMenuThread.join();
                if (didFirstClientExit) {
                    didFirstClientExit = false;
                    firstClientAuthorization();
                    firstClientLoginAndMainMenuThread =
                            new LoginAndMainMenuThread(firstClientInputStream, firstClientOutputStream,
                                    connection, 1);
                    firstClientLoginAndMainMenuThread.start();
                    continue;
                }
                secondClientLoginAndMainMenuThread.join();
                if (didSecondClientExit) {
                    didSecondClientExit = false;
                    secondClientAuthorization();
                    secondClientLoginAndMainMenuThread =
                            new LoginAndMainMenuThread(secondClientInputStream, secondClientOutputStream,
                                    connection, 2);
                    secondClientLoginAndMainMenuThread.start();
                    continue;
                }
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
                    game:
                    while (true) {
                        while (true) {
                            line = firstClientInputStream.readUTF();
                            if (line.equals("end_game")) {
                                firstClientLoginAndMainMenuThread =
                                        new LoginAndMainMenuThread(firstClientInputStream, firstClientOutputStream, connection, 1);
                                firstClientLoginAndMainMenuThread.start();
                                firstClientOutputStream.writeUTF("end_game");
                                secondClientOutputStream.writeUTF("end_game");
                                secondClientLoginAndMainMenuThread =
                                        new LoginAndMainMenuThread(secondClientInputStream, secondClientOutputStream, connection, 2);
                                secondClientLoginAndMainMenuThread.start();
                                break game;
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
                        if (analyzeTheWinner(game))
                            continue;
                        System.out.println("O is going to go");
                        while (true) {
                            line = secondClientInputStream.readUTF();
                            if (line.equals("end_game")) {
                                secondClientLoginAndMainMenuThread =
                                        new LoginAndMainMenuThread(secondClientInputStream, secondClientOutputStream, connection, 2);
                                secondClientLoginAndMainMenuThread.start();
                                firstClientOutputStream.writeUTF("end_game");
                                secondClientOutputStream.writeUTF("end_game");
                                firstClientLoginAndMainMenuThread =
                                        new LoginAndMainMenuThread(firstClientInputStream, firstClientOutputStream, connection, 1);
                                firstClientLoginAndMainMenuThread.start();
                                break game;
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
                        if (analyzeTheWinner(game))
                            continue;
                    }
                } catch (IOException e) {
                    System.out.println("game ended");
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("SQL closing error.");
                e.printStackTrace();
            }
        }
    }
}
