package kr.ac.kw.coms.globealbum.provider;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import kr.ac.kw.coms.globealbum.R;

public class Login extends AppCompatActivity{
    String TAG = "Login";
    EditText etEmail;
    EditText etPassword;
    String stEmail;
    String stPassword;
    Button btnAccount;
    LandmarkClient LmC = new LandmarkClient();

    @Override
    protected void onCreate(Bundle savedInstenceState){
        super.onCreate(savedInstenceState);
        setContentView(R.layout.login);

        Intent intent = new Intent(this.getIntent());

        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);

        stPassword = etPassword.getText().toString();
        stEmail = etEmail.getText().toString();

        btnAccount = (Button)findViewById(R.id.btnAccount);

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                LoginTask task = new LoginTask();

                task.execute(stEmail, stPassword);
            }
        });
    }

    public void onLogin(View view) {

    }

    public void onRegister(View view) {
        EditText email =  findViewById(R.id.etEmail);

        new RegisterTask().execute(email.getText().toString());
    }

    class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            String BaseUrl = "https://coms-globe.herokuapp.com";

            LmC.login(BaseUrl, strings[0], strings[1]);

            return null;
        }
    }

    class RegisterTask extends AsyncTask<String, Void, String> {
        String receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            String BaseUrl = "https://coms-globe.herokuapp.com";

            try {
                String receiveMsg = LmC.register(BaseUrl, strings[0], null);
            } catch (IOException e) {}

            return receiveMsg;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(Login.this, s.substring(0, 30), Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }
    }
}
