package com.example.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, description, productName, productPrice, saveCurrentDate, saveCurrentTime;
    private Button addNewProductButton;
    private ImageView inputProductImage;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private static final int galleryPick = 1;
    private Uri imageUri;
    private String productRandomKey, downloadImageUrl;
    private StorageReference productImageRef;
    private DatabaseReference productsRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_new_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // getting each category
        categoryName = getIntent().getExtras().get("category").toString();
        productImageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        addNewProductButton = findViewById(R.id.add_new_product_btn);
        inputProductImage = findViewById(R.id.select_product_image);
        inputProductName = findViewById(R.id.product_name);
        inputProductDescription = findViewById(R.id.product_description);
        inputProductPrice = findViewById(R.id.product_price);
        loadingBar = new ProgressDialog(this);

        /*
        When a user clicks the add image icon, the gallery opens and selects and image to add
         */
        inputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        addNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProductData();
            }
        });

    }

    private void validateProductData() {
        description = inputProductDescription.getText().toString();
        productPrice = inputProductPrice.getText().toString();
        productName = inputProductName.getText().toString();

        if (imageUri == null)
            Toast.makeText(this, "Product Image is required", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(description))
            Toast.makeText(this, "Please add description before continuing", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(productPrice))
            Toast.makeText(this, "Please add price before continuing", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(productName))
            Toast.makeText(this, "Please add product name before continuing", Toast.LENGTH_SHORT).show();
        else
            storeProductInfo();
    }

    private void storeProductInfo() {
        loadingBar.setTitle("Adding new Product");
        loadingBar.setMessage("Please wait while the product is being added");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // getting the date and time and storing them as a key
        Calendar calendar = Calendar.getInstance();
        // Using the `dd-MM-yyyy` format becuase firebase interprets `dd/mm/yyyy` as file path
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        saveCurrentTime = currenttime.format(calendar.getTime());
        // combining date and time
        productRandomKey = saveCurrentDate + saveCurrentTime;

        // link of the image being stored inside the firebase datastore
        StorageReference filepath = productImageRef.child(imageUri.getLastPathSegment() + productRandomKey);
        final UploadTask uploadTask = filepath.putFile(imageUri);
        // incase of failure occurrence
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                // diaplay message to user
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful())
                            throw Objects.requireNonNull(task.getException());

                        downloadImageUrl = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();

                    }
                    // tell the admin the task is complete
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();
                            // tell the admin the product image has been saved  to db
                            Toast.makeText(AdminAddNewProductActivity.this, "getting product image Url success...", Toast.LENGTH_SHORT).show();
                        }
                        // store in firebase db
                        saveProductInfoToDB();
                    }
                });
            }
        });
    }

    private void saveProductInfoToDB() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", description);
        productMap.put("image", downloadImageUrl);
        productMap.put("category", categoryName);
        productMap.put("price", productPrice);
        productMap.put("name", productName);

        productsRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class );
                    startActivity(intent);

                    loadingBar.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "product is added successfully", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.dismiss();
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);
    }

    // storing image in firebase


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPick && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            // display image
            Glide.with(this).load(imageUri).into(inputProductImage);
        }
    }
}