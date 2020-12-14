package com.ursic.nikola.movieticketingsystem.user;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.Calendar;
import java.util.Date;

import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class UserOneTicketFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceUsers = database.getReference("Users");

    private UserOneTicketFragmentListener userOneTicketFragmentListener;
    private Ticket ticket;
    private MovieDisplay movieDisplay;

    private ImageView imageView;
    private TextView textViewMovieName;
    private TextView textViewCinemaName;
    private TextView textViewTicketPrice;
    private TextView textViewTicketPosition;
    private TextView textViewDateTimeOfBeginning;
    private TextView textViewTicketStatus;
    private Button buttonCancelTicket;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //loadData();

        View view = inflater.inflate(R.layout.fragment_user_one_ticket, container, false);

        //sakri tipkovnicu (ako je otvorena)
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        textViewMovieName = (TextView) view.findViewById(R.id.textViewMovieName);
        textViewCinemaName = (TextView) view.findViewById(R.id.textViewCinemaName);
        textViewTicketPrice = (TextView) view.findViewById(R.id.textViewTicketPrice);
        textViewTicketPosition = (TextView) view.findViewById(R.id.textViewTicketPosition);
        textViewDateTimeOfBeginning = (TextView) view.findViewById(R.id.textViewDateTimeOfBeginning);
        textViewTicketStatus = (TextView) view.findViewById(R.id.textViewTicketStatus);
        buttonCancelTicket = (Button) view.findViewById(R.id.buttonCancelTicket);
        buttonCancelTicket.setVisibility(View.INVISIBLE);


        showTicket();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            userOneTicketFragmentListener = (UserOneTicketFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserOneTicketFragmentListener");
        }
    }

    public interface UserOneTicketFragmentListener extends IEventManagerActivity {
        User getCurrentUser();

        void setTicketsFragment();
    }

    public void setTicketAndMovieDisplay(Ticket ticket, MovieDisplay movieDisplay) {
        this.ticket = ticket;
        this.movieDisplay = movieDisplay;
    }

    private void showTicket() {
        if (ticket.getId() != null) {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = null;
            try {
                bitMatrix = multiFormatWriter.encode(ticket.getId(), BarcodeFormat.QR_CODE, 1025, 1025);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);

            textViewMovieName.setText(movieDisplay.getMovieName());
            textViewCinemaName.setText(movieDisplay.getCinemaName());
            textViewTicketPrice.setText("" + ticket.getPrice());
            textViewTicketPosition.setText("" + ticket.getPosotion());
            // Date pomTime = movieDisplay.getDateTimeOfBeginning();
            // int year = pomTime.getYear();
            //pomTime.setYear(year-1900);
            String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
            dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
            textViewDateTimeOfBeginning.setText(dateTimeOfBeginning);
            String status = ticket.getStatus();
            String status2 = translateToCroatian(status);
            textViewTicketStatus.setText(status2);
            //korinsik može otzakati kartu samo dan prije projekcije
            Date currentTime = new Date();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            currentTime.setYear(year);
            currentTime.setHours(23);
            currentTime.setMinutes(59);
            if (status.equals("RESERVED") && currentTime.before(movieDisplay.getDateTimeOfBeginning())) {
                //otkaži
                buttonCancelTicket.setText("Otkaži");
                buttonCancelTicket.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        cancelTicket();
                    }
                });
                buttonCancelTicket.setVisibility(View.VISIBLE);
            } else if (status.equals("USED") || status.equals("CANCELED")) {
                //izbriši
                buttonCancelTicket.setText("Izbriši");
                buttonCancelTicket.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteTicket();
                    }
                });
                buttonCancelTicket.setVisibility(View.VISIBLE);
            }
        }
    }

    private void cancelTicket() {
        User currentUser = userOneTicketFragmentListener.getCurrentUser();
        currentUser.removeTicket(ticket.getId());
        ticket.setStatus("AVAILABLE");
        databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
        String id = databaseReferenceTickets.push().getKey();
        Ticket newTicket = new Ticket(id, movieDisplay.getId(), ticket.getPosotion(), ticket.getPrice(), "CANCELED");
        currentUser.addTicket(newTicket.getId());
        databaseReferenceUsers.child(currentUser.getId()).setValue(currentUser);
        databaseReferenceTickets.child(newTicket.getId()).setValue(newTicket);
        userOneTicketFragmentListener.setTicketsFragment();
    }

    private void deleteTicket() {
        //samo je maknemo iz liste korniskovih karata
        User currentUser = userOneTicketFragmentListener.getCurrentUser();
        currentUser.removeTicket(ticket.getId());
        databaseReferenceUsers.child(currentUser.getId()).setValue(currentUser);
        userOneTicketFragmentListener.setTicketsFragment();
    }

    private String translateToCroatian(String status) {
        switch (status) {
            case "AVAILABLE":
                return "Dostupna";
            case "RESERVED":
                return "Rezervirana";
            case "PURCHASED":
                return "Kupljena";
            case "USED":
                return "Iskorištena";
            case "CANCELED":
                return "Otkazana";
            default:
                return  "Neodređen status";
        }
    }
}
