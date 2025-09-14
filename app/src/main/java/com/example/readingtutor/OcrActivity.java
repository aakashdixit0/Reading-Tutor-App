package com.example.readingtutor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class OcrActivity extends AppCompatActivity {

    private TextView tvResult;
    private Button btnPick;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        tvResult = findViewById(R.id.tvOcrResult);
        btnPick = findViewById(R.id.btnPickImage);
        
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    processImageResult(result.getData().getData());
                }
            });

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
    }

    private void processImageResult(Uri uri) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            InputImage image = InputImage.fromBitmap(bmp, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String resultText = text.getText();
                        tvResult.setText(resultText);
                        Intent out = new Intent();
                        out.putExtra("extractedText", resultText);
                        setResult(RESULT_OK, out);
                        finish();
                    })
                    .addOnFailureListener(e -> tvResult.setText("Failed OCR: " + e.getMessage()));
        } catch (IOException e) {
            tvResult.setText("Error loading image: " + e.getMessage());
        }
    }
}
