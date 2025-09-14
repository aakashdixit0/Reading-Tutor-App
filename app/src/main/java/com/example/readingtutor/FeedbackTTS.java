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
    
    
    public static final int VOICE_FEMALE = 0;  
    public static final int VOICE_MALE = 1;    
    public static final int VOICE_CHILD = 2;   
    
    private int voiceProfile = VOICE_FEMALE;  

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
                
                applyVoiceProfile(tts, voiceProfile);
                
                
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

    
    public void speakWrongWords(List<String> wrongWords) {
        if (wrongWords.isEmpty()) return;
        
        
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro");
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); 
        tts.speak("ध्यान दें", TextToSpeech.QUEUE_FLUSH, params, "intro");
        
        
        for (int i = 0; i < wrongWords.size(); i++) {
            String w = wrongWords.get(i);
            String utterId = (i == wrongWords.size() - 1) ? "last_word" : "word_" + i;
            params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); 
            
            try {
                Thread.sleep(300); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tts.speak(w, TextToSpeech.QUEUE_ADD, params, utterId);
        }
    }

    public void shutdown() {
        if (tts != null) tts.shutdown();
    }
    
    
    private static void applyVoiceProfile(TextToSpeech tts, int voiceProfile) {
        
        Set<Voice> voices = tts.getVoices();
        Voice selectedVoice = null;
        
        
        if (voices != null && !voices.isEmpty()) {
            for (Voice voice : voices) {
                Locale voiceLocale = voice.getLocale();
                String language = voiceLocale.getLanguage();
                String country = voiceLocale.getCountry();
                
                
                if (language.equals("hi") || language.equals("en")) {
                    
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
            case VOICE_FEMALE:
                tts.setPitch(1.1f);    
                tts.setSpeechRate(0.8f); 
                break;
            case VOICE_MALE:
                tts.setPitch(0.9f);    
                tts.setSpeechRate(0.75f); 
                break;
            case VOICE_CHILD:
                tts.setPitch(1.2f);
                tts.setSpeechRate(0.85f); 
                break;
            default:
                tts.setPitch(1.0f);    
                tts.setSpeechRate(0.8f); 
        }
    }
    
    
    public void setVoiceProfile(int voiceProfile) {
        this.voiceProfile = voiceProfile;
        if (tts != null) {
            applyVoiceProfile(tts, voiceProfile);
        }
    }

    // static helpers for quick usage from activity
    private static TextToSpeech[] ttsRef = new TextToSpeech[1];
    private static int staticVoiceProfile = VOICE_FEMALE; 

    
    public static void setStaticVoiceProfile(int voiceProfile) {
        staticVoiceProfile = voiceProfile;
        if (ttsRef[0] != null) {
            applyVoiceProfile(ttsRef[0], voiceProfile);
        }
    }

    
    public static void speakWrongWordsStatic(Context ctx, List<String> wrong) {
        speakWrongWordsStatic(ctx, wrong, staticVoiceProfile);
    }

    public static void speakWrongWordsStatic(Context ctx, List<String> wrong, int voiceProfile) {
        if (ttsRef[0] == null) {
            ttsRef[0] = new TextToSpeech(ctx, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsRef[0].setLanguage(Locale.forLanguageTag("hi-IN"));
                    
                    applyVoiceProfile(ttsRef[0], voiceProfile);
                    
                    if (!wrong.isEmpty()) {
                        
                        Bundle params = new Bundle();
                        String utterId = UUID.randomUUID().toString();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                        ttsRef[0].speak("see it", TextToSpeech.QUEUE_FLUSH, params, utterId);
                        
                        
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
                
                Bundle params = new Bundle();
                String utterId = UUID.randomUUID().toString();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterId);
                ttsRef[0].speak("see it", TextToSpeech.QUEUE_FLUSH, params, utterId);
                
                
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
    
    
    public static void printAvailableVoicesStatic(Context ctx) {
        FeedbackTTS feedbackTTS = new FeedbackTTS(ctx, null);
        feedbackTTS.printAvailableVoices();
    }
}
