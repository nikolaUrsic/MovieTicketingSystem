package com.ursic.nikola.movieticketingsystem.admin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Cinema;
import Models.Employee;

public class AdminAddNewEmployeeFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private DatabaseReference databaseReferenceCinemas = database.getReference("Cinemas");
    private Map<String, Cinema> mapOfCinemasNames = new HashMap<>();
    private List<String> listOfCinemasNames = new ArrayList<>();
    private List<String> listOfEmployeesUsernames = new ArrayList<>();
    private List<String> listOfEmployeesEmails = new ArrayList<>();
    private boolean[] checkedCinemas;
    private List<String> listOfSelectedCinemasIds = new ArrayList<>();
    private EditText editEmployeeRealName;
    private EditText editEmployeeSurname;
    private EditText editEmployeeUsername;
    private EditText editEmployeeEmail;
    private Button buttonAddCinemas;
    private Button buttonAddEmployee;

    private AdminAddNewEmployeeFragmentListener adminAddNewEmployeeFragmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        loadData();

        View view = inflater.inflate(R.layout.fragment_admin_add_new_employee, container, false);

        editEmployeeRealName = (EditText) view.findViewById(R.id.editEmployeeRealName);
        editEmployeeSurname = (EditText) view.findViewById(R.id.editEmployeeSurname);
        editEmployeeUsername = (EditText) view.findViewById(R.id.editEmployeeUsername);
        editEmployeeEmail = (EditText) view.findViewById(R.id.editEmployeeEmail);
        buttonAddCinemas = (Button) view.findViewById(R.id.buttonAddCinemas);
        buttonAddEmployee = (Button) view.findViewById(R.id.buttonAddEmployee);

        editEmployeeRealName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    editEmployeeRealNameChanged();
                }
            }
        });

        editEmployeeSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    editEmployeeSurnameChanged();
                }
            }
        });

        buttonAddCinemas.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addCinemas();
            }
        });


        buttonAddEmployee.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEmployee();
            }
        });

        return view;
    }

    private void addEmployee() {
        //treba provjerit ima li vec koriscnicko ime takvo
        String username = editEmployeeUsername.getText().toString().trim();
        if (listOfEmployeesUsernames.contains(username)) {
            Toast.makeText(getActivity(), "Zaposlenik s ovim korisničkim imenom već postoji", Toast.LENGTH_SHORT).show();
            editEmployeeUsername.requestFocus();
            return;
        }

        //treba provjerit ima li vec takav email
        String email = editEmployeeEmail.getText().toString().trim();
        if (listOfEmployeesEmails.contains(username)) {
            Toast.makeText(getActivity(), "Zaposlenik s ovakvom adresom elektroničke pošte već postoji", Toast.LENGTH_SHORT).show();
            editEmployeeEmail.requestFocus();
            return;
        }

        //ako nema treba dodat novog zaposlenika u bazu
        String name = editEmployeeRealName.getText().toString().trim();
        String surname = editEmployeeSurname.getText().toString().trim();
        String id = databaseReferenceEmployees.push().getKey();
        //password je inicjalno password
        databaseReferenceEmployees.child(id).setValue(new Employee(id, username, name, surname, "5f4dcc3b5aa765d61d8327deb882cf99", listOfSelectedCinemasIds, email, 0, 0));

        adminAddNewEmployeeFragmentListener.setEmployeesFragment();


    }

    private void editEmployeeRealNameChanged() {
        String name = editEmployeeRealName.getText().toString().trim();
        String username = editEmployeeUsername.getText().toString().trim();
        if (username.contains("_")) {
            String[] parts = username.split("_");
            username = name + "_" + parts[1];
        } else {
            username = name + "_";
        }
        editEmployeeUsername.setText(username);

    }

    private void editEmployeeSurnameChanged() {
        String surname = editEmployeeSurname.getText().toString().trim();
        String username = editEmployeeUsername.getText().toString().trim();
        if (username.contains("_")) {
            String[] parts = username.split("_");
            username = parts[0] + "_" + surname;
        } else {
            username = "_" + surname;
        }
        editEmployeeUsername.setText(username);
    }


    private void loadData() {
        //dohvat podataka o zaposlenicima (samo korisnička imena i adrese elektroničkih pošti)
        databaseReferenceEmployees.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = EmployeesSnapshot.getValue(Employee.class);
                    listOfEmployeesUsernames.add(employee.getUsername());
                    listOfEmployeesEmails.add(employee.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReferenceCinemas.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot CinemaSnapshot : dataSnapshot.getChildren()) {
                    Cinema cinema = CinemaSnapshot.getValue(Cinema.class);
                    listOfCinemasNames.add(cinema.getName());
                    mapOfCinemasNames.put(cinema.getName(), cinema);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addCinemas() {
        checkedCinemas = new boolean[listOfCinemasNames.size()];
        final ArrayList<Integer> selectedEmployees = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite u kojim kino dvoranama radi zaposlenik");
        String pomList[] = new String[listOfCinemasNames.size()];
        pomList = listOfCinemasNames.toArray(pomList);
        builder.setMultiChoiceItems(pomList, checkedCinemas, new DialogInterface.OnMultiChoiceClickListener() {
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
        listOfSelectedCinemasIds.clear();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0, l = selectedEmployees.size(); j < l; ++j) {
                    listOfSelectedCinemasIds.add(mapOfCinemasNames.get(listOfCinemasNames.get(selectedEmployees.get(j))).getId());
                }
                if (listOfSelectedCinemasIds.isEmpty()) {
                    //nije odabrana niti jedna kino dvorana
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            adminAddNewEmployeeFragmentListener = (AdminAddNewEmployeeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AdminAddNewEmployeeFragmentListener");
        }
    }

    public interface AdminAddNewEmployeeFragmentListener {
        void setEmployeesFragment();
    }

}
