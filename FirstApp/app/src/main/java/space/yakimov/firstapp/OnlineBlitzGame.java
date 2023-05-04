package space.yakimov.firstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.socket.emitter.Emitter;

import static space.yakimov.firstapp.OnlineMenu.mSocket;

public class OnlineBlitzGame extends AppCompatActivity {
    private int mapSize = 5;
    private int score, oppScore;      //Очки финальные
    private boolean isAlreadyGenerate, isMyTurn, wantRepeat, oppWantRepeat;
    private int stroke=20, column=20;     // для onTouch
    boolean flagMove = true, flagComp = false;
    private int timerCounter, myTimeCount, oppTimeCount;
    private float width, height, side;
    private List<String> globalWordList, sessionWordList, myAnswersWordList, sessionAnswersWordList;
    private String str = "";        // набираемое слово
    private String userName;
    private String opponentName;
    private Boolean isHost, isBot;
    private ListView messagesView;
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayAdapter<Message> messageAdapter;
    private TextView playersView, opponentView,scoringTextView, oppleavTextView, youKickTextView, timerLeavTextView, lastWordView, genTextView; //textPlayers, oppTimerView, myTimerView;
//    private TextView , userScoreName, userScoreView, oppScoreName, oppScoreView;
//    private ImageView boundLeft, boundBottom, boundRight, playerMoveImageBack, oppMoveImageBack;
    private ProgressBar spinnerGen;
    private ImageView windowLeave, black_veil;
    private Handler mHandler = new Handler();
    private TextInputLayout textInputView;
    private EditText textEditView;
    private Button sendMessageButton, buttonRepeat, buttonLeave;
    private Typeface font;
    private char[][] mapMainArray;
    private OnlineMapView map;
    public static int checkArray[][], globalCheck[][]; // CheckArray показывает состояния плитки( белая, синяя, ...), globalCheck показывает 0 при отсутствии, 1 при наличии
    private InterstitialAd mInterstitialAd;

    public static SoundPool sp;
    final int MAX_STREAMS_SOUND = 5;
    private int soundIdYourTurn,soundIdTap,soundIdMatch;
    private float soundsVolume = 1f;
    private ToggleButton soundButton;
    private ProgressBar progressGame;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_blitz_game);
        font = Typeface.createFromAsset(getAssets(), "fonts/comic.ttf");

        userName = getIntent().getExtras().getString("username");
        isHost = getIntent().getExtras().getBoolean("isHost");
        opponentName = getIntent().getExtras().getString("opponent");
        isBot = getIntent().getExtras().getBoolean("isBot");

        Display display = getWindowManager().getDefaultDisplay();   //блок получения размера дисплея
        Point size = new Point();
        display.getSize(size);

        height = size.y * 0.45f;
        width = size.x * 0.9f;
        if(width < height)  height = width;
        else    width = height;
        side=width/mapSize; //ширина tile


        messageAdapter = new MessageAdapter(this,messages, font);
        messagesView = findViewById(R.id.personal_chat);
        messagesView.setAdapter(messageAdapter);

//        boundBottom = findViewById(R.id.my_move_back_view_bottom);
//        boundLeft = findViewById(R.id.my_move_back_view_left);
//        boundRight = findViewById(R.id.my_move_back_view_right);
//        playerMoveImageBack = findViewById(R.id.my_move_back_view);
//        oppMoveImageBack = findViewById(R.id.opp_move_back_view);
        progressGame = findViewById(R.id.progress_bar_online);
        textEditView = findViewById(R.id.personal_text_edit);
        textInputView = findViewById(R.id.personal_text_lay);
        textEditView.setTypeface(font, Typeface.NORMAL);
        sendMessageButton = findViewById(R.id.send_butt);
        buttonLeave = findViewById(R.id.leave_button);
        buttonRepeat = findViewById(R.id.accept_repeat);
        oppleavTextView = findViewById(R.id.opp_leav_text);
        oppleavTextView.setTypeface(font, Typeface.NORMAL);
        youKickTextView = findViewById(R.id.you_kicked);
        youKickTextView.setTypeface(font, Typeface.NORMAL);
        timerLeavTextView = findViewById(R.id.text_timer_);
        timerLeavTextView.setTypeface(font, Typeface.BOLD);
        windowLeave = findViewById(R.id.window_leave);
        black_veil = findViewById(R.id.black_veil_2);
        lastWordView = findViewById(R.id.ostalos_slov);
        lastWordView.setTypeface(font, Typeface.NORMAL);
        playersView = findViewById(R.id.player_text_view);
        playersView.setTypeface(font, Typeface.NORMAL);
        opponentView = findViewById(R.id.opp_text_view);
        opponentView.setTypeface(font, Typeface.NORMAL);
        spinnerGen = findViewById(R.id.spinner_gen);
        genTextView = findViewById(R.id.text_gen);
        genTextView.setTypeface(font, Typeface.NORMAL);
        scoringTextView = findViewById(R.id.scoring);
        scoringTextView.setTypeface(font, Typeface.BOLD);
//        userScoreName = findViewById(R.id.user_result_name);
//        userScoreName.setTypeface(font, Typeface.BOLD);
//        userScoreView = findViewById(R.id.user_result);
//        userScoreView.setTypeface(font, Typeface.BOLD);
//        oppScoreName = findViewById(R.id.opp_result_name);
//        oppScoreName.setTypeface(font, Typeface.BOLD);
//        oppScoreView = findViewById(R.id.opp_result);
//        oppScoreView.setTypeface(font, Typeface.BOLD);
//        myTimerView = findViewById(R.id.my_timer);
//        myTimerView.setTypeface(font, Typeface.BOLD);
//        oppTimerView = findViewById(R.id.opp_timer);
//        oppTimerView.setTypeface(font, Typeface.BOLD);
//        myTimerView.setText(String.format("%02d:%02d", myTimeCount / 60, myTimeCount % 60));
//        oppTimerView.setText(String.format("%02d:%02d", oppTimeCount / 60, oppTimeCount % 60));
        playersView.setText(userName);
        opponentView.setText(opponentName);
//        oppScoreName.setText(opponentName);
//        userScoreName.setText(userName);
        soundButton = findViewById(R.id.sound_toggle_bliz);
        map = findViewById(R.id.online_map);
        socketInit();

        repeatButtonListener();
        leaveButtonListener();
        sendButtonListener();
        startMatch();
        adInit();
        soundButtonListener();
        soundInit();
        //приветствие бота через 4 сек после начала
        if(isBot){
            mHandler.postDelayed(helloMessage, 4000);
        }
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        hideSystemUI();
        setUIListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Если играешь не против бота, то сообщить сопернику что вышел
        if(!isBot) emitLeaving();

        mSocket.off("oppLeaved", onOppLeaved);
        mSocket.off("startGame", onStartGame);
        mSocket.off("yourturn", onYourTurn);
        mSocket.off("finish", onFinish);
        mSocket.off("personalMessage", onPersonalMessage);
        mHandler.removeCallbacksAndMessages(null);
    }


    private void socketInit(){
        mSocket.on("oppLeaved", onOppLeaved);
        mSocket.on("startGame", onStartGame);
        mSocket.on("yourturn", onYourTurn);
        mSocket.on("finish", onFinish);
        mSocket.on("personalMessage", onPersonalMessage);
        mSocket.on("repeat", onRepeat);
    }

    private void startMatch(){
        //Показать имена соперников

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isMyTurn = false;
                        wantRepeat=false;
                        oppWantRepeat=false;
                        globalCheck = new int[mapSize][mapSize];
                        globalCheck = setArray(globalCheck, 1, false);  // задали глобал чек - все плитки нужны
                        checkArray = new int [mapSize][mapSize];
                        mapMainArray = new char[mapSize][mapSize];
                        globalWordList = readSpisokSlov();
                        sessionWordList = new ArrayList<>();
                        myAnswersWordList = new ArrayList<>();
                        sessionAnswersWordList = new ArrayList<>();
                        buttonLeave.setVisibility(View.GONE);
                        buttonRepeat.setVisibility(View.GONE);
//                        userScoreName.setVisibility(View.GONE);
//                        userScoreName.setTextColor(getResources().getColor(R.color.midnightBlue));
//                        userScoreView.setTextColor(getResources().getColor(R.color.midnightBlue));
//                        userScoreView.setVisibility(View.GONE);
//                        oppScoreName.setVisibility(View.GONE);
//                        oppScoreName.setTextColor(getResources().getColor(R.color.midnightBlue));
//                        oppScoreView.setVisibility(View.GONE);
//                        oppScoreView.setTextColor(getResources().getColor(R.color.midnightBlue));
                        scoringTextView.setVisibility(View.GONE);
                        scoringTextView.setTextColor(getResources().getColor(R.color.midnightBlue));

                        playersView.setAlpha(0.5f);
//                        setPlaerMoveBackAlpha(0.5f);
//                        myTimerView.setAlpha(0.5f);
                        opponentView.setAlpha(0.5f);
//                        oppTimerView.setAlpha(0.5f);
//                        oppMoveImageBack.setAlpha(0.5f);
                        //Обнулить показания таймеров
                        myTimeCount = 60;
                        oppTimeCount = 60;
                        progressGame.setMax(myTimeCount + oppTimeCount);
                        progressGame.setProgress(myTimeCount);
//                        oppTimerView.setText(String.format("%02d:%02d", oppTimeCount / 60, oppTimeCount % 60));
//                        myTimerView.setText(String.format("%02d:%02d", myTimeCount / 60, myTimeCount % 60));
                        //Показать спинер и надпись
                        spinnerGen.setVisibility(View.VISIBLE);
                        genTextView.setVisibility(View.VISIBLE);
                    }});
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                    if(isHost && !isAlreadyGenerate) generateMap();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void generateMap(){
        isAlreadyGenerate = true;


        //Создаем список из 5-значных слов
        List<String> list = new ArrayList<>();

        char[][] mapArray = new char[mapSize][mapSize];
        for(String line : globalWordList){
            if(line.length() == mapSize){
                list.add(line);
            }
        }
        //Рандомно выбираем одно слово
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(list.size());
        String centralWord = list.get(randomInt);
        //Запонляем им середину массива
        for(int j =0; j < mapSize; j++){
            mapArray[mapSize/2][j] = centralWord.charAt(j);
        }
        //Запонляем низ массива буквами "ь", чтобы он не был пустой
        for(int i = mapSize/2 +1; i<mapSize; i++){
            for(int j = 0; j<mapSize; j++){
                mapArray[i][j] = 'ь';
            }
        }
        //запонляем верх массива буквами, чтобы получилось 15 слов
        int condition = 15;
        String alphabet = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
        long timeout = 1000;    //1 сек (максимальное время ожидания timeout * 15 = 15 сек)
        List<String> words;
    firstStage:  while(true){
            //заполняем рандомно весь верх
            for(int i = 0; i<mapSize/2; i++){
                for(int j = 0; j<mapSize; j++){
                    Random random = new Random();
                    mapArray[i][j] = alphabet.charAt(random.nextInt(alphabet.length()));
                }
            }
            long time = System.currentTimeMillis();
            while(System.currentTimeMillis() - time < timeout) {    //таймаут перегенерации неиспользуемых клеток

                //проверяем на 15 слов
                words = poiskSlovNaMap(mapArray);
                if (words.size() > condition) break firstStage; //если выполнилось условие, то заканчивает генерацию верха
                else{       //Если размер не удовлетворяет

                    //сбросили массив используемых клеток
                    globalCheck = setArray(globalCheck, 0, false);
                    //выставили буквы которые используются
                    for (String line : words) {
                        uborka(line, new int[mapSize][mapSize], "", 20, 20);
                    }
                    //перегенерировали неиспользуемые клетки верха массива
                    for(int i = 0; i<mapSize/2; i++){
                        for(int j = 0; j<mapSize; j++){
                            if(globalCheck[i][j]==1) continue;      // если клетка используется, то пропускаем перегенерацию
                            Random random = new Random();
                            mapArray[i][j] = alphabet.charAt(random.nextInt(alphabet.length()));
                        }
                    }
                }
            }
            if(condition>10) condition--;       //каждую секунду уменьшаем требование
            else break;     //Если упали ниже 10, выходим из цикла
        }

        condition = 30;
        //заполняем низ массива рандомными буквами, чтобы получилось 30 слов
        secondStage:    while(true){
            //заполняем рандомно весь низ
            for(int i = mapSize/2 +1; i<mapSize; i++){
                for(int j = 0; j<mapSize; j++){
                    Random random = new Random();
                    mapArray[i][j] = alphabet.charAt(random.nextInt(alphabet.length()));
                }
            }
            long time = System.currentTimeMillis();
            while(System.currentTimeMillis() - time < timeout) {    //таймаут перегенерации неиспользуемых клеток

                //проверяем на 30 слов
                words = poiskSlovNaMap(mapArray);
                if (words.size() > condition) break secondStage; //если выполнилось условие, то заканчивает генерацию верха
                else{       //Если размер не удовлетворяет

                    //сбросили массив используемых клеток
                    globalCheck = setArray(globalCheck, 0, false);
                    //выставили буквы которые используются
                    for (String line : words) {
                        uborka(line, new int[mapSize][mapSize], "", 20, 20);
                    }
                    //перегенерировали неиспользуемые клетки низа массива
                    for(int i = mapSize/2 +1; i<mapSize; i++){
                        for(int j = 0; j<mapSize; j++){
                            if(globalCheck[i][j]==1) continue;      // если клетка используется, то пропускаем перегенерацию
                            Random random = new Random();
                            mapArray[i][j] = alphabet.charAt(random.nextInt(alphabet.length()));
                        }
                    }
                }
            }
            if(condition>20) condition--;       //Если опустились ниже 20 слов, то берем карту из файла
            else{
                mapArray = readRandomMapFromFile();
                break;
            }
        }
        globalCheck = setArray(globalCheck, 1, false);
        isAlreadyGenerate = false;
        //Заполнили строку из букв карты
        String mapAsString = "";
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                mapAsString += mapArray[i][j];
            }
        }
        final String mapAsStr = mapAsString;
        if(!isBot) {    //Если игра против человека - отправить ему карту
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("username", userName);
                jsonData.put("opponent", opponentName);
                jsonData.put("map", mapAsStr);
                mSocket.emit("sendMap", jsonData);
            } catch (JSONException e) {
                Log.d("me", "error send map " + e.getMessage());
            }
        }else{      //Иначе начинаем игру
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startGame(mapAsStr);
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    private char[][] readRandomMapFromFile(){
        char[][] randomMap = {{'a','a','a','a','a'}, {'a','a','a','a','a'},{'б','a','л','к','a'},{'a','a','a','a','a'},{'a','a','a','a','a'},};  //защита от фейла
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(getAssets().open("lilMedium.txt")));
            List<String> lines = new ArrayList<>();
            String line;
            Random random = new Random();
            int lvl = random.nextInt(999) + 1;
            boolean flak = false;
            while ((line = reader.readLine()) != null) {
                //Если нашли карту нужного уровня
                if(line.indexOf("[L " + String.valueOf(lvl)+"]") >= 0){
                    //задать флаг считывания
                    flak = true;
                    //считать следующую строку
                    continue;
                }
                //если дошли до следующей карты, выйти из цикла
                if(flak && (line.indexOf("[L ") >= 0)) break;
                //Если установлен флаг, читаем строку
                if(flak)    lines.add(line);
            }
            for(int i =0; i<mapSize;i++){
                for(int j=0;j<mapSize; j++){
                    randomMap[i][j] = lines.get(i).charAt(j);
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.i("zhopa", e.getMessage());
        }
        return randomMap;
    }

    private void leaveWithTimer(){
        sendMessageButton.setEnabled(false);
        textInputView.setEnabled(false);
        buttonLeave.setVisibility(View.GONE);
        buttonRepeat.setVisibility(View.GONE);
        mHandler.removeCallbacks(oppTimerCountdown);
        mHandler.removeCallbacks(myTimerCountdown);
        mHandler.removeCallbacks(timerUpdaterToStart);
        mHandler.removeCallbacks(showButtonAndResult);
        mHandler.removeCallbacksAndMessages(null);

        oppleavTextView.setVisibility(View.VISIBLE);
        youKickTextView.setVisibility(View.VISIBLE);
        timerLeavTextView.setVisibility(View.VISIBLE);
        windowLeave.setVisibility(View.VISIBLE);
        black_veil.setVisibility(View.VISIBLE);

        spinnerGen.setVisibility(View.GONE);
        genTextView.setVisibility(View.GONE);

        map.setVisibility(View.GONE);

        //startTimer
        timerCounter = 6;
        mHandler.post(timerUpdaterRunnable);
    }

    private void receiveMessage(Message message){
        messages.add(message);
        // обновили список
        messageAdapter.notifyDataSetChanged();
        // прокрутили вниз
        messagesView.setSelection(messageAdapter.getCount()-1);
    }



    private Emitter.Listener onPersonalMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    Message mess;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        mess = new Message(message, username);
                        // add the message to view
                        receiveMessage(mess);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onRepeat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        oppWantRepeat = true;
                        if(wantRepeat){
                            // выставить цвет текста "повторить?" - зеленый
                            scoringTextView.setTextColor(getResources().getColor(R.color.nephrits));
                            //повторить
                            mHandler.postDelayed(startMatchDelayed, 2000);
                        }else{
                            //выставить цвет текста "повторить?" - желтый
                            scoringTextView.setTextColor(getResources().getColor(R.color.amethyst));
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };


    private void sendButtonListener(){
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isBot) {
                    sendMessageToServer();
                }else{
                    String textMess = textEditView.getText().toString().trim();
                    if(!"".equals(textMess)) {
                        //очищаем поле
                        textEditView.setText("");
                        //убираем клавиатуру
                        hideKeyboard();
                        //добавляем сообщение
                        Message mess = new Message(textMess, userName);
                        receiveMessage(mess);
                        mHandler.postDelayed(answerMess, 4000);
                    }
                }
            }
        });
    }

    private void repeatButtonListener(){
        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBot) {   //если против человека
                    //sendServerRepeat
                    buttonRepeat.setVisibility(View.GONE);
                    try {
                        JSONObject jsonData = new JSONObject();
                        jsonData.put("opponent", opponentName);
                        mSocket.emit("repeat", jsonData);
                    } catch (JSONException e) {
                        Log.d("me", "error send inv " + e.getMessage());
                    }
                    wantRepeat = true;
                    if (oppWantRepeat) {
                        // Выставить цвет текста "повторить" - зеленый
                        scoringTextView.setTextColor(getResources().getColor(R.color.nephrits));
                        mHandler.postDelayed(startMatchDelayed, 2000);
                    } else {
                        //выставить цвет текста "повторить?" - желтый
                        scoringTextView.setTextColor(getResources().getColor(R.color.amethyst));
                    }
                }
                else{   //если против бота - начать следующий матч
                    scoringTextView.setTextColor(getResources().getColor(R.color.nephrits));
                    mHandler.postDelayed(startMatchDelayed, 2000);
                }
            }
        });
    }

    private void leaveButtonListener(){
        buttonLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Если играешь не против бота, то сообщить сопернику что вышел
                if(!isBot)  emitLeaving();

                finish();
                //Добавить рекламный баннер
            }
        });
    }

    private void sendMessageToServer(){
        //получаем текст и формируем сообщение если текст более 0 символов
        String textMess = textEditView.getText().toString().trim();
        if(!"".equals(textMess)) {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("message",textMess);
                jsonData.put("username",this.userName);
                jsonData.put("opponent",opponentName);
                mSocket.emit("personalMessage", jsonData);
            } catch (JSONException e) {
                Log.d("me", "error send message " + e.getMessage());
            }
            //очищаем поле
            textEditView.setText("");
            //убираем клавиатуру
            hideKeyboard();
        }
    }

    private Emitter.Listener onOppLeaved = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        leaveWithTimer();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onStartGame = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        startGame(data.getString("map"));
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private void startGame(String matrixMap){
        //Показать таймер 5 сек
        Log.i("zhopa", matrixMap);
        timerLeavTextView.setVisibility(View.VISIBLE);
        timerCounter = 6;
        mHandler.post(timerUpdaterToStart);
        genTextView.setVisibility(View.GONE);
        spinnerGen.setVisibility(View.GONE);
        //Прочитать JSON, setMap
//        Log.i("zhopa", mapMainArray + " MAP");
        mapSetTouchListener();
        parseStringToCharArray(matrixMap);
        sessionWordList = poiskSlovNaMap(mapMainArray);
        //Показать карту
        lastWordView.setText("Всего слов: "+sessionWordList.size());
        lastWordView.setVisibility(View.VISIBLE);


        map.init(mapSize, width, mapMainArray);
        map.setVisibility(View.VISIBLE);
        map.setAlpha(0.5f);
    }

    private Emitter.Listener onYourTurn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        //обновили списки слов и вьюшку
                        String lastWordRecieved = data.getString("lastword");
                        sessionAnswersWordList.add(lastWordRecieved);
                        sessionWordList.remove(lastWordRecieved);
                        lastWordView.setText(lastWordRecieved+ ", ("+sessionWordList.size()+")");
                        //обновили карту
                        globalCheck = setArray(globalCheck, 0, false);
                        for (String line : sessionWordList) {
                            uborka(line, new int[mapSize][mapSize], "", 20, 20);
                        }
                        map.invalidate();
                        //остановили таймер оппа
                        mHandler.removeCallbacks(oppTimerCountdown);
                        //Узнали его время
                        oppTimeCount = Integer.parseInt(data.getString("mytime"));

                        opponentView.setAlpha(0.5f);
                        //дали доступ к карте, запустили таймер
                        yourTurn();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onFinish = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        //получили очки соперника
                        oppTimeCount = Integer.parseInt(data.getString("score"));
                        //обновили прогресбар
                        progressGame.setMax(myTimeCount + oppTimeCount);
                        progressGame.setProgress(myTimeCount);
                        //остановили обнову
                        mHandler.removeCallbacks(oppTimerCountdown);
                        //Обработка победы/поражения
                        showWindowResult();

                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private void botTurn(){
        int waitTime;
        Random random = new Random();
        if(oppTimeCount > 15){          //Если больше 15 сек
            //ждем 9-13 сек
            waitTime = random.nextInt(5);
            waitTime += 9;
        }else if(oppTimeCount > 10){    //Если больше 10 сек
            //ждём 5-9 сек
            waitTime = random.nextInt(5);
            waitTime += 5;
        }else if(oppTimeCount > 5){     //Если больше 5 сек
            //ждём 3 сек
            waitTime = 3;
        }else{
            //если меньше 5 сек, не ждем
            waitTime = 0;
        }
        mHandler.postDelayed(botWord, waitTime*1000);
    }

    private Runnable botWord = new Runnable() {
        public void run() {
            try {
                Random random = new Random();
                //Сортируем
//                sortir();
                //Отгадываем самое мелкое слово
//                String ans = sessionWordList.get(sessionWordList.size()-1);
                String ans = sessionWordList.get(random.nextInt(sessionWordList.size()));
                oppTimeCount += getWordScore(ans);
                if(sessionWordList.size() > 1) {//Если еще есть слова делаем очередь игрока
                    playerTurnFromBot(ans);
                }else{  //Иначе команда подсчета очков
                    try {
                        //обновили прогресбар
                        progressGame.setMax(myTimeCount + oppTimeCount);
                        progressGame.setProgress(myTimeCount);
                        //остановили обнову
                        mHandler.removeCallbacks(oppTimerCountdown);
                        //Обработка победы/поражения
                        showWindowResult();
                    } catch (Exception e) {
                        return;
                    }
                }
            } catch (Exception e) {
                Log.d("zhopa", "error send name " + e.getMessage());
            }
        }
    };

    private void playerTurnFromBot(String lastWordRecieved){
        sessionAnswersWordList.add(lastWordRecieved);
        sessionWordList.remove(lastWordRecieved);
        lastWordView.setText(lastWordRecieved+ ", ("+sessionWordList.size()+")");
        //обновили карту
        globalCheck = setArray(globalCheck, 0, false);
        for (String line : sessionWordList) {
            uborka(line, new int[mapSize][mapSize], "", 20, 20);
        }
        map.invalidate();
        //остановили таймер оппа
        mHandler.removeCallbacks(oppTimerCountdown);

        opponentView.setAlpha(0.5f);
        //дали доступ к карте, запустили таймер
        yourTurn();
    }

    private void yourTurn(){

        sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 1);
        mHandler.removeCallbacks(timerUpdaterToStart);
        try{
            Toast toast =Toast.makeText(getApplicationContext(),
                    "Ваш ход", Toast.LENGTH_SHORT);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(20);
            messageTextView.setTypeface(font);
            toast.setGravity(Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),
                    "Ваш ход", Toast.LENGTH_SHORT).show();
        }
        //открыли доступ к карте
        //убрали клаву
        hideKeyboard();
        isMyTurn = true;
        map.setAlpha(1);
        playersView.setAlpha(1);
        //запустили таймер
        mHandler.post(myTimerCountdown);
    }

    private Runnable timerUpdaterRunnable = new Runnable() {
        public void run() {
            timerLeavTextView.setText(String.valueOf(--timerCounter));
            // повторяем через каждые 1000 миллисекунд
            if(timerCounter>0) mHandler.postDelayed(this, 1000);
            else   {
                //Если таймер обнулился то финиш
                try {
                    finish();
                } catch (Exception e) {
                    Log.d("zhopa", "error send name " + e.getMessage());
                }
            }
        }
    };

    private Runnable startMatchDelayed = new Runnable() {
        public void run() {
            try {
                startMatch();
            } catch (Exception e) {
                Log.d("zhopa", "error send name " + e.getMessage());
            }
        }
    };

    private Runnable answerMess = new Runnable() {
        public void run() {
            try {

                Message firstMess = new Message("Не знаю как ответить", opponentName);
                Message secondMess = new Message("Я к такому не готов", opponentName);
                ArrayList<Message> list = new ArrayList<>();
                list.add(firstMess);
                list.add(secondMess);
                Random random = new Random();


                receiveMessage(list.get(random.nextInt(2)));
            } catch (Exception e) {
                Log.d("zhopa", "error send name " + e.getMessage());
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        if (mInterstitialAd != null) {
            mInterstitialAd.show(OnlineBlitzGame.this);
        }
    }

    public void sortir(){
        for(int i = sessionWordList.size()-1 ; i > 0 ; i--){
            for(int j = 0 ; j < i ; j++){
                if( sessionWordList.get(j).length() < sessionWordList.get(j+1).length() ){
                    String tmp = sessionWordList.get(j);
                    sessionWordList.set(j, sessionWordList.get(j+1));
                    sessionWordList.set(j+1, tmp);
                }
            }
        }
    }

    private void adInit(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-9759163947336772/1306461512", adRequest, new InterstitialAdLoadCallback() {          // Рабочая  штука
//                    InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {                      //тестовая
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });
    }

    private Runnable helloMessage = new Runnable() {
        public void run() {
            try{
                Message mess = new Message( "Приветствую! Людей не нашлось, но меня хорошо обучили!", opponentName);
                receiveMessage(mess);
            }catch (Exception e){}
        }
    };


    private Runnable showButtonAndResult = new Runnable() {
        public void run() {
            buttonRepeat.setVisibility(View.VISIBLE);
            buttonLeave.setVisibility(View.VISIBLE);
        }
    };

    private Runnable timerUpdaterToStart = new Runnable() {
        public void run() {
            timerLeavTextView.setText(String.valueOf(--timerCounter));
            // повторяем через каждые 1000 миллисекунд
            if(timerCounter>0) mHandler.postDelayed(this, 1000);
            else   {
                //Если таймер обнулился то старт
                try {
                    map.setVisibility(View.VISIBLE);
                    timerLeavTextView.setVisibility(View.GONE);
                    if(isHost){    //Если вы хост, то ваш первый ход
                        yourTurn();
                    }
                    else{
                        opponentView.setAlpha(1);
                        mHandler.post(oppTimerCountdown);
                    }
                } catch (Exception e) {
                    Log.d("zhopa", "error send name " + e.getMessage());
                }
            }
        }
    };


    private Runnable oppTimerCountdown = new Runnable() {
        public void run() {
            oppTimeCount--;
            progressGame.setMax(myTimeCount + oppTimeCount);
            progressGame.setProgress(myTimeCount);

//            oppTimerView.setText(String.format("%02d:%02d", oppTimeCount / 60, oppTimeCount % 60));
            // повторяем через каждые 1000 миллисекунд
            if(oppTimeCount>0) mHandler.postDelayed(this, 1000);
        }
    };

    private Runnable myTimerCountdown = new Runnable() {
        public void run() {
            myTimeCount--;
            progressGame.setMax(myTimeCount + oppTimeCount);
            progressGame.setProgress(myTimeCount);

//            myTimerView.setText(String.format("%02d:%02d", myTimeCount / 60, myTimeCount % 60));

            // повторяем через каждые 1000 миллисекунд
            if(myTimeCount>0) mHandler.postDelayed(this, 1000);
            else   {
                //Если таймер обнулился то отправить сообщение о конце матча
                loseMatch();
            }
        }
    };

    private void loseMatch(){
        //отправить результат на сервер
        sendMessageEndMatch("finish");
        //показать кнопки, слово
        showWindowResult();
    }

    private void sendMessageEndMatch(String tag){
        try {
            //Убрать карту
            map.setVisibility(View.GONE);
            lastWordView.setVisibility(View.GONE);
            //Отправить сообщение
            JSONObject jsonData = new JSONObject();
            jsonData.put("opponent", opponentName);
            jsonData.put("score", myTimeCount);
            //Если играешь не против бота, то отправить сообщение человеку
            if(!isBot) mSocket.emit(tag, jsonData);

            mHandler.removeCallbacks(oppTimerCountdown);
            mHandler.removeCallbacks(myTimerCountdown);

        } catch (Exception e) {
            Log.d("zhopa", "error send name " + e.getMessage());
        }
    }

    private void showWindowResult(){
        map.setVisibility(View.GONE);
        lastWordView.setVisibility(View.GONE);

        if(myTimeCount>oppTimeCount){   //Выиграл
            // установить текст - победа
            scoringTextView.setText("Победа!");
        }else if(myTimeCount == oppTimeCount){
            // установить текст - ничья
            scoringTextView.setText("Ничья!");
        }else{
            // установить текст - поражение
            scoringTextView.setText("Поражение");
        }
        scoringTextView.setVisibility(View.VISIBLE);
        //показать копки
        mHandler.postDelayed(showButtonAndResult, 1500);
    }

    private void emitLeaving(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("opponent", opponentName);
            mSocket.emit("leavingGame", jsonData);
        } catch (JSONException e) {
            Log.d("me", "error send inv " + e.getMessage());
        }
    }

    public List<String> poiskSlovNaMap(char[][] map){
        List<String> words = new ArrayList<String>();
        for (String line : globalWordList){
            if (recurr(line, new int[mapSize][mapSize], "", 20, 20, map)){
                words.add(line);
            }
        }
        return words;
    }

    public int[][] cloneArray(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }

    public boolean recurr(String word, int[][] chack, String str, int oldI, int oldJ, char[][] map){
        if(str.length() == word.length()) return true;
        boolean flag = false;
        for(int i =0; i<mapSize; i++){
            for(int j=0; j<mapSize; j++){
                if(map[i][j]==word.charAt(str.length()) && chack[i][j]!=1 && (str.equals("") || (((Math.abs(oldJ-j) == 1) && (Math.abs(oldI-i) == 0)) || ((Math.abs(oldJ-j) == 0) && (Math.abs(oldI-i) == 1)))  )){
                    int [][]klon = cloneArray(chack);
                    klon[i][j] = 1;
                    flag = recurr(word, klon, str+map[i][j], i, j, map);
                    if (flag) break ;
                }
            }
            if(flag) break;
        }
        return flag;
    }

    private int getWordScore(String word){
        if(word.length() == 3) return 6;
        else if(word.length() == 4) return 8;
        else if(word.length() == 5) return 12;
        else return 16;
    }

    private int[][] setArray(int array[][], int value, boolean checkZero) {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                if(checkZero){
                    if (array[i][j] != 0 ) array[i][j] = value;
                } else {
                    array[i][j] = value;
                }
            }
        }
        return array;
    }

    private List<String> readSpisokSlov(){
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("wordlist.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (Exception e) {
            Log.i("zhopa", e.getMessage());
        }
        return lines;
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

    private void mapSetTouchListener() {

        map.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(!isMyTurn) return true;

                int x = (int) event.getX();
                int y = (int) event.getY();
                int i = getIJ(y);
                int j = getIJ(x);


                //flagMove=false- значит движение было, checkArray!=, ячейка не синяя, остальное условие для передвижения на 1 клетку по гориз/верт
                if ((((Math.abs(column - j) == 1) && (Math.abs(stroke - i) == 0)) || ((Math.abs(column - j) == 0) && (Math.abs(stroke - i) == 1))) && flagMove == false && checkArray[i][j] != 1) {
                    //если плитка существует
                    if (globalCheck[i][j] == 1) {
                        flagMove = true;
                        column = j;
                        stroke = i;
                        sp.play(soundIdTap,  soundsVolume,  soundsVolume, 0, 0, 1);
                        checkArray[i][j] = 1;
                    } else {
                        j = column;
                        i = stroke;
                    }
                }
                if (i == 10 || j == 10) {
                    flagMove = true;
                    column = 20;
                    stroke = 20;
                    for (String lineArray : sessionWordList){
                        if (str.equals(lineArray)) {                         // Если отгадал
                            myTimeCount += getWordScore(str);
                            progressGame.setMax(myTimeCount + oppTimeCount);
                            progressGame.setProgress(myTimeCount);
                            //Поменяли таймеры
                            mHandler.removeCallbacks(myTimerCountdown);
                            mHandler.post(oppTimerCountdown);
                            //звук
                            sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 2);
                            //обновили списки
                            myAnswersWordList.add(str);
                            sessionAnswersWordList.add(str);
                            sessionWordList.remove(str);
                            //если не последнее слово
                            if(!sessionWordList.isEmpty()) {
                                playersView.setAlpha(0.5f);
                                opponentView.setAlpha(1);
                                map.setAlpha(0.5f);
                                isMyTurn = false;
                                if(!isBot) {    //Если играешь не против бота, то отправить сообщение человеку
                                    try {
                                        JSONObject jsonData = new JSONObject();
                                        jsonData.put("lastword", str);
                                        jsonData.put("opponent", opponentName);
                                        jsonData.put("mytime", myTimeCount);
                                        mSocket.emit("yourturn", jsonData);
                                    } catch (JSONException e) {
                                        Log.d("me", "error send inv " + e.getMessage());
                                    }
                                } else{     //Иначе ход бота
                                    botTurn();
                                }
                            }
                            checkArray = setArray(checkArray,2, true);
                            globalCheck = setArray(globalCheck, 0, false);
                            for (String line : sessionWordList) {
                                uborka(line, new int[mapSize][mapSize], "", 20, 20);
                            }
                            lastWordView.setText(str+ ", ("+sessionWordList.size()+")");
                            flagComp = true;
                            break;
                        } else if (sessionAnswersWordList.contains(str)) {             // Если было
                            checkArray = setArray( checkArray,3, true);
                            flagComp = true;
                            break;
                        }
                    }
                    if (!flagComp) {                      // Если не отгадал слово
                        checkArray = setArray( checkArray,4, true);
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
                                    checkArray = setArray(checkArray, 0, true);
                                    map.invalidate();
                                }
                            });

                        }
                    });
                    t.start();
                    if (sessionWordList.isEmpty()) {
                        sendMessageEndMatch("finish");
                        showWindowResult();
                    }
                    return true;
                }
                //Тут заканчивается условие если человек увел палец с края карты, дальше обработка событий, включая отпускания (повторение почти тоже самое)
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        column = j;
                        stroke = i;
                        flagMove = false;
                        str = String.valueOf(mapMainArray[i][j]);
                        sp.play(soundIdTap,  soundsVolume,  soundsVolume, 0, 0, 1);
                        checkArray[i][j] = 1;
                        map.invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (flagMove) {
                            str += String.valueOf(mapMainArray[i][j]);
                            map.invalidate();
                            flagMove = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        flagMove = true;
                        column = 20;
                        stroke = 20;
                        for (String lineArray : sessionWordList){
                            if (str.equals(lineArray)) {                         // Если отгадал
                                myTimeCount += getWordScore(str);
                                progressGame.setMax(myTimeCount + oppTimeCount);
                                progressGame.setProgress(myTimeCount);
                                //Поменяли таймеры
                                mHandler.removeCallbacks(myTimerCountdown);
                                mHandler.post(oppTimerCountdown);
                                //звук
                                sp.play(soundIdMatch,  soundsVolume,  soundsVolume, 0, 0, 2);
                                //обновили списки
                                myAnswersWordList.add(str);
                                sessionAnswersWordList.add(str);
                                sessionWordList.remove(str);
                                //если не последнее слово
                                if(!sessionWordList.isEmpty()) {
                                    playersView.setAlpha(0.5f);
                                    opponentView.setAlpha(1);
                                    map.setAlpha(0.5f);
                                    isMyTurn = false;
                                    if(!isBot) {    //Если играешь не против бота, то отправить сообщение человеку
                                        try {
                                            JSONObject jsonData = new JSONObject();
                                            jsonData.put("lastword", str);
                                            jsonData.put("opponent", opponentName);
                                            jsonData.put("mytime", myTimeCount);
                                            mSocket.emit("yourturn", jsonData);
                                        } catch (JSONException e) {
                                            Log.d("me", "error send inv " + e.getMessage());
                                        }
                                    } else{     //Иначе ход бота
                                        botTurn();
                                    }
                                }
                                checkArray = setArray(checkArray,2, true);
                                globalCheck = setArray(globalCheck, 0, false);
                            for (String line : sessionWordList) {
                                uborka(line, new int[mapSize][mapSize], "", 20, 20);
                            }
                                lastWordView.setText(str+ ", ("+sessionWordList.size()+")");
                                flagComp = true;
                                break;
                            } else if (sessionAnswersWordList.contains(str)) {             // Если было
                                checkArray = setArray( checkArray,3, true);
                                flagComp = true;
                                break;
                            }
                        }
                        if (!flagComp) {                      // Если не отгадал слово
                            checkArray = setArray( checkArray,4, true);
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
                                        checkArray = setArray(checkArray, 0, true);
                                        map.invalidate();
                                    }
                                });

                            }
                        });
                        t.start();
                        if (sessionWordList.isEmpty()) {
                            sendMessageEndMatch("finish");
                            showWindowResult();
                            //////////////////////////////////FINISH GAME /////////////////////////
                        }
                        break;
                }
                return true;
            }
        });
    }

    public int getIJ(int XY){          // Получение номера строки/стобца. Второй аргумент - левый угол таблицы
        int ji=0;
        while(ji<mapSize){
            if(XY  < side*(ji+1)) return ji;
            ji++;
        }
        return 10;
    }

    private void soundInit(){
        sp = new SoundPool(MAX_STREAMS_SOUND, AudioManager.STREAM_MUSIC, 0);
        soundIdTap = sp.load(this, R.raw.generate, 1);
        soundIdYourTurn = sp.load(this, R.raw.match, 1);
        soundIdMatch = sp.load(this, R.raw.eat1, 1);
    }

    public boolean uborka(String word, int[][] chack, String str, int oldI, int oldJ){
        if(str.length() == word.length()){
            for(int z=0; z< mapSize ; z++){
                for(int x = 0 ; x<mapSize; x++){
                    if(chack[z][x]==1) globalCheck[z][x] = 1;
                }
            }
            return true;}
        boolean flag = false;
        for(int i =0; i<mapSize; i++){
            for(int j=0; j<mapSize; j++){
                if(mapMainArray[i][j]==word.charAt(str.length()) && chack[i][j]!=1 && (str.equals("") || (((Math.abs(oldJ-j) == 1) && (Math.abs(oldI-i) == 0)) || ((Math.abs(oldJ-j) == 0) && (Math.abs(oldI-i) == 1)))  )){
                    int [][]klon = cloneArray(chack);
                    klon[i][j] = 1;
                    flag = uborka(word, klon, str+mapMainArray[i][j], i, j);
                    if (flag) break ;
                }
            }
            if(flag) break;
        }
        return flag;
    }

    private void parseStringToCharArray(String string){
        int count=0;
        for(int i=0; i<mapSize; i++){
            for(int j=0; j<mapSize; j++){
                mapMainArray[i][j] = string.charAt(count++);
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        mHandler.removeCallbacksAndMessages(null);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        if (mInterstitialAd != null) {
            mInterstitialAd.show(OnlineBlitzGame.this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        hideSystemUI();
    }

    private void setUIListener(){
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hideSystemUI();
            }
        });
    }


    private void hideKeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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