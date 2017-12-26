package com.srinivasanand.pfaculty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by srinivasanand on 16/08/17.
 */

public class FacultyIndex extends Fragment {


    private FirebaseAuth mAuth;
    Spinner sp;

    private static final int RC_SIGN_IN = 0;


    GoogleApiClient mGoogleApiClient;
    String department;
    //ProgressDialog progressBar;
    ProgressBar bar;


    Button b;
    EditText et;
    SignInButton btn;



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // progressBar=new ProgressDialog(getContext());
        // progressBar.setCancelable(false);
        //progressBar.setMessage("Initiating App...");
        //progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressBar.show();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        View v = inflater.inflate(R.layout.faculty_index, container, false);
        b = (Button) v.findViewById(R.id.checkKey2);
        btn = (SignInButton) v.findViewById(R.id.connect);
        setGooglePlusButtonText(btn,"SignIn with Google");
        et = (EditText) v.findViewById(R.id.valueofkey);
        sp = (Spinner) v.findViewById(R.id.department);
        String[] departments = {"Department", "CSE", "IT", "ECE", "EEE", "MECH", "ECM", "BT", "CIVIL", "ADMINSTRATORS", "OTHERS"};
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, departments);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        sp.setAdapter(aa);

        mAuth = FirebaseAuth.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity) getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String s = et.getText().toString().trim();


                String s1 = "1234";
                if (s.equals(s1)) {

                    sp.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.VISIBLE);
                } else {
                    btn.setVisibility(View.INVISIBLE);
                }


            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String depa = (String) sp.getSelectedItem();
                if (!depa.equals("Department")) {
                    department = (String) sp.getSelectedItem();
                    signIn();

                }
                //signIn();
            }
        });
        //progressBar.dismiss();
        mGoogleApiClient.stopAutoManage(getActivity());
        return v;
    }


    private void signIn() {
       // Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        bar= new ProgressBar(getContext());
        bar.setIndeterminate(true);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                //Toast.makeText(getActivity(), "Succesfull in getting your Account details..", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(getActivity(), "Please check the internet connectivity.", Toast.LENGTH_SHORT).show();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(getActivity(), "Authenticating with Firebase...", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            String name = mFirebaseUser.getDisplayName();
                            DatabaseReference dbf = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference ref = dbf.child("Faculty Department");
                            ref.child(name).setValue(department);

                            DatabaseReference dbf2 = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference ref2 = dbf2.child("Faculty Location");
                            DatabaseReference ref3 = ref2.child(name);
                            ref3.child("Location").setValue("1;1");
                            ref3.child("Live Tracking").setValue("true");


                            DatabaseReference dbf3=FirebaseDatabase.getInstance().getReference();
                            DatabaseReference ref4=dbf3.child("Faculty Raw Location").child(name);
                            DatabaseReference ref5=ref4.child("Raw Location");
                            ref5.setValue("Not yet Updated;Not yet Updated;Not yet Updated");
                            DatabaseReference ref6=ref4.child("Last Updated Time");
                            ref6.setValue("wait for time");
                            DatabaseReference ref31 =FirebaseDatabase.getInstance().getReference().child("Profile Pic").child(name);
                            ref31.setValue(mFirebaseUser.getPhotoUrl().toString());

                            DatabaseReference dbf8=FirebaseDatabase.getInstance().getReference().child("User Token").child(name);
                            dbf8.setValue(FirebaseInstanceId.getInstance().getToken());

                            DatabaseReference dbf9=FirebaseDatabase.getInstance().getReference().child("User").child(name);
                            dbf9.setValue(mFirebaseUser.getEmail());




                            DatabaseReference ref7=FirebaseDatabase.getInstance().getReference().child("Faculty Validate").child(name);
                            ref7.setValue("true");









                            startActivity(new Intent(getActivity(),faculty_home.class));
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed with firebase.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(),MainActivity.class));
                        }


                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

}
