package com.ursic.nikola.movieticketingsystem.employee;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;
import com.ursic.nikola.movieticketingsystem.admin.AdminEmployeeDetailsFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.Employee;
import Models.MovieDisplay;
import Models.Ticket;

public class EmployeeStampTicketFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");

    private List<MovieDisplay> listOfMovieDisplays = new ArrayList<>();
    private Map<String, Cinema> mapOfCinemaNames = new HashMap<>();
    private SimpleAdapter adapter;
    private ListView listView;

    private Ticket ticket;
    private MovieDisplay selectedMovieDisplay;

    private EmployeeStampTicketFragmentListener employeeStampTicketFragmentListener;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (ticket != null) {
            stampTicket();
        }

        loadData();

        View view = inflater.inflate(R.layout.fragment_employee_stamp_ticket, container, false);

        //sakri tipkovnicu (ako je otvorena)
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        listView = (ListView) view.findViewById(R.id.movieDisplaysListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                selectedMovieDisplay = listOfMovieDisplays.get(pos);
                employeeStampTicketFragmentListener.scanAndStampTickets(selectedMovieDisplay);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            employeeStampTicketFragmentListener = (EmployeeStampTicketFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EmployeeStampTicketFragmentListener");
        }
    }

    public interface EmployeeStampTicketFragmentListener extends IEventManagerActivity {
        void setStatisticsFragment();

        Employee getCurrentEmployee();

        void scanAndStampTickets(MovieDisplay selectedMovieDisplay);

        void makeRedToast(String toast);
    }

    private void stampTicket() {
        //provejra statusa krarte te jeli karta za ovu prijekciju filma
        if (ticket.getStatus().equals("PURCHASED") && ticket.getIdMovieDisplay().equals(selectedMovieDisplay.getId())) {
            ticket.setStatus("USED");
            databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
            Employee employee = employeeStampTicketFragmentListener.getCurrentEmployee();
            employee.ticketStamped();
            databaseReferenceEmployees.child(employee.getId()).setValue(employee);
            employeeStampTicketFragmentListener.makeToast("Karta poništena");
        } else {
            employeeStampTicketFragmentListener.makeRedToast("Karta nije ispravna");
        }
        //TREBA PONOVNO SKENIRATI
        employeeStampTicketFragmentListener.scanAndStampTickets(selectedMovieDisplay);
    }

    private void loadData() {

        //dohvat podataka od projekcijama filma
        databaseReferenceMovieDisplays.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = employeeStampTicketFragmentListener.getCurrentEmployee();
                    MovieDisplay movieDisplay = EmployeesSnapshot.getValue(MovieDisplay.class);
                    //if(movieDisplay.isDeleted()){
                    //    continue;
                    // }
                    // mapOfMovieDisplays.put(movieDisplay.getUsername(), movieDisplay);
                    Date currentTime = new Date();
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    currentTime.setYear(year);
                    //filtrirati projekcije (u odnosu na danasnji dan) i provjeriti može li zaposlenik naplaćivati
                    if (!movieDisplay.getDateTimeOfBeginning().before(currentTime) && employee.getListOfCinemasIds().contains(movieDisplay.getIdCinema())) {
                        listOfMovieDisplays.add(movieDisplay);
                    }
                }
                adapter = getMovieDisplayAdapter(listOfMovieDisplays);

                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReferenceCinemas.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot MoviesSnapshot : dataSnapshot.getChildren()) {
                    Cinema cinema = MoviesSnapshot.getValue(Cinema.class);
                    mapOfCinemaNames.put(cinema.getName(), cinema);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private SimpleAdapter getMovieDisplayAdapter(List<MovieDisplay> listOfCurrentMovieDisplays) {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (MovieDisplay movieDisplay : listOfCurrentMovieDisplays) {
            Map<String, String> datum = new HashMap<String, String>(2);
            String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
            //String dateTimeOfBeginning = android.text.format.DateFormat.format("hh:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            if (movieDisplay.isIs3D()) {
                datum.put("First Line", movieDisplay.getMovieName() + " 3D");
            } else {
                datum.put("First Line", movieDisplay.getMovieName());
            }
            datum.put("Second Line", movieDisplay.getCinemaName() + " " + dateTimeOfBeginning);
            data.add(datum);
        }
        return new SimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[]{"First Line", "Second Line"},
                new int[]{android.R.id.text1, android.R.id.text2});
    }

    public void setTicketAndMovieDisplay(Ticket ticket, MovieDisplay movieDisplay) {
        this.ticket = ticket;
        this.selectedMovieDisplay = movieDisplay;
    }

}
