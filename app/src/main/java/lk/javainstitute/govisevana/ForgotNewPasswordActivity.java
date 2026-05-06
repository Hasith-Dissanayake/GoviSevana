package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ForgotNewPasswordActivity extends AppCompatActivity {

    private TextInputEditText newPasswordInput, confirmPasswordInput;
    private Button resetPasswordButton2;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_new_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        phoneNumber = getIntent().getStringExtra("phone");

        newPasswordInput = findViewById(R.id.signupPasswordInputEditText3);
        confirmPasswordInput = findViewById(R.id.signupRePasswordInputEditText4);
        resetPasswordButton2 = findViewById(R.id.signupButton4);
        progressBar = findViewById(R.id.progressBar1);

        resetPasswordButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (newPassword.isEmpty() || newPassword.length() < 6) {
            newPasswordInput.setError("Password must be at least 6 characters");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        setInProgress(true);

        db.collection("users").document(phoneNumber)
                .update("password", hashPassword(newPassword))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        setInProgress(false);
                        Toast.makeText(ForgotNewPasswordActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setInProgress(false);
                        Toast.makeText(ForgotNewPasswordActivity.this, "Password reset failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ForgotNewPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
            return password;
        }
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            resetPasswordButton2.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            resetPasswordButton2.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
