package com.ursic.nikola.movieticketingsystem.employee;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import Models.Employee;
import Models.MovieDisplay;
import Models.Ticket;

public class EmployeeCheckTicketFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private Ticket ticket;
    private MovieDisplay movieDisplay;

    private TextView textViewMovieName;
    private TextView textViewCinema;
    private TextView textViewSeat;
    private TextView textViewTicketPrice;
    private TextView textView3D;
    private TextView textViewDateTimeOfBeginning;
    private TextView textViewTicketStatus;
    private Button buttonConfirm;
    private Button buttonBack;

    private EmployeeCheckTicketFragmentListener employeeCheckTicketFragmentListener;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_employee_check_ticket, container, false);

        textViewMovieName = (TextView) view.findViewById(R.id.textViewMovieName);
        textViewCinema = (TextView) view.findViewById(R.id.textViewCinema);
        textViewSeat = (TextView) view.findViewById(R.id.textViewSeat);
        textViewTicketPrice = (TextView) view.findViewById(R.id.textViewTicketPrice);
        textView3D = (TextView) view.findViewById(R.id.textView3D);
        textViewDateTimeOfBeginning = (TextView) view.findViewById(R.id.textViewDateTimeOfBeginning);
        buttonConfirm = (Button) view.findViewById(R.id.buttonConfirm);
        buttonBack = (Button) view.findViewById(R.id.buttonBack);
        textViewMovieName.setText(movieDisplay.getMovieName());
        textViewCinema.setText(movieDisplay.getCinemaName());
        textViewTicketStatus = (TextView) view.findViewById(R.id.textViewTicketStatus);
        textViewTicketStatus.setText(translateToCroatian(ticket.getStatus()));
        textViewSeat.setText("" + ticket.getPosotion());
        textViewTicketPrice.setText("" + ticket.getPrice());
        if(movieDisplay.isIs3D()){
            textView3D.setText("DA");
        }else{
            textView3D.setText("NE");
        }
        String dateTimeOfBeginning = android.text.format.DateFormat.format("HH:mm dd-MM-yyyy", movieDisplay.getDateTimeOfBeginning().getTime()).toString();
        dateTimeOfBeginning = dateTimeOfBeginning.substring(0, dateTimeOfBeginning.length() - 4) + movieDisplay.getDateTimeOfBeginning().getYear();
        textViewDateTimeOfBeginning.setText(dateTimeOfBeginning);
        buttonConfirm.setVisibility(View.INVISIBLE);
        String status = ticket.getStatus();
        if (status.equals("RESERVED")) {
            buttonConfirm.setText("Naplati");
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    chargeTicket();
                }
            });
            buttonConfirm.setVisibility(View.VISIBLE);
        } else if (status.equals("PURCHASED")) {
            buttonConfirm.setText("Poništi");
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stampTicket();
                }
            });
            buttonConfirm.setVisibility(View.VISIBLE);
        }

        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                employeeCheckTicketFragmentListener.setStatisticsFragment();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            employeeCheckTicketFragmentListener = (EmployeeCheckTicketFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EmployeeCheckTicketFragmentListener");
        }
    }

    public interface EmployeeCheckTicketFragmentListener extends IEventManagerActivity {
        void setStatisticsFragment();
        Employee getCurrentEmployee();
    }

    public void setTicketAndMovieDisplay(Ticket ticket, MovieDisplay movieDisplay) {
        this.ticket = ticket;
        this.movieDisplay = movieDisplay;
    }

    private void chargeTicket() {
        ticket.setStatus("PURCHASED");
        databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
        Employee employee = employeeCheckTicketFragmentListener.getCurrentEmployee();
        employee.ticketCharged();
        databaseReferenceEmployees.child(employee.getId()).setValue(employee);
        employeeCheckTicketFragmentListener.makeToast("Karta naplaćena");
        employeeCheckTicketFragmentListener.setStatisticsFragment();
    }

    private void stampTicket() {
        ticket.setStatus("USED");
        databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
        Employee employee = employeeCheckTicketFragmentListener.getCurrentEmployee();
        employee.ticketStamped();
        databaseReferenceEmployees.child(employee.getId()).setValue(employee);
        employeeCheckTicketFragmentListener.makeToast("Karta poništena");
        employeeCheckTicketFragmentListener.setStatisticsFragment();
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
