package com.srinivasanand.pfaculty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by srinivasanand on 01/11/17.
 */

public class FacultyManualUpdate extends AppCompatActivity {
    AutoCompleteTextView text;
    ArrayList<String> list=new ArrayList<String>();
    String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faculty_manual_update);
        text=(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        name=user.getDisplayName();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Qr Validator");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String ,String> map= (Map<String, String>) dataSnapshot.getValue();
                if(map!=null)
                {
                    for(Map.Entry m:map.entrySet()){
                        //System.out.println(m.getKey()+" "+m.getValue());
                        list.add((String) m.getKey());
                    }
                    ArrayAdapter adapter = new
                            ArrayAdapter(FacultyManualUpdate.this,android.R.layout.simple_list_item_1,list);

                    text.setAdapter(adapter);
                    text.setThreshold(1);

                }
                else {
                    Toast.makeText(FacultyManualUpdate.this,"Error occured, we are working on it. Try after sometime.",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(FacultyManualUpdate.this,MainActivity.class));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Button b=(Button)findViewById(R.id.validate);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String roomNo=text.getText().toString().trim();
                if(!roomNo.equals(""))
                {
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Qr Validator").child(roomNo);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean val= (boolean) dataSnapshot.getValue();
                            if(val)
                            {
                                final ProgressDialog pd = new ProgressDialog(FacultyManualUpdate.this);
                                pd.setMessage("Loading...");
                                pd.setCancelable(false);
                                pd.show();
                                Date dNow = new Date( );
                                SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
                                String time=(String)ft.format(dNow);
                                DatabaseReference ref3=FirebaseDatabase.getInstance().getReference().child("Faculty Raw Location").child(name).child("Last Updated Time");
                                ref3.setValue(time);

                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Faculty Raw Location").child(name).child("Raw Location");
                                ref2.setValue(roomNo);
                                pd.dismiss();
                                Toast.makeText(FacultyManualUpdate.this, "Succesfully Updated", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(FacultyManualUpdate.this,faculty_home.class));

                            }
                            else {
                                Toast.makeText(FacultyManualUpdate.this,"missing original data...",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(FacultyManualUpdate.this,MainActivity.class));
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }
}
