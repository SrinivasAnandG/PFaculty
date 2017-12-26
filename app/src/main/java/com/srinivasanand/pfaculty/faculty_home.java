package com.srinivasanand.pfaculty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
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
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class faculty_home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_READ_PHONE_STATE = 99;
    SurfaceView cameraPreview;
    BarcodeDetector barcodeDectector;
    CameraSource cameraSource;
    TextView txtResult;
    final int RequestCameraPermissionID = 1001;
    ImageView profilePic;
    TextView name,email;
    String currentStatus;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    final FirebaseUser user=mAuth.getCurrentUser();



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(faculty_home.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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
        setContentView(R.layout.activity_faculty_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Faculty");
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
        DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("Faculty Raw Location").child(user.getDisplayName()).child("Raw Location");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentStatus= (String) dataSnapshot.getValue();
                currentStatus.replace(";"," ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Current Location "+currentStatus, Snackbar.LENGTH_LONG)
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



        final ProgressDialog pd = new ProgressDialog(faculty_home.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        String url = "https://pfaculty-53be5.firebaseio.com/users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase.setAndroidContext(faculty_home.this);
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

        RequestQueue rQueue = Volley.newRequestQueue(faculty_home.this);
        rQueue.add(request);





        name=(TextView)findViewById(R.id.name);
        email=(TextView)findViewById(R.id.email);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Faculty Department").child(user.getDisplayName());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dept= (String) dataSnapshot.getValue();
                if(!dept.equals(null))
                {
                    name.setText(user.getDisplayName()+"("+dept+")");
                }
                else
                {
                    Toast.makeText(faculty_home.this,"Error occured, we are working on it. Try after sometime.",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(faculty_home.this,MainActivity.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        profilePic=(ImageView)findViewById(R.id.profilepic);
        Picasso.with(faculty_home.this)
                .load((String) user.getPhotoUrl().toString())
                .into(profilePic);

        email.setText(user.getEmail());





        txtResult = (TextView) findViewById(R.id.txtResult);
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);

        barcodeDectector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDectector)
                .setRequestedPreviewSize(600, 480)
                .build();
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    if (ActivityCompat.checkSelfPermission(faculty_home.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(faculty_home.this,
                                new String[]{android.Manifest.permission.CAMERA}, RequestCameraPermissionID);
                        return;
                    }
                    //progressBar.dismiss();
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                cameraSource.stop();

            }
        });

        barcodeDectector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                final String[] userId = new String[1];
                final String[] userEmail = new String[1];
                final int[] phoneNumber = new int[1];
                if (qrcodes.size() != 0) {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            cameraSource.stop();
                            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            userId[0] = mFirebaseUser.getDisplayName();
                            userEmail[0] = mFirebaseUser.getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Qr Validator");
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Map<String, String> p = (Map) dataSnapshot.getValue();
                                    int i = 0;

                                    for (Map.Entry<String, String> entry : p.entrySet()) {
                                        if (entry.getKey().equals(qrcodes.valueAt(0).displayValue)) {

                                            Date dNow = new Date( );
                                            SimpleDateFormat ft =
                                                    new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
                                            String time=(String)ft.format(dNow);
                                            DatabaseReference ref3=FirebaseDatabase.getInstance().getReference().child("Faculty Raw Location").child(userId[0]).child("Last Updated Time");
                                            ref3.setValue(time);


                                            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Faculty Raw Location").child(userId[0]).child("Raw Location");
                                            ref2.setValue(qrcodes.valueAt(0).displayValue);
                                            Toast.makeText(faculty_home.this, "Succesfully Updated", Toast.LENGTH_LONG).show();
                                            i++;
                                            break;

                                        }
                                    }
                                    if (i == 0) {
                                        Toast.makeText(faculty_home.this, "Invalid Qr.. Scan again.", Toast.LENGTH_LONG).show();
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    Toast.makeText(faculty_home.this,"check your internet connection.",Toast.LENGTH_LONG).show();

                                }
                            });



                        }
                    });
                }

            }
        });

        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();
        DatabaseReference refn=FirebaseDatabase.getInstance().getReference().child("Device Id").child(user.getDisplayName());
        refn.setValue(uid);







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
        getMenuInflater().inflate(R.menu.faculty_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Notify) {
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Admin");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String res= (String) dataSnapshot.getValue();
                    if(res!=null)
                    {
                        if(res.equals(user.getDisplayName()))
                        {
                            startActivity(new Intent(faculty_home.this,send_notification.class));
                        }
                        else
                        {
                            Toast.makeText(faculty_home.this,"You're not Admin, Sorry.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(faculty_home.this,"Something went wrong, we are working on it.",Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.search_faculty_profile) {
            startActivity(new Intent(faculty_home.this,SearchProfile.class));
            // Handle the camera action
        } else if (id == R.id.personal_chatting) {




            String url = "https://pfaculty-53be5.firebaseio.com/users.json";
            final ProgressDialog pd = new ProgressDialog(faculty_home.this);
            pd.setMessage("Loading...");
            pd.setCancelable(false);
            pd.show();
            Firebase.setAndroidContext(faculty_home.this);

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
                                startActivity(new Intent(faculty_home.this, Users.class));
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

            RequestQueue rQueue = Volley.newRequestQueue(faculty_home.this);
            rQueue.add(request);






        }  else if (id == R.id.update_manually) {
            startActivity(new Intent(faculty_home.this,FacultyManualUpdate.class));

        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,MainActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
