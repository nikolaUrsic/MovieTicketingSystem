package com.ursic.nikola.movieticketingsystem.user;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import Models.Cinema;
import Models.MovieDisplay;

public class UserMovieDisplayFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");

    private List<MovieDisplay> listOfMovieDisplays = new ArrayList<>();
    private List<MovieDisplay> listOfCurrentMovieDisplays = new ArrayList<>();
    private List<String> listOfSelectedCinemasIds = new ArrayList<>();
    private Map<String, Cinema> mapOfCinemaNames = new HashMap<>();
    private boolean[] checkedCinemas;

    private SimpleAdapter adapter;

    private ListView listView;
    private EditText editMovieName;
    private Button buttonChooseCinema;

    private UserMovieDisplayFragmentListener userMovieDisplayFragmentListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_user_movie_display, container, false);

        //sakri tipkovnicu (ako je otvorena)
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        editMovieName = (EditText) view.findViewById(R.id.editMovieName);
        buttonChooseCinema = (Button) view.findViewById(R.id.buttonChooseCinema);

        listView = (ListView) view.findViewById(R.id.movieDisplaysListView);

        editMovieName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final CharSequence pom = s;
                adapter.getFilter().filter(s);
                listOfCurrentMovieDisplays.clear();
                for (int i = 0, l = listOfMovieDisplays.size(); i < l; ++i) {
                    if (listOfMovieDisplays.get(i).getMovieName().contains(s)) {
                        listOfCurrentMovieDisplays.add(listOfMovieDisplays.get(i));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                MovieDisplay movieDisplay;
                if (!listOfCurrentMovieDisplays.isEmpty()) {
                    movieDisplay = listOfCurrentMovieDisplays.get(pos);
                } else {
                    movieDisplay = listOfMovieDisplays.get(pos);
                }
                userMovieDisplayFragmentListener.setUserBookTicketFragment(movieDisplay);

            }
        });

        buttonChooseCinema.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chooseCinema();
            }
        });

        return view;
    }

    private void loadData() {

        //dohvat podataka od projekcijama filma
        databaseReferenceMovieDisplays.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    MovieDisplay movieDisplay = EmployeesSnapshot.getValue(MovieDisplay.class);
                    if(movieDisplay.isDeleted()){
                        continue;
                    }
                    // mapOfMovieDisplays.put(movieDisplay.getUsername(), movieDisplay);
                    Date currentTime = new Date();
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    currentTime.setYear(year);
                    //filtrirati projekcije (u odnosu na danasnji dan)
                    if (!movieDisplay.getDateTimeOfBeginning().before(currentTime)) {
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

    private void chooseCinema() {
        checkedCinemas = new boolean[mapOfCinemaNames.size()];
        final ArrayList<Integer> selectedEmployees = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite kino dvoranu/dvorane");
        String pomList[] = new String[mapOfCinemaNames.size()];
        pomList = mapOfCinemaNames.keySet().toArray(new String[mapOfCinemaNames.keySet().size()]);
        builder.setMultiChoiceItems(pomList, checkedCinemas, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    //mozda bi bilo bolje koristit set umjesto liste
                    if (!selectedEmployees.contains(position)) {
                        selectedEmployees.add(position);
                    } else {
                        selectedEmployees.remove(position);
                    }
                }
            }
        });

        builder.setCancelable(false);
        listOfSelectedCinemasIds.clear();
        final String[] finalPomList = pomList;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (selectedEmployees.size() == 0) {
                    return;
                } else {
                    for (int j = 0, l = selectedEmployees.size(); j < l; ++j) {
                        listOfSelectedCinemasIds.add(mapOfCinemaNames.get(finalPomList[selectedEmployees.get(j)]).getId());
                    }
                    listOfCurrentMovieDisplays.clear();
                    for (MovieDisplay movieDisplay : listOfMovieDisplays) {
                        if (listOfSelectedCinemasIds.contains(movieDisplay.getIdCinema())) {
                            listOfCurrentMovieDisplays.add(movieDisplay);
                        }
                    }

                    adapter = getMovieDisplayAdapter(listOfCurrentMovieDisplays);
                    listView.setAdapter(adapter);
                }
            }
        });

        builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (selectedEmployees.size() == 0) {
                            userMovieDisplayFragmentListener.makeToast("Potrebno je odabrati barem jednu dvoranu");
                            return;
                        } else {
                            for (int j = 0, l = selectedEmployees.size(); j < l; ++j) {
                                listOfSelectedCinemasIds.add(mapOfCinemaNames.get(finalPomList[selectedEmployees.get(j)]).getId());
                            }
                            listOfCurrentMovieDisplays.clear();
                            for (MovieDisplay movieDisplay : listOfMovieDisplays) {
                                if (listOfSelectedCinemasIds.contains(movieDisplay.getIdCinema())) {
                                    listOfCurrentMovieDisplays.add(movieDisplay);
                                }
                            }

                            adapter = getMovieDisplayAdapter(listOfCurrentMovieDisplays);
                            listView.setAdapter(adapter);
                        }

                        //Dismiss once everything is OK.
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            userMovieDisplayFragmentListener = (UserMovieDisplayFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserMovieDisplayFragmentListener");
        }
    }

    public interface UserMovieDisplayFragmentListener extends IEventManagerActivity {
        void setUserBookTicketFragment(MovieDisplay movieDisplay);
    }


    private SimpleAdapter getMovieDisplayAdapter(List<MovieDisplay> listOfCurrentMovieDisplays) {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (MovieDisplay movieDisplay : listOfCurrentMovieDisplays) {
            Map<String, String> datum = new HashMap<String, String>(2);
            String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
            //String dateTimeOfBeginning = android.text.format.DateFormat.format("hh:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            if(movieDisplay.isIs3D()){
                datum.put("First Line", movieDisplay.getMovieName()+" 3D");
            }else{
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

}


