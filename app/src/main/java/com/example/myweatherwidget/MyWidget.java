package com.example.myweatherwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyWidget extends AppWidgetProvider {

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget);
        // Get The SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.getDefault());
        views.setTextViewText(R.id.datetime, sdf.format(new Date()));

        String city = preferences.getString("city", "");
        String state = preferences.getString("state", "");
        views.setTextViewText(R.id.city, city+", "+state);

        String temperature = preferences.getString("temperature", "");
        views.setTextViewText(R.id.temperature, temperature);

        String weatherIcon = preferences.getString("icon", "");
        Picasso.get().load(weatherIcon).into(views, R.id.icon, new int[] { appWidgetId });

        //-------------------REFRESH WIDGET----------------------------
        Intent intent = new Intent(context, MyWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        views.setOnClickPendingIntent(R.id.refreshBTN, pendingIntent);

         //Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }



    //The onUpdate() method is called when the widget is first added to the home screen and whenever it needs to be updated.
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Toast.makeText(context, "Widget has been updated! ", Toast.LENGTH_SHORT).show();
        }
    }

    //The onReceive() method checks for the AppWidgetManager.ACTION_APPWIDGET_UPDATE action and updates the widget if it's received.
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
                }
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

