package com.ursic.nikola.movieticketingsystem.admin;

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
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Employee;
import Models.Movie;

public class AdminMovieFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovies = database.getReference("Movies");

    private Map<String, Movie> mapOfMovies = new HashMap<>();
    private List<Movie> listOfMovies = new ArrayList<>();

    private ListView listView;

    private Button buttonAddNewMovie;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_movie, container, false);

        listView = (ListView) view.findViewById(R.id.moviesListView);

        loadData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Movie selectedMovie = listOfMovies.get(pos);
                AdminMovieDetailsFragment fragment = new AdminMovieDetailsFragment();
                fragment.setMovie(selectedMovie);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_contanier, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        buttonAddNewMovie = (Button) view.findViewById(R.id.buttonAddNewMovie);

        buttonAddNewMovie.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewMovie();
            }
        });

        return view;

    }

    private void loadData() {

        //dohvat podataka od filmovima
        databaseReferenceMovies.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapOfMovies.clear();
                listOfMovies.clear();
                for (DataSnapshot MoviesSnapshot : dataSnapshot.getChildren()) {
                    Movie movie = MoviesSnapshot.getValue(Movie.class);
                    mapOfMovies.put(movie.getName(), movie);
                    listOfMovies.add(movie);
                }
                String[] arrayMoviesNames = new String[listOfMovies.size()];
                for(int i =0,l=listOfMovies.size();i<l;i++){
                    arrayMoviesNames[i]=listOfMovies.get(i).getName();
                }
                //ovaj getActivity je potencijano opasan
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, arrayMoviesNames);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void addNewMovie(){

        AdminAddNewMovieFragment fragment = new AdminAddNewMovieFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

}
