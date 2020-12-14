package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.LoginActivity;
import com.ursic.nikola.movieticketingsystem.R;
import com.ursic.nikola.movieticketingsystem.SettingsFragment;
import com.ursic.nikola.movieticketingsystem.SingUpActivity;

import Models.Cinema;
import Models.MovieDisplay;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        IEventManagerActivity,
        AdminAddNewMovieFragment.AdminAddNewMovieFragmentListener,
        AdminAddNewEmployeeFragment.AdminAddNewEmployeeFragmentListener,
        AdminAddNewCinemaSeatArrangementFragment.AdminAddNewCinemaSeatArragmentFragmentListener,
        AdminAddNewCinemaFragment.AdminAddCinemaFragmentListener,
        AdminAddNewMovieDisplayFragment.AdminAddNewMovieDisplayFragmentListener,
        AdminAddNewMovieDisplayTicketsFragment.AdminNewMovieDisplayTicketsFragmentListener,
        AdminCinemaDetailsFragment.AdminCinemaDetailsFragmentListener,
        AdminMovieDisplayFragment.AdminMovieDisplayFragmentListener,
        AdminEmployeeDetailsFragment.AdminEmployeeDetailsFragmentListener,
        AdminMovieDetailsFragment.AdminMovieDetailsFragmentListener {
    private DrawerLayout drawer;
    private DialogInterface.OnClickListener dialogClickListener;
    private NavigationView navigationView;

    private String idAdmin;

    private static final int BACK_PRESS_COUNT_FOR_EXIT = 2;
    private int mBackCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Bundle bundle = getIntent().getExtras();
        idAdmin = bundle.getString("idAdmin");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //prvo se otvaraju projekcije po defaultu
        if (savedInstanceState == null) { //ako se rotira da se ne promijeni
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_contanier, new AdminMovieDisplayFragment()).commit();
            navigationView.setCheckedItem(R.id.movie_display);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.cinema:
                setCinemaFragment();
                break;
            case R.id.employees:
                setEmployeesFragment();
                break;
            case R.id.movie:
                setMovieFragment();
                break;
            case R.id.movie_display:
                setMovieDisplayFragment();
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
                        Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
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

    public void setCinemaFragment() {
        AdminCinemaFragment adminCinemaFragment = new AdminCinemaFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, adminCinemaFragment).commit();
    }

    public void setEmployeesFragment() {
        AdminEmployeesFragment adminEmployeesFragment = new AdminEmployeesFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, adminEmployeesFragment).commit();
    }

    public void setMovieFragment() {
        AdminMovieFragment adminMovieFragment = new AdminMovieFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, adminMovieFragment).commit();
    }

    public void setMovieDisplayFragment() {
        AdminMovieDisplayFragment adminMovieDisplayFragment = new AdminMovieDisplayFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, adminMovieDisplayFragment).commit();
    }

    public void setSettingsFragment() {
        Bundle bundle2 = new Bundle();
        bundle2.putString("id", idAdmin);
        bundle2.putString("role", "admin");
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle2);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, settingsFragment).commit();
    }

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

    public void setAddNewCinemaFragment(String seats){
        Bundle bundle = new Bundle();
        bundle.putString("seats", seats);
        AdminAddNewCinemaFragment fragment = new AdminAddNewCinemaFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, fragment).commit();
    }

    @Override
    public void setCinemaDetailsFragment(String seats, Cinema cinema) {
        AdminCinemaDetailsFragment fragment = new AdminCinemaDetailsFragment();
        fragment.setCinema(cinema);
        fragment.setnewSeatLayout(seats);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void setAddNewMovieDisplayTicketsFragment(MovieDisplay movieDisplay, Cinema cinema) {
         AdminAddNewMovieDisplayTicketsFragment adminNewMovieDisplayTicketsFragment = new AdminAddNewMovieDisplayTicketsFragment();
         adminNewMovieDisplayTicketsFragment.setMovieDisplay(movieDisplay);
         adminNewMovieDisplayTicketsFragment.setCinema(cinema);
         getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, adminNewMovieDisplayTicketsFragment).commit();

    }

    @Override
    public Activity getActivityFromFragment() {
        return this;
    }

    public void addNewSeatArrangement(Cinema cinema){
        AdminAddNewCinemaSeatArrangementFragment fragment = new AdminAddNewCinemaSeatArrangementFragment();
        fragment.setCalledFromAdnimCinemaDetailsFragmentAndSetCinema(true, cinema);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
