package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginThread extends Thread {

    DataInputStream inputStream;
    DataOutputStream outputStream;
    Connection connection;
    private static final String ADD_PLAYER = "insert into players (login,password,wins,draws,losses) values(?,?,?,?,?)";
    private static final String GET_PLAYER_INFO = "select login, password, wins, draws, losses from players where login = ?";

    LoginThread(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Connection connection) {
        this.inputStream = dataInputStream;
        this.outputStream = dataOutputStream;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            PreparedStatement preparedStatement = null;
            while (true) {
                String choice = inputStream.readUTF();
                System.out.println(choice);
                String login = inputStream.readUTF();
                String password = inputStream.readUTF();
                preparedStatement = connection.prepareStatement(GET_PLAYER_INFO);
                preparedStatement.setString(1, login);
                ResultSet res = preparedStatement.executeQuery();
                if (choice.equals("sign in")) {
                    if (res.next() && res.getString("password").equals(password)) {
                        outputStream.writeUTF("good");
                        outputStream.writeUTF(res.getString("login"));
                        outputStream.writeUTF(res.getString("password"));
                        outputStream.writeUTF(((Integer) res.getInt("wins")).toString());
                        outputStream.writeUTF(((Integer) res.getInt("draws")).toString());
                        outputStream.writeUTF(((Integer) res.getInt("losses")).toString());
                        break;
                    } else {
                        outputStream.writeUTF("bad");
                    }
                } else {
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
                        break;
                    } else {
                        outputStream.writeUTF("bad");
                    }
                }
                if (preparedStatement != null) preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
