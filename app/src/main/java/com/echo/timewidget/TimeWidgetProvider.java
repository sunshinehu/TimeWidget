package com.echo.timewidget;

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
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.RemoteViews;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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


    private Bitmap bkg;
    private volatile boolean changed=true;


    public TimeWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

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


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.time_widget_layout);

        updateTextView(remoteViews);

        if(changed) {
            Log.d("updateview","update");
            updateBackView(context, remoteViews);
            changed=false;
        }

        //相当于获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context,TimeWidgetProvider.class);

        //更新appwidget
        appWidgetManager.updateAppWidget(componentName, remoteViews);

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
                    bkg = GetLocalOrNetBitmap("http://www.dujin.org/sys/bing/1366.php");
                    if(bkg!=null){
                        Log.d("GETBitmap","ok");
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putInt("date",current);
                        editor.apply();
                        changed=true;
                    }
                }

            }.start();

        }



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




    private void updateBackView(Context context,RemoteViews remoteViews){

        if(bkg==null){
            Log.d("updateview","empty");
            return;
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

        blur.setRadius(25);

        blur.forEach(overlayAlloc);

        overlayAlloc.copyTo(bkg);

        remoteViews.setBitmap(R.id.back,"setImageBitmap",bkg);

        rs.destroy();


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
