package space.yakimov.firstapp;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class EasyLevel extends Activity implements View.OnTouchListener {

    String[] linesAsArray;
    private ArrayList<String> wordlist = new ArrayList<>();
    private ArrayList<String> allWordsOnMap = new ArrayList<>();
    private ArrayList<String> wordsGuessed = new ArrayList<>();
    int stroke=20, column=20;     // для onTouch
    private int numer = 0;  // Текущий номер уровня
    private int n ;         // размер матрицы n*n
    String str = "";
    int width, height, side;
    private int ID = 0;   // RANK
    int  maxExp, oldExp;
    public int expRank=0;
    boolean flagPodsAnim =false;
    boolean flagMove = true;
    boolean flagComp = false;
    boolean flagRated;
    private boolean flag05 = false;
    private boolean flag08 = false;
    private int zvezda =0;
    public static int [][] checkArray ;
    public static int [][] globalCheck ;
    MapView map;
    Button podskaz, newlvl, PodsZaZvezd, PodsZaRekl;
    TextView LastWord, Kolvo, stars, Levels, Rank, textRate;
    RatingBar ratingBar;
    ImageView  imageRank, expFront, starView;
    Typeface font;
    private SharedPreferences nSettings;
    JSONObject ranks;
    public static SoundPool sp;
    final int MAX_STREAMS_SOUND = 5;
    private int soundIdNextLvl,soundIdTap,soundIdMatch;
    private float soundsVolume = 1f;
    private ToggleButton soundButton;

    private RewardedAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_level);
        font = Typeface.createFromAsset(getAssets(), "fonts/comic.ttf");
        
        //Получаем данные из прошлого активити 
        n = getIntent().getExtras().getInt("mapSize");  //Размер карты
        numer = getIntent().getExtras().getInt("lvlNumber");    //Номер уровня
        ID = getIntent().getExtras().getInt("ID");    //Номер уровня
        zvezda = getIntent().getExtras().getInt("zvezda");    //Количество звезд
        expRank = getIntent().getExtras().getInt("expRank");    //Ранг
        jsonParsing();

        globalCheck = new int[n][n];
        checkArray = new int [n][n];
        linesAsArray = new String[n];
        nSettings = getSharedPreferences("ssettings", Context.MODE_PRIVATE);
        if(nSettings.contains("flagRated")) flagRated = nSettings.getBoolean("flagRated", false);


        //Инициализация View
        textRate = findViewById(R.id.rate_text);
        ratingBar = findViewById(R.id.ratingBar);
        LastWord = (TextView) findViewById(R.id.lastWord);
        LastWord.setTypeface(font);
        soundButton = findViewById(R.id.sound_toggle);
        stars = (TextView) findViewById(R.id.numStars);
        stars.setTypeface(font,Typeface.BOLD);
        stars.setText(String.valueOf(zvezda));
        Levels = (TextView) findViewById(R.id.LvL);
        Levels.setTypeface(font,Typeface.BOLD);
        Levels.setText("LvL " + (numer+1));
        Rank = (TextView) findViewById(R.id.textRank);
        Rank.setTypeface(font,Typeface.BOLD);
        imageRank = (ImageView) findViewById(R.id.imageRank);
        expFront = (ImageView) findViewById(R.id.expFront);
        Kolvo = (TextView) findViewById(R.id.guesWordsStat);
        Kolvo.setTypeface(font,Typeface.BOLD);
        starView = (ImageView) findViewById(R.id.imageStar);
        PodsZaZvezd = (Button) findViewById(R.id.podsZaZv);
        PodsZaRekl = (Button) findViewById(R.id.podsZaRekl);
        PodsZaZvezd.setVisibility(PodsZaZvezd.GONE);
        PodsZaRekl.setVisibility(PodsZaRekl.GONE);
        map = (MapView) findViewById(R.id.mapView);
        podskaz = (Button) findViewById(R.id.pods);
        newlvl = (Button) findViewById(R.id.newlvl);
        newlvl.setVisibility(newlvl.GONE);
        //Конец инициализации View
        
        readLinesAsArray(n,numer);     //Прочитали карту из файла
        wordlist = readSpisokSlov();       //Прочитали словарь
        allWordsOnMap = poiskSlovNaMap(wordlist, n);    //Нашли все слова на карте

        //Загружаем отгаданные слова
        ArrayList<String> arr;
        switch(n){
            case 3:
                arr = getArrayPrefs("ezlevel");
//                if(!arr.isEmpty())
                    wordsGuessed = arr;
                break;
            case 5:
                arr = getArrayPrefs("nllevel");
//                if(!arr.isEmpty())
                    wordsGuessed = arr;
                break;
            case 7:
                arr = getArrayPrefs("hdlevel");
//                if(!arr.isEmpty())
                    wordsGuessed = arr;
                break;
        }

        //Чистим список слов карты (затираем отгаданные)
        clearMapWordlist();

        //Устанавливаем видимые клетки поля
        //Выставили буквы которые используются
        resetGlobalCheck();
        for (String line : allWordsOnMap) {
            uborka(line, new int[n][n], "", 20, 20);
        }

        //Если есть отгаданные слова
        if(wordsGuessed.size()>0){
            //Если все слова отгаданы, показать кнопку следующий уровень (при запуске активити)
            if(allWordsOnMap.size()==wordsGuessed.size() ) {
                newlvl.setVisibility(newlvl.VISIBLE);
                newlvl.setEnabled(true);
                podskaz.setVisibility(podskaz.GONE);
            }
            //выставляем флаги получения награды
            if(allWordsOnMap.size()/2<=wordsGuessed.size())   flag05=true;
            if(allWordsOnMap.size()*8/10<=wordsGuessed.size())    flag08=true;
            //Записали последнее слова и количество отгаданных слов во вьюшку
            LastWord.setText(wordsGuessed.get(wordsGuessed.size()-1));
        }

        Display display = getWindowManager().getDefaultDisplay();   //блок получения размера дисплея
        Point size = new Point();
        display.getSize(size);

        height =(int) (size.y * 0.55f);
        width =(int)  (size.x * 0.8f);
        if(width < height)  height = width;
        else    width = height;
        side=width/n; //ширина tile

        Kolvo.setText(wordsGuessed.size() + "/" + allWordsOnMap.size());
        map.init( n, width, linesAsArray);
        map.invalidate();
        podskazkiSetListener();
        newlvlListener();
        map.setOnTouchListener(this);
        setRank(true);
        loadRewardedVideoAd();
        soundInit();
        soundButtonListener();
    }

    private void loadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-9759163947336772/5523974985",         //Рабочая
//        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",                       //тестовая
                adRequest, new RewardedAdLoadCallback(){
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedVideoAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedVideoAd = rewardedAd;
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
        newlvl.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public int[][] cloneArray(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }

    public boolean recurr(String word, int[][] chack, String str, int oldI, int oldJ){
        if(str.length() == word.length()) return true;
            boolean flag = false;
            for(int i =0; i<n; i++){
                for(int j=0; j<n; j++){
                    if(linesAsArray[i].charAt(j)==word.charAt(str.length()) && chack[i][j]!=1 && (str.equals("") || (((Math.abs(oldJ-j) == 1) && (Math.abs(oldI-i) == 0)) || ((Math.abs(oldJ-j) == 0) && (Math.abs(oldI-i) == 1)))  )){
                        int [][]klon = cloneArray(chack);
                        klon[i][j] = 1;
                        flag = recurr(word, klon, str+linesAsArray[i].charAt(j), i, j);
                        if (flag) break ;
                    }
                }
                if(flag) break;
            }
            return flag;
    }

    public void startPodsAnim(List<Point> pointArray){
        final List<Point> point = new ArrayList<>(pointArray);
        Thread pod = new Thread(new Runnable() {
            @Override
            public void run() {
                loop: while(flagPodsAnim){
                    setCheckArray(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.invalidate();
                        }});
                    for(int k=0; k<point.size(); k++){

                        if(!flagPodsAnim) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map.invalidate();
                                }});
                            break loop;}
                        try {
                            TimeUnit.MILLISECONDS.sleep(150);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        checkArray[point.get(k).y][point.get(k).x] = 2;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                map.invalidate();
                            }});

                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        });
        pod.start();

    }

    public boolean podskazka(String word, int[][] chack, String str, int oldI, int oldJ, ArrayList<Point> point){
        if(str.length() == word.length()){
            flagPodsAnim = true;
            startPodsAnim(point);
            return true;}
        boolean flag = false;
        for(int i =0; i<n; i++){
            for(int j=0; j<n; j++){
                if(linesAsArray[i].charAt(j)==word.charAt(str.length()) && chack[i][j]!=1 && (str.equals("") || (((Math.abs(oldJ-j) == 1) && (Math.abs(oldI-i) == 0)) || ((Math.abs(oldJ-j) == 0) && (Math.abs(oldI-i) == 1)))  )){
                    ArrayList<Point> klonPoint = new ArrayList<>(point);
                    klonPoint.add(new Point(j,i));
                    int [][]klon = cloneArray(chack);
                    klon[i][j] = 1;
                    flag = podskazka(word, klon, str+linesAsArray[i].charAt(j), i, j, klonPoint);

                    if (flag) break ;
                }
            }
            if(flag) break;
        }
        return flag;
    }

    public boolean uborka(String word, int[][] chack, String str, int oldI, int oldJ){
        if(str.length() == word.length()){
            for(int z=0; z< n ; z++){
                for(int x = 0 ; x<n; x++){
                    if(chack[z][x]==1) globalCheck[z][x] = 1;
                }
            }
            return true;}
        boolean flag = false;
        for(int i =0; i<n; i++){
            for(int j=0; j<n; j++){
                if(linesAsArray[i].charAt(j)==word.charAt(str.length()) && chack[i][j]!=1 && (str.equals("") || (((Math.abs(oldJ-j) == 1) && (Math.abs(oldI-i) == 0)) || ((Math.abs(oldJ-j) == 0) && (Math.abs(oldI-i) == 1)))  )){
                    int [][]klon = cloneArray(chack);
                    klon[i][j] = 1;
                    flag = uborka(word, klon, str+linesAsArray[i].charAt(j), i, j);
                    if (flag) break ;
                }
            }
            if(flag) break;
        }
        return flag;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(allWordsOnMap.size()==wordsGuessed.size()) return true;  // если всеп слова отгаданы, не обрабатываем нажатие

        int x = (int) event.getX();
        int y = (int) event.getY();
        int i = getIJ(y);
        int j = getIJ(x);
        PodsZaZvezd.setVisibility(PodsZaZvezd.GONE);
        PodsZaRekl.setVisibility(PodsZaRekl.GONE);
        podskaz.setVisibility(podskaz.VISIBLE);



        if ((((Math.abs(column-j) == 1) && (Math.abs(stroke-i) == 0)) || ((Math.abs(column-j) == 0) && (Math.abs(stroke-i) == 1))) && flagMove == false && checkArray[i][j]!=1)
        {
            if(globalCheck[i][j]==1) {
                flagMove = true;
                column = j;
                stroke = i;
                checkArray[i][j] = 1;
                sp.play(soundIdTap,  soundsVolume,  soundsVolume, 0, 0, 1);
            }
            else{
                j = column;
                i = stroke;
            }
        }
        if(i==10 || j==10) {
            flagMove = true;
            column = 20;
            stroke = 20;

            if (allWordsOnMap.contains(str)) {                         // Если отгадал
                wordsGuessed.add(str);
                sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 1);
                allWordsOnMap.set(allWordsOnMap.indexOf(str), "");
                if (wordsGuessed.size() >= allWordsOnMap.size() / 2) {
                    if (!flag05) {
                        switch (n){
                            case 3:
                                zvezda++;
                                break;
                            case 5:
                                zvezda+=3;
                                break;
                            case 7:
                                zvezda+=5;
                                break;
                        }
                        starAnim();
                        flag05 = true;
                        Thread q = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    TimeUnit.MILLISECONDS.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stars.setText(String.valueOf(zvezda));
                                    }
                                });
                            }
                        });
                        q.start();
                    }
                    if (wordsGuessed.size() >= allWordsOnMap.size() * 8 / 10 && !flag08) {
                        switch (n){
                            case 3:
                                zvezda++;
                                break;
                            case 5:
                                zvezda+=3;
                                break;
                            case 7:
                                zvezda+=5;
                                break;
                        }
                        starAnim();
                        flag08 = true;
                        Thread q = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    TimeUnit.MILLISECONDS.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stars.setText(String.valueOf(zvezda));
                                    }
                                });

                            }
                        });
                        q.start();
                    }
                }
                setCheckArray(2);
                resetGlobalCheck();
                for (int q = 0; q < allWordsOnMap.size(); q++) {
                    uborka(allWordsOnMap.get(q), new int[n][n], "", 20, 20);
                }
                Kolvo.setText(wordsGuessed.size() + "/" + allWordsOnMap.size());
                LastWord.setText(str);

                flagComp = true;
            } else if (wordsGuessed.contains(str)) {             // Если было
                setCheckArray(3);
                flagComp = true;
            }

            if (!flagComp) {                      // Если не отгадал слово
                setCheckArray(4);
            }
            flagComp = false;
            map.invalidate();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setCheckArray(0);
                            map.invalidate();
                        }
                    });

                }
            });
            t.start();
            sortir();

            if (allWordsOnMap.get(0) == "") {


                map.setVisibility(map.GONE);
                podskaz.setVisibility(podskaz.GONE);
                switch (n){
                    case 3:
                        zvezda++;
                        break;
                    case 5:
                        zvezda+=3;
                        break;
                    case 7:
                        zvezda+=5;
                        break;
                }
                stars.setText(String.valueOf(zvezda));
                starAnim();

                if(!flagRated && numer > 1) rateUs();       // Если не голосовал и дошел до уровень 3, то попросить проголосовать
                else{
                    newlvl.setVisibility(newlvl.VISIBLE);            // Если уже голосовал, либо уровень ниже 3, то идем дальше
                    buttFadeIn(newlvl);
                    newlvl.setEnabled(true);
                }
            }
            return true;
        }


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                flagPodsAnim=false;
                column=j;
                stroke=i;
                flagMove = false;
                str = String.valueOf(linesAsArray[i].charAt(j));
                sp.play(soundIdTap,  soundsVolume,  soundsVolume, 0, 0, 1);
                checkArray[i][j]=1;
                map.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (flagMove){
                    str += String.valueOf(linesAsArray[i].charAt(j));
                    map.invalidate();
                    flagMove = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                flagMove = true;
                column =20;
                stroke =20;

                if(allWordsOnMap.contains(str)){                         // Если отгадал
                    sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 1);
                    wordsGuessed.add(str);
                    allWordsOnMap.set(allWordsOnMap.indexOf(str), "");

                    //Обработали получение первой звезды
                    if(wordsGuessed.size()>=allWordsOnMap.size() / 2){
                        if(!flag05){
                            switch (n){
                                case 3:
                                    zvezda++;
                                    break;
                                case 5:
                                    zvezda+=3;
                                    break;
                                case 7:
                                    zvezda+=5;
                                    break;
                            }
                            starAnim();
                            flag05 = true;
                            Thread q = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        TimeUnit.MILLISECONDS.sleep(100);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            stars.setText(String.valueOf(zvezda));
                                }});
                            }});
                            q.start();
                        }
                        //Обработали получение второй звезды
                        if (wordsGuessed.size()>=allWordsOnMap.size() * 8 / 10 && !flag08){
                            switch (n){
                                case 3:
                                    zvezda++;
                                    break;
                                case 5:
                                    zvezda+=3;
                                    break;
                                case 7:
                                    zvezda+=5;
                                    break;
                            }
                            starAnim();
                            flag08=true;
                            Thread q = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        TimeUnit.MILLISECONDS.sleep(100);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            stars.setText(String.valueOf(zvezda));
                                        }});

                                }
                            });
                            q.start();
                        }
                    }
                    setCheckArray(2);
                    resetGlobalCheck();
                    for(int q=0; q<allWordsOnMap.size(); q++){
                        uborka(allWordsOnMap.get(q),new int[n][n], "", 20, 20 );
                    }
                    Kolvo.setText(wordsGuessed.size() + "/" + allWordsOnMap.size());
                    LastWord.setText(str);

                    flagComp=true;
                }
                    else if(wordsGuessed.contains(str)){             // Если было
                        setCheckArray(3);
                        flagComp=true;
                    }
                if(!flagComp){                      // Если не отгадал слово
                    setCheckArray(4);
                }
                flagComp=false;
                map.invalidate();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.MILLISECONDS.sleep(250);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setCheckArray(0);
                                map.invalidate();
                            }});

                    }
                });
                t.start();
                sortir();

                //Обработали получение третьей звезды
                if(allWordsOnMap.get(0) == ""){

                    map.setVisibility(map.GONE);
                    podskaz.setVisibility(podskaz.GONE);
                    switch (n){
                        case 3:
                            zvezda++;
                            break;
                        case 5:
                            zvezda+=3;
                            break;
                        case 7:
                            zvezda+=5;
                            break;
                    }
                    stars.setText(String.valueOf(zvezda));
                    starAnim();

                    if(!flagRated && numer > 1) rateUs();       // Если не голосовал и дошел до уровень 3, то попросить проголосовать
                    else{
                        newlvl.setVisibility(newlvl.VISIBLE);            // Если уже голосовал, либо уровень ниже 3, то идем дальше
                        buttFadeIn(newlvl);
                        newlvl.setEnabled(true);
                    }
                }
                break;
        }
        return true;
    }


    @Override
    protected void onPause() {
//        mRewardedVideoAd.pause(this);
        super.onPause();
        // Запоминаем данные

        flagPodsAnim=false;
        SharedPreferences.Editor editor = nSettings.edit();
        editor.remove("counter");
        editor.putInt("counter", zvezda);

        editor.remove("exp");
        editor.putInt("exp", expRank);

        editor.remove("ID");
        editor.putInt("ID", ID);


        String name="";
        switch(n){
            case 3:
                name = "ezlevel";
                editor.remove("easyLevels");
                editor.putInt("easyLevels", numer);
                break;
            case 5:
                name = "nllevel";
                editor.remove("normLevels");
                editor.putInt("normLevels", numer);
                break;
            case 7:
                name = "hdlevel";
                editor.remove("hardLevels");
                editor.putInt("hardLevels", numer);
                break;
        }
        editor.remove(name+"_size");
        editor.putInt(name +"_size", wordsGuessed.size());

        for(int i=0;i<wordsGuessed.size();i++) {
            editor.remove(name + "_" + i);
            editor.putString(name + "_" + i, wordsGuessed.get(i));
        }
        editor.commit();
    }

    public void sortir(){
        for(int i = allWordsOnMap.size()-1 ; i > 0 ; i--){
            for(int j = 0 ; j < i ; j++){
                if( allWordsOnMap.get(j).length() < allWordsOnMap.get(j+1).length() ){
                    String tmp = allWordsOnMap.get(j);
                    allWordsOnMap.set(j, allWordsOnMap.get(j+1));
                    allWordsOnMap.set(j+1, tmp);
                }
            }
        }
    }

    public void changeExpLine( View view, float percent, boolean flagFirstTime){

        if(flagFirstTime) view.setScaleX(0.001F);

        ObjectAnimator animat = ObjectAnimator.ofFloat(view, "scaleX", percent/100);
        animat.setDuration(500);
        animat.start();
    }

    public void buttFadeIn(View view){
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_fade_in_anim);

        FastOutLinearInInterpolator interpolator = new FastOutLinearInInterpolator();
        animation.setInterpolator(interpolator);

        view.startAnimation(animation);
    }

    public void buttFadeOut(View view){
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_fade_out_anim);

        FastOutLinearInInterpolator interpolator = new FastOutLinearInInterpolator();
        animation.setInterpolator(interpolator);

        view.startAnimation(animation);
    }

    public void starAnim(){
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.star_animation);

        BounceInterpolator interpolator = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(interpolator);

        starView.startAnimation(animation);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }


    public void jsonParsing(){


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("rank.json")));
            String line;
            String content = "";
            while ((line = reader.readLine()) != null) {
                content+=line;
            }
            reader.close();
            ranks = new JSONObject(content);
        } catch (Exception e) {
            Log.i("zhopa", e.getMessage());
        }
    }

    public String getRankData(int id, String property){
        try{
            return ((JSONObject)ranks.get(String.valueOf(id))).get(property).toString();
        }
        catch(Exception e){
            return null;
        }
    }


    public void setRank(boolean flagInit){
        // Цикл по рангам
        for(int i=ID; i<14; i++){

            // если не дошел до максимума
            if(expRank<Integer.parseInt(getRankData(i,"exp"))){
                ID = i ;
                maxExp = Integer.parseInt(getRankData(ID,"exp"));
                if(ID>0) oldExp = Integer.parseInt(getRankData(ID-1,"exp"));
                else oldExp = 0;

                Rank.setText(getRankData(ID,"name"));
                int rankRes = getResources().getIdentifier(getRankData(ID, "ImageName"), "drawable", getPackageName());
                Drawable drawable = getResources().getDrawable(rankRes);
                imageRank.setBackground(drawable);
                changeExpLine(expFront, (float)100 * (float)(expRank - oldExp) / (float)(maxExp - oldExp) , flagInit);
                break;
            }


            // иначе если опыт превысил или набрал максимум
            else if(expRank>=Integer.parseInt(getRankData(i,"exp"))){
                ID=i+1;
                        maxExp = Integer.parseInt(getRankData(ID,"exp"));
                        oldExp = Integer.parseInt(getRankData(ID-1,"exp"));
                        Rank.setText(getRankData(ID,"name"));
                        int rankRes = getResources().getIdentifier(getRankData(ID, "ImageName"), "drawable", getPackageName());
                        Drawable drawable = getResources().getDrawable(rankRes);
                        imageRank.setBackground(drawable);
                changeExpLine(expFront, (float)100 * (float)(expRank - oldExp) / (float)(maxExp - oldExp) , true);
            }
        }
    }

    private ArrayList<String> readSpisokSlov(){
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("wordlist.txt")));
            String line;
//            List<String> lines = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
//            wordlist = lines.toArray(new String[lines.size()]);
        } catch (Exception e) {
            Log.i("zhopa", e.getMessage());
        }
        return lines;
    }

    private ArrayList<String> poiskSlovNaMap(ArrayList<String> wL, int mapSize){
        ArrayList<String> array = new ArrayList<>();
        for (int k =0; k<wL.size(); k++){
            if (recurr(wL.get(k), new int[mapSize][mapSize], "", 20, 20)){
                array.add(wL.get(k));
            }
        }
        LastWord.setText("...");
        return array;
    }

    public void readLinesAsArray(int mapSize, int lvlNumber){
        try {
            BufferedReader reader=null;
            if(n==3) reader = new BufferedReader(new InputStreamReader(getAssets().open("lilEasy.txt")));
            if(n==5) reader = new BufferedReader(new InputStreamReader(getAssets().open("lilMedium.txt")));
            if(n==7) reader = new BufferedReader(new InputStreamReader(getAssets().open("lilHard.txt")));


            String line;
            boolean flak = false;  linesAsArray = new String[mapSize]; int c =0;
            while ((line = reader.readLine()) != null) {
                if(line.indexOf("[L " + String.valueOf(lvlNumber+1)+"]") >= 0){
                    flak = true;
                    continue;
                }
                if(flak && (line.indexOf("[L ") >= 0)) flak = false;
                if(flak){
                    linesAsArray[c++] = line;
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.i("zhopa", e.getMessage());
        }
    }

    public void setCheckArray(int f) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (checkArray[i][j] != 0 ) checkArray[i][j] = f;
            }
        }
    }

    public void setGlobalCheck(){
        for(int i =0; i<n; i++){
            for(int j=0; j<n; j++){
                globalCheck[i][j] = 1;
            }
        }
    }

    public void resetGlobalCheck(){
        for(int i =0; i<n; i++){
            for(int j=0; j<n; j++){
                globalCheck[i][j] = 0;
            }
        }
    }

    public int getIJ(int XY)          // Получение номера строки/стобца. Второй аргумент - левый угол таблицы
    {
        int ji=0;
        while(ji<n){
            if(XY  < side*(ji+1)) return ji;
            ji++;
    }
        return 10;
    }

    private void soundInit(){
        sp = new SoundPool(MAX_STREAMS_SOUND, AudioManager.STREAM_MUSIC, 0);
        soundIdTap = sp.load(this, R.raw.generate, 1);
        soundIdNextLvl = sp.load(this, R.raw.match, 1);
        soundIdMatch = sp.load(this, R.raw.eat1, 1);
    }

    private void rateUs(){
        textRate.setVisibility(View.VISIBLE);
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar rateBar, float rating,
                                        boolean fromUser) {
                // запоминаем флаг
                flagRated = true;
                SharedPreferences.Editor editor = nSettings.edit();
                editor.remove("flagRated");
                editor.putBoolean("flagRated", flagRated);
                editor.commit();
                // если больше 4 - перейти в плей маркет
                if(rating>=4.0){
                    try{
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));
                    }
                    catch (ActivityNotFoundException e){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                    }
                }
                //разрешить переход на следующий уровень
                newlvl.setVisibility(newlvl.VISIBLE);
                buttFadeIn(newlvl);
                newlvl.setEnabled(true);
                //спрятать окно
                textRate.setVisibility(View.GONE);
                ratingBar.setVisibility(View.GONE);
            }
        });
    }

    private void newlvlListener(){
        newlvl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newlvl.setEnabled(false);
                sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 2);
                if(numer == 1000) numer = 0;
//                if(flagPods){
//                    PodsZaZvezd.setVisibility(PodsZaZvezd.GONE);
//                    PodsZaRekl.setVisibility(PodsZaRekl.GONE);
//                    podskaz.setVisibility(podskaz.VISIBLE);
//                }
                numer++;
                LastWord.setText("...");
                Levels.setText("LvL " + (numer+1));
                flag05 = false;
                flag08 = false;
                setGlobalCheck();
                setCheckArray(0);
                Thread threadButt = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttFadeOut(newlvl);
//                                            changeButtLine(newlvl,0.001F,false);
                            }
                        });
                        try {
                            TimeUnit.MILLISECONDS.sleep(300);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newlvl.setVisibility(newlvl.GONE);
                                readLinesAsArray(n,numer);
                                allWordsOnMap = poiskSlovNaMap(wordlist,n);

                                wordsGuessed.clear();
                                Kolvo.setText(wordsGuessed.size() + "/" + allWordsOnMap.size());
                                stars.setText(String.valueOf(zvezda));
                                
                                map.setVisibility(map.VISIBLE);
                                map.init( n, width , linesAsArray);
                                map.invalidate();


                            }
                        });
                    }
                });
                threadButt.start();
                podskaz.setVisibility(podskaz.VISIBLE);
                switch(n){
                    case 3:
                        expRank++;
                        break;
                    case 5:
                        expRank+=5;
                        break;
                    case 7:
                        expRank+=15;
                        break;
                }
                if(expRank==maxExp) {
                    Thread threadRank = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeExpLine(expFront, 100F, false);
                                }
                            });
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setRank(false);
                                }
                            });
                        }
                    });
                    threadRank.start();
                }
                else setRank(false);
            }
        });
    }

    private ArrayList<String> getArrayPrefs(String arrayName) {
        ArrayList<String> array = new ArrayList<>();
        //Если нет сохраненных данных, выходим
        if (!nSettings.contains(arrayName + "_size")) return array;
        int num = nSettings.getInt(arrayName + "_size", 0);

        for(int i=0;i<num;i++)
            array.add(nSettings.getString(arrayName + "_" + i, null));
        return array;
    }

    private void clearMapWordlist(){
        //Для всех отгаданных слов
        for(String line : wordsGuessed){
            //Если есть слово в списке слов карты
            if(allWordsOnMap.contains(line)){
                //затёрли его
                allWordsOnMap.set(allWordsOnMap.indexOf(line),"");
            }
        }
    }

    private void soundButtonListener(){
        soundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    soundsVolume = 1f;
                }
                else
                {
                    soundsVolume = 0f;
                }
            }
        });
    }

    private void podskazkiSetListener(){
        PodsZaZvezd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zvezda>0) {
                    zvezda--;
                    starAnim();
                    stars.setText(String.valueOf(zvezda));
                    podskazka(allWordsOnMap.get(0), new int[n][n], "", 20, 20, new ArrayList<Point>());
                }
                else{
                    Toast.makeText(getApplicationContext(),"Соберите больше звёзд", Toast.LENGTH_SHORT).show();
                }
                PodsZaZvezd.setVisibility(PodsZaZvezd.GONE);
                PodsZaRekl.setVisibility(PodsZaRekl.GONE);
                podskaz.setVisibility(podskaz.VISIBLE);
//                flagPods = false;
            }
        });

        PodsZaRekl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRewardedVideoAd != null) {
                    Activity activityContext = EasyLevel.this;
                    mRewardedVideoAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
//                                                    Log.d(TAG, "The user earned the reward.");
                            PodsZaZvezd.setVisibility(PodsZaZvezd.GONE);
                            PodsZaRekl.setVisibility(PodsZaRekl.GONE);
                            podskaz.setVisibility(podskaz.VISIBLE);
                            podskazka(allWordsOnMap.get(0), new int[n][n], "", 20, 20, new ArrayList<Point>());
                            loadRewardedVideoAd();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),"Проверьте подключение к интернету", Toast.LENGTH_SHORT).show();
                    loadRewardedVideoAd();
                }
            }
        });

        podskaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckArray(0);
                sortir();
                flagPodsAnim=false;
                podskaz.setVisibility(podskaz.GONE);
                PodsZaRekl.setVisibility(PodsZaRekl.VISIBLE);
                PodsZaZvezd.setVisibility(PodsZaZvezd.VISIBLE);
//                flagPods=true;
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


}