package com.ursic.nikola.movieticketingsystem.employee;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ursic.nikola.movieticketingsystem.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Models.Cinema;
import Models.Employee;
import Models.Ticket;

public class EmployeeStatisticsFragment extends Fragment {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceEmployees = database.getReference("Employees");
    private String idEmployee;
    private Employee currentEmployee;
    private List<Employee> listOfEmployees = new ArrayList<>();
    RadarChart radarChart;
    TextView txtCharged;
    TextView txtStamped;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_employee_statistics, container, false);
        radarChart = (RadarChart) view.findViewById(R.id.radarChart);
        txtCharged = (TextView) view.findViewById(R.id.txtCharged);
        txtStamped = (TextView) view.findViewById(R.id.txtStamped);
        loadData();
        return view;
    }

    private void loadData() {
        //dohvat podataka o zaposlenicima
        databaseReferenceEmployees.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot EmployeesSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = EmployeesSnapshot.getValue(Employee.class);
                    listOfEmployees.add(employee);
                    if(employee.getId().equals(idEmployee)){
                        currentEmployee = employee;
                        txtCharged.setText("Broj naplaćenih karata: "+currentEmployee.getNumberOfTicketsCharged());
                        txtStamped.setText("Broj poništenih karata: "+currentEmployee.getNumberOfTicketsStamped());
                    }
                }
                initRadarChart();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initRadarChart() {
        RadarDataSet dataset1 = new RadarDataSet(chargedTickets(), "Naplata karata");
        RadarDataSet dataset2 = new RadarDataSet(stampedTickets(), "Poništavanje karata");
        dataset1.setColor(Color.BLUE);
        dataset2.setColor(Color.RED);
        RadarData data = new RadarData();
        data.addDataSet(dataset1);
        data.addDataSet(dataset2);
        radarChart.getYAxis().setEnabled(false);
        XAxis xAxis = radarChart.getXAxis();
        List<String> listOfEmpoloyeesUsernames = new ArrayList<>();
        for (int i = 0, l = listOfEmployees.size(); i < l; ++i) {
            listOfEmpoloyeesUsernames.add(listOfEmployees.get(i).getUsername());
        }
        xAxis.setValueFormatter(new IndexAxisValueFormatter(listOfEmpoloyeesUsernames));
        radarChart.setWebAlpha(180);
        radarChart.setWebColorInner(Color.DKGRAY);
        radarChart.setWebColor(Color.GRAY);
        radarChart.setData(data);
        radarChart.getDescription().setText("");
        radarChart.invalidate();
    }

    private ArrayList<RadarEntry> chargedTickets() {
        ArrayList<RadarEntry> dataVals = new ArrayList<RadarEntry>();
        for (int i = 0, l = listOfEmployees.size(); i < l; ++i) {
            //dataVals.add(new RadarEntry(6));
            dataVals.add(new RadarEntry(listOfEmployees.get(i).getNumberOfTicketsCharged()));
        }
        return dataVals;
    }

    private ArrayList<RadarEntry> stampedTickets() {
        ArrayList<RadarEntry> dataVals = new ArrayList<RadarEntry>();
        for (int i = 0, l = listOfEmployees.size(); i < l; ++i) {
            // dataVals.add(new RadarEntry(1));
            dataVals.add(new RadarEntry(listOfEmployees.get(i).getNumberOfTicketsStamped()));
        }
        return dataVals;
    }


    private class IndexAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues = new String[]{};
        private int mValueCount = 0;

        public IndexAxisValueFormatter(Collection<String> values) {
            if (values != null)
                setValues(values.toArray(new String[values.size()]));
        }

        @Override
        public String getFormattedValue(float value, AxisBase axisBase) {
            int index = Math.round(value);

            if (index < 0 || index >= mValueCount || index != (int) value)
                return "";

            return mValues[index];
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }

        public String[] getValues() {
            return mValues;
        }

        public void setValues(String[] values) {
            if (values == null)
                values = new String[]{};

            this.mValues = values;
            this.mValueCount = values.length;
        }

    }

    public void setEmployeeId(String employeeId){
        idEmployee=employeeId;
    }


}
