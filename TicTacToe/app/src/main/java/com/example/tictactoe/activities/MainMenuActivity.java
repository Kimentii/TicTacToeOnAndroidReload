package com.example.tictactoe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tictactoe.R;
import com.example.tictactoe.client.Client;

public class MainMenuActivity extends AppCompatActivity {

    static Client client;
    Button playButton;
    Button statusButton;
    Button exitButton;

    public class Listener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.play_button:
                    client.setSemaphore(null);
                    intent = new Intent(getApplicationContext(), GameActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.status_button:
                    client.setSemaphore(null);
                    intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.exit_button:
                    if (client.isConnected()) {
                        client.write("save_information");
                        client.write(client.getPlayer().getLogin());
                        client.write(String.valueOf(client.getPlayer().getWins()));
                        client.write(String.valueOf(client.getPlayer().getDraws()));
                        client.write(String.valueOf(client.getPlayer().getLosses()));
                    }
                    client.write("exit");
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        client = StartActivity.client;

        playButton = (Button) findViewById(R.id.play_button);
        statusButton = (Button) findViewById(R.id.status_button);
        exitButton = (Button) findViewById(R.id.exit_button);

        playButton.setOnClickListener(new Listener());
        statusButton.setOnClickListener(new Listener());
        exitButton.setOnClickListener(new Listener());

    }
}
