package com.example.geeksreads;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.Objects;

import CustomFunctions.APIs;
import CustomFunctions.UserSessionManager;


public class OtherProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static String aForTestUserName = "";
    public static String aForTestUserPic = "";

    ImageView OtherUserPhoto;
    Context mContext;
    TextView UserName;
    TextView BooksCount;
    ListView BookShelf;
    Button FollowButton;

    /* SideBar Views */
    ImageView userPhoto;
    TextView userName;
    TextView followersCount;
    TextView booksCount;
    MenuItem FollowItem;
    MenuItem BookItem;
    View rootView;

    /**
     * onCreate: Instantiate the OtherUSerProfile with data through sending an HTTP request
     * with the UserId and receiving the response in its format.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_profile);
        mContext = this;

        /* ToolBar and SideBar Setups */
        Toolbar myToolbar = findViewById(R.id.toolbar);
        rootView=findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /* Getting All views by id from SideBar Layout */
        Menu menu = navigationView.getMenu();
        MenuItem itemFollower = menu.findItem(R.id.Followers);
        followersCount = (TextView) itemFollower.getActionView();
        FollowItem = menu.findItem(R.id.Followers);
        followersCount.setTextColor(getResources().getColor(R.color.white));
        MenuItem itemBook = menu.findItem(R.id.MyBooks);
        booksCount = (TextView) itemBook.getActionView();
        booksCount.setTextColor(getResources().getColor(R.color.white));
        BookItem = menu.findItem(R.id.MyBooks);
        /* Get Header Items */
        View mHeader = navigationView.getHeaderView(0);
        userName = mHeader.findViewById(R.id.UserNameTxt);
        userPhoto = mHeader.findViewById(R.id.UserPhoto);
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(OtherProfileActivity.this, Profile.class);
                startActivity(mIntent);
            }
        });
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(OtherProfileActivity.this, Profile.class);
                startActivity(mIntent);
            }
        });

        JSONObject jsonUserDetails = new JSONObject();
        try {
            jsonUserDetails.put("token", UserSessionManager.getUserToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String UrlSideBar = APIs.API_GET_USER_INFO;//"http://geeksreads.000webhostapp.com/Fatema/SideBar.php";
        GetSideBarDetails getSideBarDetails = new GetSideBarDetails();
        getSideBarDetails.execute(UrlSideBar, jsonUserDetails.toString());
        ///////////////////////////////////////////////////////
        OtherUserPhoto = findViewById(R.id.UserProfilePhoto);
        UserName = findViewById(R.id.OtherUserName);
        BooksCount = findViewById(R.id.OtherNumberBooks);
        BookShelf = findViewById(R.id.OtherUserBookList);
        FollowButton = findViewById(R.id.FollowButton);

        JSONObject JSON = new JSONObject();
        final JSONObject jsonObject = new JSONObject();

        try {

            JSON.put("token", UserSessionManager.getUserToken());
            JSON.put("UserId", getIntent().getStringExtra("FollowId"));

            jsonObject.put("token", UserSessionManager.getUserToken());
            jsonObject.put("UserId", getIntent().getStringExtra("FollowId"));
            // jsonObject.put("ShelfName","Read");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String UrlService = APIs.API_GET_USER_BY_ID;
        OtherProfileActivity.GetOtherProfileDetails MyProfile = new OtherProfileActivity.GetOtherProfileDetails();
        MyProfile.execute(UrlService, JSON.toString());

        /////////////////////////////////////////////////////
/*
        final String SecondUrlService = APIs.API_GET_USER_READ_DETAILS;
        OtherProfileActivity.GetOtherProfileBooks TheBooks = new OtherProfileActivity.GetOtherProfileBooks();
        TheBooks.execute(SecondUrlService, jsonObject.toString());
        /////////////////////////////////////////////////////
*/
        OtherProfileActivity.UpdateBookShelfCount updateReadShelf = new OtherProfileActivity.UpdateBookShelfCount(UserSessionManager.getUserToken());

        /* URL For Get Shelves Count API */
        String GetShelvesCountUrl = APIs.API_GET_SHELVES_COUNT;

        updateReadShelf.execute(GetShelvesCountUrl);

        final String FollowRequest = APIs.API_FOLLOW_USER;
        final String UnFollowRequest = APIs.API_UN_FOLLOW_USER;

        final JSONObject FollowJson = new JSONObject();
        try {

            FollowJson.put("token", UserSessionManager.getUserToken());
            FollowJson.put("myuserid", UserSessionManager.getUserID());
            FollowJson.put("userId_tobefollowed", getIntent().getStringExtra("FollowId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        FollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FollowButton.getText().toString().equals("Un-Follow")) {

                    OtherProfileActivity.UnFollowUser TestObject = new OtherProfileActivity.UnFollowUser();
                    TestObject.execute(UnFollowRequest, FollowJson.toString());

                } else {

                    OtherProfileActivity.FollowUser TestObject = new OtherProfileActivity.FollowUser();
                    TestObject.execute(FollowRequest, FollowJson.toString());

                }
            }
        });


    }

    /**
     * @param menu : Menu object in the toolbar
     * @return super.onCreateOptionsMenu(menu)
     *  Overrided Function to create the toolbar and decide what to do when click it's menu items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setMaxWidth(750);
        searchView.setQueryHint("Search books");
        searchView.setBackgroundColor(getResources().getColor(R.color.white));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                rootView.requestFocus();
                Intent intent = new Intent(OtherProfileActivity.this,SearchHandlerActivity.class);
                intent.putExtra("CallingActivity","OtherProfileActivity");
                startActivity(intent);

            }
        });

        MenuItem item1 = menu.findItem(R.id.NotificationButton);
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent mIntent = new Intent(OtherProfileActivity.this, NotificationActivity.class);
                startActivity(mIntent);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class UnFollowUser extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "POST";
        boolean TaskSucc = false;
        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setTitle("Connection Status");
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
                http.setRequestProperty("x-auth-token", UserSessionManager.getUserToken());

                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));

                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();
                switch (String.valueOf(http.getResponseCode())) {
                    case "200":
                        /* A Stream object to get the returned data from API Call */
                        InputStream ips = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                        String line = "";
                        //boolean started = false;
                        while ((line = reader.readLine()) != null) {
                            //   if ()
                            result += line;
                            Log.d("AMR", "Result: " + result);
                        }
                        reader.close();
                        ips.close();
                        TaskSucc = true;
                        break;
                    case "400":
                        result = "{\"ReturnMsg\":\"Invalid email or password.\"}";
                        break;
                    case "401":
                        result = "{\"ReturnMsg\":\"Your account has not been verified.\"}";
                        break;
                    default:
                        break;
                }


                http.disconnect();
                Log.d("AMR", result);
                return result;

            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        /**
         * onPostExecute: Takes the string result and treates it as a json object
         * to set data of:
         * -Follow Button
         *
         * @param result : The result containing all the passed data from backend.
         */
        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
                dialog.setMessage("Done");
                //dialog.show();

                //  Log.i("AMR","Result: "+result);
            Log.d("AMR", "FollowUser:" + String.valueOf(TaskSucc));
                if (TaskSucc)
                    FollowButton.setText("Follow");
                else {
                    FollowButton.setText("Un-Follow");
                    Toast.makeText(getApplicationContext(), "Unable to connect to Server!", Toast.LENGTH_SHORT).show();
                }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Overrided Function to decide what to do ok pressing "Back" key.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class FollowUser extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "POST";
        boolean TaskSucc = false;
        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setTitle("Connection Status");
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
                http.setRequestProperty("x-auth-token", UserSessionManager.getUserToken());

                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();
                Log.d("AMR", "Follow Response:" + String.valueOf(http.getResponseCode()));
                switch (String.valueOf(http.getResponseCode())) {
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
                        TaskSucc = true;
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
            return result;
        }

        /**
         * onPostExecute: Takes the string result and treates it as a json object
         * to set data of:
         * -Follow Button
         *
         * @param result : The result containing all the passed data from backend.
         */
        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
                dialog.setMessage("Done");
                //dialog.show();

                if (TaskSucc)
                    FollowButton.setText("Un-Follow");
                else {
                    FollowButton.setText("Follow");
                    Toast.makeText(getApplicationContext(), "Unable to connect to Server!", Toast.LENGTH_SHORT).show();
                }
        }

    }

    /**
     * @param menuItem : item in menu of the toolbar
     * @return boolean "true"
     * Overrided Function to create sidebar and decide what to on clicking on it's menu items.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.Home) {

            Intent myIntent = new Intent(OtherProfileActivity.this, FeedActivity.class);
            startActivity(myIntent);

        } else if (id == R.id.Followers) {
            Intent myIntent = new Intent(OtherProfileActivity.this, FollowActivity.class);
            startActivity(myIntent);

        } else if (id == R.id.MyBooks) {
            Intent myIntent = new Intent(OtherProfileActivity.this, MyBooksShelvesActivity.class);
            startActivity(myIntent);
        }
        else if (id == R.id.Signout) {
            Intent myIntent = new Intent(OtherProfileActivity.this, SignOutActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Class GetOtherUserPicture:
     * Gets the image of the user from the JsonObject and translates it into a bitMap to be used by the ImageView.
     */
    @SuppressLint("StaticFieldLeak")
    private class GetOtherUserPicture extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String photoUrl = params[0];
                URL url = new URL(photoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                // Log.d(TAG,e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            OtherUserPhoto.setImageBitmap(result);

        }
    }

    /**
     * Class that get the data from host and Add it to its views.
     * The Parameters are host Url and toSend Data.
     */
    @SuppressLint("StaticFieldLeak")
    public class GetOtherProfileDetails extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "POST";

        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setTitle("Connection Status");
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
                http.setRequestProperty("x-auth-token", UserSessionManager.getUserToken());

                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));

                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();
                switch (String.valueOf(http.getResponseCode())) {
                    case "200":
                        /* A Stream object to get the returned data from API Call */
                        InputStream ips = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                        String line = "";
                        //boolean started = false;
                        while ((line = reader.readLine()) != null) {
                            //   if ()
                            result += line;
                        }
                        reader.close();
                        ips.close();
                        break;
                    case "400":
                        result = "{\"ReturnMsg\":\"Invalid email or password.\"}";
                        break;
                    case "401":
                        result = "{\"ReturnMsg\":\"Your account has not been verified.\"}";
                        break;
                    default:
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

        /**
         * onPostExecute: Takes the string result and treates it as a json object
         * to set data of:
         *  -User Name
         *  -Books Count
         *  -Profile Picture
         *
         * @param result : The result containing all the passed data from backend.
         */
        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                dialog.setMessage("Done");
                //dialog.show();
                Log.d("AMR", "OtherProfile:" + result);
                JSONObject jsonObject = new JSONObject(result);
                OtherProfileActivity.GetOtherUserPicture Pic = new OtherProfileActivity.GetOtherUserPicture();
                Pic.execute(jsonObject.getString("Photo"));
                aForTestUserPic = jsonObject.getString("Photo");
                UserName.setText(jsonObject.getString("UserName"));
                aForTestUserName = UserName.getText().toString();

                if (jsonObject.getString("IsFollowing").equals("true"))
                    FollowButton.setText("Un-Follow");
                else
                    FollowButton.setText("Follow");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    /////////////////////////////////////////////////

    /**
     * A Private class that extend Async Task to connect to server in background.
     * It get the Want to Read book lists.
     */
    @SuppressLint("StaticFieldLeak")
    private class GetOtherProfileBooks extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "POST";
        //public static final int READ_TIMEOUT = 3000;
        //public static final int CONNECTION_TIMEOUT = 3000;
        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setTitle("Connection Status");
            //progress.setVisibility(View.VISIBLE);
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
                http.setRequestProperty("x-auth-token", UserSessionManager.getUserToken());

                /* A Stream object to hold the sent data to API Call */
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                writer.write(JSONString);
                writer.flush();
                writer.close();
                ops.close();

                switch (String.valueOf(http.getResponseCode())) {
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
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            //progress.setVisibility(View.GONE);
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                dialog.setMessage(result);
                //dialog.show();
                Log.d("AMR", "OtherBooks:" + result);
                ListView wantToReadBookList = findViewById(R.id.OtherUserBookList);
                final BookList_JSONAdapter bookListJsonAdapter = new BookList_JSONAdapter(mContext, new JSONArray(result));
                wantToReadBookList.setAdapter(bookListJsonAdapter);
                wantToReadBookList.deferNotifyDataSetChanged();
                wantToReadBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        Intent intent = new Intent(OtherProfileActivity.this, BookActivity.class);
                        intent.putExtra("BookID", bookListJsonAdapter.getBookID(position));
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    ///////////////////////////////////////////////////
    /**
     * Class that get sidebar profile pic. from server
     */
    @SuppressLint("StaticFieldLeak")
    private class GetUserPicture extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String photoUrl = params[0];
                URL url = new URL(photoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                // Log.d(TAG,e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            userPhoto.setImageBitmap(result);

        }
    }
    /**
     * Class that get sidebar Data from server
     */
    @SuppressLint("StaticFieldLeak")
    private class GetSideBarDetails extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "POST";
        //public static final int READ_TIMEOUT = 3000;
        //public static final int CONNECTION_TIMEOUT = 3000;
        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setTitle("Connection Status");
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
                writer.write(JSONString);
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
            return result;
        }



        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {

                JSONObject jsonObject = new JSONObject(result);
                FollowItem.setTitle("Followers   " + jsonObject.getString("NoOfFollowers"));
                // BookItem.setTitle("My Books   0" );
                userName.setText(jsonObject.getString("UserName"));
                GetUserPicture Pic = new GetUserPicture();
                Pic.execute(jsonObject.getString("Photo"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Class that get the data from host and Add it to its views.
     * The Parameters are host Url and toSend Data.
     */
    @SuppressLint("StaticFieldLeak")
    public class UpdateBookShelfCount extends AsyncTask<String, String, String> {
        static final String REQUEST_METHOD = "POST";
        String userToken;

        /**
         * Constructor for UpdateBookSHelfCountClass
         *
         * @param userToken Parameter to send user token
         */
        public UpdateBookShelfCount(String userToken) {
            this.userToken = userToken;
        }

        /**
         * Function to be done before Executing, it starts Loading Animation
         */
        @Override
        protected void onPreExecute() {
           /*
           Do Nothing
            */
        }

        /**
         * Function that executes the logic needed in the background thread
         */
        @Override
        protected String doInBackground(String... params) {
            String UrlString = params[0];

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
                JSONObject newJson = new JSONObject();
                newJson.put("token", UserSessionManager.getUserToken());
                newJson.put("UserId", getIntent().getStringExtra("FollowId"));
                writer.write(newJson.toString());
                writer.flush();
                writer.close();
                ops.close();

                /* A Stream object to get the returned data from API Call */
                switch (String.valueOf(http.getResponseCode())) {
                    case "200":
                        /* A Stream object to get the returned data from API Call */
                        InputStream ips = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            result += line;
                        }
                        reader.close();
                        ips.close();
                        break;
                    case "400":
                        result = "{\"ReturnMsg\":\"Error Occurred.\"}";
                        break;
                    default:
                        break;
                }
                http.disconnect();
                return result;

            }
            /* Handling Exceptions */ catch (MalformedURLException e) {
                result = e.getMessage();
            } catch (IOException e) {
                result = e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String result) {

            if (result == null) {
                Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                /* Creating a JSON Object to parse the data in */
                final JSONObject jsonObject = new JSONObject(result);
                String readCount = jsonObject.getString("NoOfRead");
                String wantToReadCount = jsonObject.getString("NoOfWantToRead");
                String readingCount = jsonObject.getString("NoOfReading");
                String FullCount = Integer.toString(Integer.parseInt(readCount) + Integer.parseInt(wantToReadCount) + Integer.parseInt(readingCount));
                BooksCount.setText(FullCount + " " + "Books");
                Log.d("AMR", "BooksCount: " + FullCount);
            }
            /* Catching Exceptions */ catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Error in loading shelves data", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

