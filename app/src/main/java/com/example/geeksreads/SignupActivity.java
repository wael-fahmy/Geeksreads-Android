package com.example.geeksreads;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SignupActivity extends AppCompatActivity {
    /**
     * Global Public Static Variables used for Testing
     */
    public static String sForTest;
    /**
     * Global Private Variable to Store Sign up Username from Text boxes
     */
    private String fullNameStr;
    /**
     * Global Private Variable to Store Sign up Email Address from Text boxes
     */
    private String emailStr;
    /**
     * Global Private Variable to Store Sign up Password from Text boxes
     */
    private String passwordStr;
    /**
     * Global Variables to Store Context of this Activity itself
     */
    private Context mContext;

    /**
     * Function for Starting Logic Actions after Creating the Layout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button loginLink = findViewById(R.id.OrLoginLinkBtn);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(myIntent);
            }
        });

        mContext = this;

        /* Getting Text boxes and Buttons from the layout */
        final EditText fullName = findViewById(R.id.UserNameTxt);
        final EditText email = findViewById((R.id.EmailTxt));
        final EditText password = findViewById(R.id.PasswordTxt);
        final EditText confPassword = findViewById(R.id.ConfirmPasswordTxt);

        Button signUpButton = findViewById(R.id.SignupBtn);
        /* Function Handler for Clicking on Sign up Button, to Start Checking input Fields
           and Sending JSON String to the Backend Sign up API
         */
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNameStr = fullName.getText().toString();
                emailStr = email.getText().toString();
                passwordStr = password.getText().toString();

                /* If the user entered an invalid Username */
                if (fullNameStr.length() < 3 || fullNameStr.length() > 50) {
                    fullName.setError("Username length should be 3 characters minimum and 50 characters maximum");
                    sForTest = "Username length should be 3 characters minimum and 50 characters maximum";
                }
                /* If the user entered an invalid Email Address */
                else if (!emailStr.matches(".+[@].+[.].+")) {
                    email.setError("Please enter a valid Email");
                    sForTest = "Please enter a valid Email";
                }
                /* If the user entered an invalid Password */
                else if (passwordStr.length() < 6) {
                    password.setError("Password should be 6 characters or more");
                    password.setText("");
                    confPassword.setText("");
                    sForTest = "Password should be 6 characters or more";
                } else if (!passwordStr.matches(".*[0-9].*")) {
                    password.setError("Password should contain numbers");
                    password.setText("");
                    confPassword.setText("");
                    sForTest = "Password should contain numbers";
                } else if (!passwordStr.matches(".*[a-z].*")) {
                    password.setError("Password should contain lower case letters");
                    password.setText("");
                    confPassword.setText("");
                    sForTest = "Password should contain lower case letters";
                } else if (!passwordStr.matches(".*[A-Z].*")) {
                    password.setError("Password should contain upper case letters");
                    password.setText("");
                    confPassword.setText("");
                    sForTest = "Password should contain upper case letters";
                } else if (!passwordStr.equals(confPassword.getText().toString())) {
                    confPassword.setError("Passwords don't match");
                    password.setText("");
                    confPassword.setText("");
                    sForTest = "Passwords don't match";
                }
                /* If the user entered a valid username, email and password */
                else {
                    JSONObject JSON = new JSONObject();
                    try {
                        JSON.put("UserName", fullNameStr);
                        JSON.put("UserEmail", emailStr);
                        JSON.put("UserPassword", passwordStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /* URL For Sign up API */
                    String urlService = "http://geeksreads.000webhostapp.com/Morsy/Signup.php";

                    /* Creating a new instance of Sign up Class */
                    signUp signUp = new signUp();
                    signUp.execute(urlService, JSON.toString());
                }

            }
        });

    }

    /**
     * Class that get the data from host and Add it to its views.
     * The Parameters are host Url and toSend Data.
     */
    public class signUp extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "GET";

        @Override
        protected void onPreExecute() {
            /* Do Nothing */
        }

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";

            try {
                /* Create a URL object holding our url */
                URL url = new URL(UrlString);
                /* Create an HTTP Connection and adjust its options */
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("Json", "UTF-8") + "=" + URLEncoder.encode(JSONString, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                ops.close();

                /* A Stream object to get the returned data from API Call */
                InputStream ips = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                reader.close();
                ips.close();
                http.disconnect();
                return result;
            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                /* Creating a JSON Object to parse the data in */
                final JSONObject jsonObject = new JSONObject(result);

                /* Creating an Alert Dialog to Show Sign up Results to User */
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("Create account on GeeksReads");
                dialog.setMessage(jsonObject.getString("ReturnMsg"));
                sForTest = jsonObject.getString("ReturnMsg");

                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (jsonObject.getString("ReturnMsg").contains("A verification email has been sent")) {
                                final EditText FullName = findViewById(R.id.UserNameTxt);
                                final EditText Email = findViewById((R.id.EmailTxt));
                                final EditText Password = findViewById(R.id.PasswordTxt);
                                final EditText ConfPassword = findViewById(R.id.ConfirmPasswordTxt);
                                FullName.setText("");
                                Email.setText("");
                                Password.setText("");
                                ConfPassword.setText("");

                                /* Go to Next Activity Layout */
                                Intent myIntent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(myIntent);
                            } else {
                                /* If Sign up didn't succeed, Stay Here in the same Activity and Do Nothing */
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }
            /* Catching Exceptions */ catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
