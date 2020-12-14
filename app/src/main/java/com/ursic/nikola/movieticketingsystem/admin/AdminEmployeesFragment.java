package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.IEventManagerActivity;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Cinema;
import Models.Employee;


public class AdminEmployeesFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");

    private Map<String, Employee> mapOfEmployees = new HashMap<>();

    private ListView listView;
    private List<Employee> listOfEmployees = new ArrayList<>();
    private Button buttonAddNewEmployee;

    private IEventManagerActivity interfaceEventManagerActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_employees, container, false);

        listView = (ListView) view.findViewById(R.id.emlpoyeesListView);

        buttonAddNewEmployee = (Button) view.findViewById(R.id.buttonAddNewEmployee);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Employee selectedEmployee = listOfEmployees.get(pos);
                AdminEmployeeDetailsFragment fragment = new AdminEmployeeDetailsFragment();
                fragment.setEmployee(selectedEmployee);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_contanier, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        buttonAddNewEmployee.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewEmployee();
            }
        });

        return view;
    }

    private void loadData() {

        //dohvat podataka o zaposlenicima
        databaseReferenceEmployees.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapOfEmployees.clear();
                listOfEmployees.clear();
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = EmployeesSnapshot.getValue(Employee.class);
                    mapOfEmployees.put(employee.getUsername(), employee);
                    listOfEmployees.add(employee);
                }
                String[] arrayTicketNames = new String[mapOfEmployees.size()];
                for(int i =0,l=listOfEmployees.size();i<l;i++){
                    arrayTicketNames[i]=listOfEmployees.get(i).getName() +" " + listOfEmployees.get(i).getSurname();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(interfaceEventManagerActivity.getApplicationFromFragment(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, arrayTicketNames);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addNewEmployee(){
        AdminAddNewEmployeeFragment fragment = new AdminAddNewEmployeeFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_contanier, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            interfaceEventManagerActivity = (IEventManagerActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IEventManagerActivity");
        }
    }


}
