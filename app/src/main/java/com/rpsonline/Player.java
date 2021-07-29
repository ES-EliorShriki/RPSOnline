package com.rpsonline;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Player {
    private Context context;
    private int screenX,screenY;
    private String theType;
    private Bitmap type;
    private String team;
    private boolean visible;
    //false ->blue ;true->red.

    public Player(Context context, String theType,int screenX,int screenY,String team,boolean visible) {
        this.context=context;
        this.screenX=screenX;
        this.screenY=screenY;
        this.team=team;
        this.theType=theType;
        this.visible=visible;
        setTypeByName();
    }
    public Player(Context context, String theType,int screenX,int screenY) {
        this.context=context;
        this.theType=theType;
        this.screenX=screenX;
        this.screenY=screenY;
        this.team="None";
        setTypeByName();
    }
    public Player(Player player){
        this.context=player.getContext();
        this.visible=player.isVisible();
        this.screenX=player.getScreenX();
        this.screenY=player.getScreenY();
        this.team=player.getTeam();
        this.theType=player.getTheType();
        setTypeByName();
    }

    public Context getContext(){
        return context;
    }
    public boolean isVisible() {
        return visible;
    }
    public String getTheType() {
        return theType;
    }
    public void setType(String theType) {
        this.theType = theType;
        setTypeByName();
    }
    public void setTypeByName(){//game play type by name
        switch (theType){
            case "Rock": {
                if(team.equals("Red")){
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redrock);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redrockiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }else{
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluerock);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluerockiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }
                break;
            }
            case "Paper": {
                if(team.equals("Red")){
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redpaper);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redpaperiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }else{
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluepaper);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluepaperiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }break;
            }
            case "Scissors": {
                if(team.equals("Red")){
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redscissors);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.redscissorsiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }else{
                    if (isVisible()) {
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluescissors);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }else{
                        type = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluescissorsiv);
                        this.type = Bitmap.createScaledBitmap(this.type, screenX / 10, (int) (0.9 * screenY) / 6, false);
                    }
                }
                break;
            }
            case "Flag": {
            if(team.equals("Red")){
                type=BitmapFactory.decodeResource(context.getResources(),R.drawable.flag);
                this.type=Bitmap.createScaledBitmap(this.type,screenX/10,(int)(0.9*screenY)/6,false);
            }else{
                type=BitmapFactory.decodeResource(context.getResources(),R.drawable.blueflag);
                this.type=Bitmap.createScaledBitmap(this.type,screenX/10,(int)(0.9*screenY)/6,false);
            }
                break;
            }
            case "Mummy": {
                type=BitmapFactory.decodeResource(context.getResources(),R.drawable.mummy);
                this.type=Bitmap.createScaledBitmap(this.type,screenX/10,(int)(0.9*screenY)/6,false);
                break;
            }
            case "Empty": {
                type=BitmapFactory.decodeResource(context.getResources(),R.drawable.empty);
                this.type=Bitmap.createScaledBitmap(this.type,screenX/10,(int)(0.9*screenY)/6,false);
                break;
            }
        }
    }
    public void setType(Bitmap type) {
        this.type = type;
    }
    public int getScreenX() {
        return screenX;
    }
    public String getTeam(){
        return  team;
    }
    public void setScreenX(int screenX) {
        this.screenX = screenX;
    }
    public int getScreenY() {
        return screenY;
    }
    public void setScreenY(int screenY) {
        this.screenY = screenY;
    }
    public void setVisible(boolean visible){
        this.visible=visible;
        //setTypeByName();
    }
    public Bitmap getType(){
        return type;
    }
    @Override
    public String toString() {
        return "Player{" +
                ", type='" + type + '\'' +
                '}';
    }
}
