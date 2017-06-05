package com.example.tictactoe.client;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
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

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case "toast":
                if (toast != null) {
                    toast.setText(values[1]);
                    toast.show();
                }
        }
    }
}
