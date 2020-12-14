package com.ursic.nikola.movieticketingsystem;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.admin.AdminHomeActivity;
import com.ursic.nikola.movieticketingsystem.employee.EmployeeHomeActivity;
import com.ursic.nikola.movieticketingsystem.user.UserHomeActivity;

import java.util.HashMap;
import java.util.Map;

import Models.*;

import static android.support.constraint.Constraints.TAG;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceAdmins = database.getReference("Admins");
    private Map<String, User> mapOfUsers = new HashMap<>();
    private Map<String, Employee> mapOfEmployees = new HashMap<>();
    private Map<String, Admin> mapOfAdmins = new HashMap<>();
    private Button buttonLogin;
    private Button buttonSignUp;
    private EditText editTextUserName;
    private EditText editTextPassword;
    private PasswordHasher passwordHasher = new PasswordHasher();
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //String idTicket = databaseReferenceTickets.push().getKey();
        //Ticket ticket= new Ticket(idTicket, "movie123", "cinema123", 50.00f, false, false, false);
       // databaseReferenceTickets.child(idTicket).setValue(ticket);
       // String id = databaseReferenceEmployees.push().getKey();
       // databaseReferenceAdmins.child(id).setValue(new Admin(id, "Admin2", "5f4dcc3b5aa765d61d8327deb882cf99","admin2.admin@gmail.com" ));
       // firebaseAuth.createUserWithEmailAndPassword("admin.admin@gmail.com", "5f4dcc3b5aa765d61d8327deb882cf99");
        //password je password
        setContentView(R.layout.activity_login);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signUp();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        //dohvat podataka o korisnicima
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot UsersSnapshot : dataSnapshot.getChildren()) {
                    User user = UsersSnapshot.getValue(User.class);
                    mapOfUsers.put(user.getUsername(), user);
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
                    mapOfEmployees.put(employee.getUsername(), employee);
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
                    mapOfAdmins.put(admin.getUsername(), admin);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void login() {
        String username = editTextUserName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Molimo Vas unesite Vaše korisničko ime", Toast.LENGTH_SHORT).show();
            editTextUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Molimo Vas unesite Vašu lozinku", Toast.LENGTH_SHORT).show();
            editTextPassword.requestFocus();
            return;
        }

        if (mapOfUsers.containsKey(username)) {
            user = mapOfUsers.get(username);
            String passwordInHashFormat = user.getPasswordInHashFormat();
            String passwordInHashFormatInput = passwordHasher.hashPassword(password);
            if (passwordInHashFormatInput.equals(passwordInHashFormat)) {
                //provjera jeli korsinik aktivirao svoj račun pomoću svoje elektroničke pošte
                firebaseAuth.signInWithEmailAndPassword(user.getEmail(), password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user2 = firebaseAuth.getCurrentUser();
                                    //provjera jeli korinsik aktivirao svoj račun  pomoću svoje elektroničke pošte
                                    if (user2.isEmailVerified()) {
                                        finish();
                                        Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);
                                        intent.putExtra("idUser", user.getId());
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Molimo Vas potvrdite Vašu adresu elektroničke pošte", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
            } else {
                Toast.makeText(this, "Unjeli ste pogrešnu lozinku", Toast.LENGTH_SHORT).show();
                editTextPassword.getText().clear();
                editTextPassword.requestFocus();
                return;
            }
        } else if (mapOfEmployees.containsKey(username)) {
            Employee employee = mapOfEmployees.get(username);
            String passwordInHashFormat = employee.getPasswordInHashFormat();
            String passwordInHashFormatInput = passwordHasher.hashPassword(password);
            if (passwordInHashFormatInput.equals(passwordInHashFormat)) {
                finish();
                Intent intent = new Intent(LoginActivity.this, EmployeeHomeActivity.class);
                intent.putExtra("idEmployee", employee.getId());
                startActivity(intent);

            } else {
                Toast.makeText(this, "Unjeli ste pogrešnu lozinku", Toast.LENGTH_SHORT).show();
                editTextPassword.getText().clear();
                editTextPassword.requestFocus();
                return;
            }
        } else if (mapOfAdmins.containsKey(username)) {
            Admin admin = mapOfAdmins.get(username);
            String passwordInHashFormat = admin.getPasswordInHashFormat();
            String passwordInHashFormatInput = passwordHasher.hashPassword(password);
            if (passwordInHashFormatInput.equals(passwordInHashFormat)) {
                finish();
                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                intent.putExtra("idAdmin", admin.getId());
                startActivity(intent);

            } else {
                Toast.makeText(this, "Unjeli ste pogrešnu lozinku", Toast.LENGTH_SHORT).show();
                editTextPassword.getText().clear();
                editTextPassword.requestFocus();
                return;
            }
        } else {
            Toast.makeText(this, "Nepostojeće korisničko ime", Toast.LENGTH_SHORT).show();
            editTextUserName.getText().clear();
            editTextPassword.getText().clear();
            editTextUserName.requestFocus();
        }
    }

    private void signUp() {
        finish();
        Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
        startActivity(intent);
    }

}

