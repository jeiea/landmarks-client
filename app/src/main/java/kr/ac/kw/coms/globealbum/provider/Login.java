package kr.ac.kw.coms.globealbum.provider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.landmarks.client.LoginRep;

public class Login extends AppCompatActivity {
    String TAG = "Login";
    EditText etEmail;
    EditText etPassword;
    String stEmail;
    String stPassword;
    Button btnAccount;
    RemoteJava client = RemoteJava.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstenceState) {
        super.onCreate(savedInstenceState);
        setContentView(R.layout.layout_login);

        Intent intent = new Intent(this.getIntent());

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

    }

    public void onLogin(View view) {
        String ident = etEmail.getText().toString();
        String pass = etPassword.getText().toString();

        client.login(ident, pass, new ToastPromise<Unit>("Login success"));
    }


    public void onRegister(View view) {
        String ident = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        LoginRep sign = new LoginRep(ident, pass, ident, ident);

        client.register(sign, new ToastPromise<Unit>("Register success"));
    }

    public void onForget(View view){
        //구현하기
    }


    class ToastPromise<T> extends UIPromise<T> {
        String successMessage;

        ToastPromise(String onSuccessMessage) {
            successMessage = onSuccessMessage;
        }

        @Override
        public void success(T result) {
            Toast.makeText(Login.this, successMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(@NotNull Throwable cause) {
            Toast.makeText(Login.this, cause.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
