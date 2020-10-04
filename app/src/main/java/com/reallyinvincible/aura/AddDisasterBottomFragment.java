package com.reallyinvincible.aura;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.nex3z.togglebuttongroup.button.LabelToggle;
import com.reallyinvincible.aura.activities.DisasterMapsActivity;
import com.reallyinvincible.aura.activities.HomeActivity;
import com.reallyinvincible.aura.activities.MapActivity;
import com.reallyinvincible.aura.models.Information;
import com.reallyinvincible.aura.utils.UtilConstants;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddDisasterBottomFragment extends BottomSheetDialogFragment {

    private SingleSelectToggleGroup singleSelectToggleGroup;
    private FusedLocationProviderClient mFusedLocationClient;
    private String phoneNumber, disasterType;
    private int intensityLevel;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private View editTextLayoutContainer;
    private EditText disasterTypeEditText, disasterIntensityEditText;
    private Boolean isOther;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_disaster, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Information", Context.MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("UserPhoneNumber", "");
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("DisasterInformation");
        disasterTypeEditText = view.findViewById(R.id.et_disaster_type);
        disasterIntensityEditText = view.findViewById(R.id.et_intensity_level);
        editTextLayoutContainer = view.findViewById(R.id.container_edit_text);
        submitButton = view.findViewById(R.id.btn_submit_disaster);

        singleSelectToggleGroup = view.findViewById(R.id.stg_disaster_type);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        final String disasterArray[] = UtilConstants.arr;
        final int length = disasterArray.length;

        for (int i = 0; i < length; i++) {
            LabelToggle labelToggle = new LabelToggle(singleSelectToggleGroup.getContext());
            labelToggle.setText(disasterArray[i]);
            labelToggle.setId(i);
            labelToggle.setMarkerColor(getResources().getColor(R.color.colorAccent));
            labelToggle.setTextColor(getResources().getColor(R.color.textColor));
            singleSelectToggleGroup.addView(labelToggle, new LabelToggle
                    .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        LabelToggle labelToggle = new LabelToggle(singleSelectToggleGroup.getContext());
        labelToggle.setText("other");
        labelToggle.setId(length);
        labelToggle.setMarkerColor(getResources().getColor(R.color.colorAccent));
        labelToggle.setTextColor(getResources().getColor(R.color.textColor));
        singleSelectToggleGroup.addView(labelToggle, new LabelToggle
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        SingleSelectToggleGroup.OnCheckedChangeListener onCheckedChangeListener = new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
                if (checkedId == length) {
                    isOther = true;
                    editTextLayoutContainer.setVisibility(View.VISIBLE);
                    disasterTypeEditText.setVisibility(View.VISIBLE);
                } else {
                    isOther = false;
                    editTextLayoutContainer.setVisibility(View.GONE);
                    disasterTypeEditText.setVisibility(View.GONE);
                    disasterType = disasterArray[checkedId];
                }
            }
        };

        singleSelectToggleGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        submitButton.setOnClickListener(view1 -> {
            if (isOther) {
                disasterType = disasterTypeEditText.getText().toString();
            }
            intensityLevel = Integer.parseInt(disasterIntensityEditText.getText().toString());
            DisasterMapsActivity.getDialogueControlInterface().dismiss();
            getLocation();

        });

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        // Logic to handle location object
                        Information information = new Information(location.getLatitude(), location.getLongitude(), phoneNumber, disasterType, intensityLevel);
                        mDatabaseReference.push().setValue(information);
                    }
                });
    }

}
