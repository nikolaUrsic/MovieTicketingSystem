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

import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Models.Cinema;

public class AdminAddNewCinemaSeatArrangementFragment extends Fragment {

    private ViewGroup layout;
    private List<TextView> seatViewList = new ArrayList<>();
    private int seatSize = 100;
    private int seatGaping = 10;
    private LinearLayout layoutSeat;

    private Button buttonAddFullRow;
    private Button buttonAddEmptyRow;
    private ViewGroup layoutCurrentSeat;
    private Button buttonAddRow;
    private Button buttonDeleteRow;
    private Button buttonNext;
    private String seats="/";
    private String currentSeats="";
    private int numberOfSeatsInOneRow;
    private boolean calledFromAdnimCinemaDetailsFragment = false;
    private Cinema cinema;

    private AdminAddNewCinemaSeatArragmentFragmentListener adminAddNewCinemaSeatArragmentFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initAlertDialog();

        View view = inflater.inflate(R.layout.fragment_admin_add_new_cinema_seat_arrangement, container, false);

        layout = view.findViewById(R.id.layoutSeat);
        layoutCurrentSeat = view.findViewById(R.id.layoutCurrentSeat);
        buttonAddFullRow = (Button) view.findViewById(R.id.buttonAddFullRow);
        buttonAddEmptyRow = (Button) view.findViewById(R.id.buttonAddEmptyRow);
        buttonAddRow = (Button) view.findViewById(R.id.buttonAddRow);
        buttonDeleteRow = (Button) view.findViewById(R.id.buttonDeleteRow);
        buttonNext = (Button) view.findViewById(R.id.buttonNext);

        buttonAddFullRow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addFullRow();
            }
        });
        buttonAddEmptyRow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEmptyRow();
            }
        });
        buttonAddRow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addRow(currentSeats);
            }
        });
        buttonDeleteRow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteRow();
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();
            }
        });

        return view;
    }

    private void initAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Broj sijedala jednog reda");
        alertDialog.setMessage("Molimo Vas unestite maksimalni broj sjedala jednog reda");
        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_cinema);
        alertDialog.setPositiveButton("Potvrdi",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        numberOfSeatsInOneRow = Integer.parseInt(input.getText().toString());
                        for(int i =0; i<numberOfSeatsInOneRow;++i){
                            currentSeats+="U";
                        }
                        refreshLayoutCurrentSeat();
                    }
                });
        alertDialog.setNegativeButton("Odustani",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //vrati se na prijasnji fragment
                        Fragment fragment = new AdminCinemaFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                    }
                });
        alertDialog.show();
    }

    private void addFullRow(){
        for(int i =0;i<numberOfSeatsInOneRow;++i){
            seats+="U";
        }
        seats+="/";
        updateGUI();
    }

    private void addEmptyRow(){
        for(int i = 0;i<numberOfSeatsInOneRow;++i){
            seats+="_";
        }
        seats+="/";
        updateGUI();
    }

    private void addRow(String row){
        if(row.length() == (numberOfSeatsInOneRow)) {
            seats += row;
            seats += "/";
            updateGUI();
        }else{
            Toast.makeText(getContext(),"Nepravilan red", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRow(){
        //ako se ima što izbrisati
        if(seats.length()>1) {
            seats = seats.substring(0, seats.length() - 1 - numberOfSeatsInOneRow);
            updateGUI();
        }
    }

    private void next(){
        if(seats.length()<numberOfSeatsInOneRow){
            Toast.makeText(getContext(),"Potrebno je dodati barem jedan red", Toast.LENGTH_SHORT).show();
            return;
        }
        if(calledFromAdnimCinemaDetailsFragment){
            adminAddNewCinemaSeatArragmentFragmentListener.setCinemaDetailsFragment(seats, cinema);
        }else {
            adminAddNewCinemaSeatArragmentFragmentListener.setAddNewCinemaFragment(seats);
        }
    }

    private void updateGUI(){
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

    private void refreshLayoutCurrentSeat(){
        layoutCurrentSeat.removeAllViews();
        layoutSeat = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(8 * seatGaping, 8 * seatGaping, 8 * seatGaping, 8 * seatGaping);
        layoutCurrentSeat.addView(layoutSeat);
        LinearLayout layout = null;
        int count = 0;
        View.OnClickListener onClickListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                id--;
                StringBuilder myName = new StringBuilder(currentSeats);
                myName.setCharAt(id, '_');
                currentSeats= myName.toString();
                refreshLayoutCurrentSeat();
            }
        };

        View.OnClickListener onClickListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                id--;
                StringBuilder myName = new StringBuilder(currentSeats);
                myName.setCharAt(id, 'U');
                currentSeats= myName.toString();
                refreshLayoutCurrentSeat();
            }
        };

        layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layoutSeat.addView(layout);
        for (int index = 0; index < currentSeats.length(); index++) {
            if (currentSeats.charAt(index) == 'U') {
                count++;
                TextView view = new TextView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setOnClickListener(onClickListener1);
                view.setBackgroundResource(R.drawable.ic_cinema);
                view.setTextColor(Color.WHITE);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                layout.addView(view);
                seatViewList.add(view);
            } else if (currentSeats.charAt(index) == '_') {
                count++;
                TextView view = new TextView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setOnClickListener(onClickListener2);
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setId(count);
                view.setText("");
                layout.addView(view);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminAddNewCinemaSeatArragmentFragmentListener = (AdminAddNewCinemaSeatArragmentFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminAddNewCinemaSeatArragmentFragmentListener");
        }
    }

    public interface AdminAddNewCinemaSeatArragmentFragmentListener {
        void setAddNewCinemaFragment(String seats);
        void setCinemaDetailsFragment(String seats, Cinema cinema);
    }

    public void setCalledFromAdnimCinemaDetailsFragmentAndSetCinema(boolean calledFromAdnimCinemaDetailsFragment, Cinema cinema){
        this.calledFromAdnimCinemaDetailsFragment = calledFromAdnimCinemaDetailsFragment;
        this.cinema = cinema;
    }
}
