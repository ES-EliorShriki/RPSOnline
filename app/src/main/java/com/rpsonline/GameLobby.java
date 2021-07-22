package com.rpsonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameLobby extends AppCompatActivity {
    int screenX,screenY,flagID;
    String roomName;
    String playerName;
    String role;
    String flagPlace;
    String turn;
    String oppRole;
    TextView tvName;
    boolean isFlagSelected,done,opponentDone,isMute;
    Button btnLeave;
    FirebaseDatabase database;
    Dialog preGameSelection;
    Player[][] players= new Player[7][6];
    DatabaseReference roomRef,player2Ref,turnRef;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lobby);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferences=getSharedPreferences("RPSGame",MODE_PRIVATE);
        isMute=preferences.getBoolean("isMuted",true);
        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");
        playerName=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        database=FirebaseDatabase.getInstance();
        Point point=new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        screenX=point.x;
        screenY=point.y;
        role=roomName.equals(playerName)?"Red":"Blue";
        oppRole=role.equals("Red")?"Blue":"Red";
        preGameSelection=new Dialog(this);
        btnLeave=findViewById(R.id.btnLeave);
        tvName=findViewById(R.id.player1Name);
        tvName.setText(playerName);
        roomRef=database.getReference("rooms/"+roomName);
        player2Ref=database.getReference("rooms/"+roomName+"/Blue/status");
        turnRef=database.getReference("rooms/"+roomName+"/turn");
        if(role.equals("Red")) {
            if (Math.random() > 0.5) {
                turnRef.setValue("Red");
                turn = "Red";
            } else {
                turnRef.setValue("Blue");
                turn = "Blue";
            }
        }else{
            turnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    turn=dataSnapshot.getValue().toString();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomRef.setValue(null);
                startActivity(new Intent(GameLobby.this,Lobby.class));
                finish();
            }
        });
        if(roomName.equals(playerName)){
            player2Ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        if (dataSnapshot.getValue().toString().equals("left")){
                            Toast.makeText(GameLobby.this,"Your Opponent left the match",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(GameLobby.this,MainMenu.class));
                            database.getReference("rooms/"+roomName).setValue(null);
                            player2Ref.removeEventListener(this);
                            finish();
                        }
                        else if(dataSnapshot.getValue().toString().equals("Done")) {
                            if(done) {
                                Intent intent1 = new Intent(GameLobby.this, GameActivity.class);
                                intent1.putExtra("roomName", roomName);
                                intent1.putExtra("team", role);
                                intent1.putExtra("turn", turn);
                                startActivity(intent1);
                                player2Ref.removeEventListener(this);
                                finish();
                            }
                            opponentDone=true;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }else{
            database.getReference("rooms/"+roomName+"/Red/status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        if (dataSnapshot.getValue().toString().equals("left")){
                            Toast.makeText(GameLobby.this,"Your Opponent left the match",Toast.LENGTH_SHORT).show();
                            database.getReference("rooms/"+roomName).setValue(null);
                            startActivity(new Intent(GameLobby.this,MainMenu.class));
                            database.getReference("rooms/"+roomName+"/Red/status").removeEventListener(this);
                            finish();
                        }
                        else if(dataSnapshot.getValue().toString().equals("Done")) {
                            if(done) {
                                Intent intent1 = new Intent(GameLobby.this, GameActivity.class);
                                intent1.putExtra("roomName", roomName);
                                intent1.putExtra("team", role);
                                intent1.putExtra("turn", turn);
                                startActivity(intent1);
                                database.getReference("rooms/"+roomName+"/Red/status").removeEventListener(this);
                                finish();
                            }
                            opponentDone=true;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        createEmptyTable();
        setPreGameSelection();
    }
    public void startThread(){
        DBThread thread =new DBThread();
        thread.start();
    }
    public void setPreGameSelection(){
        TextView tvTitle;
        preGameSelection.setContentView(R.layout.pregameselection);
        preGameSelection.setCancelable(false);
        tvTitle=preGameSelection.findViewById(R.id.tvTitleSelection);
        if (isFlagSelected){
            tvTitle.setText("Choose the mummy location");
            ImageView iv =preGameSelection.findViewById(flagID);
            if (role.equals("Red"))
                iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.flag));
            else
                iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.blueflag));
        }
        preGameSelection.show();
    }
    public void selectPlace(View v){
        String setType=isFlagSelected?"Mummy":"Flag";
        String place=v.getTag().toString();
        flagID=v.getId();
        int x=Integer.parseInt(place.substring(0,1));
        int y=Integer.parseInt(place.substring(1));
        if(role.equals("Blue")){
            y+=4;
            place=Integer.toString(x)+Integer.toString(y);
        }
        if (isFlagSelected){
            if (place.equals(flagPlace)){
                Toast.makeText(this,"Please Choose Different Position",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        database.getReference("rooms/"+roomName+"/table/"+place).setValue(setType+","+role);
        players[x][y]=new Player(this,setType,screenX,screenY,role,false);
        preGameSelection.dismiss();
        if(!isFlagSelected)
        {
            isFlagSelected=true;
            setPreGameSelection();
            flagPlace=place;
        }else{
            startThread();
        }
    }
    public void createEmptyTable(){
        Point point=new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        for (int i=0;i<7;i++){
            for (int j=0;j<6;j++)
                players[i][j]=new Player(this,"Empty",point.x,point.y);
        }
    }
    public void createTable(int j){
        int max=j+2;
        int[] amount={0,0,0};//0 rock;1 paper; 2 scissors
        double odds;
        for (j= j; j < max; j++) {
            for (int i = 0; i < 7; i++) {
                if (players[i][j].getTheType().equals("Empty"))
                {
                    odds = Math.random();
                    if (odds <= 0.33 && amount[0] < 4) {
                        amount[0]++;
                        players[i][j] = new Player(this, "Rock", screenX, screenY, role, false);
                        database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Rock,"+role);
                    } else if (odds >= 0.67 && amount[1] < 4) {
                        amount[1]++;
                        players[i][j] = new Player(this, "Paper", screenX, screenY, role, false);
                        database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Paper,"+role);
                    } else if (odds > 0.33 && odds < 0.67 && amount[2] < 4) {
                        amount[2]++;
                        players[i][j] = new Player(this, "Scissors", screenX, screenY, role, false);
                        database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Scissors,"+role);
                    } else {
                        if (amount[0] < 4) {
                            players[i][j] = new Player(this, "Rock", screenX, screenY, role, false);
                            database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Rock,"+role);
                        } else if (amount[1] < 4) {
                            players[i][j] = new Player(this, "Paper", screenX, screenY, role, false);
                            database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Paper,"+role);
                        } else {
                            players[i][j] = new Player(this, "Scissors", screenX, screenY, role, false);
                            database.getReference("rooms/" + roomName + "/table/" + Integer.toString(i) + Integer.toString(j)).setValue("Scissors,"+role);
                        }
                    }
                }
            }
        }

    }
    class DBThread extends Thread{
        @Override
        public void run() {
            int j=0;
            if(role.equals("Blue"))
                j=4;
            createTable(j);
            done=true;
            database.getReference("rooms/"+roomName+"/"+role+"/status").setValue("Done");
            if(opponentDone){
                Intent intent1 = new Intent(GameLobby.this, GameActivity.class);
                intent1.putExtra("roomName", roomName);
                intent1.putExtra("team", role);
                intent1.putExtra("turn", turn);
                startActivity(intent1);
                finish();
            }
        }
    }
    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (!(opponentDone&&done)) {
            database.getReference("rooms/" + roomName + "/" + oppRole).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        database.getReference("rooms/" + roomName).setValue(null);
                    } else {
                        database.getReference("rooms/" + roomName + "/" + role + "/status").setValue("left");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            finish();
        }
    }
}
