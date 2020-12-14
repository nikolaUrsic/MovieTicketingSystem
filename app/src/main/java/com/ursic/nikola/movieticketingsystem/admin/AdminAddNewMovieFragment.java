package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Models.Employee;
import Models.Movie;

public class AdminAddNewMovieFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovies = database.getReference("Movies");
    private List<String> listOfMoviesNames = new ArrayList<>();
    private boolean[] checkedGenres;
    private List<String> listOfGenres = new ArrayList<String>(Arrays.asList("Akcija", "Animirani", "Biografija", "Dokumentarni", "Drama", "Horor", "Komedija", "Ljubavni", "Pustolovni", "Ratni", "Triler", "Vestern"));
    private List<String> listOfSelectedGenres = new ArrayList<>();

    private EditText editMovieName;
    private EditText editMovieDuration;
    private EditText editMovieDescription;
    private Button buttonChooseGenre;
    private Button buttonAddMovie;

    private AdminAddNewMovieFragmentListener adminAddNewMovieFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_add_new_movie, container, false);

        editMovieName = (EditText) view.findViewById(R.id.editMovieName);
        editMovieDuration = (EditText) view.findViewById(R.id.editMovieDuration);
        editMovieDescription = (EditText) view.findViewById(R.id.editMovieDescription);
        buttonChooseGenre = (Button) view.findViewById(R.id.buttonChooseGenre);
        buttonAddMovie = (Button) view.findViewById(R.id.buttonAddMovie);

        buttonChooseGenre.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chooseGenre();
            }
        });

        buttonAddMovie.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addMovie();
            }
        });

        return view;
    }

    private void addMovie() {
        //treba provjerit ima li vec film sa takvim imenom
        String movieName = editMovieName.getText().toString().trim();
        if (TextUtils.isEmpty(movieName)) {
            Toast.makeText(getActivity(), "Molimo Vas unesite naziv filma", Toast.LENGTH_SHORT).show();
            editMovieName.requestFocus();
            return;
        }
        if (listOfMoviesNames.contains(movieName)) {
            Toast.makeText(getActivity(), "Film sa ovakvim imenom već postoji", Toast.LENGTH_SHORT).show();
            editMovieName.requestFocus();
            return;
        }

        int duration = Integer.parseInt(editMovieDuration.getText().toString());

        String movieDescription = editMovieDescription.getText().toString().trim();
        if (TextUtils.isEmpty(movieDescription)) {
            Toast.makeText(getActivity(), "Molimo Vas unesite opis filma", Toast.LENGTH_SHORT).show();
            editMovieDescription.requestFocus();
            return;
        }

        if(listOfSelectedGenres.isEmpty()){
            Toast.makeText(getActivity(), "Niste odabrali niti jedan žarn filma", Toast.LENGTH_SHORT).show();
            buttonChooseGenre.requestFocus();
            return;
        }

        String id = databaseReferenceMovies.push().getKey();
        databaseReferenceMovies.child(id).setValue(new Movie(id, movieName,  duration, listOfSelectedGenres, movieDescription));
        adminAddNewMovieFragmentListener.hideKeyboard();
        adminAddNewMovieFragmentListener.setMovieFragment();

    }

    private void chooseGenre() {
        checkedGenres = new boolean[listOfGenres.size()];
        final ArrayList<Integer> selectedGenres = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite žanr filma (ili više njih)");
        String pomList[] = new String[listOfGenres.size()];
        pomList = listOfGenres.toArray(pomList);
        builder.setMultiChoiceItems(pomList, checkedGenres, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    //mozda bi bilo bolje koristit set umjesto liste
                    if (!selectedGenres.contains(position)) {
                        selectedGenres.add(position);
                    } else {
                        selectedGenres.remove(position);
                    }
                }
            }
        });

        builder.setCancelable(false);
        listOfSelectedGenres.clear();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j =0, l = selectedGenres.size();j<l;++j) {
                    listOfSelectedGenres.add(listOfGenres.get(selectedGenres.get(j)));
                }
                if (listOfSelectedGenres.isEmpty()) {
                    //nije odabran niti jedan žarn
                }
            }
        });

        builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void loadData() {
        //dohvat podataka od fimlovima
        databaseReferenceMovies.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot MoviesSnapshot : dataSnapshot.getChildren()) {
                    Movie movie = MoviesSnapshot.getValue(Movie.class);
                    listOfMoviesNames.add(movie.getName());
                }
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
            adminAddNewMovieFragmentListener = (AdminAddNewMovieFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminAddNewMovieFragmentListener");
        }
    }

    public interface AdminAddNewMovieFragmentListener extends IEventManagerActivity {
        void setMovieFragment();
    }

}
