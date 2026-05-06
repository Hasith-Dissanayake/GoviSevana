package lk.javainstitute.govisevana;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import lk.javainstitute.govisevana.helper.ProximitySensorHelper;
import lk.javainstitute.govisevana.navigations.AddProductFragment;
import lk.javainstitute.govisevana.navigations.FarmerAccountFragment;
import lk.javainstitute.govisevana.navigations.HomeFragment;
import lk.javainstitute.govisevana.navigations.MessagesFragment;
import lk.javainstitute.govisevana.navigations.SearchFragment;

public class FarmerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private ProximitySensorHelper proximitySensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        bottomNavigationView = findViewById(R.id.bottom_navigation);


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_search) {
                    selectedFragment = new SearchFragment();
                } else if (item.getItemId() == R.id.nav_addproduct) {
                    selectedFragment = new AddProductFragment();
                } else if (item.getItemId() == R.id.nav_messages) {
                    selectedFragment = new MessagesFragment();
                } else if (item.getItemId() == R.id.nav_frarmeraccount) {
                    selectedFragment = new FarmerAccountFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        proximitySensorHelper = new ProximitySensorHelper(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        proximitySensorHelper.registerSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        proximitySensorHelper.unregisterSensor();
    }
}
