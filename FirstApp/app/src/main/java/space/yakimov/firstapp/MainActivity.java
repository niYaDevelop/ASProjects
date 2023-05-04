package space.yakimov.firstapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends Activity implements View.OnClickListener{
    Button play, online;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        playButt();
        onlineButt();
        initAd();
    }

    @Override
    public void onResume(){
        super.onResume();
        hideSystemUI();
        play.setEnabled(true);
        online.setEnabled(true);
    }

    private void playButt(){
        play = (Button) findViewById(R.id.button4);
        play.setOnClickListener(this);
        play.setTag("play");
    }

    private void onlineButt(){
        online = (Button) findViewById(R.id.button5);
        online.setOnClickListener(this);
        online.setTag("online");
    }


    private void initAd(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        switch (String.valueOf(view.getTag())){            // Узнаем какая кнопка нажата и переходим на уровень
            case "play" :
                play.setEnabled(false);
                online.setEnabled(false);
                startActivity(new Intent(this, setLevel.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case "online":
                online.setEnabled(false);
                play.setEnabled(false);
                startActivity(new Intent(this, OnlineMenu.class));
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;
//            case "addWord" :
//                startActivity(new Intent(this, NormalLevel.class));
//                break;
        }
    }
}
