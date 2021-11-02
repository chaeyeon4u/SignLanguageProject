package com.example.capstoneproject;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    TextView changedTextTV;

    ImageButton languageChangeIB;

    EditText whatTranslateET;

    // 기본 언어 한글
    String language = "Korean";

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 초기화
        init();
        buttonClickListenr();


        whatTranslateET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable editable) {

                new Thread(){
                    @Override
                    public void run() {
                        String word = whatTranslateET.getText().toString();

                        Papago papago = new Papago();
                        String resultWord;
                        String firstWord;

//                        if(language.equals("Korean")){
//                            resultWord= papago.getTranslation(word,"ko","en");
//                        }else{
//                            resultWord= papago.getTranslation(word,"en","ko");
//                        }

                        firstWord= papago.getTranslation(word,"ko","en");
                        resultWord= papago.getTranslation(firstWord,"en","ko");

                        Bundle papagoBundle = new Bundle();
                        papagoBundle.putString("resultWord",resultWord);

                        Log.d(TAG, "resultWord" + resultWord);

                        Message msg = papago_handler.obtainMessage();
                        msg.setData(papagoBundle);
                        papago_handler.sendMessage(msg);

                        Log.d(TAG, "msg" + msg);
                    }
                }.start();

            }
        });
    }

    private void init(){
        changedTextTV = findViewById(R.id.changedTextTV);
    }

    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            changedTextTV.setText(resultWord);
        }
    };

    private void buttonClickListenr(){

        languageChangeIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "languageChangeIB onClick");

                if(language.equals("Korean")){
                    language= "English";


                }else{
                    language= "Korean";

                }
            }
        });
    }
}