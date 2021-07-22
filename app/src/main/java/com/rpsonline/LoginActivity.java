package com.rpsonline;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.ContentHandler;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseAuth dbAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    EditText etEmail,etPassword;
    Button btnSignIn,btnToSignUp;
    ImageView ivMusic;
    SharedPreferences preferences;
    boolean isMuted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        dbAuth=FirebaseAuth.getInstance();
        ivMusic=findViewById(R.id.ivMusic);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        btnSignIn=findViewById(R.id.btnSignIn);
        btnToSignUp=findViewById(R.id.btnToSignUp);
        preferences=getSharedPreferences("RPSGame",MODE_PRIVATE);
        isMuted=preferences.getBoolean("isMuted",true);
        ivMusic.setOnClickListener(this);
        btnToSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        mAuthStateListener=new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser mFirebaseUser= firebaseAuth.getCurrentUser();
                if(mFirebaseUser!=null)
                {
                    //Toast.makeText(LoginActivity.this,"Login successful",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,MainMenu.class));
                    if (!isMuted)
                        stopService(new Intent(LoginActivity.this,MusicService.class));
                    finish();
                }else
                {
                    Toast.makeText(LoginActivity.this,"Please Login",Toast.LENGTH_SHORT).show();
                }
            }
        };
        if(isMuted) {
            ivMusic.setImageResource(R.drawable.ic_volume_off_black_24dp);
            stopService(new Intent(this,MusicService.class));
            MusicService.isPlaying=false;
        }
        else {
            ivMusic.setImageResource(R.drawable.ic_volume_up_black_24dp);
            if (!MusicService.isPlaying)
                startService( new Intent(this,MusicService.class));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isMuted)
            stopService(new Intent(this,MusicService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isMuted)
            startService(new Intent(this,MusicService.class));
    }
    @Override
    public void onClick(View v) {
        if (!isMuted)
            stopService(new Intent(LoginActivity.this,MusicService.class));
        final String email=etEmail.getText().toString();
        final String password=etPassword.getText().toString();
        if(v==btnSignIn) {
            if(email.isEmpty()){
                etEmail.setError("Please enter email");
                etEmail.requestFocus();
            }else  if (password.isEmpty()){
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
            }else if(email.isEmpty()&&password.isEmpty()){
                Toast.makeText(LoginActivity.this,"plz enter something",Toast.LENGTH_SHORT).show();
            }else if(!(email.isEmpty()&&password.isEmpty())){
                dbAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener
                        (LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this,"Login failed plz try again",Toast.LENGTH_SHORT).show();
                                }else{
                                    startActivity(new Intent(LoginActivity.this,MainMenu.class));
                                    finish();
                                }
                            }
                        });
            }
        }else if(btnToSignUp==v)
        {
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            finish();
        }else if(ivMusic==v){
            isMuted=!isMuted;
            preferences.edit().putBoolean("isMuted",isMuted).apply();
            if (isMuted){
                ivMusic.setImageResource(R.drawable.ic_volume_off_black_24dp);
                stopService(new Intent(this,MusicService.class));
            }else{
                ivMusic.setImageResource(R.drawable.ic_volume_up_black_24dp);
                if (!MusicService.isPlaying)
                    startService(new Intent(this,MusicService.class));
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        dbAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    public void onBackPressed() {
        return;
    }
}