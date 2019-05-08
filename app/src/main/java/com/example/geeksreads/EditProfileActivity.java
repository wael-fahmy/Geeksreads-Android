package com.example.geeksreads;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import CustomFunctions.UserSessionManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geeksreads.views.LoadingView;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import CustomFunctions.APIs;

public class EditProfileActivity extends AppCompatActivity {

    /**
     * Global Public Static Variables used for Testing
     */
    public static String sForTest;

    /**
     * Global Public Static Variables used for Testing Email Verification Step
     */
    public static String sForTest_OriginalEmail;

    /**
     * Global Variables to Store Context of this Activity itself
     */
    View rootView;
    private Context mContext;
    SwipeRefreshLayout mSwipeRefreshLayout;

    EditText userNameTxt;
    EditText birthDateTxt;
    Button saveChange;
    Button chooseNewPic;
    Button changePassword;

    LoadingView Loading;
    /* Function to check the validity of the input date string with a format of DD/MM/YYYY and before 5 Years*/
    public static boolean isThisDateValid(String dateToValidate)
    {
            try
            {
                System.out.println("START");
                if (dateToValidate.split("[/]").length != 3)
                {
                    System.out.println("ERROR //");
                    //System.out.println(dateToValidate.split("[/]")[0]);
                    //System.out.println(dateToValidate.split("[/]")[1]);
                    return false;
                }
                else
                {
                    String Date = dateToValidate;

                    String Day = Date.substring(0, Date.indexOf('/'));
                    Date = Date.substring(Date.indexOf('/')+1);

                    String Month = Date.substring(0, Date.indexOf('/'));
                    Date = Date.substring(Date.indexOf('/')+1);

                    String Year = Date;

                    System.out.println("Day:"+Day);
                    System.out.println("Month:"+Month);
                    System.out.println("Year:"+Year);
                    if (Month.length() < 1)
                    {
                        System.out.println("Month<1");
                        return false;
                    }
                    else if (Day.length() < 1)
                    {
                        System.out.println("Day<1");
                        return false;
                    }
                    else if (Year.length() < 4)
                    {
                        System.out.println("Year<4");
                        return false;
                    }
                    else
                    {
                        try
                        {
                            int iDay =Integer.parseInt(Day);
                            int iMonth = Integer.parseInt(Month);
                            int iYear = Integer.parseInt(Year);

                            if (iDay <= 0 || iDay > 31)
                            {
                                return false;
                            }
                            else if (iMonth <= 0 || iMonth > 12)
                            {
                                return false;
                            }
                            else if (iYear <= 0)
                            {
                                return false;
                            }


                        }catch(Exception ex2)
                        {
                            System.out.println("EXCEPTION");
                            return false;
                        }
                    }
                }


            }catch (Exception ex)
            {
                return false;
            }

        //Log.w("DATTEEEE", dateToValidate);
        SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
        sdf.setLenient(false);
        //Log.w("DATTEEEE", dateToValidate);
        try {
            // if not valid, it will throw ParseException
            Date date = sdf.parse(dateToValidate);
            //System.out.println(dateToValidate + " Parsing OK!");
            // current date minus 5 years
            Calendar currentDateBefore5Years = Calendar.getInstance();
            currentDateBefore5Years.add(Calendar.YEAR, -5);
            //System.out.println(dateToValidate + " Subtracting OK!");
            //System.out.println("Original Date = " + dateToValidate + ", Subtracted Date = " + currentDateBefore5Years.getTime().toString());
            if (date.after(currentDateBefore5Years.getTime())) {
                //System.out.println("Original Date = " + dateToValidate + ", Subtracted Date = " + currentDateBefore5Years.getTime().toString() + ",  False");
                //Log.w("DATTEEEE", "FALSE");
                return false;
            } else {
                //Log.w("DATTEEEE", "TRUE");
                //System.out.println("Original Date = " + dateToValidate + ", Subtracted Date = " + currentDateBefore5Years.getTime().toString() + ",  True");
                return true;
            }

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }
    }

    enum editProfileValidationErrors
    {
        INVALID_USERNAME_LENGTH,
        INVALID_EMAIL,
        INVALID_BIRTH_DATE,
        NO_ERRORS
    }

    /* Function to check the validity of inputs to Edit User API */
    public static editProfileValidationErrors validateEditProfileData(String userNameStr, String emailStr, String birthDateStr)
    {
        /* If the user entered an invalid Username */
        if (userNameStr.length() < 3 || userNameStr.length() > 50) {
            return editProfileValidationErrors.INVALID_USERNAME_LENGTH;
        }
        /* If the user entered an invalid Email Address */
        else if (!emailStr.matches(".+[@].+[.].+")) {
            return editProfileValidationErrors.INVALID_EMAIL;
        }
        else if (!isThisDateValid(birthDateStr)) {
            return editProfileValidationErrors.INVALID_BIRTH_DATE;
        }
        /* If the user entered a valid username, email and password */
        else {
            return editProfileValidationErrors.NO_ERRORS;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        rootView=findViewById(R.id.toolbar);
        userNameTxt = findViewById(R.id.UserNameTxt);
        birthDateTxt = findViewById(R.id.BirthDate);
        saveChange = findViewById(R.id.SaveChangesBtn);
        chooseNewPic = findViewById(R.id.choosePhotoBtn);
        changePassword = findViewById(R.id.ChangePasswordBtn);


        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView allControls[] = {saveChange, changePassword, chooseNewPic, userNameTxt, birthDateTxt};
        Loading = new LoadingView(allControls, (FrameLayout)findViewById(R.id.progressBarHolder), (TextView)findViewById(R.id.ProgressName));
        mContext = this;
        /* URL For Get Current User Info API */
        final String urlService = APIs.API_GET_USER_INFO;

        /* Creating a new instance of Get Profile Data Class */
        GetProfileData getProfileData = new GetProfileData();
        final JSONObject getDataJson = new JSONObject();
        try {
            //Log.w("MahmoudTOKEN", UserSessionManager.getUserToken());
            getDataJson.put("token", UserSessionManager.getUserToken());
            //Log.w("Mahmoud___", getDataJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getProfileData.execute(urlService, getDataJson.toString());

        mSwipeRefreshLayout = findViewById(R.id.EditSwipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             *  Overrided Function to decide what to do when refreshing the layout.
             */
            @Override
            public void onRefresh() {

                GetProfileData getProfileData = new GetProfileData();
                getProfileData.execute(urlService, getDataJson.toString());
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Go to Next Activity Layout */
                Intent myIntent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                startActivity(myIntent);
            }
        });


        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userNameStr = userNameTxt.getText().toString();
                String birthDateStr = birthDateTxt.getText().toString();

                userNameTxt.setError(null);
                birthDateTxt.setError(null);

                editProfileValidationErrors editProfileValidationError = validateEditProfileData(userNameStr, "STUB@STUB.COM", birthDateStr);

                switch(editProfileValidationError)
                {
                    case INVALID_USERNAME_LENGTH:
                        userNameTxt.setError("Username length should be 3 characters minimum and 50 characters maximum!");
                        sForTest = "Username length should be 3 characters minimum and 50 characters maximum!";
                        break;
                    case INVALID_EMAIL:
                        sForTest = "Please enter a valid email!";
                        break;
                    case INVALID_BIRTH_DATE:
                        birthDateTxt.setError("Please enter a valid date!");
                        sForTest = "Please enter a valid date!";
                        break;
                    case NO_ERRORS:
                        JSONObject JSON = new JSONObject();
                        try {
                            String Date = birthDateStr;
                            String Month = Date.substring(0, Date.indexOf('/'));
                            Date = Date.substring(Date.indexOf('/')+1);
                            String Day = Date.substring(0, Date.indexOf('/'));
                            Date = Date.substring(Date.indexOf('/')+1);
                            String Year = Date;

                            if (Month.length() < 2)
                            {
                                Month = "0" + Month;
                            }
                            if (Day.length() < 2)
                            {
                                Day = "0" + Day;
                            }
                            Date = Year + "-" + Day+ "-" + Month+ "T00:00:00.000Z";
                            JSON.put("token", UserSessionManager.getUserToken());
                            JSON.put("NewUserName", userNameStr);
                            JSON.put("NewUserBirthDate", Date);
                            Log.w("DATEEEE", Date);
                            if (!newUserPhoto_Url.equals(""))
                            {
                                JSON.put("NewUserPhoto", newUserPhoto_Url);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        /* URL For Update Current User Data API */
                        String urlService = APIs.API_UPDATE_USER_INFO;

                        /* Creating a new instance of Sign up Class */
                        SaveProfileData saveProfileData = new SaveProfileData();
                        saveProfileData.execute(urlService, JSON.toString());
                        break;
                }
            }
        });



        chooseNewPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              PickImageFromGallery();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_menu, menu);
        MenuItem item = menu.findItem(R.id.menuSetting);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setMaxWidth(800);
        searchView.setQueryHint("Search books");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                rootView.requestFocus();
                Intent intent = new Intent(EditProfileActivity.this,SearchHandlerActivity.class);
                intent.putExtra("CallingActivity","EditProfileActivity");
                startActivity(intent);

            }
        });

        MenuItem item1 = menu.findItem(R.id.NotificationButton);
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent mIntent = new Intent(EditProfileActivity.this, NotificationActivity.class);
                startActivity(mIntent);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**********************************************************************************************/
    String newUserPhoto_B64 = "";
    String newUserPhoto_Url = "";
    private void PickImageFromGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    /* Function to encode from Bitmap into Base64 String */
    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100)
        {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                String encodedImage = encodeImage(selectedImage);
                newUserPhoto_B64 = encodedImage;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (!newUserPhoto_B64.equals(""))
            {
                JSONObject JSON = new JSONObject();
                try {
                    JSON.put("UserID", UserSessionManager.getUserID());
                    JSON.put("Photo", newUserPhoto_B64);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /* URL For Update Current User Data API */
                String urlService = APIs.API_UPLOAD_PHOTO;

                /* Creating a new instance of upload photo Class */
                UploadPhotoClass uploadPhotoObj = new UploadPhotoClass();
                uploadPhotoObj.execute(urlService, JSON.toString());
            }
        }
    }

    private Bitmap getBitmapFromBase64(String encodedImage)
    {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
    private void displayBitmap(ImageView userProfilePhoto,  Bitmap bitmap)
    {
        userProfilePhoto.destroyDrawingCache();
        userProfilePhoto.setDrawingCacheEnabled(false);
        userProfilePhoto.setWillNotCacheDrawing(true);
        userProfilePhoto.setImageBitmap(bitmap);
    }

    /**********************************************************************************************/


    /*  Class GetUserPicture:
     *      Gets user picture from the url and changes it into bitmap
     *      sets the imageView into the same bitmap
     */
    private class GetPicture extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String photoUrl = params[0];
                URL url = new URL(photoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDefaultUseCaches(false);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView userProfilePhoto = findViewById(R.id.UserProfilePhoto);
            userProfilePhoto.setImageBitmap(result);

        }
    }

    private class GetProfileData extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "POST";
        JSONObject mJSON = new JSONObject();

        @Override
        protected void onPreExecute() {
            Loading.Start("Loading profile data, Please wait...");
        }

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";
            //Log.w("Mahmoud12", JSONString);
            try {
                /* Create a URL object holding our url */
                URL url = new URL(UrlString);
                /* Create an HTTP Connection and adjust its options */
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestProperty("content-type", "application/json");

                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();
                //Log.w("Mahmoud12000", String.valueOf(http.getResponseCode()));
                switch (String.valueOf(http.getResponseCode()))
                {
                    case "200":
                        /* A Stream object to get the returned data from API Call */
                        InputStream ips = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                        String line = "";
                        //boolean started = false;
                        while ((line = reader.readLine()) != null) {
                            result += line;
                        }
                        reader.close();
                        ips.close();
                        //Log.w("Mahmoud13", result);
                        break;
                    default:
                        result = "{\"ReturnMsg\":\"An Error Occurred!\"}";
                        break;
                }

                http.disconnect();
                return result;
            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            //Log.w("Mahmoud14", result);
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                if (result.equals("{\"ReturnMsg\":\"An Error Occurred!\"}"))
                {
                    Toast.makeText(mContext,"An Error Occurred!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    /* Creating a JSON Object to parse the data in */
                    final JSONObject jsonObject = new JSONObject(result);

                    EditText userNameTxt = findViewById(R.id.UserNameTxt);
                    EditText birthDateTxt = findViewById(R.id.BirthDate);

                    userNameTxt.setText(jsonObject.getString("UserName"));
                    String bD = jsonObject.getString("UserBirthDate");
                    /* Remove Time */
                    bD = bD.substring(0, bD.indexOf('T'));
                    /* Take Year */
                    String Year = bD.substring(0, bD.indexOf('-'));
                    /* Remove From First until First Dash */
                    bD = bD.substring(bD.indexOf('-')+1);

                    /* Take Month */
                    String Month = bD.substring(0, bD.indexOf('-'));
                    /* Remove From First until Second Dash */
                    bD = bD.substring(bD.indexOf('-')+1);

                    /* Take Day */
                    String Day = bD;
                    String finalDate = Day + "/" + Month + "/" + Year;
                    //Log.w("FINALDATEEE", finalDate);
                    birthDateTxt.setText(finalDate);
                    newUserPhoto_Url = jsonObject.getString("Photo");
                    Log.w("PHOTO", newUserPhoto_Url);
                    if (!jsonObject.getString("Photo").equals(""))
                    {
                        GetPicture Pic = new GetPicture();
                        Pic.execute(jsonObject.getString("Photo"));
                    }
                    else
                    {
                        /* Do Nothing */
                    }
                }
            }
            /* Catching Exceptions */ catch (JSONException e) {
                e.printStackTrace();
            }
            Loading.Stop();
        }

    }

    private class SaveProfileData extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "POST";
        JSONObject mJSON = new JSONObject();

        @Override
        protected void onPreExecute() {
            Loading.Start("Saving your changes, Please wait...");
        }

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";

            try {
                /* Create a URL object holding our url */
                URL url = new URL(UrlString);
                /* Create an HTTP Connection and adjust its options */
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestProperty("content-type", "application/json");
                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                String data = JSONString;

                writer.write(data);
                writer.flush();
                writer.close();
                ops.close();
                Log.w("MORSY", data+UrlString);
                switch (String.valueOf(http.getResponseCode()))
                {
                    case "200":
                        result = "{\"ReturnMsg\":\"Your changes are saved successfully!\"}";
                        break;
                    default:
                        result = "{\"ReturnMsg\":\"An error occurred while saving your changes!\"}";
                        break;
                }
                http.disconnect();
                return result;
            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                /* Creating a JSON Object to parse the data in */
                final JSONObject jsonObject = new JSONObject(result);
                sForTest = jsonObject.getString("ReturnMsg");

                Toast.makeText(mContext, jsonObject.getString("ReturnMsg"), Toast.LENGTH_SHORT).show();

                finish();

            }
            /* Catching Exceptions */ catch (JSONException e) {
                e.printStackTrace();
            }
            Loading.Stop();
        }
    }

    private class UploadPhotoClass extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "POST";
        JSONObject mJSON = new JSONObject();

        @Override
        protected void onPreExecute() {
            Loading.Start("Uploading your photo, Please wait...");
        }

        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];
            String JSONString = params[1];
            String result = "";

            try {
                /* Create a URL object holding our url */
                URL url = new URL(UrlString);
                /* Create an HTTP Connection and adjust its options */
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod(REQUEST_METHOD);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestProperty("content-type", "application/json");
                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                String data = JSONString;

                writer.write(data);
                writer.flush();
                writer.close();
                ops.close();
                switch (String.valueOf(http.getResponseCode()))
                {
                    case "200":
                        /* A Stream object to get the returned data from API Call */
                        InputStream ips = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                        String line = "";
                        //boolean started = false;
                        while ((line = reader.readLine()) != null) {
                            result += line;
                        }
                        reader.close();
                        ips.close();
                        break;
                    default:
                        result = "{\"ReturnMsg\":\"An error occurred while uploading your photo!\"}";
                        break;
                }
                http.disconnect();
                return result;
            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                /* Creating a JSON Object to parse the data in */
                final JSONObject jsonObject = new JSONObject(result);
                sForTest = jsonObject.getString("ReturnMsg");

                Toast.makeText(mContext, jsonObject.getString("ReturnMsg"), Toast.LENGTH_SHORT).show();

                if (jsonObject.getString("ReturnMsg").contains("Successful"))
                {
                    newUserPhoto_Url = jsonObject.getString("PhotoUrl");
                    ImageView userProfilePhoto = findViewById(R.id.UserProfilePhoto);
                    displayBitmap(userProfilePhoto, getBitmapFromBase64(newUserPhoto_B64));

                    if (Profile.UserPhoto != null)
                    {
                        displayBitmap(Profile.UserPhoto, getBitmapFromBase64(newUserPhoto_B64));
                    }

                }

            }
            /* Catching Exceptions */ catch (JSONException e) {
                e.printStackTrace();
            }
            Loading.Stop();
        }
    }

}
