package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

public class SignupPhoneNumberActivity extends AppCompatActivity {

    private ProgressBar progressBar1;
    private Button signupButton1;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_phone_number);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        signupButton1 = findViewById(R.id.signupButton1);
        progressBar1 = findViewById(R.id.progressBar1);
        db = FirebaseFirestore.getInstance();

        CountryCodePicker countryCodePicker = findViewById(R.id.countryCodePicker);
        EditText signupMobileNumberTextView = findViewById(R.id.signupMobileNumberTextView);


        progressBar1.setVisibility(View.GONE);
        countryCodePicker.registerCarrierNumberEditText(signupMobileNumberTextView);

        signupButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setInProgress(true);

                String phoneNumber = countryCodePicker.getFullNumberWithPlus();
                if (!countryCodePicker.isValidFullNumber()) {
                    signupMobileNumberTextView.setError("Phone Number Not Valid");
                    setInProgress(false);
                    return;
                }

                //  Check if the phone number already
                checkIfUserExists(phoneNumber);
            }
        });

        TextView signupToLoginTextView = findViewById(R.id.signupToLoginTextView);
        signupToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupPhoneNumberActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }


    private void checkIfUserExists(String phoneNumber) {
        CollectionReference usersRef = db.collection("users");

        usersRef.whereEqualTo("phone", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        setInProgress(false);

                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {

                                Toast.makeText(SignupPhoneNumberActivity.this, "Phone number already registered.", Toast.LENGTH_SHORT).show();
                            } else {

                                proceedToOtp(phoneNumber);
                            }
                        } else {
                            Toast.makeText(SignupPhoneNumberActivity.this, "Error checking phone number.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    private void proceedToOtp(String phoneNumber) {
        Intent intent = new Intent(SignupPhoneNumberActivity.this, SignupOtpActivity.class);
        intent.putExtra("phone", phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }



    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar1.setVisibility(View.VISIBLE);
            signupButton1.setVisibility(View.GONE);
        } else {
            progressBar1.setVisibility(View.GONE);
            signupButton1.setVisibility(View.VISIBLE);

        }
    }
}
