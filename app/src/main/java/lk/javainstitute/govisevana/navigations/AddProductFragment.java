package lk.javainstitute.govisevana.navigations;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.RecyclerAdapter;
import lk.javainstitute.govisevana.helper.ImageUploadHelper;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.ProductModel;

public class AddProductFragment extends Fragment {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int MAX_IMAGE_COUNT = 10;

    private RecyclerView recyclerView;
    private ImageView addPhotoImageView;
    private EditText productTitle, productDescription, productQuantity, productPrice;
    private Button addProductButton;
    private ProgressBar progressBar;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;

    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;
    private String farmerPhone, farmerName, productId;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        initUI(view);
        loadDataIfEditing();

        addPhotoImageView.setOnClickListener(v -> checkStoragePermission());
        addProductButton.setOnClickListener(v -> validateAndUploadProduct());

        return view;
    }

    private void initUI(View view) {
        recyclerView = view.findViewById(R.id.photosRecyclerView);
        addPhotoImageView = view.findViewById(R.id.AddPhotoImageView);
        productTitle = view.findViewById(R.id.editTextText);
        productDescription = view.findViewById(R.id.textViewProductTitle2);
        productQuantity = view.findViewById(R.id.textViewQuantity);
        productPrice = view.findViewById(R.id.textViewPrice);
        addProductButton = view.findViewById(R.id.addProductButton);
        progressBar = view.findViewById(R.id.progressBarProduct);

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());
        farmerPhone = preferenceHelper.getUserPhone();
        farmerName = preferenceHelper.getUserName();

        recyclerAdapter = new RecyclerAdapter(imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void loadDataIfEditing() {
        if (getArguments() != null) {
            isEditing = true;
            productId = getArguments().getString("productId");
            productTitle.setText(getArguments().getString("title"));
            productDescription.setText(getArguments().getString("description"));
            productQuantity.setText(String.valueOf(getArguments().getInt("quantity")));
            productPrice.setText(String.valueOf(getArguments().getDouble("price")));

            ArrayList<String> imageUrls = getArguments().getStringArrayList("imageUrls");
            if (imageUrls != null) {
                for (String url : imageUrls) {
                    imageUris.add(Uri.parse(url));
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            addProductButton.setText("Update Product");
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(getContext(), "Permission Denied! Allow access to select images.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount() && imageUris.size() < MAX_IMAGE_COUNT; i++) {
                    imageUris.add(clipData.getItemAt(i).getUri());
                }
            } else if (data.getData() != null && imageUris.size() < MAX_IMAGE_COUNT) {
                imageUris.add(data.getData());
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    private void validateAndUploadProduct() {
        String title = productTitle.getText().toString().trim();
        String description = productDescription.getText().toString().trim();
        String quantityText = productQuantity.getText().toString().trim();
        String priceText = productPrice.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            productTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            productDescription.setError("Description is required");
            return;
        }
        if (!quantityText.matches("\\d+")) {
            productQuantity.setError("Enter a valid quantity");
            return;
        }
        if (!priceText.matches("\\d+(\\.\\d{1,2})?")) {
            productPrice.setError("Enter a valid price (e.g., 5.99)");
            return;
        }
        if (imageUris.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        if (isEditing) {
            updateProduct(title, description, Integer.parseInt(quantityText), Double.parseDouble(priceText));
        } else {
            uploadNewProduct(title, description, Integer.parseInt(quantityText), Double.parseDouble(priceText));
        }
    }

    private void updateProduct(String title, String description, int quantity, double price) {
        db.collection("products").document(productId)
                .update("title", title, "description", description, "quantity", quantity, "price", price)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);
                        clearFields();
                        Toast.makeText(getContext(), "Product Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadNewProduct(String title, String description, int quantity, double price) {
        ArrayList<String> uploadedImageUrls = new ArrayList<>();
        for (Uri imageUri : imageUris) {
            ImageUploadHelper.uploadImage(requireContext(), imageUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    if (uploadedImageUrls.size() == imageUris.size()) {
                        saveProductToDatabase(title, description, quantity, price, uploadedImageUrls);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Image Upload Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProductToDatabase(String title, String description, int quantity, double price, ArrayList<String> imageUrls) {

        DocumentReference newProductRef = db.collection("products").document();
        String productId = newProductRef.getId();


        ProductModel product = new ProductModel(
                productId,
                title,
                description,
                quantity,
                price,
                imageUrls,
                farmerName,
                farmerPhone,
                false // Default approval status
        );


        newProductRef.set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Product Added Successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        productTitle.setText("");
        productDescription.setText("");
        productQuantity.setText("");
        productPrice.setText("");
        imageUris.clear();
        recyclerAdapter.notifyDataSetChanged();
    }

}
