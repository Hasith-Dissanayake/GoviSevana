package lk.javainstitute.govisevana;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

import lk.javainstitute.govisevana.helper.ProximitySensorHelper;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.navigations.AccountFragment;
import lk.javainstitute.govisevana.navigations.CartFragment;
import lk.javainstitute.govisevana.navigations.HomeFragment;
import lk.javainstitute.govisevana.navigations.MessagesFragment;
import lk.javainstitute.govisevana.navigations.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private SharedPreferenceHelper preferenceHelper;

    private ProximitySensorHelper proximitySensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        preferenceHelper = new SharedPreferenceHelper(MainActivity.this);


        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = getSelectedFragment(id);

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
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

    private Fragment getSelectedFragment(int id) {
        if (id == R.id.nav_home) {
            return new HomeFragment();
        } else if (id == R.id.nav_search) {
            return new SearchFragment();
        } else if (id == R.id.nav_messages || id == R.id.nav_cart || id == R.id.nav_account) {
            // Check user is logged in
            if (!preferenceHelper.isLoggedIn()) {
                openLoginActivity();
                return null; // Prevent switching to the fragment
            }
            return (id == R.id.nav_messages) ? new MessagesFragment() :
                    (id == R.id.nav_cart) ? new CartFragment() : new AccountFragment();
        }
        return null;
    }

    //load fragment
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    private void openLoginActivity() {
        if (!isLoginActivityRunning()) {
            Intent intent =new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);

        }
    }


    private boolean isLoginActivityRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return topActivity != null && topActivity.getClassName().equals(LoginActivity.class.getName());
        }
        return false;
    }
}
