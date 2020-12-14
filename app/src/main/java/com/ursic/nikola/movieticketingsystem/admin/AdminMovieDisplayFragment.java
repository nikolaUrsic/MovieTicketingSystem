package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.MovieDisplay;

public class AdminMovieDisplayFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");

    private Map<String, MovieDisplay> mapOfMovieDisplays = new HashMap<>();
    private List<MovieDisplay> listOfMovieDisplays = new ArrayList<>();

    private ListView listView;
    private Button buttonAddNewMovieDisplay;
    private AdminMovieDisplayFragmentListener adminMovieDisplayFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_movie_display, container, false);

        listView = (ListView) view.findViewById(R.id.movieDisplaysListView);

        buttonAddNewMovieDisplay = (Button) view.findViewById(R.id.buttonAddNewMovieDisplay);

        buttonAddNewMovieDisplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewMovieDisplay();
            }
        });

        return view;
    }

    private void loadData() {

        //dohvat podataka o prikazima filmova
        databaseReferenceMovieDisplays.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, String>> data = new ArrayList<Map<String, String>>();

                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    MovieDisplay movieDisplay = EmployeesSnapshot.getValue(MovieDisplay.class);
                    if(movieDisplay.isDeleted()){
                        continue;
                    }
                    listOfMovieDisplays.add(movieDisplay);
                   // mapOfMovieDisplays.put(movieDisplay.getUsername(), movieDisplay);
                    String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
                    dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();

                    Map<String, String> datum = new HashMap<String, String>(2);
                    //String dateTimeOfBeginning = android.text.format.DateFormat.format("hh:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
                    if(movieDisplay.isIs3D()){
                        datum.put("First Line", movieDisplay.getMovieName()+" 3D");
                    }else{
                        datum.put("First Line", movieDisplay.getMovieName());
                    }
                    datum.put("Second Line", movieDisplay.getCinemaName() + " "+dateTimeOfBeginning);
                    data.add(datum);

                }

                SimpleAdapter adapter = new SimpleAdapter(adminMovieDisplayFragmentListener.getActivityFromFragment(), data,
                        android.R.layout.simple_list_item_2,
                        new String[] {"First Line", "Second Line" },
                        new int[] {android.R.id.text1, android.R.id.text2 });

                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminMovieDisplayFragmentListener = (AdminMovieDisplayFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminMovieDisplayFragmentListener");
        }
    }

    public interface AdminMovieDisplayFragmentListener extends IEventManagerActivity {
        Activity getActivityFromFragment();
    }

    private void addNewMovieDisplay(){
        AdminAddNewMovieDisplayFragment fragment = new AdminAddNewMovieDisplayFragment();
        fragment.setListOfMovieDisplays(listOfMovieDisplays);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
