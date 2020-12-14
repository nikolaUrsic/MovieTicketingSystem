package com.ursic.nikola.movieticketingsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

import Models.Admin;
import Models.Employee;
import Models.IUser;
import Models.PasswordHasher;
import Models.User;

import static android.support.constraint.Constraints.TAG;

public class SettingsFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceAdmins = database.getReference("Admins");

    private String id;
    private String role;
    private IUser user;

    private Button buttonSave;
    private Button buttonBack;
    private EditText editTextPassword0;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private PasswordHasher passwordHasher = new PasswordHasher();

    private IEventManagerActivity interfaceEventManagerActivity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        id = getArguments().getString("id");
        role = getArguments().getString("role");

        loadData();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        buttonSave = (Button) view.findViewById(R.id.buttonSave);
        buttonBack = (Button) view.findViewById(R.id.buttonBack);
        editTextPassword0 = (EditText) view.findViewById(R.id.editTextPassword0);
        editTextPassword1 = (EditText) view.findViewById(R.id.editTextPassword1);
        editTextPassword2 = (EditText) view.findViewById(R.id.editTextPassword2);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                interfaceEventManagerActivity.finishFromFragment();
                if (role.equals("user")) {
                    Intent intent = new Intent(interfaceEventManagerActivity.getApplicationFromFragment(), UserHomeActivity.class);
                    intent.putExtra("idUser", id);
                    startActivity(intent);
                }

                if (role.equals("employee")) {
                    Intent intent = new Intent(interfaceEventManagerActivity.getApplicationFromFragment(), EmployeeHomeActivity.class);
                    intent.putExtra("idEmployee", id);
                    startActivity(intent);
                }

                if (role.equals("admin")) {
                    Intent intent = new Intent(interfaceEventManagerActivity.getApplicationFromFragment(), AdminHomeActivity.class);
                    intent.putExtra("idAdmin", id);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    private void loadData() {
        if (role.equals("user")) {
            databaseReferenceUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (role.equals("employee")) {
            databaseReferenceEmployees.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(Employee.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (role.equals("admin")) {
            databaseReferenceAdmins.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(Admin.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void save() {
        String password0 = editTextPassword0.getText().toString().trim();
        String password1 = editTextPassword1.getText().toString().trim();
        final String password2 = editTextPassword2.getText().toString().trim();

        if (!passwordHasher.hashPassword(password0).equals(user.getPasswordInHashFormat())) {
            interfaceEventManagerActivity.makeToast("Niste ispravno unjeli staru lozinku");
            editTextPassword0.getText().clear();
            editTextPassword1.getText().clear();
            editTextPassword2.getText().clear();
            editTextPassword0.requestFocus();
            return;
        }

        if (!password1.equals(password2)) {
            interfaceEventManagerActivity.makeToast("Unjeli ste različite lozinke");
            editTextPassword1.getText().clear();
            editTextPassword2.getText().clear();
            editTextPassword1.requestFocus();
            return;
        }
        final String passwordInHashFormat = passwordHasher.hashPassword(password2);
        user.setPasswordInHashFormat(passwordInHashFormat);

        if (role.equals("user")) {
            final FirebaseAuth auth = FirebaseAuth.getInstance();
            final FirebaseUser user2 = auth.getCurrentUser();

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user2.getEmail(), password0);

            // Prompt the user to re-provide their sign-in credentials
            user2.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user2.updatePassword(password2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            auth.signOut();
                                            Log.d(TAG, "Password updated");
                                        } else {
                                            Log.d(TAG, "Error password not updated");
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error auth failed");
                            }
                        }
                    });
            databaseReferenceUsers.child(user.getId()).setValue(user);
        }

        if (role.equals("employee")) {
            databaseReferenceEmployees.child(user.getId()).setValue(user);
        }

        if (role.equals("admin")) {
            databaseReferenceAdmins.child(user.getId()).setValue(user);
        }

        interfaceEventManagerActivity.finishFromFragment();
        Intent intent = new Intent(interfaceEventManagerActivity.getApplicationFromFragment(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            interfaceEventManagerActivity = (IEventManagerActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsFragmentListener");
        }
    }

}
