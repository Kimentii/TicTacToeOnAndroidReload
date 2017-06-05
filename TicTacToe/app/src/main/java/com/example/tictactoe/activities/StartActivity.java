package com.example.tictactoe.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tictactoe.R;
import com.example.tictactoe.client.Client;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

public class StartActivity extends AppCompatActivity {

    Button signInButton;
    Button signUpButton;
    EditText loginEditText;
    EditText passwordEditText;
    TextView text;
    static Client client;
    Semaphore semaphore;

    public class Listener implements View.OnClickListener {
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.sign_up_button:
                    client.setSemaphore(null);
                    intent = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(intent);
                    break;
                case R.id.sign_in_button:
                    if (client.isConnected()) {
                        client.write("sign in");
                        client.write(loginEditText.getText().toString(), passwordEditText.getText().toString());
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (client.isLoggedIn()) {
                            intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            text.setText("wrong login or password");
                            text.setTextColor(Color.RED);
                        }
                    } else {
                        text.setText("you are not connected");
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new Listener());
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new Listener());

        loginEditText = (EditText) findViewById(R.id.login_sign_in);
        passwordEditText = (EditText) findViewById(R.id.password_sign_in);
        text = (TextView) findViewById(R.id.welcome_text);

        Toast toast = Toast.makeText(StartActivity.this, "", Toast.LENGTH_SHORT);
        semaphore = new Semaphore(1);
        semaphore.tryAcquire();
        client = new Client(toast, semaphore);
        client.execute();
    }
}
