package com.srinivasanand.pfaculty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by srinivasanand on 06/12/17.
 */
public class NotifyBranch extends Fragment {

    Spinner branch;
    EditText msg;
    Button send;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.notify_branch,container,false);



        branch=(Spinner)v.findViewById(R.id.branch);
        msg=(EditText)v.findViewById(R.id.msg);
        send=(Button)v.findViewById(R.id.send);
        String[] departments = {"Branch", "CSE", "IT", "ECE", "EEE", "MECH", "ECM", "BT", "CIVIL"};
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, departments);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        branch.setAdapter(aa);

        final Map<String,Integer> map1=new HashMap<String,Integer>();
        map1.put("CSE",05);
        map1.put("IT",12);
        map1.put("ECE",04);
        map1.put("EEE",02);
        map1.put("MECH",03);
        map1.put("ECM",19);
        map1.put("BT",23);
        map1.put("CIVIL",12);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String branch1= (String) branch.getSelectedItem();

                if(!branch1.equals("Branch"))
                {
                    String amsg=msg.getText().toString().trim();
                    if(!amsg.equals(""))
                    {
                        final String branch2=map1.get(branch1).toString();
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("User");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String,String> map= (Map<String, String>) dataSnapshot.getValue();
                                if(map!=null)
                                {
                                    for(Map.Entry m:map.entrySet()){
                                        String email= (String) m.getValue();
                                        //Toast.makeText(getContext(),email.substring(0,2)+" "+email.substring(6,8),Toast.LENGTH_SHORT).show();
                                        if((email.substring(6,8)).equals(branch2))
                                        {
                                            //Toast.makeText(getContext(),email.substring(0,2)+" "+email.substring(6,8),Toast.LENGTH_SHORT).show();
                                            DatabaseReference ref23=FirebaseDatabase.getInstance().getReference().child("User Token").child(m.getKey().toString());
                                            ref23.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String tok= (String) dataSnapshot.getValue();
                                                    if(tok!=null)
                                                    {
                                                        final WebView mWebview=new WebView(getContext());
                                                        mWebview.getSettings().setJavaScriptEnabled(true);
                                                        mWebview.setWebViewClient(new WebViewClient() {
                                                            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                                            }
                                                        });
                                                        //System.out.println(m.getKey()+" "+m.getValue());
                                                        mWebview .loadUrl("https://tfaculty.000webhostapp.com/pfacultysend.php?send_notification=3&token="+tok+"&message="+msg.getText().toString().trim()+"&from=Admin");
                                                        mWebview .loadUrl("https://tfaculty.000webhostapp.com/pfacultysend.php?send_notification=3&token="+tok+"&message="+msg.getText().toString().trim()+"&from=Admin");

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("Notifications").child(m.getKey().toString());
                                            ref2.push().setValue(msg.getText().toString().trim());
                                        }

                                    }
                                }
                                else
                                {
                                    Toast.makeText(getContext(),"Something went wrong, We are working in it.",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(getContext(),"Message can't be empty..",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(),"please select year and branch",Toast.LENGTH_SHORT).show();
                }
            }
        });




        return v;
    }
}
