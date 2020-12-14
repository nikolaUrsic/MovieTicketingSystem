package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.Employee;
import Models.MovieDisplay;

public class AdminCinemaDetailsFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");

    private List<MovieDisplay> listOfMovieDisplays = new ArrayList<>();

    private Cinema cinema;

    private List<String> listOfCinemaNames = new ArrayList<>();
    private EditText editCinemaName;
    private Button buttonAddNewSeatArrangement;
    private Button buttonAddNewCinema;
    private RadioButton radio3D;
    private RadioButton radioNot3D;
    private boolean posibilityOf3D;
    private ViewGroup layout;
    private List<TextView> seatViewList = new ArrayList<>();
    private int seatSize = 100;
    private int seatGaping = 10;
    private LinearLayout layoutSeat;
    private int numberOfSeats;
    private String newSeatLayout;

    private AdminCinemaDetailsFragmentListener adminCinemaDetailsFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_cinema_details, container, false);
        loadData();
        editCinemaName = (EditText) view.findViewById(R.id.editCinemaName);
        radio3D = (RadioButton) view.findViewById(R.id.radio3D);
        radioNot3D = (RadioButton) view.findViewById(R.id.radioNot3D);
        buttonAddNewSeatArrangement=(Button) view.findViewById(R.id.buttonAddNewSeatArrangement);
        buttonAddNewCinema = (Button) view.findViewById(R.id.buttonAddNewCinema);

        radio3D.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radioButtonChanged(true);
            }
        });

        radioNot3D.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radioButtonChanged(false);
            }
        });

        layout = view.findViewById(R.id.layoutSeat);

        buttonAddNewSeatArrangement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                adminCinemaDetailsFragmentListener.addNewSeatArrangement(cinema);
            }
        });
        buttonAddNewCinema.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addCinema();
            }
        });

        String seats = cinema.getSeatArrangement();

        numberOfSeats = seats.length() - seats.replace("U", "").length();
        updateGUI();
        return view;
    }

    private void radioButtonChanged(boolean isNumbered) {
        if (isNumbered) {
            posibilityOf3D = true;
            radio3D.setChecked(true);
            radioNot3D.setChecked(false);
        } else {
            posibilityOf3D = false;
            radio3D.setChecked(false);
            radioNot3D.setChecked(true);
        }
    }

    private void addCinema(){
        //treba provjerit ima li vec kino dvorana sa takvim nazivom
        String cinemaName = editCinemaName.getText().toString().trim();
        if (TextUtils.isEmpty(cinemaName)) {
            Toast.makeText(getActivity(), "Molimo Vas unesite naziv kino dvorane", Toast.LENGTH_SHORT).show();
            editCinemaName.requestFocus();
            return;
        }
        if(listOfCinemaNames.contains(cinemaName)){
            Toast.makeText(getActivity(), "Kino dvorana sa takvim nazivoim već postoji", Toast.LENGTH_SHORT).show();
            editCinemaName.requestFocus();
            return;
        }
        if(newSeatLayout != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(adminCinemaDetailsFragmentListener.getActivityFromFragment());
            builder.setMessage("Želite li promijeniti raspored sjedala? Time se brišu sve projekcije u kino dvorani").setPositiveButton("Potvrdi", dialogClickListener)
                    .setNegativeButton("Odustani", dialogClickListener).show();
        }else{
            databaseReferenceCinemas.child(cinema.getId()).setValue(new Cinema(cinema.getId(),cinemaName, cinema.getSeatArrangement(), numberOfSeats,posibilityOf3D));
            adminCinemaDetailsFragmentListener.hideKeyboard();
            adminCinemaDetailsFragmentListener.setCinemaFragment();
        }
    }

    private void updateGUI() {
        editCinemaName.setText(cinema.getName());
        if(cinema.isPosibilityOf3D()){
            posibilityOf3D = true;
            radio3D.setChecked(true);
            radioNot3D.setChecked(false);
        }else{
            posibilityOf3D = false;
            radio3D.setChecked(false);
            radioNot3D.setChecked(true);
        }

        String seats;
        if(newSeatLayout == null) {
            seats = cinema.getSeatArrangement();
        }else{
            seats = newSeatLayout;
        }

        layout.removeAllViews();
        layoutSeat = new LinearLayout(adminCinemaDetailsFragmentListener.getActivityFromFragment());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(8 * seatGaping, 8 * seatGaping, 8 * seatGaping, 8 * seatGaping);
        layout.addView(layoutSeat);
        LinearLayout layout = null;
        int count = 0;
        for (int index = 0; index < seats.length(); index++) {
            if (seats.charAt(index) == '/') {
                layout = new LinearLayout(adminCinemaDetailsFragmentListener.getActivityFromFragment());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layoutSeat.addView(layout);
            } else if (seats.charAt(index) == 'U') {
                count++;
                TextView view = new TextView(adminCinemaDetailsFragmentListener.getActivityFromFragment());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setBackgroundResource(R.drawable.ic_cinema);
                view.setTextColor(Color.WHITE);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                layout.addView(view);
                seatViewList.add(view);
            } else if (seats.charAt(index) == '_') {
                TextView view = new TextView(adminCinemaDetailsFragmentListener.getActivityFromFragment());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setText("");
                layout.addView(view);
            }
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    String cinemaName = editCinemaName.getText().toString().trim();
                    numberOfSeats = newSeatLayout.length() - newSeatLayout.replace("U", "").length();
                    databaseReferenceCinemas.child(cinema.getId()).setValue(new Cinema(cinema.getId(),cinemaName, newSeatLayout, numberOfSeats,posibilityOf3D));
                    for(MovieDisplay movieDisplay : listOfMovieDisplays){
                        if(movieDisplay.getIdCinema().equals(cinema.getId())){
                            //brisi ALI SA SET DELETED!!
                            movieDisplay.setDeleted(true);
                            databaseReferenceMovieDisplays.child(movieDisplay.getId()).setValue(movieDisplay);
                        }
                    }
                    adminCinemaDetailsFragmentListener.hideKeyboard();
                    adminCinemaDetailsFragmentListener.setCinemaFragment();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    adminCinemaDetailsFragmentListener.setCinemaFragment();
                    break;
            }
        }
    };

    private void loadData() {
        //dohvat podataka o prikazima filmova
        databaseReferenceMovieDisplays.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    MovieDisplay movieDisplay = EmployeesSnapshot.getValue(MovieDisplay.class);
                    listOfMovieDisplays.add(movieDisplay);
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
            adminCinemaDetailsFragmentListener = (AdminCinemaDetailsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminCinemaDetailsFragmentListener");
        }
    }

    public interface AdminCinemaDetailsFragmentListener extends  IEventManagerActivity{
        void setCinemaFragment();
        Activity getActivityFromFragment();
        void addNewSeatArrangement(Cinema cinema);
    }

    public void setCinema(Cinema cinema){
        this.cinema = cinema;
    }

    public void setnewSeatLayout(String newSeatLayout){
        this.newSeatLayout = newSeatLayout;
    }

}
