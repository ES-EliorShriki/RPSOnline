package com.rpsonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends AppCompatActivity {

    ListView lvRooms;
    Button btnCreate,btnBack;
    List<String> roomList;
    String playerName="";
    String roomName="";
    ImageView ivMusic;
    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;
    SharedPreferences preferences;
    boolean isMuted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        database=FirebaseDatabase.getInstance();
        preferences=getSharedPreferences("RPSGame",MODE_PRIVATE);
        isMuted=preferences.getBoolean("isMuted",true);
        ivMusic=findViewById(R.id.ivMusic);
        playerName=user.getDisplayName();

        btnBack=findViewById(R.id.btnBack);
        lvRooms=findViewById(R.id.listView);
        btnCreate=findViewById(R.id.btnCreate);

        roomList=new ArrayList<>();

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
        ivMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMuted=!isMuted;
                preferences.edit().putBoolean("isMuted",isMuted).apply();
                if (isMuted){
                    ivMusic.setImageResource(R.drawable.ic_volume_off_black_24dp);
                    stopService(new Intent(Lobby.this,MusicService.class));
                }else{
                    ivMusic.setImageResource(R.drawable.ic_volume_up_black_24dp);
                        startService(new Intent(Lobby.this,MusicService.class));
                }
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate.setText("Creating Room");
                btnCreate.setEnabled(false);
                roomName=playerName;
                roomRef=database.getReference("rooms/"+roomName+"/Red/PlayerName");
                addRoom();
                roomRef.setValue(playerName);
            }
        });
        lvRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomName=roomList.get(position);
                roomRef= database.getReference("rooms/"+roomName+"/Blue/PlayerName");
                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()==null){
                            roomRef.setValue(playerName);
                            addRoom();
                        }else
                            Toast.makeText(Lobby.this,"This Lobby is full",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Lobby.this,MainMenu.class));
                if (!isMuted)
                    stopService(new Intent(Lobby.this,MusicService.class));
                finish();
            }
        });
        addRoomsEventListner();
    }

    private void addRoom() {
        btnCreate.setText("Create room");
        btnCreate.setEnabled(true);
        Intent intent=new Intent(Lobby.this,GameLobby.class);
        intent.putExtra("roomName",roomName);
        startActivity(intent);
        finish();
    }

    private void addRoomsEventListner(){
        roomsRef=database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear();
                Iterable<DataSnapshot> rooms=dataSnapshot.getChildren();
                for (DataSnapshot snapshot:rooms){
                    roomList.add(snapshot.getKey());
                    ArrayAdapter<String> adapter= new ArrayAdapter<>
                            (Lobby.this,android.R.layout.simple_list_item_1,roomList);
                    lvRooms.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
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
