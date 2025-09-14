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
    private String currentText = ""; // वर्तमान में पढ़ा जाने वाला टेक्स्ट
    private int currentWordPosition = 0; // वर्तमान शब्द की स्थिति
    private int voiceProfile = FeedbackTTS.VOICE_FEMALE; // डिफ़ॉल्ट वॉइस प्रोफाइल
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
        
        // वॉइस प्रोफाइल प्राप्त करें
        voiceProfile = getIntent().getIntExtra("voiceProfile", FeedbackTTS.VOICE_FEMALE);
        // स्टैटिक वॉइस प्रोफाइल सेट करें
        FeedbackTTS.setStaticVoiceProfile(voiceProfile);
        
        // मूल पाठ को प्रदर्शित करें और वर्तमान टेक्स्ट को इनिशियलाइज़ करें
        currentText = originalText;
        currentWordPosition = 0;
        tvOriginal.setText(originalText);

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                btnStart.setText("बोलना शुरू करें...");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                btnStart.setText("फिर से सुनें");
            }
            @Override public void onError(int error) { 
                tvSpoken.setText("आवाज़ पहचानने में त्रुटि: " + error); 
                btnStart.setText("फिर से सुनें");
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spoken = matches.get(0);
                    tvSpoken.setText(spoken);
                    
                    // नई comparison method का उपयोग करें
                    TextComparison.ComparisonResult comparisonResult = 
                        TextComparison.findWrongWordsWithPosition(currentText, spoken);
                    
                    if (comparisonResult.hasErrors && !comparisonResult.wrongWords.isEmpty()) {
                        // गलत शब्दों को हाइलाइट करें
                        highlightWrongWords(comparisonResult.wrongWords);
                        
                        // वॉइस प्रोफाइल को फिर से लागू करें
                        applyVoiceProfile(tts);
                        
                        // FeedbackTTS का उपयोग करके गलत शब्दों को स्पष्ट रूप से बोलें
                        isSpeaking = true;
                        
                        // पहले एक संक्षिप्त प्रतिक्रिया दें
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wrong_feedback");
                        tts.speak("ध्यान दें, कुछ शब्द गलत हैं", TextToSpeech.QUEUE_FLUSH, params, "wrong_feedback");
                        
                        // थोड़ा रुकें और फिर गलत शब्दों को बोलें
                        handler.postDelayed(() -> {
                            // गलत शब्दों को स्पष्ट और धीमी गति से बोलें
                            FeedbackTTS.speakWrongWordsStatic(SpeechService.this, comparisonResult.wrongWords, voiceProfile);
                        }, 1000);
                        
                        // गलत शब्द की स्थिति को अपडेट करें और बचे हुए टेक्स्ट को सेट करें
                        if (comparisonResult.wrongWordPosition >= 0) {
                            currentWordPosition = currentWordPosition + comparisonResult.wrongWordPosition;
                            currentText = comparisonResult.remainingText;
                            
                            // UI में बचे हुए टेक्स्ट को दिखाएं
                            updateCurrentTextDisplay();
                        }
                        
                        // 5 सेकंड के बाद फिर से सुनना शुरू करें
                        handler.postDelayed(() -> {
                            if (!isFinishing()) {
                                isSpeaking = false;
                                Toast.makeText(SpeechService.this, "अब गलत शब्द से आगे पढ़ें", Toast.LENGTH_SHORT).show();
                            }
                        }, 5000);
                    } else {
                        // सही होने पर अगले भाग पर जाएं
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "correct_feedback");
                        params.putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
                        
                        // अगर पूरा टेक्स्ट पढ़ लिया गया है
                        if (currentText.trim().isEmpty() || isTextCompleted(spoken, currentText)) {
                            tts.speak("बहुत बढ़िया! आपने पूरा पाठ सही पढ़ा है", TextToSpeech.QUEUE_FLUSH, params, "correct_feedback");
                            // पूरा होने पर मूल टेक्स्ट को रीसेट करें
                            handler.postDelayed(() -> {
                                resetToBeginning();
                            }, 3000);
                        } else {
                            tts.speak("बहुत अच्छा! आगे पढ़ते रहें", TextToSpeech.QUEUE_FLUSH, params, "correct_feedback");
                            // सही पढ़ने पर आगे बढ़ें
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
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "पाठ को पढ़ें");
                recognizer.startListening(intent);
            } else {
                Toast.makeText(this, "कृपया प्रतिक्रिया सुनने के बाद पढ़ें", Toast.LENGTH_SHORT).show();
            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.forLanguageTag("hi-IN"));
                // वॉइस प्रोफाइल के अनुसार पिच, स्पीड और वॉइस सेट करें
                applyVoiceProfile(tts);
                
                // उपलब्ध वॉइस की जानकारी लॉग करें
                logAvailableVoices();
                
                // TTS के पूरा होने पर सूचित करें
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
    
    /**
     * वॉइस प्रोफाइल के अनुसार पिच, स्पीड और वॉइस सेट करता है
     */
    private void applyVoiceProfile(TextToSpeech tts) {
        // उपलब्ध वॉइस की जांच करें
        Set<Voice> voices = tts.getVoices();
        Voice selectedVoice = null;
        
        // सबसे अच्छी वॉइस का चयन करें
        if (voices != null && !voices.isEmpty()) {
            for (Voice voice : voices) {
                Locale voiceLocale = voice.getLocale();
                String language = voiceLocale.getLanguage();
                
                // हिंदी वॉइस खोजें
                if (language.equals("hi") || language.equals("en")) {
                    // वॉइस प्रोफाइल के अनुसार वॉइस चुनें
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
            
            // अगर कोई विशिष्ट वॉइस नहीं मिली, तो कोई भी हिंदी/अंग्रेजी वॉइस चुनें
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
            
            // अगर वॉइस मिली है तो उसे सेट करें
            if (selectedVoice != null) {
                tts.setVoice(selectedVoice);
            }
        }
        
        // पिच और स्पीड सेट करें
        switch (voiceProfile) {
            case FeedbackTTS.VOICE_FEMALE:
                tts.setPitch(1.1f);    // थोड़ा ऊंचा पिच (महिला वॉइस)
                tts.setSpeechRate(0.8f); // धीमा स्पीड (बेहतर समझ के लिए)
                break;
            case FeedbackTTS.VOICE_MALE:
                tts.setPitch(0.9f);    // कम पिच (पुरुष वॉइस)
                tts.setSpeechRate(0.75f); // और भी धीमा स्पीड
                break;
            case FeedbackTTS.VOICE_CHILD:
                tts.setPitch(1.2f);    // ऊंचा पिच (बच्चे की वॉइस)
                tts.setSpeechRate(0.85f); // मध्यम स्पीड
                break;
            default:
                tts.setPitch(1.0f);    // डिफ़ॉल्ट पिच
                tts.setSpeechRate(0.8f); // धीमा स्पीड
        }
    }
    
    // गलत शब्दों को हाइलाइट करने के लिए मेथड
    private void highlightWrongWords(List<String> wrongWords) {
        // गलत शब्दों को UI पर हाइलाइट करें
        if (!wrongWords.isEmpty()) {
            // गलत शब्दों को एक स्ट्रिंग में जोड़ें
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wrongWords.size(); i++) {
                sb.append(wrongWords.get(i));
                if (i < wrongWords.size() - 1) {
                    sb.append(", ");
                }
            }
            
            // गलत शब्दों को टेक्स्टव्यू में दिखाएं
            TextView tvWrongWords = findViewById(R.id.tvWrongWords);
            if (tvWrongWords != null) {
                tvWrongWords.setText("गलत शब्द: " + sb.toString());
                tvWrongWords.setVisibility(View.VISIBLE);
                tvWrongWords.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            
            // पहले गलत शब्द को टोस्ट में भी दिखाएं
            Toast.makeText(this, "गलत शब्द: " + wrongWords.get(0), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * उपलब्ध वॉइस की जानकारी लॉग करता है
     */
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
                    
                    // लॉग में वॉइस की जानकारी प्रिंट करें
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
    
    /**
     * वर्तमान टेक्स्ट डिस्प्ले को अपडेट करता है
     */
    private void updateCurrentTextDisplay() {
        if (currentText != null && !currentText.trim().isEmpty()) {
            // वर्तमान टेक्स्ट को हाइलाइट करके दिखाएं
            String displayText = "पढ़ने के लिए बचा हुआ: " + currentText;
            tvOriginal.setText(displayText);
            tvOriginal.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
    }
    
    /**
     * जांचता है कि क्या टेक्स्ट पूरा हो गया है
     */
    private boolean isTextCompleted(String spoken, String remaining) {
        if (remaining == null || remaining.trim().isEmpty()) {
            return true;
        }
        
        // सामान्यीकरण
        String normSpoken = spoken.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String normRemaining = remaining.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        
        // अगर बोला गया टेक्स्ट बचे हुए टेक्स्ट के बराबर या उससे ज्यादा है
        return normSpoken.length() >= normRemaining.length() && 
               normRemaining.startsWith(normSpoken.substring(0, Math.min(normSpoken.length(), normRemaining.length())));
    }
    
    /**
     * सही पढ़ने के बाद प्रगति को अपडेट करता है
     */
    private void updateProgressAfterCorrectReading(String spoken) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return;
        }
        
        // सामान्यीकरण
        String normSpoken = spoken.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String normCurrent = currentText.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        
        String[] spokenWords = normSpoken.split("\\s+");
        String[] currentWords = normCurrent.split("\\s+");
        
        // पढ़े गए शब्दों की संख्या गिनें
        int wordsRead = 0;
        for (int i = 0; i < Math.min(spokenWords.length, currentWords.length); i++) {
            if (spokenWords[i].equals(currentWords[i])) {
                wordsRead++;
            } else {
                break;
            }
        }
        
        // अगर कुछ शब्द सही पढ़े गए हैं तो आगे बढ़ें
        if (wordsRead > 0) {
            currentWordPosition += wordsRead;
            
            // बचे हुए टेक्स्ट को अपडेट करें
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
    
    /**
     * शुरुआत में रीसेट करता है
     */
    private void resetToBeginning() {
        currentText = originalText;
        currentWordPosition = 0;
        tvOriginal.setText(originalText);
        tvOriginal.setTextColor(getResources().getColor(android.R.color.black));
        
        // गलत शब्दों का डिस्प्ले छुपाएं
        TextView tvWrongWords = findViewById(R.id.tvWrongWords);
        if (tvWrongWords != null) {
            tvWrongWords.setVisibility(View.GONE);
        }
        
        Toast.makeText(this, "नया पाठ शुरू करें", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        handler.removeCallbacksAndMessages(null);
    }
}
