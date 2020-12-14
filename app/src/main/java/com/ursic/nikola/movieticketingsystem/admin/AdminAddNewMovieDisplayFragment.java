package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Cinema;
import Models.Employee;
import Models.Movie;
import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class AdminAddNewMovieDisplayFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovies = database.getReference("Movies");
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");
    private Map<String, Movie> mapOfMoviesNames = new HashMap<>();
    private Map<String, Cinema> mapOfCinemaNames = new HashMap<>();
    private List<MovieDisplay> listOfMovieDisplays = new ArrayList<>();
    private List<String> listOfMoviesNames = new ArrayList<>();
    ArrayList<String> listOFMoviesNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Date dateTimeOfBeginning = new Date();
    private boolean is3D = false;

    private Movie selectedMovie;
    private Cinema selectedCinema;

    private ListView listView;
    private TextView textMovie;
    private EditText editMovieName;
    private Button buttonDateOfBeginning;
    private TextView textDateOfBeginning;
    private DatePickerDialog datePickerDialog;
    private Button buttonTimeOfBeginning;
    private TimePickerDialog timePickerDialog;
    private TextView textTimeOfBeginning;
    private TextView textCinema;
    private Button buttonChooseCinema;
    private RadioButton radio3D;
    private Button buttonNext;
    private String[] cinemas;
    private AdminAddNewMovieDisplayFragmentListener adminAddNewMovieDisplayFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_add_new_movie_display, container, false);

        textMovie = (TextView) view.findViewById(R.id.textMovie);
        editMovieName = (EditText) view.findViewById(R.id.editMovieName);
        listView = (ListView) view.findViewById(R.id.moviesListView);
        buttonDateOfBeginning = (Button) view.findViewById(R.id.buttonDateOfBeginning);
        textDateOfBeginning = (TextView) view.findViewById(R.id.textDateOfBeginning);
        buttonTimeOfBeginning = (Button) view.findViewById(R.id.buttonTimeOfBeginning);
        textTimeOfBeginning = (TextView) view.findViewById(R.id.textTimeOfBeginning);
        textCinema = (TextView) view.findViewById(R.id.textCinema);
        buttonChooseCinema = (Button) view.findViewById(R.id.buttonChooseCinema);
        radio3D = (RadioButton) view.findViewById(R.id.radio3D);
        radio3D.setVisibility(View.INVISIBLE);
        buttonNext = (Button) view.findViewById(R.id.buttonNext);

        editMovieName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                String movieName = adapter.getItem(pos);
                selectedMovie = mapOfMoviesNames.get(movieName);
                textMovie.setText("Film: " + movieName);
            }
        });

        buttonDateOfBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        textDateOfBeginning.setText("Datum početka: " + dayOfMonth + ". " + month + ". " + year + ".");
                        dateTimeOfBeginning.setYear(year);
                        dateTimeOfBeginning.setMonth(month);
                        dateTimeOfBeginning.setDate(dayOfMonth);
                    }
                };

                datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener(onDateSetListener);
                datePickerDialog.show();
            }
        });

        buttonTimeOfBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        textTimeOfBeginning.setText("Vrijeme početka: " + hourOfDay + ":" + minute);
                        dateTimeOfBeginning.setHours(hourOfDay);
                        dateTimeOfBeginning.setMinutes(minute);
                    }
                };
                timePickerDialog = new TimePickerDialog(getContext(), onTimeSetListener, 0, 0, true);
                timePickerDialog.show();
            }
        });

        buttonChooseCinema.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chooseCinema();
            }
        });

        radio3D.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radioButtonChanged();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();
            }
        });

        return view;
    }

    private void next() {
        if (selectedMovie == null) {
            Toast.makeText(getActivity(), "Molimo Vas odaberite film", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCinema == null) {
            Toast.makeText(getActivity(), "Molimo Vas odaberite kino dvoranu", Toast.LENGTH_SHORT).show();
            return;
        }
        //ako je vrijeme prikaza prije trenutnog vremena
        Date currentTime = new Date();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        currentTime.setYear(year);
        if (dateTimeOfBeginning.before(currentTime)) {
            Toast.makeText(getActivity(), "Vrijeme prikaza mora biti prije trenutnog vremena", Toast.LENGTH_SHORT).show();
            return;
        }
        //treba provjerit jeli se možda već prikazuje neki film u toj dvorani
        if(cinemaOccupied()){
            Toast.makeText(getActivity(), "Kino dvorana je u to vrijeme zauzeta", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseReferenceMovieDisplays.push().getKey();

        MovieDisplay movieDisplay = new MovieDisplay(id, selectedMovie.getId(), selectedMovie.getName(), selectedCinema.getId(), selectedCinema.getName(), dateTimeOfBeginning, is3D, new ArrayList<String>(), false);

        adminAddNewMovieDisplayFragmentListener.setAddNewMovieDisplayTicketsFragment(movieDisplay, selectedCinema);

    }

    private void chooseCinema() {
        cinemas = new String[mapOfCinemaNames.size()];
        int i = 0;
        for (String key : mapOfCinemaNames.keySet()) {
            cinemas[i] = key;
            ++i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite kino dvoranu");
        builder.setItems(cinemas, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textCinema.setText("Kino dvorana: " + cinemas[which]);
                selectedCinema = mapOfCinemaNames.get(cinemas[which]);
                if (selectedCinema.isPosibilityOf3D()) {
                    radio3D.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.show();
    }


    private void loadData() {
        //dohvat podataka od filmovima
        databaseReferenceMovies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot MoviesSnapshot : dataSnapshot.getChildren()) {
                    Movie movie = MoviesSnapshot.getValue(Movie.class);
                    mapOfMoviesNames.put(movie.getName(), movie);
                    listOFMoviesNames.add(movie.getName());
                }
                adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, listOFMoviesNames);
                listView.setAdapter(adapter);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //dohvat podataka od kino dvoranama
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

    private void radioButtonChanged() {
        if (is3D) {
            is3D = false;
            radio3D.setChecked(false);
        } else {
            is3D = true;
            radio3D.setChecked(true);
        }
    }

    private boolean cinemaOccupied(){
        for(MovieDisplay movieDisplay : listOfMovieDisplays){
            if(!movieDisplay.getIdCinema().equals(selectedCinema.getId())) {
                continue;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(movieDisplay.getDateTimeOfBeginning());
            cal.add(Calendar.MINUTE, mapOfMoviesNames.get(movieDisplay.getMovieName()).getDuration());
            Date date1 = cal.getTime();
            cal.setTime(dateTimeOfBeginning);
            cal.add(Calendar.MINUTE, selectedMovie.getDuration());
            Date date2 = cal.getTime();
            //ako odabrana projekija pocinje prije nego sto je ova zavrsila a ne zavrsava prije povetka
            //ili ako ova pocienje
            if((dateTimeOfBeginning.before(date1) && !date2.before(movieDisplay.getDateTimeOfBeginning())) || (movieDisplay.getDateTimeOfBeginning().before(date2) && (!date1.before(dateTimeOfBeginning)))){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminAddNewMovieDisplayFragmentListener = (AdminAddNewMovieDisplayFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminAddNewMovieDisplayFragmentListener");
        }
    }

    public interface AdminAddNewMovieDisplayFragmentListener extends IEventManagerActivity {
        void setAddNewMovieDisplayTicketsFragment(MovieDisplay movieDisplay, Cinema cinema);
    }

    public void setListOfMovieDisplays(List<MovieDisplay> listOfMovieDisplays){
        this.listOfMovieDisplays = listOfMovieDisplays;
    }

}
