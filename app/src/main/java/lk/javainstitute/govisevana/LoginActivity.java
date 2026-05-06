package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.UserModel;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;

    private TextInputEditText passwordInput;
    private EditText phoneInput;
    private CountryCodePicker countryCodePicker;
    private Button loginButton;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(this);


        countryCodePicker = findViewById(R.id.countryCodePicker);
        phoneInput = findViewById(R.id.loginMobileNumberTextView);
        passwordInput = findViewById(R.id.loginpasswordlayout1);
        loginButton = findViewById(R.id.loginButton);
        countryCodePicker.registerCarrierNumberEditText(phoneInput);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });


        TextView signupText = findViewById(R.id.loginTextView2);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupPhoneNumberActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });


        TextView loginForgotPassword = findViewById(R.id.signinForgotPassword);
        loginForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String phoneNumber = countryCodePicker.getFullNumberWithPlus().trim();
        String passwordText = passwordInput.getText().toString().trim();

        if (!countryCodePicker.isValidFullNumber()) {
            phoneInput.setError("Invalid phone number!");
            return;
        }
        if (passwordText.isEmpty()) {
            passwordInput.setError("Password is required!");
            return;
        }

        // Hash the entered password for comparison
        String hashedPassword = hashPassword(passwordText);

        db.collection("users").document(phoneNumber).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            if (!user.isActive()) {
                                Toast.makeText(LoginActivity.this, "Your account is " +
                                        "inactive. Please contact GoviSevana Support.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (user.getPassword().equals(hashedPassword)) {
                                // Store login state and user type
                                preferenceHelper.setLoggedIn(true);
                                preferenceHelper.setUserType(user.getUsertype());
                                preferenceHelper.setUserName(user.getFullname()); // Store Name
                                preferenceHelper.setUserPhone(user.getPhone()); // Store Phone

                                navigateToMainActivity(user.getUsertype());
                            } else {
                                Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Login failed: " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void navigateToMainActivity(String userType) {
        Intent intent;
        if ("Farmer".equalsIgnoreCase(userType)) {
            intent = new Intent(LoginActivity.this, FarmerActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


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
            return password;
        }
    }
}
