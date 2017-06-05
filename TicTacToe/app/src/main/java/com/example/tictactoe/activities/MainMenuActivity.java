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
            switch (view.getId()) {
                case R.id.play_button:
                    client.setSemaphore(null);
                    Intent intent = new Intent(getApplicationContext(), Game.class);
                    startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        playButton = (Button) findViewById(R.id.play_button);
        statusButton = (Button) findViewById(R.id.status_button);
        exitButton = (Button) findViewById(R.id.exit_button);

        client = StartActivity.client;
    }
}
