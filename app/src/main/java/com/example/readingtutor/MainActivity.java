package com.example.readingtutor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvOriginal;
    private Button btnUploadImage, btnStartReading;
    private String originalText = "";
    private int selectedVoiceProfile = FeedbackTTS.VOICE_FEMALE; 

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> ocrLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOriginal = findViewById(R.id.tvOriginal);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnStartReading = findViewById(R.id.btnStartReading);
        
        
        setupVoiceProfileRadioButtons();

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            // handle permission results if needed
        });
        
        ocrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String extracted = result.getData().getStringExtra("extractedText");
                    if (extracted != null) {
                        originalText = extracted;
                        tvOriginal.setText(extracted);
                    }
                }
            });

        btnUploadImage.setOnClickListener(v -> {
            // start OcrActivity to pick image and extract text
            Intent i = new Intent(MainActivity.this, OcrActivity.class);
            ocrLauncher.launch(i);
        });

        btnStartReading.setOnClickListener(v -> {
            if (originalText.isEmpty()) {
                tvOriginal.setText("Please upload a reading page first (Image/PDF/Text).");
                return;
            }
            // start speech activity/service (simple approach - start SpeechService activity)
            Intent i = new Intent(MainActivity.this, SpeechService.class);
            i.putExtra("originalText", originalText);
            i.putExtra("voiceProfile", selectedVoiceProfile); 
            startActivity(i);
        });

        // ask required permissions
        requestNeededPermissions();
    }

    private void requestNeededPermissions() {
        String[] perms;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            perms = new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            perms = new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        
        boolean missing = false;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                missing = true; break;
            }
        }
        if (missing) permissionLauncher.launch(perms);
    }

    // We've replaced onActivityResult with ActivityResultLauncher
    
    
    private void setupVoiceProfileRadioButtons() {
        
        android.widget.RadioGroup rgVoiceProfile = findViewById(R.id.rgVoiceProfile);
        android.widget.RadioButton rbFemaleVoice = findViewById(R.id.rbFemaleVoice);
        android.widget.RadioButton rbMaleVoice = findViewById(R.id.rbMaleVoice);
        android.widget.RadioButton rbChildVoice = findViewById(R.id.rbChildVoice);
        
        
        rbFemaleVoice.setChecked(true);
        
        
        rgVoiceProfile.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFemaleVoice) {
                selectedVoiceProfile = FeedbackTTS.VOICE_FEMALE;
            } else if (checkedId == R.id.rbMaleVoice) {
                selectedVoiceProfile = FeedbackTTS.VOICE_MALE;
            } else if (checkedId == R.id.rbChildVoice) {
                selectedVoiceProfile = FeedbackTTS.VOICE_CHILD;
            }
            
            
            FeedbackTTS.setStaticVoiceProfile(selectedVoiceProfile);
        });
    }
}
