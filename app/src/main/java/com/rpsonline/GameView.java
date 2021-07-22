package com.rpsonline;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {
    final String oppTeam;
    final String roomName;
    final int soundEffects[]=new int[6];
    FirebaseDatabase firebaseDatabase;
    Thread thread;
    boolean isPlaying,isSelected,isGameEnded,isDonePrep;
    TableInitializeThread dbThread;
    Paint paint;
    Context context;
    int screenX, screenY;
    int selectedX,selectedY;
    Background background1,background2;
    Player[][] players= new Player[7][6];
    String team,turn;
    Dialog dialog;
    Bitmap selected,redTurn,blueTurn,invisible;
    CountDownTimer countDownTimer;
    int currentTime=0;
    SoundPool turnSound;
    GameActivity activity;
    //soundEffects 0=blueTurn| 1=redTurn|2=win|3=lose|4=victory|5=youFailed
    public GameView(GameActivity activity,Context context, int screenX, int screenY, final String roomName,String team,String turn) {
        super(activity);
        isPlaying=true;
        this.activity = activity;
        this.context = context;
        dialog = new Dialog(context);
        firebaseDatabase = FirebaseDatabase.getInstance();
        this.screenX = screenX;
        this.screenY = screenY;
        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.setX(screenX);
        this.roomName = roomName;
        this.team = team;
        this.turn=turn;
        if (team.equals("Red")) {
            oppTeam = "Blue";
        } else {
            oppTeam = "Red";
        }
        paint = new Paint();
        setSoundEffects();
        createEmptyTable();
        defineBitmaps();
        timerInitializer();
        thread = new Thread(this);
        dbThread=new TableInitializeThread();
        startDBThread();
        thread.start();
        countDownTimer.start();
    }
    public void defineBitmaps(){
        selected = BitmapFactory.decodeResource(context.getResources(), R.drawable.selected);
        selected = Bitmap.createScaledBitmap(selected, screenX / 10, (int) (0.9 * screenY) / 6, false);
        blueTurn = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluebadge);
        redTurn = BitmapFactory.decodeResource(context.getResources(), R.drawable.redbadge);
        if (team.equals("Red"))
        {
            invisible=BitmapFactory.decodeResource(context.getResources(),R.drawable.blueunknown);
            invisible=Bitmap.createScaledBitmap(invisible,screenX/10,(int)(0.9*screenY)/6,false);
        }
        else{
            invisible=BitmapFactory.decodeResource(context.getResources(),R.drawable.redunknown);
            invisible=Bitmap.createScaledBitmap(invisible,screenX/10,(int)(0.9*screenY)/6,false);
        }
        blueTurn=Bitmap.createScaledBitmap(blueTurn,screenX/10,screenY/5,false);
        redTurn=Bitmap.createScaledBitmap(redTurn,screenX/10,screenY/5,false);
    }
    public void setSoundEffects(){
        AudioAttributes audioAttributes= new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        turnSound=new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build();
        soundEffects[0]=turnSound.load(context,R.raw.blueturn,0);
        soundEffects[1]=turnSound.load(context,R.raw.redturn,0);
        soundEffects[2]=turnSound.load(context,R.raw.win,1);
        soundEffects[3]=turnSound.load(context,R.raw.lose,1);
        soundEffects[4]=turnSound.load(context,R.raw.victory,1);
        soundEffects[5]=turnSound.load(context,R.raw.youfailed,1);
    }
    public void timerInitializer(){
        countDownTimer=new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentTime=(int)millisUntilFinished/1000;
            }
            @Override
            public void onFinish() {
                if (isPlaying) {
                    switchTurn();
                }
            }
        };
    }
    public void startDBThread(){
        dbThread.start();
    }
    public void stopDBThread(){
        try {
            dbThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void createEmptyTable(){
        for (int i=0;i<7;i++){
            for (int j=0;j<6;j++)
                players[i][j]=new Player(context,"Empty",screenX,screenY);
        }
    }
    public void run() {
        while(isPlaying) {
            update();
            draw();
            sleep();
        }
       draw();
    }
    private void sleep() {
        try {
            thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void update() {
        background1.setX(background1.getX()-15);
        background2.setX(background2.getX()-15);
        if (background1.getX()+background1.getBackground().getWidth()<0)
            background1.setX(screenX);
        if (background2.getX()+background2.getBackground().getWidth()<0)
            background2.setX(screenX);
        if (turn!=null) {
            if (!turn.equals(team))
                isSelected = false;
        }
    }
    private void draw() {
        if(getHolder().getSurface().isValid()){
            Canvas canvas=getHolder().lockCanvas();
            Paint paint1=new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(100);
            if (!isPlaying){
                canvas.drawBitmap(background1.getBackground(),0,0,paint);
                waitBeforeExiting (100);
                canvas.drawText("BYYYYYYYE",(float)0.05*screenX,(float)0.3*screenY,paint1);
                getHolder().unlockCanvasAndPost(canvas);
                return;
            }
            if(isGameEnded){
                canvas.drawBitmap(background1.getBackground(),0,0,paint);
                waitBeforeExiting(6000);
                dialog.dismiss();
                getHolder().unlockCanvasAndPost(canvas);
                return;
            }
            canvas.drawBitmap(background1.getBackground(),background1.getX(),background1.getY(),paint);
            canvas.drawBitmap(background2.getBackground(),background2.getX(),background2.getY(),paint);
            canvas=drawTable(canvas);
            if(turn!=null&&turn.equals("Blue")){
                canvas.drawBitmap(blueTurn,(float)0.025*screenX,(float)0.025*screenY,paint);
            }else{
                canvas.drawBitmap(redTurn,(float)0.025*screenX,(float)0.025*screenY,paint);
            }
            canvas.drawText(Integer.toString(currentTime),(float)0.05*screenX,(float)0.13*screenY,paint1);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    public Canvas drawTable(Canvas canvas){
        for (int i=0;i<7;i++){
            for (int j=0;j<6;j++){
                if(!(team.equals(players[i][j].getTeam()) || players[i][j].isVisible() || players[i][j].getTheType().equals("Empty"))){
                    canvas.drawBitmap(invisible,(float)(screenX*0.15)+(screenX/10)*i, (float) ((screenY*0.05)+((0.9*screenY)/6)*j),paint);
                }else
                {
                    canvas.drawBitmap(players[i][j].getType(), (float) (screenX * 0.15) + (screenX / 10) * i, (float) ((screenY * 0.05) + ((0.9 * screenY) / 6) * j), paint);
                    if (i == selectedX && j == selectedY && isSelected)
                    {
                        canvas.drawBitmap(selected, (float) (screenX * 0.15) + (screenX / 10) * i, (float) ((screenY * 0.05) + ((0.9 * screenY) / 6) * j), paint);
                    }
                }
            }
        }
        return canvas;
    }
    private void waitBeforeExiting(int millis) {
        try {
            Thread.sleep(millis);
            activity.startActivity(new Intent(context, MainMenu.class));
            isPlaying=false;
            stopDBThread();
            countDownTimer.cancel();
            activity.finish();
            firebaseDatabase.getReference("rooms/"+roomName).setValue(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void resume(){
       if(isGameEnded)
           isPlaying=false;
    }
    public void pause(){
        if(!(isGameEnded||!isPlaying))
            firebaseDatabase.getReference("rooms/"+roomName+"/"+team+"/status").setValue("Out");
        isPlaying=false;
        isGameEnded=true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void switchTurn(){
        if(turn.equals(team)) {
            if (team.equals("Red")) {
                firebaseDatabase.getReference("rooms/" + roomName + "/turn").setValue("Blue");
                turn = "Blue";
                countDownTimer.start();
            } else {
                firebaseDatabase.getReference("rooms/" + roomName + "/turn").setValue("Red");
                turn = "Red";
                countDownTimer.start();
            }
        }else{
            if (turn.equals("Red")) {
                turn = "Blue";
                countDownTimer.start();
            } else {
                turn = "Red";
                countDownTimer.start();
            }
        }
        if (turn.equals("Red"))
            turnSound.play(soundEffects[1], 1, 1, 0, 0, 1);
        else
            turnSound.play(soundEffects[0], 1, 1, 0, 0, 1);
    }
    public void setOnTie(String type, final int indexX, final int indexY, final int oppX, final int oppY){
        players[indexX][indexY].setType(type);
        firebaseDatabase.getReference("rooms/"+roomName+"/"+team+"/tie").setValue(type);
        firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/tie").
                addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            players[oppX][oppY].setType(dataSnapshot.getValue().toString());
                            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/tie").setValue(null);
                            dialog.dismiss();
                            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/tie").removeEventListener(this);
                            if(turn.equals(team))
                                fight(oppY, oppX);
                            else
                                fight(indexY, indexX);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }
    public void tie(final int indexX, final int indexY){
        countDownTimer.cancel();
        final TextView title;
        final ImageView dialogPaper,dialogScissors,dialogRock;
        dialog.setContentView(R.layout.drawevent);
        dialog.setCancelable(false);
        dialogPaper=dialog.findViewById(R.id.paper);
        dialogScissors=dialog.findViewById(R.id.scissors);
        dialogRock=dialog.findViewById(R.id.rock);
        title=dialog.findViewById(R.id.tvTitleDraw);
        dialogPaper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPaper.setOnClickListener(null);
                dialogScissors.setOnClickListener(null);
                dialogRock.setOnClickListener(null);
                title.setText("Waiting for opponent");
                if(turn.equals(team))
                    setOnTie("Paper",selectedX,selectedY,indexX,indexY);
                else
                    setOnTie("Paper",indexX,indexY,selectedX,selectedY);
            }
        });
        dialogRock.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialogPaper.setOnClickListener(null);
                dialogScissors.setOnClickListener(null);
                dialogRock.setOnClickListener(null);
                title.setText("Waiting for opponent");
                if(turn.equals(team))
                    setOnTie("Rock",selectedX,selectedY,indexX,indexY);
                else
                    setOnTie("Rock",indexX,indexY,selectedX,selectedY);
            }
        });
        dialogScissors.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialogPaper.setOnClickListener(null);
                dialogScissors.setOnClickListener(null);
                dialogRock.setOnClickListener(null);
                title.setText("Waiting for opponent");
                if(turn.equals(team))
                    setOnTie("Scissors",selectedX,selectedY,indexX,indexY);
                else
                    setOnTie("Scissors",indexX,indexY,selectedX,selectedY);
            }
        });
        dialog.show();
    }
    public void fight(int indexY,int indexX){
        String type1 =players[selectedX][selectedY].getTheType();
        String type2=players[indexX][indexY].getTheType();
        players[indexX][indexY].setVisible(true);
        players[selectedX][selectedY].setVisible(true);
        if(type2.equals("Mummy"))
        {
            players[selectedX][selectedY]=new Player(context,"Empty",screenX,screenY);
            switchTurn();
        }else if(type2.equals("Flag")){
            if(players[indexX][indexY].getTeam().equals(team))
                lose();
            else
                win();
        }else {
            switch (type1) {
                case "Rock": {
                    switch (type2) {
                        case "Rock": {
                            tie(indexX, indexY);
                            isSelected = false;
                            return;
                        }
                        case "Paper": {
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            players [indexX][indexY]=new Player(players[indexX][indexY]);
                            isSelected = false;
                            break;
                        }
                        case "Scissors": {
                            players[indexX][indexY] = new Player(players[selectedX][selectedY]);
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            isSelected = false;
                            break;
                        }
                    }
                    break;
                }
                case "Paper": {
                    switch (type2) {
                        case "Rock": {
                            players[indexX][indexY] = new Player(players[selectedX][selectedY]);
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            isSelected = false;
                            break;
                        }
                        case "Paper": {
                            isSelected = false;
                            tie(indexX, indexY);
                            return;
                        }
                        case "Scissors": {
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            players [indexX][indexY]=new Player(players[indexX][indexY]);
                            isSelected = false;
                            break;
                        }
                    }
                    break;
                }
                case "Scissors": {
                    switch (type2) {
                        case "Rock": {
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            players [indexX][indexY]=new Player(players[indexX][indexY]);
                            isSelected = false;
                            break;
                        }
                        case "Paper": {
                            players[indexX][indexY] = new Player(players[selectedX][selectedY]);
                            players[selectedX][selectedY] = new Player(context, "Empty", screenX, screenY);
                            isSelected = false;
                            break;
                        }
                        case "Scissors": {
                            tie(indexX, indexY);
                            isSelected = false;
                            return;
                        }
                    }
                    break;
                }
            }
            switchTurn();
            //Toast.makeText(context, "Fight", Toast.LENGTH_SHORT).show();
        }
    }
    public void win()   {
        isGameEnded=true;
        dialog.setContentView(R.layout.exitpopup);
        TextView tvTitle=dialog.findViewById(R.id.tvTitleDraw);
        tvTitle.setText("You Won");
        ImageView ivPic=dialog.findViewById(R.id.ivEndGame);
        ivPic.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.win));
        dialog.setCancelable(false);
        dialog.show();
        turnSound.play(soundEffects[4],1,1,1,0,1);
    }
    public void lose(){
        isGameEnded=true;
        dialog.setContentView(R.layout.exitpopup);
        TextView tvTitle=dialog.findViewById(R.id.tvTitleDraw);
        tvTitle.setText("You Lost");
        ImageView ivPic=dialog.findViewById(R.id.ivEndGame);
        ivPic.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.loser));
        dialog.setCancelable(false);
        dialog.show();
        turnSound.play(soundEffects[5],1,1,1,0,1);
    }
    public void move(int indexY,int indexX){
            if(players[selectedX][selectedY].getTeam().equals(players[indexX][indexY].getTeam()))
            {
                if(!(players[indexX][indexY].getTheType().equals("Mummy")|| players[indexX][indexY].getTheType().equals("Flag")
                ||players[indexX][indexY].getTheType().equals("Empty"))) {
                    selectedX = indexX;
                    selectedY = indexY;
                }
            }
            else if((Math.abs(indexX-selectedX)==1 &&Math.abs(indexY-selectedY)==0)
                    ||(Math.abs((indexY-selectedY))==1 && Math.abs(indexX-selectedX)==0))
            {
                if(players[indexX][indexY].getTheType().equals("Empty"))
                {
                    players[indexX][indexY] = new Player(players[selectedX][selectedY]);
                    players[selectedX][selectedY] = new Player(context,"Empty",screenX,screenY);
                    firebaseDatabase.getReference("rooms/"+roomName+"/"+team+"/move").setValue(team+",("+selectedX+","+selectedY+")/("+indexX+","+indexY+")");
                    countDownTimer.start();
                    isSelected = false;
                    switchTurn();
                }
                else {
                    firebaseDatabase.getReference("rooms/" + roomName + "/" + team + "/move").setValue(team + ",(" + selectedX + "," + selectedY + ")/(" + indexX + "," + indexY + ")");
                    fight(indexY, indexX);
                }
            }
    }
    public boolean onTouchEvent(MotionEvent event) {
        float touchX=event.getX();
        float touchY=event.getY();
        int action=event.getAction();
        int indexX,indexY;
        if (action==MotionEvent.ACTION_DOWN&&turn.equals(team)){
            if(touchX<=screenX*0.85&&touchX>=screenX*0.15&&touchY<=screenY*0.95&&touchY>=screenY*0.05){
                indexX=(int)((touchX-0.15*screenX)/(screenX/10));
                indexY=(int)((touchY-0.05*screenY)/((0.9*screenY)/6));
                if(isSelected)
                    move(indexY,indexX);
                else {
                    if ((!(players[indexX][indexY].getTheType().equals("Empty") ||
                            players[indexX][indexY].getTheType().equals("Mummy") ||
                            players[indexX][indexY].getTheType().equals("Flag"))) &&
                            players[indexX][indexY].getTeam().equals(team)) {
                        isSelected = true;
                        selectedY = indexY;
                        selectedX = indexX;
                    }
                }
                Log.i("Table","touched "+indexX+" "+indexY);
            }
        }
        return true;
    }
    class TableInitializeThread extends Thread{
        public void opponentMove(String from,String to){
            selectedX=Integer.parseInt(from.substring(1,2));
            selectedY=Integer.parseInt(from.substring(3,4));
            int toX=Integer.parseInt(to.substring(1,2));
            int toY=Integer.parseInt(to.substring(3,4));
            isSelected=false;
            if(players[toX][toY].getTheType().equals("Empty"))
            {
                players[toX][toY] = new Player(players[selectedX][selectedY]);
                players[selectedX][selectedY] = new Player(context,"Empty",screenX,screenY);
                switchTurn();
            }else {
                fight(toY,toX);
            }
        }
        public void setMessageFromReference(final DatabaseReference reference, final int row, final int col){
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if(dataSnapshot.getValue()!=null) {
                        String message = dataSnapshot.getValue().toString();
                        String type = message.substring(0, message.indexOf(","));
                        String team = message.substring(message.indexOf(",") + 1);
                        players[col][row] = new Player(context, type, screenX, screenY, team, false);
                        reference.removeEventListener(this);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        public void update(){
            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/move").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isGameEnded||!isPlaying) {
                        firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/move").removeEventListener(this);
                        firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/move").setValue(null);
                    }
                    else if (dataSnapshot.getValue() != null) {
                        String message = dataSnapshot.getValue().toString();
                        if (!message.equals("Mashu")) {
                            if (!team.equals(message.substring(0, message.indexOf(",")))) ;
                            {
                                String from = message.substring(message.indexOf(",") + 1, message.indexOf(')') + 1);
                                String to = message.substring(message.indexOf("/") + 1);
                                opponentMove(from, to);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        if (dataSnapshot.getValue().toString().equals("Out")) {
                            isPlaying=false;
                            isGameEnded=false;
                            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/move").setValue("Mashu");
                            firebaseDatabase.getReference("rooms/" + roomName + "/" + oppTeam + "/status").removeEventListener(this);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        public void run() {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 7; i++) {
                    DatabaseReference reference = firebaseDatabase.getReference("rooms/" + roomName + "/table/" + i + j);
                    setMessageFromReference(reference, j, i);
                }
            }
            for (int j = 4; j < 6; j++) {
                for (int i = 0; i < 7; i++) {
                    DatabaseReference reference = firebaseDatabase.getReference("rooms/" + roomName + "/table/" + i + j);
                    setMessageFromReference(reference, j, i);
                }
            }
            update();
        }
    }
}
