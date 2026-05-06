package lk.javainstitute.govisevana.navigations;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import lk.javainstitute.govisevana.MainActivity;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.helper.ImageUploadHelper;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextView farmerName, farmerPhone, myAddress;
    private ImageView editProfileButton, farmerProfileImage;
    private Button logoutButton;

    private SharedPreferenceHelper preferenceHelper;
    private FirebaseFirestore db;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);


        farmerName = view.findViewById(R.id.farmerName);
        farmerPhone = view.findViewById(R.id.farmerPhone);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        farmerProfileImage = view.findViewById(R.id.farmerProfileImage);
        logoutButton = view.findViewById(R.id.logoutButton);

        myAddress = view.findViewById(R.id.myAddress);


        preferenceHelper = new SharedPreferenceHelper(requireContext());
        db = FirebaseFirestore.getInstance();


        farmerName.setText(preferenceHelper.getUserName());
        farmerPhone.setText(preferenceHelper.getUserPhone());


        loadProfileImage();


        farmerProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }
        });


        myAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddressPopup();
            }
        });

        LinearLayout myOrdersSection = view.findViewById(R.id.myOrdersSection);
        myOrdersSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToOrdersFragment();
            }
        });

        LinearLayout companyDetails = view.findViewById(R.id.companyDetails);
        companyDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFarmerCompanyFragment();
            }
        });

        return view;
    }

    private void navigateToFarmerCompanyFragment() {
        Fragment companyFragment = new CompanyFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, companyFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToOrdersFragment() {
        Fragment ordersFragment = new OrdersFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ordersFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddressPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_address_details, null);
        builder.setView(dialogView);

        EditText editFullName = dialogView.findViewById(R.id.editFullName);
        EditText editNumber = dialogView.findViewById(R.id.editNumber);
        EditText editAddress = dialogView.findViewById(R.id.editAddress);
        EditText editStreet = dialogView.findViewById(R.id.editStreet);
        Button saveButton = dialogView.findViewById(R.id.saveAddressButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        // address details from Firestore
        loadAddressDetails(editFullName, editNumber, editAddress, editStreet);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddressToFirestore(
                        editFullName.getText().toString().trim(),
                        editNumber.getText().toString().trim(),
                        editAddress.getText().toString().trim(),
                        editStreet.getText().toString().trim()
                );
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void loadAddressDetails(EditText editFullName, EditText editNumber, EditText editAddress, EditText editStreet) {
        db.collection("addresses").document(preferenceHelper.getUserPhone())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            editFullName.setText(documentSnapshot.getString("fullName"));
                            editNumber.setText(documentSnapshot.getString("mobileNumber"));
                            editAddress.setText(documentSnapshot.getString("address"));
                            editStreet.setText(documentSnapshot.getString("city"));
                        }
                    }
                });
    }

    private void saveAddressToFirestore(String fullName, String mobileNumber, String address, String city) {
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("fullName", fullName);
        addressData.put("mobileNumber", mobileNumber);
        addressData.put("address", address);
        addressData.put("city", city);

        db.collection("addresses").document(preferenceHelper.getUserPhone())
                .set(addressData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Address saved successfully!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to save address!", Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_name, null);
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.editNameInput);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        input.setText(preferenceHelper.getUserName());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    updateName(newName);
                    dialog.dismiss();
                } else {
                    input.setError("Name cannot be empty!");
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }




    private void updateName(String newName) {
        String phoneNumber = preferenceHelper.getUserPhone();
        DocumentReference userRef = db.collection("users").document(phoneNumber);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("fullname", newName);

        userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        preferenceHelper.setUserName(newName);
                        farmerName.setText(newName);
                        Toast.makeText(getContext(), "Name updated successfully!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to update name!", Toast.LENGTH_SHORT).show();

                    }
                });
    }



    private void logoutUser() {

        preferenceHelper.setLoggedIn(false);
        preferenceHelper.setUserProfileImage("");


        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        getActivity().finish();
    }


    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            openImagePicker();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            imageUri = data.getData();
            farmerProfileImage.setImageURI(imageUri);


            ImageUploadHelper.uploadImage(requireContext(), imageUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveImageUrlToFirestore(imageUrl);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void saveImageUrlToFirestore(String imageUrl) {
        String phoneNumber = preferenceHelper.getUserPhone();
        DocumentReference userRef = db.collection("users").document(phoneNumber);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("profileImageUrl", imageUrl);

        userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        preferenceHelper.setUserProfileImage(imageUrl);

                        // Update UI
                        new Handler().postDelayed(() -> {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_person_24).into(farmerProfileImage);
                        }, 1000);

                        Toast.makeText(getContext(), "Profile Image Updated!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to Save Image URL!", Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void loadProfileImage() {
        String savedUrl = preferenceHelper.getUserProfileImage();

        if (savedUrl != null && !savedUrl.isEmpty()) {
            Picasso.get().load(savedUrl).placeholder(R.drawable.ic_person_24).into(farmerProfileImage);
        } else {
            String phoneNumber = preferenceHelper.getUserPhone();
            db.collection("users").document(phoneNumber)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String firebaseUrl = documentSnapshot.getString("profileImageUrl");
                                if (firebaseUrl != null && !firebaseUrl.isEmpty()) {
                                    preferenceHelper.setUserProfileImage(firebaseUrl);
                                    Picasso.get().load(firebaseUrl).placeholder(R.drawable.ic_person_24).into(farmerProfileImage);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to Load Profile Image!", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }
}
