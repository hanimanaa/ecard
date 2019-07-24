package com.dimatechs.ecard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dimatechs.ecard.Model.Users;
import com.dimatechs.ecard.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import java.util.HashMap;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private Button LoginButton;
    private EditText InputNumber,InputPassword;
    private ProgressDialog loadingBar;
    private String parentDbName="Users";
    private CheckBox chkBoxRememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton=(Button)findViewById(R.id.login_btn);
        InputNumber=(EditText) findViewById(R.id.login_phone_number_input);
        InputPassword=(EditText) findViewById(R.id.login_password_input);
        loadingBar=new ProgressDialog(this);

        chkBoxRememberMe = (CheckBox) findViewById(R.id.remember_me_chkb);
        Paper.init(this);


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String UserPhoneKey = Paper.book().read(Prevalent.UserPhoneKey);
        String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);

        if (UserPhoneKey != "" && UserPasswordKey != "")
        {
            if (!TextUtils.isEmpty(UserPhoneKey) && !TextUtils.isEmpty(UserPasswordKey))
            {
                Log.d("hani","log old");
                AllowAccessToAccount(UserPhoneKey, UserPasswordKey);
                loadingBar.setTitle("כניסת משתמש");
                loadingBar.setMessage("המתן בבקשה");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

            }
        }

    }

    private void LoginUser()
    {
        String phone=InputNumber.getText().toString();
        String password=InputPassword.getText().toString();

        if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "רשום טלפון בבקשה . . .", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "רשום סיסמה בבקשה . . .", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.d("hani","log new");
            AllowAccessToAccount(phone, password);
            loadingBar.setTitle("כניסת משתמש");
            loadingBar.setMessage("המתן בבקשה");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

        }
    }

    private void AllowAccessToAccount(final String phone, final String password)
    {

        if(chkBoxRememberMe.isChecked())
        {
            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(parentDbName).child(phone).exists())
                {

                   Users usersData =dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                   if(usersData.getPhone().equals(phone))
                   {
                       if(usersData.getPassword().equals(password))
                       {
                           Toast.makeText(LoginActivity.this, "ברוך הבא", Toast.LENGTH_SHORT).show();
                           loadingBar.dismiss();
                           Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                           Prevalent.currentOnlineUser = usersData;
                           startActivity(intent);
                       }
                       else
                       {
                           loadingBar.dismiss();
                           Toast.makeText(LoginActivity.this, "סיסמה לא נכונה", Toast.LENGTH_SHORT).show();
                       }
                   }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "מס טלפון לא רשום", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "צור קשר עם פאדי כיואן טלפון 0508128670", Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
