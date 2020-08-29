package com.fouadbahari.lellafood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Model.User;
import com.fouadbahari.lellafood.View.HomeActivity;
import com.fouadbahari.lellafood.View.MapsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity   {

    private Button btnSignIn, btnRegister;


    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    private RelativeLayout rootLayout;



    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser mFirebaseUser=auth.getCurrentUser();
        if (mFirebaseUser!=null){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();

        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.USER_REFERENCES);


        btnRegister = (Button) findViewById(R.id.registerBtnId);
        btnSignIn = (Button) findViewById(R.id.signInBtnId);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        btnSignIn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showLoginDialog();

                            }
                        });


                        btnRegister.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showRegisterDialog();
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        Intent intent=new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri=Uri.fromParts("package",getPackageName(),null);
                        intent.setData(uri);
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        token.continuePermissionRequest();
                    }
                }).check();



    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.activity_register, null);

        final MaterialEditText email = register_layout.findViewById(R.id.getEmailId);
        final MaterialEditText password = register_layout.findViewById(R.id.getPasswordId);
        final MaterialEditText name = register_layout.findViewById(R.id.getNameId);
        final MaterialEditText phone = register_layout.findViewById(R.id.getPhoneId);
        final MaterialEditText address = register_layout.findViewById(R.id.getAddressId);


        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(name.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter name!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phone.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter phone number!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter address number!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                final User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());
                                user.setPhone(phone.getText().toString());
                                user.setAddress(address.getText().toString());
                                user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());


                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Snackbar.make(rootLayout, "Register success!",
                                                        Snackbar.LENGTH_SHORT).show();
                                                goToHomeActivity(user);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(rootLayout, "Failed"+e.getMessage(),
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, "Failed"+e.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void goToHomeActivity(User user) {
        Common.currentUser=user;

        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();


    }


    private void showLoginDialog()  {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign-in").setMessage("Please  use email to sign in!");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signin_layout = inflater.inflate(R.layout.activity_sign_in, null);

        final MaterialEditText email = signin_layout.findViewById(R.id.emailSignInID);
        final MaterialEditText password = signin_layout.findViewById(R.id.passwordSignInId);


        dialog.setView(signin_layout);

        dialog.setPositiveButton("SIGN-IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();



                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.show();

                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                waitingDialog.dismiss();
                                FirebaseUser mUser=FirebaseAuth.getInstance().getCurrentUser();
                                users.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                                              User user = snapshot.getValue(User.class);
                                              goToHomeActivity(user);
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {

                                      Toast.makeText(MainActivity.this, ""+error.getMessage(),
                                              Toast.LENGTH_SHORT).show();
                                  }
                              });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, "Failed", Snackbar.LENGTH_SHORT).show();


                    }
                });

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


}
