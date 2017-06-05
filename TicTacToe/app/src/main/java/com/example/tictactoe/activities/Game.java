package com.example.tictactoe.activities;

import android.content.Intent;
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

public class Game extends AppCompatActivity {

    static Client client;
    private Button[][] field;

    public class Listener implements View.OnClickListener {
        int x;
        int y;

        Listener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view) {

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
            for (int j = 0; j < field[0].length; j++) {
                field[i][j].setOnClickListener(new Listener(i, j));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        buildGameField();

        client = MainMenuActivity.client;
    }
}
