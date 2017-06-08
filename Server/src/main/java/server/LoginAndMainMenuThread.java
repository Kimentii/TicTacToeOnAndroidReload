package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginAndMainMenuThread extends Thread {
    DataInputStream inputStream;
    DataOutputStream outputStream;
    Connection connection;
    int clientNumber;
    private static final String ADD_PLAYER = "insert into players (login,password,wins,draws,losses) values(?,?,?,?,?)";
    private static final String DELETE_PLAYER = "delete from players where login = ?";
    private static final String GET_PLAYER_INFO = "select login, password, wins, draws, losses from players where login = ?";
    private static final String UPDATE_PLAYER = "update players set wins = ?, draws = ?, losses = ? where login = ?";

    LoginAndMainMenuThread(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Connection connection, int clientNumber) {
        this.inputStream = dataInputStream;
        this.outputStream = dataOutputStream;
        this.connection = connection;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            PreparedStatement preparedStatement = null;
            while (true) {
                String choice = inputStream.readUTF();
                System.out.println(choice);
                if (choice.equals("want_play")) break;
                if(choice.equals("end_game")) continue;
                if (choice.equals("sign_in")) {
                    String login = inputStream.readUTF();
                    String password = inputStream.readUTF();
                    preparedStatement = connection.prepareStatement(GET_PLAYER_INFO);
                    preparedStatement.setString(1, login);
                    ResultSet res = preparedStatement.executeQuery();
                    outputStream.writeUTF("authorization");
                    if (res.next() && res.getString("password").equals(password)) {
                        outputStream.writeUTF("good");
                        outputStream.writeUTF(res.getString("login"));
                        outputStream.writeUTF(res.getString("password"));
                        outputStream.writeUTF(((Integer) res.getInt("wins")).toString());
                        outputStream.writeUTF(((Integer) res.getInt("draws")).toString());
                        outputStream.writeUTF(((Integer) res.getInt("losses")).toString());
                    } else {
                        outputStream.writeUTF("bad");
                    }
                } else if (choice.equals("sign_up")) {
                    String login = inputStream.readUTF();
                    String password = inputStream.readUTF();
                    preparedStatement = connection.prepareStatement(GET_PLAYER_INFO);
                    preparedStatement.setString(1, login);
                    ResultSet res = preparedStatement.executeQuery();
                    outputStream.writeUTF("authorization");
                    if (!res.next()) {
                        outputStream.writeUTF("good");
                        preparedStatement = connection.prepareStatement(ADD_PLAYER);
                        preparedStatement.setString(1, login);
                        preparedStatement.setString(2, password);
                        preparedStatement.setInt(3, 0);
                        preparedStatement.setInt(4, 0);
                        preparedStatement.setInt(5, 0);
                        preparedStatement.execute();
                        outputStream.writeUTF(login);
                        outputStream.writeUTF(password);
                        outputStream.writeUTF("0");
                        outputStream.writeUTF("0");
                        outputStream.writeUTF("0");
                    } else {
                        outputStream.writeUTF("bad");
                    }
                } else if (choice.equals("delete_player")) {
                    String login = inputStream.readUTF();
                    preparedStatement = connection.prepareStatement(GET_PLAYER_INFO);
                    preparedStatement.setString(1, login);
                    ResultSet res = preparedStatement.executeQuery();
                    outputStream.writeUTF("deleting_player");
                    if (res.next()) {
                        preparedStatement = connection.prepareStatement(DELETE_PLAYER);
                        preparedStatement.setString(1, login);
                        preparedStatement.executeUpdate();
                        outputStream.writeUTF("good");
                    } else {
                        outputStream.writeUTF("bad");
                    }
                } else if (choice.equals("save_information")) {
                    String login = inputStream.readUTF();
                    int wins = Integer.parseInt(inputStream.readUTF());
                    int draws = Integer.parseInt(inputStream.readUTF());
                    int losses = Integer.parseInt(inputStream.readUTF());
                    preparedStatement = connection.prepareStatement(UPDATE_PLAYER);
                    preparedStatement.setInt(1, wins);
                    preparedStatement.setInt(2, draws);
                    preparedStatement.setInt(3, losses);
                    preparedStatement.setString(4, login);
                    preparedStatement.executeUpdate();
                } else if (choice.equals("exit")) {
                    outputStream.writeUTF("exit");
                    if (clientNumber == 1)
                        Main.didFirstClientExit = true;
                    else
                        Main.didSecondClientExit = true;
                    break;
                }
                if (preparedStatement != null) preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
