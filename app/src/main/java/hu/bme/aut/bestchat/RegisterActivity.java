package hu.bme.aut.bestchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, password, email;
    Button btnRegister;
    FirebaseAuth auth;
    DatabaseReference reference;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        btnRegister = findViewById(R.id.btnRegister);
        relativeLayout = findViewById(R.id.relativelayout);

        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();



                if(txt_username.isEmpty() || txt_email.isEmpty() || txt_password.isEmpty()){
                    if(txt_username.isEmpty()){
                        username.requestFocus();
                        username.setError("Please enter your username!");
                    }
                    if(txt_email.isEmpty()) {
                        email.requestFocus();
                        email.setError("Please enter your email address!");
                    }
                    if(txt_password.isEmpty()) {
                        password.requestFocus();
                        password.setError("Please enter your new password!");
                    }
                } else if (txt_password.length() < 6){
                    password.requestFocus();
                    password.setError("Passowrd must be at least 6 characters!");
                } else {
                    register(txt_username,txt_email,txt_password);
                }
            }
        });
    }

    private void register(final String username, final String email, String password){
        if(isNetworkAvailable(RegisterActivity.this)) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser fbUser = auth.getCurrentUser();
                                assert fbUser != null;
                                String uid = fbUser.getUid();

                                reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("id", uid);
                                hashMap.put("email", email);
                                hashMap.put("username", username);
                                hashMap.put("imageURL", "default");
                                hashMap.put("status", "Online");
                                hashMap.put("search", username.toLowerCase());
                                hashMap.put("lasttimeonline","0");
                                hashMap.put("fullname","");
                                hashMap.put("number","");
                                hashMap.put("hometown","");
                                hashMap.put("borntown","");
                                hashMap.put("borntime","");
                                hashMap.put("whocanseemyprofile","Everyone");


                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                Snackbar.make(relativeLayout, "You can't register with this email or password!", 2500).show();
                            }
                        }
                    });
        } else {
            Snackbar.make(relativeLayout,"You are not connected to the Internet!",2500).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
