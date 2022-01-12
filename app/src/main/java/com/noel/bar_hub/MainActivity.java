package com.noel.bar_hub;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

import java.util.Locale;



public class MainActivity extends AppCompatActivity {


      // Variable declaration

      // Text to speech variables

       private TextToSpeech textToSpeech;
       // This info will come from you database where admin will add products.

       private String welcome = "Hello and welcome to my Visual shopping Assistant, Speak to search for any products.";
       // Speech to text variables

       private static final int REQUEST_CODE = 100;
       private String requested_product;
       private String Url;
       FirebaseFirestore db = FirebaseFirestore.getInstance();
       // private DocumentReference prod_ref = db.collection("products").document(requested_product);

  @Override
  public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_main);


      // check for permissions (needs update)
     // if(ContextCompat.checkSelfPermission( this, Manifest.permission) != PackageManager.PERMISSION_GRANTED)

      // where the magic happens. Initializing androids tts

      textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
          @Override
          public void onInit(int status) {

              if (status != TextToSpeech.ERROR) {
                  Locale localeToUse = new Locale("en","US");
                  textToSpeech.setLanguage(localeToUse);
                  textToSpeech.setSpeechRate(0.80f);
                  textToSpeech.speak(welcome, TextToSpeech.QUEUE_FLUSH, null);
              }
          }
      });

      welcomeUser();
  }

  public void welcomeUser(){
      // String data = editText.getText().toString();

      // text to speak just replace 'data' to something else for example hello
      int speechStatus = textToSpeech.speak(welcome,TextToSpeech.QUEUE_FLUSH,null);

      if(speechStatus == TextToSpeech.ERROR){
          Log.e("TTS","Error with speech");
      }

      new CountDownTimer(5000, 4000){
          @Override
          public void onTick(long millisUntilFinished) {
              speak("Please request the product you want after the beep sound.");
          }


          @Override
          public void onFinish() {
               listenToUser();
          }


      }.start();
  }

  // start intent to speech recognizer

  public void listenToUser(){

      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT ,"Need to speak");

      try{
          startActivityForResult(intent, REQUEST_CODE);
      }catch(ActivityNotFoundException a){
          Toast.makeText(getApplicationContext(), "Sorry your device is not supported", Toast.LENGTH_SHORT).show();
      }
  }

  // get results from speech recognizer and pass to textOutput

  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
      super.onActivityResult(requestCode,resultCode,data);

      switch (requestCode){
          case REQUEST_CODE:{
              if(resultCode == RESULT_OK && null != data){
                  ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                  requested_product = result.get(0).toString();
                  //speak("This product does not exist in the database.");
                  Toast.makeText(this, requested_product, Toast.LENGTH_SHORT).show();
                  fetchData(requested_product);
              }
              break;
          }
      }
  }
 public void fetchData(String prod){

     db.collection("products").document(prod).get().
              addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                  @Override
                  public void onSuccess(DocumentSnapshot documentSnapshot) {
                      if(documentSnapshot.exists()){
                          String name = documentSnapshot.getString("name");
                          String price = documentSnapshot.getString("price");
                          String desc = documentSnapshot.getString("description");

                          speak(name);
                          speak(price);
                          speak(desc);
                      }
                      else{
                          speak("Product Unavailable");
                      }
                  }
              })
              .addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {

                  }
              });

 }

 public void speak(String message){
     textToSpeech.speak( message,TextToSpeech.QUEUE_FLUSH,null);
 }


  @Override
    public void onDestroy(){
      super.onDestroy();
      if(textToSpeech != null){
          textToSpeech.stop();
          textToSpeech.shutdown();
      }
  }

}
