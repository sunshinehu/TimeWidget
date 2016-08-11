package com.echo.timewidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.RemoteViews;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by huchenxi on 2016/8/11.
 */
public class TimeWidgetHandler extends Handler {

    public static final int UPDATE_VIEW=0x1;

    private WeakReference<Context> mContext;

    public TimeWidgetHandler(Context context) {
        super(Looper.getMainLooper());
        mContext=new WeakReference<Context>(context);
    }

    public void setContext(Context context){
        mContext=new WeakReference<Context>(context);
    }

    @Override
    public void handleMessage(Message msg) {
        if(mContext.get()==null){
            return;
        }
        super.handleMessage(msg);

        switch (msg.what){
            case UPDATE_VIEW:
                Context context=mContext.get();
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.time_widget_layout);

                updateTextView(remoteViews);

                Bitmap bitmap= (Bitmap) msg.obj;

                remoteViews.setBitmap(R.id.back, "setImageBitmap", bitmap);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext.get());
                ComponentName componentName = new ComponentName(mContext.get(), TimeWidgetProvider.class);

                appWidgetManager.updateAppWidget(componentName, remoteViews);

                break;
        }

    }



    private void updateTextView(RemoteViews remoteViews) {

        //Time 1 February 12
        //Time 2 Apr 12
        //Time 3 Apr 16
        //Time 4 Apr 24
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(2016, 1, 12);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(2016, 3, 12);

        int time1 = daysBetween(calendar1.getTime(), new Date());
        int time2 = daysBetween(calendar2.getTime(), new Date());
        remoteViews.setTextViewText(R.id.day1, time1 + 1 + "");
        remoteViews.setTextViewText(R.id.day2, " & " + (time2 + 1));
    }




    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate 较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    private int daysBetween(Date smdate, Date bdate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

}
