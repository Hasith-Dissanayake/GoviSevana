package lk.javainstitute.govisevana;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;



public class SignupOtpActivity extends AppCompatActivity {

    private String phoneNumber;
    private long TIMEOUT_SECONDS = 60L;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private boolean isProcessing = false;

    private FirebaseAuth mAuth;

    private TextView resendCodeTextView, textMobile;
    private ProgressBar progressBar;
    private Button signupButton;
    private EditText[] otpInputs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_otp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();


        textMobile = findViewById(R.id.textMobile);
        phoneNumber = getIntent().getStringExtra("phone");
        if (phoneNumber != null) {
            textMobile.setText(phoneNumber);
        }

        otpInputs = new EditText[]{
                findViewById(R.id.inputcode1),
                findViewById(R.id.inputcode2),
                findViewById(R.id.inputcode3),
                findViewById(R.id.inputcode4),
                findViewById(R.id.inputcode5),
                findViewById(R.id.inputcode6)
        };

        progressBar = findViewById(R.id.progressBar2);
        signupButton = findViewById(R.id.signupButton2);
        resendCodeTextView = findViewById(R.id.resendCodeTextView);


        progressBar.setVisibility(View.GONE);


        setupOTPInputs();

        // Send OTP
        sendOTP(phoneNumber, false);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyOTP();
            }
        });

        resendCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isProcessing) {
                    sendOTP(phoneNumber, true);
                }
            }
        });
    }


    private void sendOTP(String phoneNumber, boolean isResend) {

        startResendTimer();

        setInProgress(true);

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        if (!isProcessing) {
                            isProcessing = true;
                            mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    setInProgress(false);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupOtpActivity.this,"OTP Verified Automatically!",Toast.LENGTH_LONG).show();
                                        navigateToNextActivity();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(SignupOtpActivity.this,"OTP Verification Failed: "+e.getMessage(),Toast.LENGTH_LONG).show();
                        setInProgress(false);
                        isProcessing = false;
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        Toast.makeText(SignupOtpActivity.this,"OTP Sent Successfully",Toast.LENGTH_LONG).show();
                        setInProgress(false);
                    }
                });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }


    private void startResendTimer() {
        resendCodeTextView.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendCodeTextView.setText("Resend OTP in " + millisUntilFinished / 1000 + " seconds");
            }

            public void onFinish() {
                resendCodeTextView.setEnabled(true);
                resendCodeTextView.setText("Resend OTP");
            }
        }.start();
    }


    private void verifyOTP() {
        if (isProcessing) return;
        isProcessing = true;
        setInProgress(true);

        String enteredOTP = getEnteredOTP();
        if (enteredOTP.length() < 6) {
            Toast.makeText(SignupOtpActivity.this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
            setInProgress(false);
            isProcessing = false;
            return;
        }

        if (verificationCode == null) {
            Toast.makeText(SignupOtpActivity.this,"Please request OTP again.",Toast.LENGTH_LONG).show();
            setInProgress(false);
            isProcessing = false;
            return;
        }

        // Verify OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            setInProgress(false);
            isProcessing = false;

            if (task.isSuccessful()) {
                Toast.makeText(SignupOtpActivity.this,"OTP Verified Successfully!",Toast.LENGTH_LONG).show();
                navigateToNextActivity();
            } else {
                Toast.makeText(SignupOtpActivity.this,"Invalid OTP. Try Again.",Toast.LENGTH_LONG).show();
            }
        });
    }


    private void navigateToNextActivity() {
        if (!isFinishing()) {
            Intent intent = new Intent(SignupOtpActivity.this, SignupUserDetailsActivity.class);
            intent.putExtra("phone", phoneNumber);
            startActivity(intent);
            finish();
        }
    }


    private void setupOTPInputs() {
        for (int i = 0; i < otpInputs.length - 1; i++) {
            final int nextIndex = i + 1;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().trim().isEmpty()) {
                        otpInputs[nextIndex].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }


    private String getEnteredOTP() {
        StringBuilder otp = new StringBuilder();
        for (EditText otpInput : otpInputs) {
            otp.append(otpInput.getText().toString().trim());
        }
        return otp.toString();
    }



    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            signupButton.setVisibility(View.GONE);

        } else {
            progressBar.setVisibility(View.GONE);
            signupButton.setVisibility(View.VISIBLE);
        }
    }
}
