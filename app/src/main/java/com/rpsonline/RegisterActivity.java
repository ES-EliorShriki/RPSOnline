package com.rpsonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSignUp;
    EditText etEmail,etPassword,etNickname;
    FirebaseAuth mAuth;
    TextView toSignIn;
    SharedPreferences preferences;
    boolean isMuted;
    ImageView ivMusic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferences=getSharedPreferences("RPSGame",MODE_PRIVATE);
        isMuted=preferences.getBoolean("isMuted",true);
        mAuth=FirebaseAuth.getInstance();
        ivMusic=findViewById(R.id.ivMusic);
        btnSignUp=findViewById(R.id.btnSignUp);
        etEmail=findViewById(R.id.etEmailRegister);
        etNickname=findViewById(R.id.etNickname);
        etPassword=findViewById(R.id.etPasswordRegister);
        toSignIn=findViewById(R.id.tvSignInDirect);
        ivMusic.setOnClickListener(this);
        toSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
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
    public void onClick(View v) {
        String email=etEmail.getText().toString();
        String password=etPassword.getText().toString();
        final String nickname=etNickname.getText().toString();
        if (!isMuted)
            stopService(new Intent(RegisterActivity.this,MusicService.class));
        if(v==btnSignUp)//sign up button options
        {
            if(email.isEmpty())
            {
                etEmail.setError("Please enter email");
                etEmail.requestFocus();
            }else  if (password.isEmpty())
            {
                etPassword.setError("Please enter your password between 8-16 chars");
                etPassword.requestFocus();
            }else if(email.isEmpty()&&password.isEmpty())
            {
                Toast.makeText(RegisterActivity.this,"Please enter something",Toast.LENGTH_SHORT).show();
            }else if(!(email.isEmpty()&&password.isEmpty()&&password.length()>8&&password.length()<16&&nickname.length()<16))
            {
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener
                        (RegisterActivity.this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(!task.isSuccessful())
                                {
                                    Toast.makeText(RegisterActivity.this,"Register failed please try again",Toast.LENGTH_SHORT).show();
                                }else
                                {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(nickname).build();
                                    user.updateProfile(profileUpdates);
                                    startActivity(new Intent(RegisterActivity.this,MainMenu.class));
                                    finish();
                                }
                            }
                        });
            }
        }else if(v==toSignIn){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }else if(ivMusic==v){
            isMuted=!isMuted;
            preferences.edit().putBoolean("isMuted",isMuted).apply();
            if (isMuted){
                ivMusic.setImageResource(R.drawable.ic_volume_off_black_24dp);
                stopService(new Intent(this,MusicService.class));
            }else{
                ivMusic.setImageResource(R.drawable.ic_volume_up_black_24dp);
                    startService(new Intent(this,MusicService.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
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
}
