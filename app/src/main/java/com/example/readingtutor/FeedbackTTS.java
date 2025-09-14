package com.example.readingtutor;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FeedbackTTS {
    private TextToSpeech tts;
    private FeedbackListener listener;
    
    // वॉइस प्रोफाइल्स
    public static final int VOICE_FEMALE = 0;  // महिला वॉइस (डिफ़ॉल्ट)
    public static final int VOICE_MALE = 1;    // पुरुष वॉइस
    public static final int VOICE_CHILD = 2;   // बच्चे की वॉइस
    
    private int voiceProfile = VOICE_FEMALE;  // डिफ़ॉल्ट वॉइस प्रोफाइल

    public interface FeedbackListener {
        void onFeedbackComplete();
    }

    public FeedbackTTS(Context ctx, FeedbackListener listener) {
        this(ctx, listener, VOICE_FEMALE);
    }
    
    public FeedbackTTS(Context ctx, FeedbackListener listener, int voiceProfile) {
        this.listener = listener;
        this.voiceProfile = voiceProfile;
        tts = new TextToSpeech(ctx, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.forLanguageTag("hi-IN"));
                // वॉइस प्रोफाइल के अनुसार पिच और स्पीड सेट करें
                applyVoiceProfile(tts, voiceProfile);
                
                // फीडबैक पूरा होने पर सूचित करें
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {
                        if (listener != null && utteranceId.equals("last_word")) {
                            listener.onFeedbackComplete();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (listener != null) {
                            listener.onFeedbackComplete();
                        }
                    }
                });
            }
        });
    }

    /**
     * गलत शब्दों को बोलता है - बेहतर उच्चारण के साथ
     */
    public void speakWrongWords(List<String> wrongWords) {
        if (wrongWords.isEmpty()) return;
        
        // पहले एक प्रोत्साहन संदेश बोलें
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro");
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); // अधिकतम वॉल्यूम
        tts.speak("ध्यान दें", TextToSpeech.QUEUE_FLUSH, params, "intro");
        
        // फिर गलत शब्दों को स्पष्ट रूप से और धीमी गति से बोलें
        for (int i = 0; i < wrongWords.size(); i++) {
            String w = wrongWords.get(i);
            String utterId = (i == wrongWords.size() - 1) ? "last_word" : "word_" + i;
            params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); // अधिकतम वॉल्यूम
            // शब्द के बीच में थोड़ा रुकें
            try {
                Thread.sleep(300); // 300 मिलीसेकंड का विराम
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tts.speak(w, TextToSpeech.QUEUE_ADD, params, utterId);
        }
    }

    public void shutdown() {
        if (tts != null) tts.shutdown();
    }
    
    /**
     * वॉइस प्रोफाइल के अनुसार पिच, स्पीड और वॉइस सेट करता है
     */
    private static void applyVoiceProfile(TextToSpeech tts, int voiceProfile) {
        // उपलब्ध वॉइस की जांच करें
        Set<Voice> voices = tts.getVoices();
        Voice selectedVoice = null;
        
        // सबसे अच्छी वॉइस का चयन करें
        if (voices != null && !voices.isEmpty()) {
            for (Voice voice : voices) {
                Locale voiceLocale = voice.getLocale();
                String language = voiceLocale.getLanguage();
                String country = voiceLocale.getCountry();
                
                // हिंदी वॉइस खोजें
                if (language.equals("hi") || language.equals("en")) {
                    // वॉइस प्रोफाइल के अनुसार वॉइस चुनें
                    if (voiceProfile == VOICE_FEMALE && voice.getName().toLowerCase().contains("female")) {
                        selectedVoice = voice;
                        break;
                    } else if (voiceProfile == VOICE_MALE && voice.getName().toLowerCase().contains("male")) {
                        selectedVoice = voice;
                        break;
                    } else if (voiceProfile == VOICE_CHILD && voice.getName().toLowerCase().contains("child")) {
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
            case VOICE_FEMALE:
                tts.setPitch(1.1f);    // थोड़ा ऊंचा पिच (महिला वॉइस)
                tts.setSpeechRate(0.8f); // धीमा स्पीड (बेहतर समझ के लिए)
                break;
            case VOICE_MALE:
                tts.setPitch(0.9f);    // कम पिच (पुरुष वॉइस)
                tts.setSpeechRate(0.75f); // और भी धीमा स्पीड
                break;
            case VOICE_CHILD:
                tts.setPitch(1.2f);    // ऊंचा पिच (बच्चे की वॉइस)
                tts.setSpeechRate(0.85f); // मध्यम स्पीड
                break;
            default:
                tts.setPitch(1.0f);    // डिफ़ॉल्ट पिच
                tts.setSpeechRate(0.8f); // धीमा स्पीड
        }
    }
    
    /**
     * वॉइस प्रोफाइल सेट करता है
     */
    public void setVoiceProfile(int voiceProfile) {
        this.voiceProfile = voiceProfile;
        if (tts != null) {
            applyVoiceProfile(tts, voiceProfile);
        }
    }

    // static helpers for quick usage from activity
    private static TextToSpeech[] ttsRef = new TextToSpeech[1];
    private static int staticVoiceProfile = VOICE_FEMALE; // डिफ़ॉल्ट वॉइस प्रोफाइल

    /**
     * स्टैटिक वॉइस प्रोफाइल सेट करता है
     */
    public static void setStaticVoiceProfile(int voiceProfile) {
        staticVoiceProfile = voiceProfile;
        if (ttsRef[0] != null) {
            applyVoiceProfile(ttsRef[0], voiceProfile);
        }
    }

    /**
     * स्टैटिक तरीके से गलत शब्दों को बोलता है - बेहतर उच्चारण के साथ
     */
    public static void speakWrongWordsStatic(Context ctx, List<String> wrong) {
        speakWrongWordsStatic(ctx, wrong, staticVoiceProfile);
    }

    public static void speakWrongWordsStatic(Context ctx, List<String> wrong, int voiceProfile) {
        if (ttsRef[0] == null) {
            ttsRef[0] = new TextToSpeech(ctx, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsRef[0].setLanguage(Locale.forLanguageTag("hi-IN"));
                    // वॉइस प्रोफाइल के अनुसार पिच और स्पीड सेट करें
                    applyVoiceProfile(ttsRef[0], voiceProfile);
                    
                    if (!wrong.isEmpty()) {
                        // पहले एक प्रोत्साहन संदेश बोलें
                        Bundle params = new Bundle();
                        String utterId = UUID.randomUUID().toString();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                        ttsRef[0].speak("ध्यान दें", TextToSpeech.QUEUE_FLUSH, params, utterId);
                        
                        // फिर गलत शब्दों को बोलें
                        for (String w : wrong) {
                            utterId = UUID.randomUUID().toString();
                            params = new Bundle();
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                            ttsRef[0].speak(w, TextToSpeech.QUEUE_ADD, params, utterId);
                        }
                    }
                }
            });
        } else {
            if (!wrong.isEmpty()) {
                // पहले एक प्रोत्साहन संदेश बोलें
                Bundle params = new Bundle();
                String utterId = UUID.randomUUID().toString();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                ttsRef[0].speak("ध्यान दें", TextToSpeech.QUEUE_FLUSH, params, utterId);
                
                // फिर गलत शब्दों को बोलें
                for (String w : wrong) {
                    utterId = UUID.randomUUID().toString();
                    params = new Bundle();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                    ttsRef[0].speak(w, TextToSpeech.QUEUE_ADD, params, utterId);
                }
            }
        }
    }

    public static void speakTextStatic(Context ctx, String text) {
        speakTextStatic(ctx, text, staticVoiceProfile);
    }
    
    public static void speakTextStatic(Context ctx, String text, int voiceProfile) {
        if (ttsRef[0] == null) {
            ttsRef[0] = new TextToSpeech(ctx, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsRef[0].setLanguage(Locale.forLanguageTag("hi-IN"));
                    // वॉइस प्रोफाइल के अनुसार पिच और स्पीड सेट करें
                    applyVoiceProfile(ttsRef[0], voiceProfile);
                    
                    Bundle params = new Bundle();
                    String utterId = UUID.randomUUID().toString();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                    ttsRef[0].speak(text, TextToSpeech.QUEUE_FLUSH, params, utterId);
                }
            });
        } else {
            Bundle params = new Bundle();
            String utterId = UUID.randomUUID().toString();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
            ttsRef[0].speak(text, TextToSpeech.QUEUE_FLUSH, params, utterId);
        }
    }
    
    /**
     * उपलब्ध वॉइस की सूची प्रिंट करता है (डीबगिंग के लिए)
     */
    public void printAvailableVoices() {
        if (tts != null) {
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                for (Voice voice : voices) {
                    Locale locale = voice.getLocale();
                    String language = locale.getLanguage();
                    String country = locale.getCountry();
                    String name = voice.getName();
                    boolean isNetworkVoice = voice.isNetworkConnectionRequired();
                    
                    // लॉग में वॉइस की जानकारी प्रिंट करें
                    android.util.Log.d("FeedbackTTS", "Voice: " + name + 
                            ", Language: " + language + 
                            ", Country: " + country + 
                            ", Network: " + isNetworkVoice);
                }
            } else {
                android.util.Log.d("FeedbackTTS", "No voices available");
            }
        }
    }
    
    /**
     * स्टैटिक तरीके से उपलब्ध वॉइस की सूची प्रिंट करता है
     */
    public static void printAvailableVoicesStatic(Context ctx) {
        FeedbackTTS feedbackTTS = new FeedbackTTS(ctx, null);
        feedbackTTS.printAvailableVoices();
    }
}
