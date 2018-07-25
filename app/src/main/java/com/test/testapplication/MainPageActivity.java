package com.test.testapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String token;
    private MainFragment mainFragment;
    private FragmentTransaction fragmentTransaction;
    private Fragment currentFragment;
    private ImageView imageView;
    private TextView nameTextView, roleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        token = getIntent().getStringExtra("token"); //receiving token from login activity
        initializeVariables();
        openingMainFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        new GetUserDataTask().execute(); //starting getting user data

    }

    private void openingMainFragment() {
        mainFragment = new MainFragment();
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment, mainFragment);
        fragmentTransaction.commit();
        currentFragment = mainFragment; //each fragment is showing now
    }

    private void initializeVariables() {
        imageView = findViewById(R.id.imageView);
        nameTextView = findViewById(R.id.nameTextView);
        roleTextView = findViewById(R.id.roleTextView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fragmentTransaction = getFragmentManager().beginTransaction();
        switch (id) {
            case R.id.nav_main:{
                if (mainFragment != currentFragment) { //if mainFragment = currentFragment changes won't be needed
                    //but if we will add functions to our app, this code will be useful
                    fragmentTransaction.remove(currentFragment);
                    fragmentTransaction.add(R.id.fragment, mainFragment);
                }
                break;
            }
            case R.id.nav_exit:{
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finishActivity(0);
             break;

            }
        }

        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public class GetUserDataTask extends AsyncTask<Void, Void, Void> {
        private String firstname;
        private String lastname;
        private String photoUrl;
        private String role;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://somethingurl.com/users/me?token=" + token).openConnection();
                //it is not right url
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                //json parsing
                jsonParse(builder);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private void jsonParse(StringBuilder builder) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(builder.toString(), JsonObject.class);
            firstname = jsonObject.get("firstname").getAsString();
            lastname = jsonObject.get("lastname").getAsString();
            photoUrl = jsonObject.get("avatar").getAsString();
            role = jsonObject.get("role").getAsString();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            nameTextView.setText(firstname + " " + lastname);
            roleTextView.setText(role);
            if (!photoUrl.isEmpty()) new DownloadPhotoTask().execute(photoUrl);
        }
    }

    public class DownloadPhotoTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                //strings[0] is url of avatar
                File file = new File(getFilesDir(), "avatar.png");
                if (file.exists()) file.delete();
                FileWriter writer = new FileWriter(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null){
                    writer.write(line); //downloading image to file avatar.png
                    writer.flush();
                }
                writer.close();
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            File file = new File(getFilesDir(), "avatar.png");
            imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }
}
