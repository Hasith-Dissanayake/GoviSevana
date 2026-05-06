package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.UserModel;

public class SignupUserDetailsActivity extends AppCompatActivity {

    private String phoneNumber;
    private FirebaseFirestore db;

    private TextInputEditText fullname, password, rePassword;
    private RadioGroup userType;
    private Button signupButton3;
    private ProgressBar progressBarSignup;

    private SharedPreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_user_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(this);

        fullname = findViewById(R.id.signupFullNameInputEditText);
        password = findViewById(R.id.signupPasswordInputEditText);
        rePassword = findViewById(R.id.signupRePasswordInputEditText);
        userType = findViewById(R.id.radioGroupUserType);
        signupButton3 = findViewById(R.id.signupButton3);
        progressBarSignup = findViewById(R.id.progressBarSignup);


        phoneNumber = getIntent().getStringExtra("phone");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Error: Phone number is missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        signupButton3.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String fullNameText = fullname.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String rePasswordText = rePassword.getText().toString().trim();


        if (fullNameText.isEmpty()) {
            fullname.setError("Full Name is required!");
            return;
        }
        if (passwordText.isEmpty() || passwordText.length() < 6) {
            password.setError("Password must be at least 6 characters!");
            return;
        }
        if (!passwordText.equals(rePasswordText)) {
            rePassword.setError("Passwords do not match!");
            return;
        }

        int selectedUserTypeId = userType.getCheckedRadioButtonId();
        if (selectedUserTypeId == -1) {
            Toast.makeText(this, "Please select user type!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedUserType = findViewById(selectedUserTypeId);
        String userTypeText = selectedUserType.getText().toString().trim();

        // Normalize user type
        if (userTypeText.equalsIgnoreCase("I am a Farmer")) {
            userTypeText = "Farmer";
        } else if (userTypeText.equalsIgnoreCase("I am a Buyer")) {
            userTypeText = "Buyer";
        }

        String hashedPassword = hashPassword(passwordText);


        UserModel user = new UserModel();
        user.setPhone(phoneNumber);
        user.setFullname(fullNameText);
        user.setPassword(hashedPassword);
        user.setUsertype(userTypeText);
        user.setCreatedTimestamp(Timestamp.now());
        user.setActive(true);  // Set user as active by default



        setInProgress(true);
        saveUserToFirestore(user);
    }

    private void saveUserToFirestore(UserModel user) {
        db.collection("users")
                .document(user.getPhone()) // Use phone number as document ID
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SignupUserDetailsActivity.this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                        Log.i("Firestore", "User saved: " + user.getPhone() + " as " + user.getUsertype());

                        // Store login state and user type in SharedPreferences
                        preferenceHelper.setLoggedIn(true);
                        preferenceHelper.setUserType(user.getUsertype());
                        preferenceHelper.setUserName(user.getFullname()); // Store Name
                        preferenceHelper.setUserPhone(user.getPhone());

                        setInProgress(false);
                        navigateToNextActivity(user.getUsertype());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupUserDetailsActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Firestore", "Error saving user", e);
                        setInProgress(false);
                    }
                });


    }

    private void navigateToNextActivity(String userType) {
        Intent intent;
        if ("Farmer".equals(userType)) {

            intent = new Intent(SignupUserDetailsActivity.this, FarmerActivity.class);
        } else {

            intent = new Intent(SignupUserDetailsActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBarSignup.setVisibility(View.VISIBLE);
            signupButton3.setVisibility(View.INVISIBLE);
        } else {
            progressBarSignup.setVisibility(View.GONE);
            signupButton3.setVisibility(View.VISIBLE);
        }
    }

    // Hash password securely using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("PasswordHash", "SHA-256 Algorithm not found, using plain text.");
            return password; // Fallback to plain text
        }
    }
}
