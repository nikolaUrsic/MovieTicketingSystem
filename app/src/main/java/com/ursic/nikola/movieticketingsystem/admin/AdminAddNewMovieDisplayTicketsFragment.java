package com.ursic.nikola.movieticketingsystem.admin;

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
import android.text.TextUtils;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

import Models.Cinema;
import Models.Employee;
import Models.MovieDisplay;
import Models.Ticket;

public class AdminAddNewMovieDisplayTicketsFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceMovieDisplays = database.getReference("MovieDisplays");

    private Cinema cinema;
    private MovieDisplay movieDisplay;
    private String seats;

    private AdminNewMovieDisplayTicketsFragmentListener adminNewMovieDisplayTicketsFragmentListener;

    private ViewGroup layout;
    private List<TextView> seatViewList = new ArrayList<>();
    private int seatSize = 100;
    private int seatGaping = 10;
    private LinearLayout layoutSeat;

    private EditText editPrice;
    private Button buttonSelectAll;
    private Button buttonAddPrice;
    private Button buttonNext;

    private List<Integer> selectedIds = new ArrayList<>();
    private List<Integer> currentlySelectedIds = new ArrayList<>();
    private Map<Integer, Float> mapOfSeatsAndPrices = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_add_new_movie_display_tickets, container, false);

        layout = view.findViewById(R.id.layoutSeat);
        buttonSelectAll = (Button) view.findViewById(R.id.buttonSelectAll);
        editPrice = (EditText) view.findViewById(R.id.editPrice);
        buttonAddPrice = (Button) view.findViewById(R.id.buttonAddPrice);
        buttonNext = (Button) view.findViewById(R.id.buttonNext);

        buttonSelectAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectAll();
            }
        });

        buttonAddPrice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPrice();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewMovieDisplay();
            }
        });

        updateGUI();

        return view;
    }

    private void updateGUI(){

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int id = v.getId();
                id--; //TODO potencijalno opasno mozda treba maknuti zbog add all-a (dodatno testirati)
                if(selectedIds.contains(id)) {
                    if(currentlySelectedIds.contains(id)) {
                        v.setBackgroundResource(R.drawable.ic_cinema3);
                        currentlySelectedIds.removeAll(Arrays.asList(id));
                    }else{
                        v.setBackgroundResource(R.drawable.ic_cinema2);
                        currentlySelectedIds.add(id);
                    }
                }else{
                    if(currentlySelectedIds.contains(id)) {
                        v.setBackgroundResource(R.drawable.ic_cinema);
                        currentlySelectedIds.removeAll(Arrays.asList(id));
                    }else{
                        v.setBackgroundResource(R.drawable.ic_cinema2);
                        currentlySelectedIds.add(id);
                    }
                }
            }
        };

        layout.removeAllViews();
        layoutSeat = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(8 * seatGaping, 8 * seatGaping, 8 * seatGaping, 8 * seatGaping);
        layout.addView(layoutSeat);

        LinearLayout layout = null;

        int count = 0;

        for (int index = 0; index < seats.length(); index++) {
            if (seats.charAt(index) == '/') {
                layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layoutSeat.addView(layout);
            } else if (seats.charAt(index) == 'U') {
                count++;
                TextView view = new TextView(getContext());
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
                view.setOnClickListener(onClickListener);
                layout.addView(view);
                seatViewList.add(view);
            } else if (seats.charAt(index) == '_') {
                TextView view = new TextView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setText("");
                layout.addView(view);
            }
        }
    }

    private void addPrice(){
        if(TextUtils.isEmpty(editPrice.getText().toString())){
            Toast.makeText(getActivity(), "Molimo Vas unesite cijenu karte", Toast.LENGTH_SHORT).show();
            editPrice.requestFocus();
            return;
        }
        float ticketPrice = Float.parseFloat(editPrice.getText().toString());
        if (ticketPrice <= 0.0) {
            Toast.makeText(getActivity(), "Molimo Vas unesite ispravnu cijenu karte", Toast.LENGTH_SHORT).show();
            editPrice.requestFocus();
            return;
        }
        //dodavanje svih trenutno odabranih u odabrane
        for(int i =0,l=currentlySelectedIds.size();i<l;++i) {
            int pom = currentlySelectedIds.get(i);
            mapOfSeatsAndPrices.put(pom, ticketPrice);
            TextView view123 =   seatViewList.get(pom);
            view123.setBackgroundResource(R.drawable.ic_cinema3);
            if (!selectedIds.contains(pom)) {
                selectedIds.add(pom);
            }
        }
        currentlySelectedIds.clear();
    }

    private void selectAll(){
        currentlySelectedIds.clear();
        int numberOfSeats = (int) seats.chars().filter(new IntPredicate() {
            @Override
            public boolean test(int ch) {
                return ch == 'U';
            }
        }).count();
        for (int index = 0; index < numberOfSeats; index++) {
            TextView view123 =   seatViewList.get(index);
            view123.setBackgroundResource(R.drawable.ic_cinema2);
            currentlySelectedIds.add(index);
        }
    }

    private void addNewMovieDisplay(){
        adminNewMovieDisplayTicketsFragmentListener.hideKeyboard();
        int numberOfSeats = (int) seats.chars().filter(new IntPredicate() {
            @Override
            public boolean test(int ch) {
                return ch == 'U';
            }
        }).count();
        if(mapOfSeatsAndPrices.size()!=numberOfSeats){
            Toast.makeText(getActivity(), "Molimo Vas unesite cijene za sva sjedala", Toast.LENGTH_SHORT).show();
            editPrice.requestFocus();
            return;
        }
        //generirati onoliko karata koliko ima sjedala u kino dvorani
        for (int index = 0; index < numberOfSeats; ++index) {
            float price = mapOfSeatsAndPrices.get(index);
            String id = databaseReferenceTickets.push().getKey();
            Ticket ticket = new Ticket(id, movieDisplay.getId(),index+1 ,price,"AVAILABLE");
            databaseReferenceTickets.child(id).setValue(ticket);
            movieDisplay.addTicket(id);
        }
        databaseReferenceMovieDisplays.child(movieDisplay.getId()).setValue(movieDisplay);
        adminNewMovieDisplayTicketsFragmentListener.hideKeyboard();
        adminNewMovieDisplayTicketsFragmentListener.setMovieDisplayFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminNewMovieDisplayTicketsFragmentListener = (AdminNewMovieDisplayTicketsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminNewMovieDisplayTicketsFragmentListener");
        }
    }

    public void setMovieDisplay(MovieDisplay movieDisplay) {
        this.movieDisplay = movieDisplay;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
        this.seats = cinema.getSeatArrangement();
    }

    public interface AdminNewMovieDisplayTicketsFragmentListener extends IEventManagerActivity {
        void setMovieDisplayFragment();
    }

}
