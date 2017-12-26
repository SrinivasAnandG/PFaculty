package com.srinivasanand.pfaculty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.StringTokenizer;

/**
 * Created by srinivasanand on 01/11/17.
 */
public class PointFaculty  extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    TextView t1,t2;
    ImageView im;
    String FName;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    final FirebaseUser user=mAuth.getCurrentUser();
    ImageButton chat;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_design_profile_screen_xml_ui_design);
        FName=getIntent().getStringExtra("id");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        t1= (TextView) findViewById(R.id.user_profile_name);
        t2= (TextView) findViewById(R.id.user_profile_short_bio);
        im= (ImageView) findViewById(R.id.header_cover_image);
        t1.setText(FName);
        chat=(ImageButton)findViewById(R.id.chat);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Profile Pic").child(FName);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uris= (String) dataSnapshot.getValue();
                Picasso.with(PointFaculty.this)
                        .load((String) uris.toString())
                        .into(im);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        t2.setText(user.getEmail());
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!((user.getDisplayName()).equals(FName)))
                {
                    UserDetails.chatWith = FName;
                    startActivity(new Intent(PointFaculty.this, Chat.class));
                }
                else {
                    Toast.makeText(getApplicationContext(),"you cant text you .",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("Device Id").child(FName);
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id= (String) dataSnapshot.getValue();
                //Toast.makeText(getApplicationContext(),FName,Toast.LENGTH_SHORT).show();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Location").child(id);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String val= (String) dataSnapshot.getValue();
                        if(val!=null)
                        {
                            mMap.clear();
                            StringTokenizer toc=new StringTokenizer(val,";");
                            Double Lng=Double.parseDouble(toc.nextToken());
                            Double Lat=Double.parseDouble(toc.nextToken());

                            LatLng loc = new LatLng(Lat,Lng);
                            mMap.addMarker(new
                                    MarkerOptions().position(loc).title(FName));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"something went wrong, we are working on it.",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
