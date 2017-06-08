package com.example.tictactoe.client;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public class Client extends AsyncTask<Void, String, Void> {

    private final String IP = "10.0.2.2";
    private final int PORT = 6666;
    Socket clientSocket;
    Toast toast;
    Semaphore semaphore;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    private boolean isConnected;
    private boolean isLoggedIn;
    private boolean isGameStarted;
    String playerSymbol;
    String opponentSymbol;
    Button[][] field;
    Button mainMenuButton;
    TextView gameText;
    Player player;

    public Client(Toast t, Semaphore s) {
        clientSocket = new Socket();
        semaphore = s;
        toast = t;
    }

    public void setToast(Toast t) {
        toast = t;
    }

    public void setSemaphore(Semaphore s) {
        semaphore = s;
    }

    public void setField(Button[][] field) {
        this.field = field;
    }

    public void setMainMenuButton(Button mainMenuButton) {
        this.mainMenuButton = mainMenuButton;
    }

    public void setGameText(TextView gameText) {
        this.gameText = gameText;
    }

    public String getPlayerSymbol() {
        return playerSymbol;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public Player getPlayer() {
        return player;
    }

    public void write(String mes) {
        try {
            if (isConnected()) {
                outputStream.writeUTF(mes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String log, String pas) {
        try {
            if (isConnected()) {
                outputStream.writeUTF(log);
                outputStream.writeUTF(pas);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnOnButtons() {
        publishProgress("turn_on_buttons");
    }

    public void turnOnMainMenuButton() {
        publishProgress("turn_on_main_menu_button");
    }

    public void shutDownButtons() {
        publishProgress("shut_down_buttons");
    }

    private void resetGameField() {
        publishProgress("reset_game_field");
    }

    private void connect() {
        try {
            InetAddress ipAddress = InetAddress.getByName(IP); // создаем объект который отображает вышеописанный IP-адрес.
            System.out.println("Trying connect to server.");
            clientSocket.connect(new InetSocketAddress(IP, PORT), 3000);
            System.out.println("Connected to server.");
            onProgressUpdate("toast", "Connected.");
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            toast.setText("Connected.");
            isConnected = true;
        } catch (Exception e) {
            onProgressUpdate("toast", "Can't connect.");
        }
    }

    public void closeSocket() {
        if (clientSocket != null && clientSocket.isConnected()) {
            try {
                clientSocket.shutdownInput();
                clientSocket.shutdownOutput();
                clientSocket.close();
                System.out.println("client is closed");
            } catch (java.net.SocketException e) {
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        connect();
        if (!isConnected()) return null;
        while (true) {
            try {
                String message = inputStream.readUTF();
                System.out.println("mes:" + message);
                if (message.equals("exit")) break;
                if (message.equals("authorization")) {
                    String ans = inputStream.readUTF();
                    System.out.println(ans);
                    if (ans.equals("good")) {
                        String log, pas;
                        int w, d, l;
                        log = inputStream.readUTF();
                        pas = inputStream.readUTF();
                        w = Integer.parseInt(inputStream.readUTF());
                        d = Integer.parseInt(inputStream.readUTF());
                        l = Integer.parseInt(inputStream.readUTF());
                        player = new Player(log, pas, w, d, l);
                        isLoggedIn = true;
                    } else {
                        isLoggedIn = false;
                    }
                    semaphore.release();
                } else if (message.equals("deleting_player")) {
                    if (!inputStream.readUTF().toString().equals("good")) {
                        if (toast != null) {
                            toast.setText("can't delete");
                            toast.show();
                        }
                    }
                } else if (message.equals("start_game")) {
                    isGameStarted = true;
                    playerSymbol = inputStream.readUTF();
                    if (playerSymbol.equals("X")) {
                        publishProgress("toast", "you are X");
                        publishProgress("text", "your turn");
                        opponentSymbol = "O";
                        turnOnButtons();
                    } else {
                        publishProgress("toast", "you are O");
                        publishProgress("text", "wait");
                        opponentSymbol = "X";
                    }
                    System.out.println("I'm " + playerSymbol);
                    System.out.println("My opponent is " + opponentSymbol);
                    String x, y;
                    while (true) {
                        x = inputStream.readUTF();
                        if (x.equals("end_game")) {
                            publishProgress("text", "opponent came out");
                            publishProgress("turn_on_main_menu_button");
                            isGameStarted = false;
                            break;
                        }
                        if (x.equals("winner")) {
                            String winner = inputStream.readUTF();
                            publishProgress("toast", winner + " wins");
                            resetGameField();
                            if (playerSymbol.equals(winner))
                                player.incrementWins();
                            else
                                player.incrementLosses();
                            if (playerSymbol.equals("O"))
                                shutDownButtons();
                            else
                                turnOnButtons();
                            continue;
                        }
                        if (x.equals("draw")) {
                            System.out.println("Draw");
                            publishProgress("toast", "draw");
                            resetGameField();
                            player.incrementDraws();
                            if (playerSymbol.equals("O"))
                                shutDownButtons();
                            else
                                turnOnButtons();
                            continue;
                        }
                        y = inputStream.readUTF();
                        System.out.println("Client:" + x);
                        System.out.println("Client:" + y);
                        publishProgress("step", x, y);
                        publishProgress("text", "your turn");
                        turnOnButtons();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        isConnected = false;
        closeSocket();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... strings) {
        super.onProgressUpdate(strings);
        switch (strings[0]) {
            case "toast":
                if (toast != null) {
                    toast.setText(strings[1]);
                    toast.show();
                }
                break;
            case "step":
                int x, y;
                x = Integer.parseInt(strings[1]);
                y = Integer.parseInt(strings[2]);
                field[x][y].setText(opponentSymbol);
                break;
            case "text":
                if (gameText != null) gameText.setText(strings[1]);
                break;
            case "reset_game_field":
                for (int i = 0; i < field.length; i++) {
                    for (int j = 0; j < field[i].length; j++)
                        field[i][j].setText("");
                }
                break;
            case "shut_down_buttons":
                if (field != null) {
                    for (int i = 0; i < field.length; i++) {
                        for (int j = 0; j < field[i].length; j++) {
                            field[i][j].setClickable(false);
                        }
                    }
                }
                if (mainMenuButton != null) mainMenuButton.setClickable(false);
                break;
            case "turn_on_buttons":
                if (field != null) {
                    for (int i = 0; i < field.length; i++) {
                        for (int j = 0; j < field[i].length; j++) {
                            field[i][j].setClickable(true);
                        }
                    }
                }
                if (mainMenuButton != null) mainMenuButton.setClickable(true);
                break;
            case "turn_on_main_menu_button":
                if (mainMenuButton != null) mainMenuButton.setClickable(true);
                break;
        }
    }
}
