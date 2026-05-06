package lk.javainstitute.govisevana.navigations;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import lk.javainstitute.govisevana.R;

public class CompanyFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng COMPANY_LOCATION = new LatLng(7.6103127, 80.1714454);
    private static final float DEFAULT_ZOOM = 15f;
    private static final String PHONE_NUMBER = "0771234567";

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Handler mainHandler;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_company, container, false);

        mainHandler = new Handler(Looper.getMainLooper());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dialPhoneNumber();
                    } else {
                        Toast.makeText(getContext(), "Permission denied to make phone calls",
                                Toast.LENGTH_SHORT).show();
                    }
                });


        TextView phoneNumberText = rootView.findViewById(R.id.phoneNumberText);
        phoneNumberText.setOnClickListener(v -> checkAndRequestCallPermission());


        mainHandler.postDelayed(() -> {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }, 300);

        return rootView;
    }

    private void checkAndRequestCallPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            dialPhoneNumber();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void dialPhoneNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(COMPANY_LOCATION)
                .title("GoviSevana (PVT) LTD")
                .snippet("Bandarakoswaththa, Kurunegala"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COMPANY_LOCATION, DEFAULT_ZOOM));

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
