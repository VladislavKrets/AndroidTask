package com.test.testapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText usernameEditText, passwordEditText;
    private Button enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        enterButton = findViewById(R.id.enterButton);
        enterButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        new EnterTask().execute();
    }

    private class EnterTask extends AsyncTask<Void, Void, Void> {

        private String username;
        private String password;
        private boolean isEmpty;
        private String token;
        @Override
        protected void onPreExecute() {
            username = usernameEditText.getText().toString();
            password = passwordEditText.getText().toString();
            isEmpty = username == null || password == null || username.isEmpty() || password.isEmpty();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isEmpty) {
                try {
                    String urlParameters = String.format("username=%s&password=&s", username, password);
                    byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://somethingurl.com").openConnection();
                    //http://somethingurl.com is not valid url
                    //you can change it to host of a real server
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setRequestProperty("charset", "utf-8");
                    urlConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                    urlConnection.setUseCaches(false);
                    //setting properties for post method
                    DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

                    dataOutputStream.write(postData);

                    dataOutputStream.close(); //closing resources

                    //get request is not compatible for sending such parameters as username and password
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) builder.append(line); //getting request
                    reader.close();
                    //json parsing
                    jsonParse(builder);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void jsonParse(StringBuilder builder) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(builder.toString(), JsonObject.class);
            try {
                token = jsonObject.get("token").getAsString();
            } catch (Exception e) {
                token = null;
                //if exception was thrown during parsing token it means data was incorrect
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //for testing
            //isEmpty = false;
            //token = "sometoken";

            if (isEmpty || token == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(getResources().getString(R.string.emptyFields));
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss(); //closing dialog if ok clicked
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                //showing alert with warning of unfilled fields
            }
            else {
                Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                intent.putExtra("token", token); //sending token to main page activity
                startActivity(intent);
                finishActivity(0); //after login this activity is not necessary
            }
        }

    }

}
