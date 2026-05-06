package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText phoneNumberInput;
    private Button sendOtpButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();


        countryCodePicker = findViewById(R.id.countryCodePicker2);
        phoneNumberInput = findViewById(R.id.resetMobileNumberTextView);
        sendOtpButton = findViewById(R.id.resetButton1);
        progressBar = findViewById(R.id.resetProgressBar1);


        countryCodePicker.registerCarrierNumberEditText(phoneNumberInput);


        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleForgotPassword();
            }
        });
    }

    private void handleForgotPassword() {
        String phoneNumber = getPhoneNumber();
        if (phoneNumber == null) {
            return; // Invalid phone number
        }


        checkUserExists(phoneNumber);
    }

    private String getPhoneNumber() {
        if (phoneNumberInput.getText() == null || phoneNumberInput.getText().toString().trim().isEmpty()) {
            phoneNumberInput.setError("Enter your phone number");
            return null;
        }

        try {
            String fullPhoneNumber = countryCodePicker.getFullNumberWithPlus();
            if (!countryCodePicker.isValidFullNumber()) {
                phoneNumberInput.setError("Invalid phone number");
                return null;
            }
            return fullPhoneNumber;
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing phone number", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void checkUserExists(String phoneNumber) {
        setInProgress(true);

        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("phone", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            proceedToOtp(phoneNumber);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Phone number not found!", Toast.LENGTH_SHORT).show();
                            setInProgress(false);
                        }
                    }
                });
    }

    private void proceedToOtp(String phoneNumber) {
        Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordOtpActivity.class);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        finish();
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            sendOtpButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            sendOtpButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
