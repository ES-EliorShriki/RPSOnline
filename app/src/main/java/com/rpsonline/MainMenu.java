package com.rpsonline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainMenu extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseAuth firebaseAuth;
    TextView tvTitle;
    Button btnLobby,btnSignOut;
    SharedPreferences preferences;
    ImageView ivMusic;
    boolean isMuted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferences=getSharedPreferences("RPSGame",MODE_PRIVATE);
        isMuted=preferences.getBoolean("isMuted",true);
        tvTitle=findViewById(R.id.tvTitle);
        btnLobby=findViewById(R.id.btnLobby);
        btnSignOut=findViewById(R.id.btnSignOut);
        firebaseAuth=FirebaseAuth.getInstance();
        ivMusic=findViewById(R.id.ivMusic);
        ivMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMuted=!isMuted;
                preferences.edit().putBoolean("isMuted",isMuted).apply();
                if (isMuted){
                    ivMusic.setImageResource(R.drawable.ic_volume_off_black_24dp);
                    stopService(new Intent(MainMenu.this,MusicService.class));
                }else{
                    ivMusic.setImageResource(R.drawable.ic_volume_up_black_24dp);
                    if (!MusicService.isPlaying)
                        startService(new Intent(MainMenu.this,MusicService.class));
                }
            }
        });
        tvTitle.setText("Welcome "+firebaseAuth.getCurrentUser().getDisplayName());
        btnLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this,Lobby.class));
                if (!isMuted)
                    stopService(new Intent(MainMenu.this,MusicService.class));
                finish();
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(MainMenu.this,LoginActivity.class));
                if (!isMuted)
                    stopService(new Intent(MainMenu.this,MusicService.class));
                finish();
            }
        });
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
    public void onBackPressed() {
        return;
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
