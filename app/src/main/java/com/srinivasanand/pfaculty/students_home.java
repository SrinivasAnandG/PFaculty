package com.srinivasanand.pfaculty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class students_home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_READ_PHONE_STATE = 99;
    AutoCompleteTextView text;
    ArrayList<String> list=new ArrayList<String>();
    ImageView imgButton;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    final FirebaseUser user=mAuth.getCurrentUser();
    final int RequestCameraPermissionID = 1001;

    private GoogleMap mMap;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Students");

        Intent i=new Intent(this,TrackLocation.class);
        i.putExtra("name","Srinivas");
        TrackLocation.s=user.getDisplayName();
        startService(i);





        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, TrackLocation.class);
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }
        PendingIntent pintent = PendingIntent
                .getService(this, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // Start service every hour
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                15*60*1000, pintent);




        final ProgressDialog pd = new ProgressDialog(students_home.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        String url = "https://pfaculty-53be5.firebaseio.com/users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase.setAndroidContext(students_home.this);
                Firebase reference = new Firebase("https://pfaculty-53be5.firebaseio.com/users");

                if(s.equals("null")) {
                    reference.child(user.getDisplayName()).child("password").setValue(user.getEmail());
                    //Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        JSONObject obj = new JSONObject(s);

                        if (!obj.has(user.getDisplayName())) {
                            reference.child(user.getDisplayName()).child("password").setValue(user.getEmail());
                            //Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                        } else {
                            //Toast.makeText(Register.this, "username already exists", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                pd.dismiss();
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );
                pd.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(students_home.this);
        rQueue.add(request);










        text=(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Faculty Department");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String ,String> map= (Map<String, String>) dataSnapshot.getValue();
                for(Map.Entry m:map.entrySet()){
                    //System.out.println(m.getKey()+" "+m.getValue());
                    list.add((String) m.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,list);

        text.setAdapter(adapter);
        text.setThreshold(1);
        imgButton =(ImageView)findViewById(R.id.search);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String val=text.getText().toString().trim();
                if(!val.equals(""))
                {
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Faculty Validate").child(val);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String fname= (String) dataSnapshot.getValue();
                            if(fname!=null&&fname.equals("true"))
                            {
                                Intent intent = new Intent(getBaseContext(), PointFaculty.class);
                                intent.putExtra("id",val);
                                startActivity(intent);

                            }
                            else
                            {
                                Toast.makeText(students_home.this,"User Does not exists...",Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    Toast.makeText(students_home.this,"Please search something...",Toast.LENGTH_SHORT).show();
                }

            }
        });













        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.students_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.search_faculty_profile) {

            startActivity(new Intent(students_home.this,SearchProfile.class));
            // Handle the camera action
        } else if (id == R.id.personal_chatting) {


            String url = "https://pfaculty-53be5.firebaseio.com/users.json";
            final ProgressDialog pd = new ProgressDialog(students_home.this);
            pd.setMessage("Loading...");
            pd.setCancelable(false);
            pd.show();
            Firebase.setAndroidContext(students_home.this);

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String s) {
                    if(s.equals("null")){
                        // Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                    }
                    else{
                        try {
                            JSONObject obj = new JSONObject(s);

                            if(!obj.has(user.getDisplayName())){
                                //Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                            }
                            else if(obj.getJSONObject(user.getDisplayName()).getString("password").equals(user.getEmail())){
                                UserDetails.username = user.getDisplayName();
                                UserDetails.password = user.getEmail();
                                startActivity(new Intent(students_home.this, Users.class));
                            }
                            else {
                                // Toast.makeText(Login.this, "incorrect password", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    pd.dismiss();
                }
            },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("" + volleyError);
                    pd.dismiss();
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(students_home.this);
            rQueue.add(request);



        }

        else if (id == R.id.Notifications) {

            startActivity(new Intent(students_home.this,NotificationsByAdmin.class));

        }

        else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,MainActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
