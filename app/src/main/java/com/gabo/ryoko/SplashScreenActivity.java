package com.gabo.ryoko;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.gabo.ryoko.Common.Common;
import com.gabo.ryoko.Model.RiderModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private  FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference rideInfoRef;

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScree();
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    private void delaySplashScree() {
        progressBar.setVisibility(View.VISIBLE);
        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() ->
                        firebaseAuth.addAuthStateListener(listener)
                        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();
    }

    private void init() {
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        rideInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE);

        providers= Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user!=null)
            {
                checkFromFirebase();
            }else {
                showLoginLayout();
            }
        };

    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this, "Error: " +response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkFromFirebase() {
        rideInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            RiderModel riderModel = snapshot.getValue(RiderModel.class);
                            goToHomeActivity(riderModel);
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.DialogTheme);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);

        TextInputEditText edt_first_name = (TextInputEditText)itemView.findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name = (TextInputEditText)itemView.findViewById(R.id.edt_last_name);
        TextInputEditText edt_phone = (TextInputEditText)itemView.findViewById(R.id.edt_phone_number);

        Button btn_continue = (Button)itemView.findViewById(R.id.btn_register);

        //set data
        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!= null &&
                !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        //set View
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edt_first_name.getText().toString()))
            {
                Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show();
                return;
            }
            else  if (TextUtils.isEmpty(edt_last_name.getText().toString()))
            {
                Toast.makeText(this, "Por favor ingrese su apellido", Toast.LENGTH_SHORT).show();
                return;
            }
            else  if (TextUtils.isEmpty(edt_phone.getText().toString()))
            {
                Toast.makeText(this, "Por favor ingrese su numero de telefono", Toast.LENGTH_SHORT).show();

                return;
            }
            else
            {
                RiderModel riderModel = new RiderModel();
                riderModel.setFirstName(edt_first_name.getText().toString());
                riderModel.setLastName(edt_last_name.getText().toString());
                riderModel.setPhoneNumber(edt_phone.getText().toString());


                rideInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(riderModel)
                        .addOnFailureListener(e ->
                                {
                                    dialog.dismiss();
                                    Toast.makeText(SplashScreenActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this,"Registro Completo!!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            goToHomeActivity(riderModel);
                        });

            }
        });
    }

    private void goToHomeActivity(RiderModel riderModel) {
        Common.currentRider = riderModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }

}