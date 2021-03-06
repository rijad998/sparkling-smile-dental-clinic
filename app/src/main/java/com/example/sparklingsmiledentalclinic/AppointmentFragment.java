package com.example.sparklingsmiledentalclinic;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */

public class AppointmentFragment extends Fragment {

    public AppointmentFragment() {
        // Required empty public constructor
    }

    TextView pickDateTxtView;
    Button pickDateBtn;
    Button appointmentBtn;
    Appointment appointment;
    User user;
    Boolean alreadyExist;
    DateC datec;
    Calendar c;
    DatePickerDialog dpd;
    Boolean doExecute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_appointment, container, false);

        pickDateTxtView = (TextView) v.findViewById(R.id.pickDateTxtView);
        pickDateBtn = (Button) v.findViewById(R.id.pickDateBtn);
        appointmentBtn = v.findViewById(R.id.makeAnAppointmentBtn);


        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);

                dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDayOfMonth) {
                        pickDateTxtView.setText(mYear + "/" + (mMonth + 1) + "/" + mDayOfMonth);
                        datec = new DateC(mDayOfMonth, (mMonth + 1), mYear);
                    }
                }, year, month, day);
                dpd.show();
            }
        });

        appointmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickDateTxtView.getText().toString().equals("")) {
                    Toast.makeText(getActivity(),"You need to pick a date for an appointment", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        doExecute = true;
                        createAnAppointment();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return v;
    }

    public void createAnAppointment(){
        final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.getKey().equals(currentUserID)){
                        user = snapshot.getValue(User.class);
                        appointment = new Appointment(user.getName(), user.getPhone());
                        checkTheDatabase();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkTheDatabase() {

            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Appointments");
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.getKey().equals(datec.getYear().toString())) {

                            FirebaseDatabase.getInstance().getReference().child("Appointments").child(datec.getYear().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshotK) {
                                    for (DataSnapshot snapshotM : dataSnapshotK.getChildren()) {

                                        if (snapshotM.getKey().equals(datec.getMonth().toString())) {

                                            FirebaseDatabase.getInstance().getReference().child("Appointments").child(datec.getYear().toString()).child(datec.getMonth().toString()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshotL) {
                                                    for (DataSnapshot snapshotD : dataSnapshotL.getChildren()) {

                                                        if (snapshotD.getKey().equals(datec.getDay().toString())) {

                                                            FirebaseDatabase.getInstance().getReference().child("Appointments").child(datec.getYear().toString()).child(datec.getMonth().toString()).child(datec.getDay().toString()).addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshotF) {

                                                                    long numberOfAppointments = dataSnapshotF.getChildrenCount();
                                                                    if (numberOfAppointments < 11) {
                                                                        Log.i("Possible", "there is space");
                                                                    } else {
                                                                        Toast.makeText(getActivity(), "Sorry but there are no available appointments on " + pickDateTxtView.getText(), Toast.LENGTH_LONG).show();
                                                                        doExecute=false;
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            addAnAppointment();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
                                        } else {
                                            addAnAppointment();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        } else {
                            addAnAppointment();
                        }
                    }
                }

                ;

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }




    public void addAnAppointment() {
        if(doExecute){
        doExecute = false;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Appointments").child(datec.getYear().toString()).child(datec.getMonth().toString()).child(datec.getDay().toString());
        try {
            databaseReference.push().setValue(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Appointment successfully added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Hmmm, something went wrong... Please try again.", Toast.LENGTH_SHORT).show();
                }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }}
}
