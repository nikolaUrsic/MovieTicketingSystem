package com.ursic.nikola.movieticketingsystem.employee;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

import Models.Employee;
import Models.MovieDisplay;
import Models.Ticket;

public class EmployeeHomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        IEventManagerActivity,
        EmployeeChargeTicketFragment.EmployeeChargeTicketFragmentListener,
        EmployeeStampTicketFragment.EmployeeStampTicketFragmentListener,
        EmployeeCheckTicketFragment.EmployeeCheckTicketFragmentListener {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private Map<String, Ticket> mapOfTickets = new HashMap<>();
    private Map<String, MovieDisplay> mapOfMovieDisplays = new HashMap<>();
    private NavigationView navigationView;
    private String idTicket;
    private String nextFragment;
    private DrawerLayout drawer;
    private String idEmployee;
    private Employee employee;
    private MovieDisplay movieDisplay;

    private static final String CHARGE_TICKET_FRAGMENT = "EmployeeChargeTicketFragment";
    private static final String STAMP_TICKET_FRAGMENT = "EmployeeStampTicketFragment";
    private static final String CHECK_TICKET_FRAGMENT = "EmployeeCheckTicketFragment";

    private static final int BACK_PRESS_COUNT_FOR_EXIT = 2;
    private int mBackCounter;

    private boolean ticketsLoaded = false;
    private boolean moveiDisplaysLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);

        Bundle bundle = getIntent().getExtras();
        idEmployee = bundle.getString("idEmployee");
        nextFragment = bundle.getString("nextFragment");

        loadData();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //prvo se otvaraja statistika po defaultu
        if (savedInstanceState == null && nextFragment == null) { //ako se rotira da se ne promijeni
            setStatisticsFragment();
            navigationView.setCheckedItem(R.id.statistics);
        }

    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.charge:
                scanTicket(CHARGE_TICKET_FRAGMENT);
                break;
            case R.id.stamp:
                // scanTicket(STAMP_TICKET_FRAGMENT);
                EmployeeStampTicketFragment employeeStampTicketFragment = new EmployeeStampTicketFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, employeeStampTicketFragment).commit();
                break;
            case R.id.check:
                scanTicket(CHECK_TICKET_FRAGMENT);
                break;
            case R.id.statistics:
                setStatisticsFragment();
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
                        Intent intent = new Intent(EmployeeHomeActivity.this, LoginActivity.class);
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

    private void scanTicket(String nextFragment) {
        finish();
        Intent intent = new Intent(EmployeeHomeActivity.this, ScanActivity.class);
        intent.putExtra("idEmployee", idEmployee);
        intent.putExtra("nextFragment", nextFragment);
        startActivity(intent);
    }

    private void handleScanResult(String nextFragment) {
        Bundle bundle = getIntent().getExtras();
        idTicket = bundle.getString("idTicket");

        //provjera jeli očitana karta
        if (!mapOfTickets.containsKey(idTicket)) {
            Toast.makeText(this, "Očitani kod nije karta", Toast.LENGTH_SHORT).show();
            return;
        }
        Ticket ticket = mapOfTickets.get(idTicket);
        //provjara može li zaposlenik naplaćivati kartu
        MovieDisplay movieDisplay = mapOfMovieDisplays.get(ticket.getIdMovieDisplay());
        String cinemaId = movieDisplay.getIdCinema();
        if (!employee.getListOfCinemasIds().contains(cinemaId)) {
            Toast.makeText(this, "Nemate pravo za rad sa ovom kartom", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (nextFragment) {
            case CHARGE_TICKET_FRAGMENT:
                EmployeeChargeTicketFragment employeeChargeTicketFragment = new EmployeeChargeTicketFragment();
                employeeChargeTicketFragment.setTicket(ticket);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, employeeChargeTicketFragment).commit();
                break;
            case STAMP_TICKET_FRAGMENT:
                EmployeeStampTicketFragment employeeStampTicketFragment = new EmployeeStampTicketFragment();
                employeeStampTicketFragment.setTicketAndMovieDisplay(ticket, movieDisplay);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, employeeStampTicketFragment).commit();                break;
            case CHECK_TICKET_FRAGMENT:
                EmployeeCheckTicketFragment employeeCheckTicketFragment = new EmployeeCheckTicketFragment();
                employeeCheckTicketFragment.setTicketAndMovieDisplay(ticket, movieDisplay);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, employeeCheckTicketFragment).commit();
                break;
        }
    }

    public void setStatisticsFragment() {
        EmployeeStatisticsFragment employeeStatisticFragment = new EmployeeStatisticsFragment();
        employeeStatisticFragment.setEmployeeId(idEmployee);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, employeeStatisticFragment).commit();
    }

    public void setSettingsFragment() {
        Bundle bundle2 = new Bundle();
        bundle2.putString("id", idEmployee);
        bundle2.putString("role", "employee");
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle2);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contanier, settingsFragment).commit();
    }

    public void loadData() {

        //dohvat podataka o zaposleniku
        databaseReferenceEmployees.child(idEmployee).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                employee = dataSnapshot.getValue(Employee.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //dohvat liste karata
        databaseReferenceTickets.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapOfTickets.clear();
                for (DataSnapshot TicketsSnapshot : dataSnapshot.getChildren()) {
                    Ticket ticket2 = TicketsSnapshot.getValue(Ticket.class);
                    mapOfTickets.put(ticket2.getId(), ticket2);
                }
                ticketsLoaded=true;
                if (nextFragment != null && ticketsLoaded && moveiDisplaysLoaded) {
                    handleScanResult(nextFragment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        //dohvat liste karata
        databaseReferenceMovieDisplays.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapOfMovieDisplays.clear();
                for (DataSnapshot TicketsSnapshot : dataSnapshot.getChildren()) {
                    MovieDisplay ticket2 = TicketsSnapshot.getValue(MovieDisplay.class);
                    mapOfMovieDisplays.put(ticket2.getId(), ticket2);
                }
                moveiDisplaysLoaded=true;
                if (nextFragment != null && ticketsLoaded && moveiDisplaysLoaded) {
                    handleScanResult(nextFragment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
    public void makeRedToast(String text){
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.RED);
        toast.show();
    }

    @Override
    public Application getApplicationFromFragment() {
        return this.getApplication();
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
    public Employee getCurrentEmployee() {
        return employee;
    }

    @Override
    public void scanAndStampTickets(MovieDisplay selectedMovieDisplay) {
        this.movieDisplay = selectedMovieDisplay;
        scanTicket(STAMP_TICKET_FRAGMENT);
    }
}
