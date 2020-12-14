package com.ursic.nikola.movieticketingsystem.user;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.LoginActivity;
import com.ursic.nikola.movieticketingsystem.R;
import com.ursic.nikola.movieticketingsystem.SettingsFragment;
import com.ursic.nikola.movieticketingsystem.admin.AdminHomeActivity;

import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class UserHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        IEventManagerActivity,
        UserMovieDisplayFragment.UserMovieDisplayFragmentListener,
        UserBookTicketFragment.UserBookTicketFragmentListener,
        UserTicketsFragment.UserTicketsFragmentListener,
        UserOneTicketFragment.UserOneTicketFragmentListener {
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");

    private static final int BACK_PRESS_COUNT_FOR_EXIT = 2;
    private int mBackCounter;

    private String idUser;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Bundle bundle = getIntent().getExtras();
        idUser = bundle.getString("idUser");

        loadData();

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //prvo se otvaraju projekcije po defaultu
        if (savedInstanceState == null) { //ako se rotira da se ne promijeni
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_contanier, new UserMovieDisplayFragment()).commit();
            navigationView.setCheckedItem(R.id.movie_display);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.movie_display:
                setMovieDisplayFragment();
                break;
            case R.id.tickets:
                setTicketsFragment();
                break;
            case R.id.settings:
                setSettingsFragment();
                break;
            case R.id.sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Jeste li sigurni da se želite odjaviti?").setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Intent intent = new Intent(UserHomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                        .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void setMovieDisplayFragment() {
        UserMovieDisplayFragment userMovieDisplayFragment = new UserMovieDisplayFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, userMovieDisplayFragment).commit();
    }

    public void setTicketsFragment() {
        UserTicketsFragment userTicketsFragment = new UserTicketsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, userTicketsFragment).commit();
    }

    public void setSettingsFragment() {
        Bundle bundle2 = new Bundle();
        bundle2.putString("id", idUser);
        bundle2.putString("role", "user");
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle2);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, settingsFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackCounter = 0;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            mBackCounter++;
            if (mBackCounter >= BACK_PRESS_COUNT_FOR_EXIT) {
                super.onBackPressed();
            } else {
                Toast.makeText(getApplicationContext(), "Pritisnite tipku za povratak još jedanput izlazak", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void finishFromFragment() {
        this.finish();
    }

    @Override
    public void makeToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Application getApplicationFromFragment() {
        return this.getApplication();
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void setUserBookTicketFragment(MovieDisplay movieDisplay) {
        UserBookTicketFragment userBookTicketFragment = new UserBookTicketFragment();
        userBookTicketFragment.setMovieDisplay(movieDisplay);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, userBookTicketFragment).commit();
    }

    public void setUserOneTicketFragment(Ticket ticket, MovieDisplay movieDisplay) {
        UserOneTicketFragment userOneTicketFragment = new UserOneTicketFragment();
        userOneTicketFragment.setTicketAndMovieDisplay(ticket, movieDisplay);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, userOneTicketFragment).commit();
    }

    @Override
    public User getCurrentUser() {
        return user;
    }

    @Override
    public Activity getActivityFromFragment() {
        return this;
    }


    private void loadData() {
        databaseReferenceUsers.child(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
