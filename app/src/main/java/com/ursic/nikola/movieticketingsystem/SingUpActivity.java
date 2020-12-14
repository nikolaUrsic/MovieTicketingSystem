package com.ursic.nikola.movieticketingsystem;

import java.util.regex.Pattern;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Admin;
import Models.Employee;
import Models.PasswordHasher;
import Models.User;

public class SingUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceAdmins = database.getReference("Admins");
    private List<String> listOfUserNames = new ArrayList<>();

    private String username;
    private String email;
    private String password;

    private EditText editUserName;
    private EditText editUserEmail;
    private EditText editUserPassword;
    private Button buttonSignUp;

    private PasswordHasher passwordHasher = new PasswordHasher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        editUserName = (EditText) findViewById(R.id.editUserName);
        editUserEmail = (EditText) findViewById(R.id.editUserEmail);
        editUserPassword = (EditText) findViewById(R.id.editUserPassword);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signUp();
            }
        });

    }

    private void signUp() {
        username = editUserName.getText().toString().trim();
        email = editUserEmail.getText().toString().trim();
        password = editUserPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Molimo Vas unesite Vaše korisničko ime", Toast.LENGTH_SHORT).show();
            editUserName.requestFocus();
            return;
        }

        if (listOfUserNames.contains(username)) {
            Toast.makeText(this, "Takvo korisničko ime već postoji", Toast.LENGTH_SHORT).show();
            editUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Molimo Vas unesite adresu Vaše elektroničke pošte", Toast.LENGTH_SHORT).show();
            editUserEmail.requestFocus();
            return;
        }

        if (!isValid(email)) {
            Toast.makeText(this, "Vaša adresa elektroničke pošte nije valjana", Toast.LENGTH_SHORT).show();
            editUserEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 5) {
            Toast.makeText(this, "Molimo Vas unesite lozinku od barem 6 zakova", Toast.LENGTH_SHORT).show();
            editUserPassword.requestFocus();
            return;
        }

        //registracija korsinika za firebaseAuth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //slanje maila za potvrdu
                            FirebaseUser user1 = firebaseAuth.getCurrentUser();
                            user1.sendEmailVerification();
                            //registracija korisnika u bazi podataka
                            String idUser = databaseReferenceUsers.push().getKey();
                            User user = new User(idUser, username, passwordHasher.hashPassword(password), email);
                            databaseReferenceUsers.child(idUser).setValue(user);
                            finish();
                            Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SingUpActivity.this, "Problemi sa registracijom",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //dohvat podataka od korisnicima
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot UserSnapshot : dataSnapshot.getChildren()) {
                    User user = UserSnapshot.getValue(User.class);
                    listOfUserNames.add(user.getUsername());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //dohvat podataka o zaposlenicima
        databaseReferenceEmployees.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = EmployeesSnapshot.getValue(Employee.class);
                    listOfUserNames.add(employee.getUsername());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //dohvat podataka od administratorima
        databaseReferenceAdmins.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot AdminsSnapshot : dataSnapshot.getChildren()) {
                    Admin admin = AdminsSnapshot.getValue(Admin.class);
                    listOfUserNames.add(admin.getUsername());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}

