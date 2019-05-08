package com.example.geeksreads;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import CustomFunctions.APIs;
import CustomFunctions.UserSessionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class NotificationService extends Service {

    private final Context context;

    public NotificationService() {
        this.context = this;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notify";
            String description = "test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NotificationsService", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    void showPushNotification(){
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", UserSessionManager.getUserToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String UrlService = APIs.API_GET_USER_NOTIFICATIONS;

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        GetNotificationsList performBackgroundTask = new GetNotificationsList();
                        performBackgroundTask.execute(UrlService, jsonObject.toString());
                    }
                });
            }
        };
        timer.schedule(task, 0, 10000); //it executes this every 1000ms


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showPushNotification();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        this.showPushNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * A Private class that extend Async Task to connect to server in background.
     * It get user Notifications lists.
     */
    @SuppressLint("StaticFieldLeak")
    private class GetNotificationsList extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        boolean TaskSuccess;

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";

            try {
                //Create a URL object holding our url
                URL url = new URL(UrlString);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestProperty("content-type", "application/json");

                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));

                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();

                Log.d("ResponseCode: " , String.valueOf(http.getResponseCode()) );
                if (String.valueOf(http.getResponseCode()).equals("200")) {
                    /* A Stream object to get the returned data from API Call */
                    InputStream ips = http.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                    reader.close();
                    ips.close();
                    TaskSuccess = true;
                }
                else {
                    TaskSuccess = false;
                    InputStream es = http.getErrorStream();
                    BufferedReader ereader = new BufferedReader(new InputStreamReader(es, StandardCharsets.ISO_8859_1));
                    String eline;
                    while ((eline = ereader.readLine()) != null) {
                        result += eline;
                    }
                    ereader.close();
                    es.close();
                }
                http.disconnect();
                return result;

            } catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            //progress.setVisibility(View.GONE);

            if (result == null) {
                return;
            }
            try {
                Log.d("NotificationResult", result);

                if (TaskSuccess) {
                    JSONArray Notifications = new JSONArray(result);
                    Log.d("length", String.valueOf(Notifications.length()));
                    for (int i = 0; i < Notifications.length(); i++)
                    {
                        try{
                            JSONObject CurrentNotification=Notifications.getJSONObject(i);
                            Log.d("SEEN", String.valueOf(CurrentNotification.getBoolean("Seen")));
                            String notificationBody;
                            String NotificationId=CurrentNotification.getString("NotificationId");

                            if (CurrentNotification.getString("NotificationType").equals("ReviewLike")) {
                                notificationBody=CurrentNotification.getString("MakerName") + " Liked you review on "
                                        + CurrentNotification.getString("BookName");
                            } else if (CurrentNotification.getString("NotificationType").equals("Comment")) {
                                notificationBody=CurrentNotification.getString("MakerName") + " commented on review in "
                                        + CurrentNotification.getString("BookName");
                            } else {
                                notificationBody=CurrentNotification.getString("MakerName") + " Started Following You";
                            }

                        if (!CurrentNotification.getBoolean("Seen")) {

                            createNotificationChannel();
                            Intent intent = new Intent(context, NotificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                            NotificationCompat.Builder builder;

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                builder = new NotificationCompat.Builder(context, "NotificationsService")
                                        .setSmallIcon(R.drawable.ic_book)
                                        .setContentTitle("GeeksReads")
                                        .setContentText(notificationBody)
                                        .setContentIntent(pendingIntent)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setAutoCancel(true)
                                        .setDefaults(Notification.DEFAULT_SOUND)
                                        .setDefaults(Notification.DEFAULT_VIBRATE);
                            } else {
                                builder = new NotificationCompat.Builder(context);
                            }
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.notify(1, builder.build());


                            final JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("NotificationId", NotificationId);
                                jsonObject.put("token", UserSessionManager.getUserToken());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            final String UrlService = APIs.API_MAKE_NOTIFICATION_SEEN;

                            Log.d("PRINT" ,  jsonObject.toString() );
                            MarkSeen seen = new MarkSeen();
                            seen.execute(UrlService, jsonObject.toString());
                            break;
                        }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class MarkSeen extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "POST";
        boolean TaskSuccess;

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";

            try {
                //Create a URL object holding our url
                URL url = new URL(UrlString);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestProperty("content-type", "application/json");

                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));

                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();

                Log.d("ResponseCode: " , String.valueOf(http.getResponseCode()) );
                if (String.valueOf(http.getResponseCode()).equals("200")) {
                    /* A Stream object to get the returned data from API Call */
                    InputStream ips = http.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                    reader.close();
                    ips.close();
                    TaskSuccess = true;
                }
                else {
                    TaskSuccess = false;
                    InputStream es = http.getErrorStream();
                    BufferedReader ereader = new BufferedReader(new InputStreamReader(es, StandardCharsets.ISO_8859_1));
                    String eline;
                    while ((eline = ereader.readLine()) != null) {
                        result += eline;
                    }
                    ereader.close();
                    es.close();
                }
                http.disconnect();
                return result;

            } catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            //progress.setVisibility(View.GONE);

            if (result == null) {
                return;
            }
            try {

                JSONObject Message = new JSONObject(result);
                Log.d("NotificationResult", Message.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}