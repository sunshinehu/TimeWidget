package com.echo.timewidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.google.gson.Gson;

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
import java.util.Calendar;

/**
 * Created by huchenxi on 2016/3/31.
 */
public class TimeWidgetProvider extends AppWidgetProvider {

    TimeWidgetHandler mHandler;


    public TimeWidgetProvider() {
        super();
    }

    public static Bitmap GetLocalOrNetBitmap(String url) {
        Log.d("download", url);
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        } catch (Exception e) {
            Log.d("download", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("life", "receive");

    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (mHandler == null) {
            mHandler = new TimeWidgetHandler(context);
        } else {
            mHandler.setContext(context);
        }

        Log.d("life", "update");

        final SharedPreferences sharedPreferences = context.getSharedPreferences("echo", Context.MODE_PRIVATE);
        final int day = sharedPreferences.getInt("date", -1);
        final int current = Calendar.getInstance().get(Calendar.DATE);


        new Thread() {

            @Override
            public void run() {
                super.run();
                if (current != day) {
                    Bitmap bkg = null;
                    Log.d("Download", "start");
                    try {
                        String url = parseUrl();
                        bkg = GetLocalOrNetBitmap(url);
                    } catch (Exception e) {

                    }
                    if (bkg != null) {
                        Log.d("bkg", "not null");
                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/wallpaper");
                        File newFile = new File(Environment.getExternalStorageDirectory().getPath() + "/newwallpaper");

                        boolean getBitmapFlag = false;

                        try {
                            OutputStream os = new FileOutputStream(newFile);
                            bkg.compress(Bitmap.CompressFormat.JPEG, 100, os);
                            os.flush();
                            os.close();
                            getBitmapFlag = true;
                        } catch (FileNotFoundException e) {
                            Log.d("bkg", e.getMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.d("bkg", e.getMessage());
                            e.printStackTrace();
                        }

                        if (getBitmapFlag) {
                            Log.d("GETBitmap", "ok");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("date", current);
                            editor.apply();

                            if (newFile.exists()) {
                                if (file.exists()) {
                                    file.delete();
                                }
                                newFile.renameTo(file);
                            }
                        }
                    } else {
                        Log.d("bkg", "null");
                    }

                }
                Bitmap bitmap = doRender(context);
                if (bitmap != null) {
                    Log.d("bitmap", "not null");
                    Message msg = mHandler.obtainMessage();
                    msg.obj = bitmap;
                    msg.what = TimeWidgetHandler.UPDATE_VIEW;
                    msg.sendToTarget();
                } else {
                    Log.d("bitmap", "null");
                }
            }
        }.start();

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
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

    private String parseUrl() throws Exception {
        InputStream is = new BufferedInputStream(new URL("http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1").openStream(), 1024);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        while ((len = is.read(data)) != -1) {
            outStream.write(data, 0, len);
        }
        is.close();
        String content = new String(outStream.toByteArray());//通过out.Stream.toByteArray获取到写的数据
        Log.d("test", content);
        BingInfo bingInfo = new Gson().fromJson(content, BingInfo.class);
        if (bingInfo != null && bingInfo.getImages() != null && bingInfo.getImages().size() > 0) {
            return "http://cn.bing.com/" + bingInfo.getImages().get(0).getUrl();
        }
        return null;
    }

    private Bitmap doRender(Context mContext) {


        Bitmap bkg = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/wallpaper");
        if (bkg != null) {
            bkg = bkg.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            bkg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sun_flower);
            bkg = bkg.copy(Bitmap.Config.ARGB_8888, true);
        }

        if (bkg == null) {
            Log.d("updateview", "empty");
            return null;
        }

        Canvas canvas = new Canvas(bkg);

        canvas.translate(0, 0);
        canvas.drawBitmap(bkg, 0, 0, null);

        RenderScript rs = RenderScript.create(mContext);

        Allocation overlayAlloc = Allocation.createFromBitmap(rs, bkg);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());

        blur.setInput(overlayAlloc);

        blur.setRadius(15);

        blur.forEach(overlayAlloc);

        overlayAlloc.copyTo(bkg);

        rs.destroy();

        return bkg;

    }


}
