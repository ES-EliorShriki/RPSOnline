package com.rpsonline;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background {
    private  int x;
    private  int y;
    private Bitmap background;
    public Background(int screenX, int screenY, Resources res){
        background= BitmapFactory.decodeResource(res,R.drawable.background);
        background = Bitmap.createScaledBitmap(background,screenX,screenY,false);
        x=0;
        y=0;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public void setX(int x){
        this.x=x;
    }
    public void setY(int y){
        this.y=y;
    }
    public Bitmap getBackground(){
        return background;
    }
    public void setBackground(Bitmap background){
        this.background=background;
    }
}
