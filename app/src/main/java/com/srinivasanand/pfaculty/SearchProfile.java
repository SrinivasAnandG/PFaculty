package com.srinivasanand.pfaculty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by srinivasanand on 07/12/17.
 */

public class SearchProfile extends AppCompatActivity {

    AutoCompleteTextView text;
    ArrayList<String> list=new ArrayList<String>();
    ImageView imgButton;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    final FirebaseUser user=mAuth.getCurrentUser();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_profile);


        text=(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("User");
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
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("User").child(val);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String fname= (String) dataSnapshot.getValue();
                            if(fname!=null)
                            {
                                Intent intent = new Intent(getBaseContext(), PointFaculty.class);
                                intent.putExtra("id",val);
                                startActivity(intent);

                            }
                            else
                            {
                                Toast.makeText(SearchProfile.this,"User Does not exists...",Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    Toast.makeText(SearchProfile.this,"Please search something...",Toast.LENGTH_SHORT).show();
                }

            }
        });



    }
}
