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

public class StudentIndex extends Fragment {
    GoogleApiClient mGoogleApiClient;
    SignInButton btn;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN =0;







    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.student_index, container, false);
        //ProgressDialog progressBar=new ProgressDialog(getContext());
        //progressBar.setCancelable(true);
        //progressBar.setMessage("Loading Man Wait..");
        //progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressBar.show();
        //progressBar.dismiss();

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient =new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity)getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getContext(),"failed in getting Google Accounts",Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        btn=(SignInButton)v.findViewById(R.id.connect);
        setGooglePlusButtonText(btn,"SignIn with Google");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"google signin step 1 success",Toast.LENGTH_SHORT).show();
                signIn();
            }
        });


        //progressBar.dismiss();
        mGoogleApiClient.stopAutoManage(getActivity());
        return v;

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(getActivity(),"step 3",Toast.LENGTH_SHORT).show();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                //Toast.makeText(getActivity(),"Successful in getting account details",Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(getActivity(),"Failed in getting account details",Toast.LENGTH_SHORT).show();
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
                            //Toast.makeText(getContext(), "Authenticating with Database, Succesful...", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            String name=mFirebaseUser.getDisplayName();
                            DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("User Token").child(name);
                            ref.setValue(FirebaseInstanceId.getInstance().getToken());
                            DatabaseReference ref2 =FirebaseDatabase.getInstance().getReference().child("Faculty Validate").child(name);
                            ref2.setValue("false");
                            DatabaseReference ref3 =FirebaseDatabase.getInstance().getReference().child("Profile Pic").child(name);
                            ref3.setValue(mFirebaseUser.getPhotoUrl().toString());

                            startActivity(new Intent(getContext(),students_home.class));



                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Database Authentication Filed.check your internet connection.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        }


                    }
                });
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
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }


}
