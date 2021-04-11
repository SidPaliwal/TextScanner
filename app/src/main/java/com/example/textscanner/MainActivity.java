package com.example.textscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {


    private Uri picUri;
    private int gallery_pressed = 0;


    ImageView imageview;

    TextView textview;
    ImageView camera;
    ImageView Gallery;
    ImageView copy;
    ImageView share;
    ImageView cancel;
    private static final int RESULT_LOAD_IMG = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageview = findViewById(R.id.image_id);
        textview = findViewById(R.id.textId);
        Gallery = findViewById(R.id.gallery_image);
        cancel = findViewById(R.id.cancel_text);

        //check camera permission

        camera = findViewById(R.id.camera_image);
        copy = findViewById(R.id.copy_text);
        share = findViewById(R.id.share_text);

        camera.setOnClickListener(this::doProcess);
        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery_pressed = 1;
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("TextView",textview.getText().toString());
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(getApplicationContext(),"Text Copied",Toast.LENGTH_SHORT).show();


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textview.setText("TextView");
                imageview.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                camera.setVisibility(View.VISIBLE);
                Gallery.setVisibility(View.VISIBLE);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(intent.EXTRA_TEXT,textview.getText().toString());
                startActivity(intent.createChooser(intent,"Share Text"));
            }
        });
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }



    }

    public void copy_share(){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("TextView",textview.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getApplicationContext(),"Text Copied",Toast.LENGTH_SHORT).show();
}

        public void doProcess(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        gallery_pressed =0;


        startActivityForResult(intent,101);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /*if(requestCode==gallery_pick && resultCode==RESULT_OK && data!=null){
            Uri imageuri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }*/

       /* if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            
        }*/
        if (resultCode == RESULT_OK && gallery_pressed==1) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageview.setImageBitmap(selectedImage);

                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(selectedImage);

                FirebaseVision firebaseVision = FirebaseVision.getInstance();
                // create an instance
                FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
                Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);


                task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String t = firebaseVisionText.getText();
                        imageview.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.VISIBLE);

                        textview.setText(t);
                        camera.setVisibility(View.GONE);
                        Gallery.setVisibility(View.GONE);
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }


        if(requestCode==101 && gallery_pressed==0){

            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");

            imageview.setImageBitmap(bitmap);
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVision firebaseVision = FirebaseVision.getInstance();
            // create an instance
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);


            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    String t = firebaseVisionText.getText();
                    imageview.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    textview.setText(t);
                    camera.setVisibility(View.GONE);
                    Gallery.setVisibility(View.GONE);
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }


        
    }



}