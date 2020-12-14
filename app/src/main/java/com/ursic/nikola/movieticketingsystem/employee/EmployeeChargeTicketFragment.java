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
import com.ursic.nikola.movieticketingsystem.user.UserBookTicketFragment;

import Models.Employee;
import Models.MovieDisplay;
import Models.Ticket;
import Models.User;

public class EmployeeChargeTicketFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceTickets = database.getReference("Tickets");
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private Ticket ticket;

    private TextView textTotalPrice;
    private Button buttonConfirm;
    private Button buttonBack;

    private EmployeeChargeTicketFragmentListener employeeChargeTicketFragmentListener;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_employee_charge_ticket, container, false);
        textTotalPrice = (TextView) view.findViewById(R.id.textTotalPrice);
        buttonConfirm = (Button) view.findViewById(R.id.buttonConfirm);
        buttonBack = (Button) view.findViewById(R.id.buttonBack);
        textTotalPrice.setText("" + ticket.getPrice());

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chargeTicket();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                employeeChargeTicketFragmentListener.setStatisticsFragment();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            employeeChargeTicketFragmentListener = (EmployeeChargeTicketFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EmployeeChargeTicketFragmentListener");
        }
    }

    public interface EmployeeChargeTicketFragmentListener extends  IEventManagerActivity{
        void setStatisticsFragment();
        Employee getCurrentEmployee();
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    private void chargeTicket() {
        if (ticket.getStatus().equals("RESERVED")) {
            ticket.setStatus("PURCHASED");
            databaseReferenceTickets.child(ticket.getId()).setValue(ticket);
            Employee employee = employeeChargeTicketFragmentListener.getCurrentEmployee();
            employee.ticketCharged();
            databaseReferenceEmployees.child(employee.getId()).setValue(employee);
            employeeChargeTicketFragmentListener.makeToast("Karta naplaćena");
            employeeChargeTicketFragmentListener.setStatisticsFragment();
        } else if (ticket.getStatus().equals("PURCHASED")){
            employeeChargeTicketFragmentListener.makeToast("Karta je već naplaćena");
            employeeChargeTicketFragmentListener.setStatisticsFragment();
        }else if (ticket.getStatus().equals("USED")){
            employeeChargeTicketFragmentListener.makeToast("Karta je već iskorištena");
            employeeChargeTicketFragmentListener.setStatisticsFragment();
        }
    }
}


