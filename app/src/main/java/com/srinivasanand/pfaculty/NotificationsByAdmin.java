package com.srinivasanand.pfaculty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by srinivasanand on 07/12/17.
 */
public class NotificationsByAdmin extends AppCompatActivity{

    LinearLayout layout;
    RelativeLayout layout_2;
    ScrollView scrollView;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    final FirebaseUser user=mAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_by_admin);
        this.setTitle("Notifications by Admin");

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Notifications").child(user.getDisplayName());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map = (Map<String, String>) dataSnapshot.getValue();
                if(map!=null)
                {
                    for(Map.Entry m:map.entrySet()){
                        addMessageBox(m.getValue().toString().trim(),2);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(NotificationsByAdmin.this);
        textView.setPadding(20,20,20,20);
        textView.setText(message);
        textView.setTextSize(16);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 2.0f;

        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.rounded_corners);
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.rounded_corners);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }



}

