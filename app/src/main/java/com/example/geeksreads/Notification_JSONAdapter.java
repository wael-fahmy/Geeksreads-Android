package com.example.geeksreads;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;


public class Notification_JSONAdapter extends BaseAdapter {

    private final Context context;
    private JSONArray data;

    Notification_JSONAdapter(Context context, JSONArray data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public Object getItem(int i) {

        try {
            return data.getJSONObject(i);
        } catch (JSONException jse) {
            jse.printStackTrace();
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = Objects.requireNonNull(inflater).inflate(R.layout.notification_template, viewGroup, false);
        try {

            String body = data.getJSONObject(i).getString("body");
            if (!data.getJSONObject(i).getBoolean("seen")) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notify = new Notification.Builder(context.getApplicationContext()).setContentTitle("GeeksRead").setContentText(body).setContentTitle("GeeksReads").setSmallIcon(R.drawable.ic_book).build();
                notify.defaults |= Notification.DEFAULT_SOUND;
                notify.defaults |= Notification.DEFAULT_VIBRATE;
                notify.flags |= Notification.FLAG_AUTO_CANCEL;
                Objects.requireNonNull(notificationManager).notify(0, notify);
            }
            TextView notificationContent = itemView.findViewById(R.id.NotificationContent);
            notificationContent.setText(body);

            TextView notificationDate = itemView.findViewById(R.id.NotificationDate);
            notificationDate.setText(data.getJSONObject(i).getString("the"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemView;
    }
}
