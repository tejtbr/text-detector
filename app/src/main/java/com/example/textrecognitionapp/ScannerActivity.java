package com.example.textrecognitionapp;


import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;



public class ScannerActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private ImageView imageView;
    private TextView textView;
    private Bitmap bitmap;

    static final int REQUEST_IMAGE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        btn1=findViewById(R.id.button1);
        btn2=findViewById(R.id.button2);
        imageView=findViewById(R.id.imageView2);
        textView=findViewById(R.id.textView2);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkperm()){
                    capturimage();
                }else{
                    requestperm();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detecttext();
            }
        });
    }
    private boolean checkperm(){
        int permcam= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return permcam== PackageManager.PERMISSION_GRANTED;
    }

    //to get the permission
    private void requestperm(){
        int PERMISSION_CODE=200;
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},PERMISSION_CODE);


    }

    private void capturimage(){
        Intent takepicture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takepicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takepicture,REQUEST_IMAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraperm=grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(cameraperm){
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                capturimage();
            }else{
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE&&resultCode==RESULT_OK){
            Bundle extras=data.getExtras();
            bitmap= (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);

        }
    }

    private void detecttext(){
        InputImage image=InputImage.fromBitmap(bitmap,0);
        TextRecognizer recognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result=recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder sb=new StringBuilder();
                for(Text.TextBlock block : text.getTextBlocks()){
                    String blocktext=block.getText();
                    Point[] blockcornerpoints=block.getCornerPoints();
                    Rect blockframe=block.getBoundingBox();
                    for(Text.Line line:block.getLines()){
                        String linetext=line.getText();
                        Point[] linecornerpoints=line.getCornerPoints();
                        Rect linerect=line.getBoundingBox();
                        for(Text.Element element: line.getElements()){
                            String elementtext=element.getText();
                            sb.append(elementtext);
                        }
                        textView.setText(blocktext);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Failed to detect test", Toast.LENGTH_SHORT).show();
            }
        });

    }
}