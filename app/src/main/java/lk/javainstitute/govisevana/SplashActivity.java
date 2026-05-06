package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 3000;
    private SharedPreferenceHelper preferenceHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferenceHelper = new SharedPreferenceHelper(this);
        db = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserStatus, SPLASH_DURATION);
    }

    private void checkUserStatus() {
        String userPhone = preferenceHelper.getUserPhone(); // Get stored user phone

        if (userPhone == null || userPhone.isEmpty()) {
            handleFirstLaunch(); // Check first launch before navigating
            return;
        }

        db.collection("users").document(userPhone)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("active")) {
                        boolean isActive = documentSnapshot.getBoolean("active");

                        if (!isActive) {
                            preferenceHelper.setLoggedIn(false);
                            Toast.makeText(SplashActivity.this, "Your account has been deactivated.", Toast.LENGTH_LONG).show();
                            handleFirstLaunch();
                        } else {
                            navigateToNextScreen();
                        }
                    } else {
                        handleFirstLaunch();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SplashActivity.this, "Failed to check user status.", Toast.LENGTH_SHORT).show();
                    handleFirstLaunch();
                });
    }

    private void handleFirstLaunch() {
        if (preferenceHelper.isFirstLaunch()) {
            preferenceHelper.setFirstLaunch(false); // Ensure the flag is updated
            Intent intent = new Intent(this, IntroActivity.class);
            Log.d("SplashActivity", "Launching IntroActivity");
            startActivity(intent);
        } else {
            navigateToNextScreen();
        }
        overridePendingTransition(0, 0);
        finish();
    }

    private void navigateToNextScreen() {
        boolean isLoggedIn = preferenceHelper.isLoggedIn();
        String userType = preferenceHelper.getUserType();

        Intent intent;
        if (isLoggedIn) {
            if ("Farmer".equalsIgnoreCase(userType)) {
                intent = new Intent(this, FarmerActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
