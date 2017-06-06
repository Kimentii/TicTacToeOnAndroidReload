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
        if (field != null) {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    field[i][j].setClickable(true);
                }
            }
        }
    }

    public void shutDownButtons() {
        if (field != null) {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    field[i][j].setClickable(false);
                }
            }
        }
    }

    private void resetGameField() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++)
                field[i][j].setText("");
        }
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

    @Override
    protected Void doInBackground(Void... voids) {
        connect();
        if (!isConnected()) return null;
        boolean repeat = true;
        while (repeat) {
            try {
                String res = inputStream.readUTF();
                if (res.equals("good")) {
                    String log, pas;
                    int w, d, l;
                    log = inputStream.readUTF();
                    pas = inputStream.readUTF();
                    w = Integer.parseInt(inputStream.readUTF());
                    d = Integer.parseInt(inputStream.readUTF());
                    l = Integer.parseInt(inputStream.readUTF());
                    player = new Player(log, pas, w, d, l);
                    isLoggedIn = true;
                    repeat = false;
                } else {
                    isLoggedIn = false;
                }
                semaphore.release();
            } catch (IOException e) {
                e.printStackTrace();
                repeat = false;
            }
        }

        try {
            String message = inputStream.readUTF();
            if (message.equals("start_game")) {
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
                    if (x.equals("closed")) {
                        throw new java.io.EOFException();
                    }
                    if (x.equals("winner")) {
                        publishProgress("toast", inputStream.readUTF() + "wins");
                        publishProgress("reset_game_field");
                        if (playerSymbol.equals("O"))
                            publishProgress("shut_down_buttons");
                        else
                            publishProgress("turn_on_buttons");
                        continue;
                    }
                    if (x.equals("draw")) {
                        publishProgress("toast", "draw");
                        publishProgress("reset_game_field");
                        if (playerSymbol.equals("O"))
                            publishProgress("shut_down_buttons");
                        else
                            publishProgress("turn_on_buttons");
                        continue;
                    }
                    y = inputStream.readUTF();
                    System.out.println("Client:" + x);
                    System.out.println("Client:" + y);
                    publishProgress("step", x, y);
                    turnOnButtons();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                resetGameField();
                break;
            case "shut_down_buttons":
                shutDownButtons();
                break;
            case "turn_on_buttons":
                turnOnButtons();
                break;
        }
    }
}
