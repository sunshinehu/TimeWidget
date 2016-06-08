package com.echo.timewidget;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by huchenxi on 2016/6/8.
 */
public class PortalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences= getSharedPreferences("echo",
            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("flag",true);
        editor.apply();
    }
}
