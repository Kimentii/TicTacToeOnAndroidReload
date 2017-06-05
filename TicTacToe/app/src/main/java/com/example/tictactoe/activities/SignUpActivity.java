package com.example.tictactoe.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tictactoe.R;
import com.example.tictactoe.client.Client;

import java.util.concurrent.Semaphore;

public class SignUpActivity extends AppCompatActivity {

    EditText loginEditText;
    EditText passwordEditText;
    EditText repeatPasswordEditText;
    TextView text;
    Button signUpButton;
    static Client client;
    Semaphore semaphore;

    public class Listener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (client.isConnected()) {
                if (loginEditText.getText().length() > 0) {
                    if (passwordEditText.getText().length() > 0) {
                        if(passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
                            client.write("sign up");
                            client.write(loginEditText.getText().toString(), passwordEditText.getText().toString());
                            try {
                                semaphore.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (client.isLoggedIn()) {
                                client.setSemaphore(null);
                                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                text.setText("login is already used");
                                text.setTextColor(Color.RED);
                            }
                        }
                        else
                            text.setText("password mismatch");
                    } else
                        text.setText("enter password");
                } else
                    text.setText("enter login");
            } else
                text.setText("you are not connected.");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpButton = (Button) findViewById(R.id.sign_up_button2);
        signUpButton.setOnClickListener(new Listener());

        loginEditText = (EditText) findViewById(R.id.login_sign_up);
        passwordEditText = (EditText) findViewById(R.id.password_sing_up);
        repeatPasswordEditText = (EditText) findViewById(R.id.repeat_password_sing_up);
        text = (TextView) findViewById(R.id.sign_up_text);

        client = StartActivity.client;
        semaphore = new Semaphore(1);
        semaphore.tryAcquire();
        client.setSemaphore(semaphore);
    }
}
