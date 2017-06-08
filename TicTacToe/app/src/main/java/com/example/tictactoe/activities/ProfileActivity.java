package com.example.tictactoe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tictactoe.R;
import com.example.tictactoe.client.Client;

public class ProfileActivity extends AppCompatActivity {

    Button deleteProfileButton;
    Button mainMenuButton;
    TextView winsText;
    TextView drawsText;
    TextView lossesText;
    static Client client;

    public class Listener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.button_delete_profile:
                    client.write("delete_player");
                    client.write(client.getPlayer().getLogin());
                    intent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.button_main_menu_profile_activity:
                    intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        client = StartActivity.client;

        winsText = (TextView) findViewById(R.id.text_wins);
        drawsText = (TextView) findViewById(R.id.text_draws);
        lossesText = (TextView) findViewById(R.id.text_losses);
        winsText.setText(String.valueOf(client.getPlayer().getWins()));
        drawsText.setText(String.valueOf(client.getPlayer().getDraws()));
        lossesText.setText(String.valueOf(client.getPlayer().getLosses()));

        deleteProfileButton = (Button) findViewById(R.id.button_delete_profile);
        mainMenuButton = (Button) findViewById(R.id.button_main_menu_profile_activity);
        Listener listener = new Listener();
        deleteProfileButton.setOnClickListener(listener);
        mainMenuButton.setOnClickListener(listener);
    }
}
