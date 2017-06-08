package com.example.tictactoe.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.tictactoe.R;
import com.example.tictactoe.client.Client;

import org.w3c.dom.Text;

public class GameActivity extends AppCompatActivity {

    Button mainMenuButton;
    static Client client;
    TextView text;
    private Button[][] field;

    public class MainMenuButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            client.write("end_game");
            client.setGameText(null);
            client.setField(null);
            client.setMainMenuButton(null);
            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public class Listener implements View.OnClickListener {
        int x;
        int y;

        Listener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view) {
            if (client.isGameStarted()) {
                if (field[x][y].length() == 0) {
                    shutDownButtons();
                    mainMenuButton.setClickable(false);
                    field[x][y].setText(client.getPlayerSymbol());
                    text.setText("wait");
                    client.write(((Integer) x).toString(), ((Integer) y).toString());
                }
            } else {
                text.setText("wait other player");
                text.setTextColor(Color.RED);
            }
        }
    }

    public void shutDownButtons() {
        synchronized (field) {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    field[i][j].setClickable(false);
                }
            }
        }
    }

    private void buildGameField() {
        field = new Button[3][3];
        field[0][0] = (Button) findViewById(R.id.button1);
        field[0][1] = (Button) findViewById(R.id.button2);
        field[0][2] = (Button) findViewById(R.id.button3);
        field[1][0] = (Button) findViewById(R.id.button4);
        field[1][1] = (Button) findViewById(R.id.button5);
        field[1][2] = (Button) findViewById(R.id.button6);
        field[2][0] = (Button) findViewById(R.id.button7);
        field[2][1] = (Button) findViewById(R.id.button8);
        field[2][2] = (Button) findViewById(R.id.button9);
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                field[i][j].setOnClickListener(new Listener(i, j));
            }
        }
        mainMenuButton.setClickable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        client = StartActivity.client;

        text = (TextView) findViewById(R.id.game_text);
        mainMenuButton = (Button) findViewById(R.id.button_to_main_menu);
        mainMenuButton.setOnClickListener(new MainMenuButtonListener());
        buildGameField();
        shutDownButtons();
        client.setField(field);
        client.setMainMenuButton(mainMenuButton);
        client.setGameText(text);

        client.write("want_play");
    }
}
