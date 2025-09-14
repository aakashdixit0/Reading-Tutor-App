package com.example.readingtutor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SpeechService extends AppCompatActivity {

    private TextView tvSpoken;
    private TextView tvOriginal;
    private Button btnStart;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private String originalText = "";
    private String currentText = ""; 
    private int currentWordPosition = 0; 
    private int voiceProfile = FeedbackTTS.VOICE_FEMALE; 
    private boolean isSpeaking = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        tvSpoken = findViewById(R.id.tvSpoken);
        tvOriginal = findViewById(R.id.tvOriginal);
        btnStart = findViewById(R.id.btnStartStt);

        originalText = getIntent().getStringExtra("originalText");
        if (originalText == null) originalText = "";
        
      
        voiceProfile = getIntent().getIntExtra("voiceProfile", FeedbackTTS.VOICE_FEMALE);
       
        FeedbackTTS.setStaticVoiceProfile(voiceProfile);
        
        
        currentText = originalText;
        currentWordPosition = 0;
        tvOriginal.setText(originalText);

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                btnStart.setText("Start Speaking...");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                btnStart.setText("Listen again");
            }
            @Override public void onError(int error) { 
                tvSpoken.setText("could not recognise the voice " + error); 
                btnStart.setText("Listen again");
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spoken = matches.get(0);
                    tvSpoken.setText(spoken);
                    
               
                    TextComparison.ComparisonResult comparisonResult = 
                        TextComparison.findWrongWordsWithPosition(currentText, spoken);
                    
                    if (comparisonResult.hasErrors && !comparisonResult.wrongWords.isEmpty()) {
                      
                        highlightWrongWords(comparisonResult.wrongWords);
                        
                        
                        applyVoiceProfile(tts);
                        
                       
                        isSpeaking = true;
                        
                   
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wrong_feedback");
                        tts.speak("Some words are still wrong", TextToSpeech.QUEUE_FLUSH, params, "wrong_feedback");
                        
                        
                        handler.postDelayed(() -> {
                           
                            FeedbackTTS.speakWrongWordsStatic(SpeechService.this, comparisonResult.wrongWords, voiceProfile);
                        }, 1000);
                        
                        
                        if (comparisonResult.wrongWordPosition >= 0) {
                            currentWordPosition = currentWordPosition + comparisonResult.wrongWordPosition;
                            currentText = comparisonResult.remainingText;
                            
                         
                            updateCurrentTextDisplay();
                        }
                        
                       
                        handler.postDelayed(() -> {
                            if (!isFinishing()) {
                                isSpeaking = false;
                                Toast.makeText(SpeechService.this, "Now move on from the wrong word", Toast.LENGTH_SHORT).show();
                            }
                        }, 5000);
                    } else {
                       
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "correct_feedback");
                        params.putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
                        
                        
                        if (currentText.trim().isEmpty() || isTextCompleted(spoken, currentText)) {
                            tts.speak("very good your reading is correct", TextToSpeech.QUEUE_FLUSH, params, "correct_feedback");
                            
                            handler.postDelayed(() -> {
                                resetToBeginning();
                            }, 3000);
                        } else {
                            tts.speak("good, keep reading", TextToSpeech.QUEUE_FLUSH, params, "correct_feedback");
                            
                            updateProgressAfterCorrectReading(spoken);
                        }
                    }
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        btnStart.setOnClickListener(v -> {
            if (!isSpeaking) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("hi-IN"));
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "read the lesson");
                recognizer.startListening(intent);
            } else {
                Toast.makeText(this, "Please read the feedback after listening", Toast.LENGTH_SHORT).show();
            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.forLanguageTag("hi-IN"));
                
                applyVoiceProfile(tts);
                
             
                logAvailableVoices();
                
               
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {
                        handler.post(() -> isSpeaking = false);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        handler.post(() -> isSpeaking = false);
                    }
                });
            }
        });
    }
    
    
    private void applyVoiceProfile(TextToSpeech tts) {
        
        Set<Voice> voices = tts.getVoices();
        Voice selectedVoice = null;
        
        
        if (voices != null && !voices.isEmpty()) {
            for (Voice voice : voices) {
                Locale voiceLocale = voice.getLocale();
                String language = voiceLocale.getLanguage();
                
                
                if (language.equals("hi") || language.equals("en")) {
                    
                    if (voiceProfile == FeedbackTTS.VOICE_FEMALE && voice.getName().toLowerCase().contains("female")) {
                        selectedVoice = voice;
                        break;
                    } else if (voiceProfile == FeedbackTTS.VOICE_MALE && voice.getName().toLowerCase().contains("male")) {
                        selectedVoice = voice;
                        break;
                    } else if (voiceProfile == FeedbackTTS.VOICE_CHILD && voice.getName().toLowerCase().contains("child")) {
                        selectedVoice = voice;
                        break;
                    }
                }
            }
            
            
            if (selectedVoice == null) {
                for (Voice voice : voices) {
                    Locale voiceLocale = voice.getLocale();
                    String language = voiceLocale.getLanguage();
                    if (language.equals("hi") || language.equals("en")) {
                        selectedVoice = voice;
                        break;
                    }
                }
            }
            
            
            if (selectedVoice != null) {
                tts.setVoice(selectedVoice);
            }
        }
        
        
        switch (voiceProfile) {
            case FeedbackTTS.VOICE_FEMALE:
                tts.setPitch(1.1f);    
                tts.setSpeechRate(0.8f); 
                break;
            case FeedbackTTS.VOICE_MALE:
                tts.setPitch(0.9f);    
                tts.setSpeechRate(0.75f); 
                break;
            case FeedbackTTS.VOICE_CHILD:
                tts.setPitch(1.2f);    
                tts.setSpeechRate(0.85f); 
                break;
            default:
                tts.setPitch(1.0f);    
                tts.setSpeechRate(0.8f); 
        }
    }
    
    
    private void highlightWrongWords(List<String> wrongWords) {
        
        if (!wrongWords.isEmpty()) {
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wrongWords.size(); i++) {
                sb.append(wrongWords.get(i));
                if (i < wrongWords.size() - 1) {
                    sb.append(", ");
                }
            }
            
            
            TextView tvWrongWords = findViewById(R.id.tvWrongWords);
            if (tvWrongWords != null) {
                tvWrongWords.setText("Wrong words : " + sb.toString());
                tvWrongWords.setVisibility(View.VISIBLE);
                tvWrongWords.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            
            
            Toast.makeText(this, "Wrong words: " + wrongWords.get(0), Toast.LENGTH_SHORT).show();
        }
    }
    
    
    private void logAvailableVoices() {
        if (tts != null) {
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                android.util.Log.d("SpeechService", "Total available voices: " + voices.size());
                for (Voice voice : voices) {
                    Locale locale = voice.getLocale();
                    String language = locale.getLanguage();
                    String country = locale.getCountry();
                    String name = voice.getName();
                    boolean isNetworkVoice = voice.isNetworkConnectionRequired();
                    
                    android.util.Log.d("SpeechService", "Voice: " + name + 
                            ", Language: " + language + 
                            ", Country: " + country + 
                            ", Network: " + isNetworkVoice);
                }
            } else {
                android.util.Log.d("SpeechService", "No voices available");
            }
        }
    }
    
    
    private void updateCurrentTextDisplay() {
        if (currentText != null && !currentText.trim().isEmpty()) {
            
            String displayText = "left to read: " + currentText;
            tvOriginal.setText(displayText);
            tvOriginal.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
    }
    
   
    private boolean isTextCompleted(String spoken, String remaining) {
        if (remaining == null || remaining.trim().isEmpty()) {
            return true;
        }
        
        
        String normSpoken = spoken.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String normRemaining = remaining.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        
        
        return normSpoken.length() >= normRemaining.length() && 
               normRemaining.startsWith(normSpoken.substring(0, Math.min(normSpoken.length(), normRemaining.length())));
    }
    
    
    private void updateProgressAfterCorrectReading(String spoken) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return;
        }
        
        
        String normSpoken = spoken.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String normCurrent = currentText.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        
        String[] spokenWords = normSpoken.split("\\s+");
        String[] currentWords = normCurrent.split("\\s+");
        
        
        int wordsRead = 0;
        for (int i = 0; i < Math.min(spokenWords.length, currentWords.length); i++) {
            if (spokenWords[i].equals(currentWords[i])) {
                wordsRead++;
            } else {
                break;
            }
        }
        
        
        if (wordsRead > 0) {
            currentWordPosition += wordsRead;
            
           
            if (wordsRead < currentWords.length) {
                StringBuilder remainingBuilder = new StringBuilder();
                for (int i = wordsRead; i < currentWords.length; i++) {
                    if (i > wordsRead) remainingBuilder.append(" ");
                    remainingBuilder.append(currentWords[i]);
                }
                currentText = remainingBuilder.toString();
            } else {
                currentText = "";
            }
            
            updateCurrentTextDisplay();
        }
    }
    
   
    private void resetToBeginning() {
        currentText = originalText;
        currentWordPosition = 0;
        tvOriginal.setText(originalText);
        tvOriginal.setTextColor(getResources().getColor(android.R.color.black));
        
        
        TextView tvWrongWords = findViewById(R.id.tvWrongWords);
        if (tvWrongWords != null) {
            tvWrongWords.setVisibility(View.GONE);
        }
        
        Toast.makeText(this, "Start a new lesson ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        handler.removeCallbacksAndMessages(null);
    }
}
