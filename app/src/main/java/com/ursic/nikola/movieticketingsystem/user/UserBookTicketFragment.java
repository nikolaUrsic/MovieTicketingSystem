package com.ursic.nikola.movieticketingsystem.user;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;
import com.ursic.nikola.movieticketingsystem.SendMailTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.Employee;
import Models.Movie;
import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class UserBookTicketFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");
    private DatabaseReference databaseReferenceMovies = database.getReference("Movies");

    private Movie movie;
    private MovieDisplay movieDisplay;
    private Cinema cinema;
    private Map<Integer, Ticket> mapOfTickets = new HashMap<>();
    private List<Integer> currentlySelectedIds = new ArrayList<>();

    private TextView textViewMovieName;
    private TextView textViewCinema;
    private TextView textView3D;
    private TextView textViewDateTimeOfBeginning;
    private TextView textViewGenre;
    private TextView textViewMovieDescription;
    private ViewGroup layout;
    private List<TextView> seatViewList = new ArrayList<>();
    private int seatSize = 100;
    private int seatGaping = 10;
    private LinearLayout layoutSeat;

    private Button buttonNext;
    private boolean movieDisplaysLoaded = false;
    private boolean cinemasLoaded = false;
    private boolean movieLoaded = false;

    private UserBookTicketFragmentListener userBookTicketFragmentListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_book_ticket, container, false);

        layout = view.findViewById(R.id.layoutSeat);
        movieDisplaysLoaded=false;
        cinemasLoaded=false;
        movieLoaded=false;
        loadData();
        textViewMovieName = (TextView) view.findViewById(R.id.textViewMovieName);
        textViewCinema = (TextView) view.findViewById(R.id.textViewCinema);
        textView3D = (TextView) view.findViewById(R.id.textView3D);
        textViewGenre = (TextView) view.findViewById(R.id.textViewGenre);
        textViewDateTimeOfBeginning = (TextView) view.findViewById(R.id.textViewDateTimeOfBeginning);
        textViewMovieDescription = (TextView) view.findViewById(R.id.textViewMovieDescription);
        buttonNext = (Button) view.findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            userBookTicketFragmentListener = (UserBookTicketFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserBookTicketFragmentListener");
        }
    }


    private void next() {
        float totalPrice = 0;
        for(int i =0,l=currentlySelectedIds.size();i<l;++i){
            totalPrice+=mapOfTickets.get(currentlySelectedIds.get(i)).getPrice();
        }
        if(!currentlySelectedIds.isEmpty()) {
            initCkeckPriceDialog(currentlySelectedIds.size(), totalPrice);
        }else{
            userBookTicketFragmentListener.makeToast("Niste odabrali niti jedno sjedalo za rezervaciju");
        }
    }

    private void updateGUI() {
        if(cinemasLoaded == false || movieDisplaysLoaded == false || movieLoaded == false){
            return;
        }

        textViewMovieName.setText(movieDisplay.getMovieName());
        textViewCinema.setText(movieDisplay.getCinemaName());
        if(movieDisplay.isIs3D()){
            textView3D.setText("DA");
        }else{
            textView3D.setText("NE");
        }
        String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
        dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
        textViewDateTimeOfBeginning.setText(dateTimeOfBeginning);
        String genres="";
        for(String pom : movie.getGenres()){
            genres+=pom+ " ";
        }
        textViewGenre.setText(genres);
        textViewMovieDescription.setText(movie.getDescription());

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                //id--;
                if (currentlySelectedIds.contains(id)) {
                    v.setBackgroundResource(R.drawable.ic_cinema);
                    currentlySelectedIds.removeAll(Arrays.asList(id));
                } else {
                    v.setBackgroundResource(R.drawable.ic_cinema3);
                    currentlySelectedIds.add(id);
                }
            }
        };

        String seats = cinema.getSeatArrangement();

        layout.removeAllViews();
        layoutSeat = new LinearLayout(userBookTicketFragmentListener.getActivityFromFragment());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(8 * seatGaping, 8 * seatGaping, 8 * seatGaping, 8 * seatGaping);
        layout.addView(layoutSeat);

        LinearLayout layout = null;

        int count = 0;

        for (int index = 0; index < seats.length(); index++) {
            if (seats.charAt(index) == '/') {
                layout = new LinearLayout(userBookTicketFragmentListener.getActivityFromFragment());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layoutSeat.addView(layout);
            } else if (seats.charAt(index) == 'U') {
                count++;

                TextView view = new TextView(userBookTicketFragmentListener.getActivityFromFragment());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                if (mapOfTickets.get(count).getStatus().equals("AVAILABLE")) {
                    view.setBackgroundResource(R.drawable.ic_cinema);
                    view.setOnClickListener(onClickListener);
                } else {
                    view.setBackgroundResource(R.drawable.ic_cinema2);
                }
                view.setTextColor(Color.WHITE);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                layout.addView(view);
                seatViewList.add(view);
            } else if (seats.charAt(index) == '_') {
                TextView view = new TextView(userBookTicketFragmentListener.getActivityFromFragment());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setText("");
                layout.addView(view);
            }
        }
    }

    public void setMovieDisplay(MovieDisplay movieDisplay) {
        this.movieDisplay = movieDisplay;
    }

    public interface UserBookTicketFragmentListener extends IEventManagerActivity {
        void setMovieDisplayFragment();
         User getCurrentUser();
         Activity getActivityFromFragment();
    }


    private void loadData() {


        databaseReferenceTickets.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Ticket ticket1 = EmployeesSnapshot.getValue(Ticket.class);
                    if (movieDisplay.getListOfTicketsIds().contains(ticket1.getId())) {
                        mapOfTickets.put(ticket1.getPosotion(), ticket1);
                    }
                }
                movieDisplaysLoaded = true;
                currentlySelectedIds.clear();
                updateGUI();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //dohvat podataka o kino dvoranama
        databaseReferenceCinemas.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Cinema cinema1 = EmployeesSnapshot.getValue(Cinema.class);
                    if (cinema1.getId().equals(movieDisplay.getIdCinema())) {
                        cinema = cinema1;
                        cinemasLoaded = true;
                        updateGUI();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        databaseReferenceMovies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Movie cinema1 = EmployeesSnapshot.getValue(Movie.class);
                    if (cinema1.getId().equals(movieDisplay.getIdMovie())) {
                        movie =cinema1;
                        movieLoaded = true;
                        updateGUI();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initCkeckPriceDialog(int numberOfTickets, float totalPrice) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(userBookTicketFragmentListener.getActivityFromFragment());
        alertDialog.setTitle("Želite li potvrditi rezervaciju?");
        alertDialog.setMessage("Ukupni iznos za "+numberOfTickets+" karata je: "+totalPrice+" kuna");
        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_cinema);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Potvrdi",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        User user = userBookTicketFragmentListener.getCurrentUser();
                        for(int i =0,l=currentlySelectedIds.size();i<l;++i){
                            Ticket ticket=mapOfTickets.get(currentlySelectedIds.get(i));
                            ticket.setStatus("RESERVED");
                            sendTicketEmail(ticket);
                            user.addTicket(ticket.getId());
                            databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
                        }
                        databaseReferenceUsers.child(user.getId()).setValue(user);
                        userBookTicketFragmentListener.setMovieDisplayFragment();
                        userBookTicketFragmentListener.makeToast("Detalji karte su poslani putem elektroničke pošte");
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Odustani",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void sendTicketEmail(Ticket ticket3) {

        String fromEmail = "donotreplymovieticketingsystem@gmail.com";
        String fromPassword = "sifra123";
        String toEmails = userBookTicketFragmentListener.getCurrentUser().getEmail();
        //String toEmails = user.getEmail();
        List toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));
        String emailSubject = "Potvrda rezervacije karte";
        String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
        dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();

        String emailBody = "Poštovani,\n" +
                "ovime potvrđujemo Vašu rezervaciju karte.\n" +
                "Detalji rezervacije:\n" +
                "\tŠifra karte: "+ ticket3.getId()+"\n"+
                "\tNaziv filma: "+ movieDisplay.getMovieName()+"\n"+
                "\tKino dvorana: "+movieDisplay.getCinemaName()+"\n"+
                "\tDatum i vrijeme početka projekcije: "+dateTimeOfBeginning+"\n"+
                "\tCijena karte: "+ ticket3.getPrice()+"\n"+
                "\tBroj mjesta: "+ ticket3.getPosotion();
        Activity activity = userBookTicketFragmentListener.getActivityFromFragment();
        new SendMailTask(activity).execute(fromEmail,
                fromPassword, toEmailList, emailSubject, emailBody);
    }


}
