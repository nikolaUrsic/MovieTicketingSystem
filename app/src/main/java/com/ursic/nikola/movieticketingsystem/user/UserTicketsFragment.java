package com.ursic.nikola.movieticketingsystem.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class UserTicketsFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");

    private User user;
    private List<Ticket> listOfTickets = new ArrayList<>();
    private Map<String, MovieDisplay> mapOfMoiveDisplays = new HashMap<>();
    private UserTicketsFragmentListener userTicketsFragmentListener;

    private SimpleAdapter adapter;

    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_user_tickets, container, false);

        //sakri tipkovnicu (ako je otvorena)
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        listView = (ListView) view.findViewById(R.id.ticketsListView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Ticket selectedTicket = listOfTickets.get(pos);
                userTicketsFragmentListener.setUserOneTicketFragment(selectedTicket, mapOfMoiveDisplays.get(selectedTicket.getIdMovieDisplay()));

            }
        });


        //buttonAddNewEmployee = (Button) view.findViewById(R.id.buttonAddNewEmployee);

        //buttonAddNewEmployee.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        //   addNewEmployee();
        //}
        //});

        return view;


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            userTicketsFragmentListener = (UserTicketsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserTicketsFragmentListener");
        }
    }

    public interface UserTicketsFragmentListener extends IEventManagerActivity {
        User getCurrentUser();
        void setUserOneTicketFragment(Ticket ticket, MovieDisplay movieDisplay);
    }

    private void loadData() {

        user = userTicketsFragmentListener.getCurrentUser();

        databaseReferenceMovieDisplays.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    MovieDisplay movieDisplay = EmployeesSnapshot.getValue(MovieDisplay.class);
                    mapOfMoiveDisplays.put(movieDisplay.getId(), movieDisplay);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReferenceTickets.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot MoviesSnapshot : dataSnapshot.getChildren()) {
                    Ticket ticket = MoviesSnapshot.getValue(Ticket.class);
                    if (user.getListOfTicketsIds().contains(ticket.getId())) {
                        listOfTickets.add(ticket);
                    }
                }
                if(adapter == null) {
                    adapter = getTicketAdapter(listOfTickets);
                }

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private SimpleAdapter getTicketAdapter(List<Ticket> listOfTickets) {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Ticket ticket : listOfTickets) {
            MovieDisplay movieDisplay = mapOfMoiveDisplays.get(ticket.getIdMovieDisplay());
            Map<String, String> datum = new HashMap<String, String>(2);
            String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
            datum.put("First Line", movieDisplay.getMovieName() + " " + dateTimeOfBeginning);
            datum.put("Second Line", movieDisplay.getCinemaName() + " " + ticket.getPosotion());
            data.add(datum);
        }
        return new SimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[]{"First Line", "Second Line"},
                new int[]{android.R.id.text1, android.R.id.text2});
    }
}
