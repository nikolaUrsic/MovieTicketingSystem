package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Cinema;

public class AdminCinemaFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");

    private Map<String, Cinema> mapOfCinemas = new HashMap<>();
    private List<Cinema> listOfCinemas = new ArrayList<>();

    private ListView listView;

    private Button buttonAddNewCinema;

    private IEventManagerActivity interfaceEventManagerActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_cinema, container, false);

        listView = (ListView) view.findViewById(R.id.cinemasListView);

        buttonAddNewCinema = (Button) view.findViewById(R.id.buttonAddNewCinema);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Cinema selectedCinema = listOfCinemas.get(pos);
                AdminCinemaDetailsFragment fragment = new AdminCinemaDetailsFragment();
                fragment.setCinema(selectedCinema);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_contanier, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        buttonAddNewCinema.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewCinema();
            }
        });

        return view;
    }

    private void loadData() {

        //dohvat podataka o kino dvoranama
        databaseReferenceCinemas.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapOfCinemas.clear();
                listOfCinemas.clear();
                for (DataSnapshot CinemasSnapshot : dataSnapshot.getChildren()) {
                    Cinema cinema = CinemasSnapshot.getValue(Cinema.class);
                    mapOfCinemas.put(cinema.getName(), cinema);
                    listOfCinemas.add(cinema);
                }
                String[] arrayTicketNames = new String[listOfCinemas.size()];
                for(int i =0,l=listOfCinemas.size();i<l;i++){
                    arrayTicketNames[i]=listOfCinemas.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(interfaceEventManagerActivity.getApplicationFromFragment(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, arrayTicketNames);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addNewCinema(){
        AdminAddNewCinemaSeatArrangementFragment fragment = new AdminAddNewCinemaSeatArrangementFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            interfaceEventManagerActivity = (IEventManagerActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IEventManagerActivity");
        }
    }

}
