package com.rpsonline;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.List;

public class MusicService extends Service {
    private MediaPlayer player;
    public static boolean isPlaying;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        player=MediaPlayer.create(this,R.raw.music);
        player.setLooping(true);
        player.start();
        isPlaying=true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying=false;
        player.stop();
    }

}
