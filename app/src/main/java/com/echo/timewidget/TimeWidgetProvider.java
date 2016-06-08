package com.echo.timewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.RemoteViews;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by huchenxi on 2016/3/31.
 */
public class TimeWidgetProvider extends AppWidgetProvider {


    public TimeWidgetProvider() {
        super();
    }




    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("life","receive");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.time_widget_layout);

        updateTextView(remoteViews);


        //获得appwidget管理实例，用于管理appwidget以便进行更新操作
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        //相当于获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context,TimeWidgetProvider.class);

        //更新appwidget
        appWidgetManager.updateAppWidget(componentName, remoteViews);



    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d("life","update");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.time_widget_layout);

        updateTextView(remoteViews);



        final SharedPreferences sharedPreferences= context.getSharedPreferences("echo",
            Context.MODE_PRIVATE);
        final int day=sharedPreferences.getInt("date",-1);
        final int current=Calendar.getInstance().get(Calendar.DATE);

        if(current!=day){

            new Thread(){

                @Override
                public void run() {
                    super.run();
                    Log.d("Download","start");
                    Bitmap bkg = GetLocalOrNetBitmap("http://www.dujin.org/sys/bing/1920.php");

                    if(bkg!=null){

                        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/wallpaper");

                        if(file.exists()){
                            file.delete();
                        }

                        try {
                            OutputStream os=new FileOutputStream(file);
                            bkg.compress(Bitmap.CompressFormat.JPEG, 100, os);
                            os.flush();
                            os.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Log.d("GETBitmap","ok");
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putInt("date",current);
                        editor.putBoolean("flag",true);
                        editor.apply();
                    }
                }

            }.start();

        }


        boolean flag=sharedPreferences.getBoolean("flag",true);

        Log.d("updateView","flag"+flag);


        if(flag && updateBackView(context,remoteViews)){

            Log.d("updateView","success");

            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("flag",false);
            editor.apply();
        }


        //相当于获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context,TimeWidgetProvider.class);

        //更新appwidget
        appWidgetManager.updateAppWidget(componentName, remoteViews);




    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }



    private void updateTextView(RemoteViews remoteViews){

        //Time 1 February 12
        //Time 2 Apr 12
        //Time 3 Apr 16
        //Time 4 Apr 24
        Calendar calendar1=Calendar.getInstance();
        calendar1.set(2016,1,12);
        Calendar calendar2=Calendar.getInstance();
        calendar2.set(2016,3,12);

        int time1=daysBetween(calendar1.getTime(),new Date());
        int time2=daysBetween(calendar2.getTime(),new Date());
        remoteViews.setTextViewText(R.id.day1,time1+1+"");
        remoteViews.setTextViewText(R.id.day2," & "+(time2+1));


    }


    public static Bitmap GetLocalOrNetBitmap(String url)
    {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try
        {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    private static void copy(InputStream in, OutputStream out)
        throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }




    private boolean updateBackView(Context context,RemoteViews remoteViews){

        Log.d("updateView","start update");

        Bitmap bkg=BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/wallpaper");
        if(bkg!=null) {
            bkg =bkg.copy(Bitmap.Config.ARGB_8888, true);
        }

        if(bkg==null){
            Log.d("updateview","empty");
            return false;
        }

        Canvas canvas = new Canvas(bkg);

        canvas.translate(0, 0);
        canvas.drawBitmap(bkg, 0, 0, null);

        RenderScript rs = RenderScript.create(context);

        Allocation overlayAlloc = Allocation.createFromBitmap(
            rs, bkg);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
            rs, overlayAlloc.getElement());

        blur.setInput(overlayAlloc);

        blur.setRadius(15);

        blur.forEach(overlayAlloc);

        overlayAlloc.copyTo(bkg);

        remoteViews.setBitmap(R.id.back,"setImageBitmap",bkg);

        rs.destroy();

        Log.d("updateView","update finish");

        return true;

    }





    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    private int daysBetween(Date smdate,Date bdate)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try {
            smdate=sdf.parse(sdf.format(smdate));
            bdate=sdf.parse(sdf.format(bdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

}
