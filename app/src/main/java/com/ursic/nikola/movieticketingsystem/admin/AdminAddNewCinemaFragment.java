package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

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

import Models.Cinema;
import Models.Employee;

public class AdminAddNewCinemaFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");
    private Map<String, Employee> mapOfEmployeesUsernames = new HashMap<>();
    private Map<String, Employee> mapOfEmployeesIds = new HashMap<>();
    private List<String> listOfEmployeesUsernames = new ArrayList<>();
    private List<String> listOfCinemaNames = new ArrayList<>();
    private List<String> listOfSelectedEmployeesIds = new ArrayList<>();
    private EditText editCinemaName;
    private Button buttonAddEmployees;
    private Button buttonAddNewCinema;
    private RadioButton radio3D;
    private RadioButton radioNot3D;
    private boolean[] checkedEmployees;
    private boolean posibilityOf3D = false;

    private int numberOfSeats;
    private String seats;

    private AdminAddCinemaFragmentListener adminAddCinemaFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        seats = getArguments().getString("seats");

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_add_new_cinema, container, false);

        editCinemaName = (EditText) view.findViewById(R.id.editEmployeeRealName);
        radio3D = (RadioButton) view.findViewById(R.id.radio3D);
        radioNot3D = (RadioButton) view.findViewById(R.id.radioNot3D);
        buttonAddEmployees = (Button) view.findViewById(R.id.buttonAddEmployees);
        buttonAddNewCinema = (Button) view.findViewById(R.id.buttonAddNewCinema);

        radio3D.setChecked(false);
        radioNot3D.setChecked(true);

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

        buttonAddEmployees.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEmployees();
            }
        });

        buttonAddNewCinema.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addCinema();
            }
        });

        numberOfSeats = seats.length() - seats.replace("U", "").length();

        return view;
    }

    private void loadData(){
        //dohvat podataka o zaposlenicima
        databaseReferenceEmployees.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = EmployeesSnapshot.getValue(Employee.class);
                    mapOfEmployeesUsernames.put(employee.getUsername(), employee);
                    mapOfEmployeesIds.put(employee.getId(), employee);
                    listOfEmployeesUsernames.add(employee.getUsername());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //dohvat podataka o kino dvoranama
        databaseReferenceCinemas.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot CinemaSnapshot : dataSnapshot.getChildren()) {
                    Cinema cinema = CinemaSnapshot.getValue(Cinema.class);
                    listOfCinemaNames.add(cinema.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addEmployees() {
        checkedEmployees = new boolean[listOfEmployeesUsernames.size()];
        final ArrayList<Integer> selectedEmployees = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite zaposlenike za događaj");
        String pomList[] = new String[listOfEmployeesUsernames.size()];
        pomList = listOfEmployeesUsernames.toArray(pomList);
        builder.setMultiChoiceItems(pomList, checkedEmployees, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    if (!selectedEmployees.contains(position)) {
                        selectedEmployees.add(position);
                    } else {
                        selectedEmployees.remove(position);
                    }
                }
            }
        });

        builder.setCancelable(false);
        listOfSelectedEmployeesIds.clear();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j =0, l = selectedEmployees.size();j<l;++j) {
                    listOfSelectedEmployeesIds.add(mapOfEmployeesUsernames.get(listOfEmployeesUsernames.get(selectedEmployees.get(j))).getId());
                }
                if (listOfSelectedEmployeesIds.isEmpty()) {
                    //nije odabran niti jedan zaposlenik
                }
            }
        });

        builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

        String id = databaseReferenceCinemas.push().getKey();
        //dodati id kino dvorane svim odabranim zapolsneicima
        for(int i =0,l = listOfSelectedEmployeesIds.size();i<l;++i){
            Employee employee = mapOfEmployeesIds.get(listOfSelectedEmployeesIds.get(i));
            employee.addCinemaId(id);
            databaseReferenceEmployees.child(employee.getId()).setValue(employee);
        }

        databaseReferenceCinemas.child(id).setValue(new Cinema(id,cinemaName, seats, numberOfSeats,posibilityOf3D));

        adminAddCinemaFragmentListener.hideKeyboard();
        adminAddCinemaFragmentListener.setCinemaFragment();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminAddCinemaFragmentListener = (AdminAddCinemaFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminAddCinemaFragmentListener");
        }
    }

    public interface AdminAddCinemaFragmentListener extends  IEventManagerActivity{
        void setCinemaFragment();
    }

}
