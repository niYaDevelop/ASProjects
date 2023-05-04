 package space.yakimov.firstapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;

 public class OnlineMenu extends AppCompatActivity implements FriendList.FriendListFragmentListener , FriendExpandableAdapter.FriendListButtonsListener {

    private String userName, opponent;
    public static Socket mSocket;
    private boolean isBusy, isComingAfterGame = false, isOnPause;
    private int timerCounter;
    private Handler mHandler = new Handler();
    private SharedPreferences nSettings;
    private ArrayList<String> onlineClients = new ArrayList<>();
    private TextInputLayout registerNameInputView;
    private EditText registerNameEditView;
    private Button  declineButton, acceptButton, cancelButton, inviteButton, buttonRegister, mmButton, mmButtonStop;
    private TextView textViewInvitedUser, textTimerWaiting, userNameView, registerRulesView, mmTextView;
    private ImageView blackVeil, inviteWindow, registerWindow;
    private ProgressBar mmSpinner;
    private Typeface font;
    FriendList friendListFragment;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_menu);
        nSettings = getSharedPreferences("ssettings", Context.MODE_PRIVATE);
        font = Typeface.createFromAsset(getAssets(), "fonts/comic.ttf");
        blackVeil = findViewById(R.id.black_veil);
        userNameView = findViewById(R.id.user_name);
        userNameView.setTypeface(font, Typeface.BOLD);
        textViewInvitedUser = findViewById(R.id.invited_player);
        textViewInvitedUser.setTypeface(font, Typeface.BOLD);
        registerNameEditView = findViewById(R.id.text_edit_name);
        registerNameEditView.setTypeface(font, Typeface.NORMAL);
        registerNameInputView = findViewById(R.id.name_field);
        registerRulesView = findViewById(R.id.register_rules);
        registerRulesView.setTypeface(font, Typeface.ITALIC);
        registerWindow = findViewById(R.id.window_register);
        buttonRegister = findViewById(R.id.button_register);
        textTimerWaiting = findViewById(R.id.timer_text_view);
        textTimerWaiting.setTypeface(font, Typeface.BOLD);
        inviteWindow = findViewById(R.id.window_invite);
        acceptButton = findViewById(R.id.accept_button);
        declineButton = findViewById(R.id.decline_button);
        cancelButton = findViewById(R.id.cancel_button);
        inviteButton = findViewById(R.id.invite_button);
        drawerLayout = findViewById(R.id.drawer_layout);
        mmButton = findViewById(R.id.match_making_button);
        mmButtonStop = findViewById(R.id.match_making_button_stop);
        mmTextView = findViewById(R.id.match_making_text_invite);
        mmTextView.setTypeface(font, Typeface.ITALIC);
        mmSpinner = findViewById(R.id.match_making_progress_bar);


        mSocketInit();
        setUserName();
        cancelButtonListener();
        acceptButtonListener();
        declineButtonListener();
        mmButtonListener();
        MmButtonStopListener();
        swipeListener();
        friendListFragment = (FriendList) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        hideSystemUI();
        setUIListener();
        if(isComingAfterGame){
            isComingAfterGame = false;
            isBusy = false;
        }
        isOnPause = false;
        mSocket.emit("onlineRequest");
    }

     @Override
     public void onPause() {  // При открывании возвращаем коннект
         super.onPause();
         isOnPause = true;
     }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("registration", onRegistration);
        mSocket.off("giveAuthData", onAuth); // колбэк на функцию onAuth
        mSocket.off("checkIsBusy", onCheckIsBusy);
        mSocket.off("opponentIsBusy", onOpponentIsBusy);
        mSocket.off("waitingForAccept", onWaitingAccepting);
        mSocket.off("stopWaiting", onStopWaiting);
        mSocket.off("acceptingGame", onAcceptingGame);
        mSocket.off("error", onError);
        mSocket.off("addFriend", onAddFriend);
        mSocket.off("onlineResponse", onOnlineResponse);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void emitInvite(String name){
        try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("hostname", userName);
                jsonData.put("opponent", name);
                mSocket.emit("checkOpponentIsBusy", jsonData);
            } catch (JSONException e) {
                Log.d("zhopa", "error send inv " + e.getMessage());
            }
    }

     public void inviteToGame(int pos){
         friendListFragment.inviteFriend(pos);
     }

     public void deleteFriend(int pos){
         friendListFragment.deleteFriend(pos);
     }

    public String onGetUserName(){
        return userName;
    }

    public void onCheckFriendName(String name){
        //Запрос серверу о существовании такого имени
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            mSocket.emit("checkFriendName", jsonData);
        } catch (JSONException e) {
            Log.d("me", "error send inv " + e.getMessage());
        }
    }

    private void mSocketInit(){

        try {
//            mSocket = IO.socket("http://185.122.29.164:80");      //HomePC
            mSocket = IO.socket("http://mindgamesonline.ru:80");     //server Domain
//            mSocket = IO.socket("http://83.220.170.8:80");   //server adress

            mSocket.on("registration", onRegistration);
            mSocket.on("giveAuthData", onAuth);
            mSocket.on("checkIsBusy", onCheckIsBusy);
            mSocket.on("opponentIsBusy", onOpponentIsBusy);
            mSocket.on("waitingForAccept", onWaitingAccepting);
            mSocket.on("stopWaiting", onStopWaiting);
            mSocket.on("acceptingGame", onAcceptingGame);
            mSocket.on("error", onError);
            mSocket.on("addFriend", onAddFriend);
            mSocket.on("onlineResponse", onOnlineResponse);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);


            mSocket.connect();
        } catch (URISyntaxException e) {Log.v("zhopa", "error connecting to socket");}


    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isOnPause)  //Если не свернул приложение, то показать ошибку
                    {
                        try{
                            Toast toast =Toast.makeText(getApplicationContext(),
                                    "Ошибка соединения", Toast.LENGTH_SHORT);
                            ViewGroup group = (ViewGroup) toast.getView();
                            TextView messageTextView = (TextView) group.getChildAt(0);
                            messageTextView.setTextSize(20);
                            messageTextView.setTypeface(font);
                            toast.show();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),
                                    "Ошибка соединения", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }
    };


    private void acceptButtonListener(){
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("username", userName);
                    jsonData.put("opponent", opponent);
                    mSocket.emit("acceptingGame", jsonData);
                } catch (JSONException e) {
                    Log.d("me", "error send inv " + e.getMessage());
                }
            }
        });
    }

    private void declineButtonListener(){
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("username", userName);
                    jsonData.put("opponent", opponent);
                    mSocket.emit("stopWaiting", jsonData);
                } catch (JSONException e) {
                    Log.d("me", "error change room" + e.getMessage());
                }
            }
        });
    }

    private void cancelButtonListener(){
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("username", userName);
                    jsonData.put("opponent", opponent);
                    mSocket.emit("stopWaiting", jsonData);
                } catch (JSONException e) {
                Log.d("me", "error change room" + e.getMessage());
                }
            }
        });
    }

     private void setFriendListOnline(){
        friendListFragment.setOnlineFriends(onlineClients);
     }

     private void buttonRegisterListener(){
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //получили имя из строки
                String nameReg = registerNameEditView.getText().toString();
                //отправили на валидацию, если прошла - то шлем на сервер
                if(isValidNickName(nameReg)) {
                    try {
                        JSONObject jsonData = new JSONObject();
                        jsonData.put("username", nameReg);
                        mSocket.emit("registration", jsonData);
                    } catch (JSONException e) {
                        Log.d("me", "error send reg " + e.getMessage());
                    }
                }
                else{
                    try{
                        Toast toast =Toast.makeText(getApplicationContext(),
                                "Никнейм введён не корректно", Toast.LENGTH_SHORT);
                        ViewGroup group = (ViewGroup) toast.getView();
                        TextView messageTextView = (TextView) group.getChildAt(0);
                        messageTextView.setTextSize(20);
                        messageTextView.setTypeface(font);
                        toast.show();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),
                                "Никнейм введён не корректно", Toast.LENGTH_SHORT).show();
                    }
                }
                hideKeyboard();
            }
        });
    }


     private Emitter.Listener onError = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         Toast toast =Toast.makeText(getApplicationContext(),
                                 "Произошла ошибка, переустановите приложение", Toast.LENGTH_SHORT);
                         ViewGroup group = (ViewGroup) toast.getView();
                         TextView messageTextView = (TextView) group.getChildAt(0);
                         messageTextView.setTextSize(20);
                         messageTextView.setTypeface(font);
                         toast.show();
                         finish();
                     } catch (Exception e) {
                         return;
                     }
                 }
             });
         }
     };

     @Override
     public void finish() {
         super.finish();
         overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
     }

     //Если друг занят
     private Emitter.Listener onOpponentIsBusy = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     try {
                         String name = data.getString("opponent");
                         Toast toast =Toast.makeText(getApplicationContext(),
                                 name+" сейчас занят", Toast.LENGTH_SHORT);
                         ViewGroup group = (ViewGroup) toast.getView();
                         TextView messageTextView = (TextView) group.getChildAt(0);
                         messageTextView.setTextSize(20);
                         messageTextView.setTypeface(font);
                         toast.show();
                     } catch (JSONException e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onStopWaiting = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         isBusy = false;
                         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                         hideMatchMakingUI();
                         //разлокировали кнопку поиска
                         mmButton.setEnabled(true);
                         mHandler.removeCallbacks(startBotGame);
                     } catch (Exception e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onWaitingAccepting = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     String hostName, opp;
                     try {
                         isBusy = true;
                         //Закрыли шторку
                         drawerLayout.closeDrawers();
                         //Заблокировали шторку
                         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                         hostName = data.getString("hostname");
                         opp = data.getString("opponent");
                         //Если вы хост, то показать кнопку отклонения
                         if(hostName.equals(userName)){
                             opponent = opp;
                             cancelButton.setVisibility(View.VISIBLE);
                         }
                         //Иначе показать окно приглашения с кнопкой принятия и отклонения
                         else{
                             opponent = hostName;
                             acceptButton.setVisibility(View.VISIBLE);
                             declineButton.setVisibility(View.VISIBLE);
                         }
                         textViewInvitedUser.setText(opponent);
                         //Показали окошко
                         textViewInvitedUser.setVisibility(View.VISIBLE);
                         blackVeil.setVisibility(View.VISIBLE);
                         inviteWindow.setVisibility(View.VISIBLE);
                         //заблокировали кнопку поиска
                         mmButton.setEnabled(false);
                         //запустить таймер ожидания
                         textTimerWaiting.setVisibility(View.VISIBLE);
                         timerCounter = 11;
                         mHandler.post(timerUpdaterRunnable);
                     } catch (JSONException e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onAcceptingGame = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     String host, player;
                     try {
                         mHandler.removeCallbacks(startBotGame);
                         player = data.getString("username");
                         host = data.getString("opponent");
                         launchGame(host, player, false);
                     } catch (Exception e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onCheckIsBusy = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     try {
                         data.put("isBusy", String.valueOf(isBusy));
                         mSocket.emit("busyStatus", data);
                     } catch (JSONException e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onAddFriend = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     boolean answer;
                     String name;
                     try {
                         name = data.getString("name");
                         answer = data.getBoolean("answer");
                         Log.i("zhopa", String.valueOf(answer));
                         //Если игрок существует, добавить друга
                         if(answer) friendListFragment.addFriend(name);
                         //Иначе сообщение что его нет
                         else friendListFragment.fakeName(name);

                     } catch (JSONException e) {
                         return;
                     }
                 }
             });
         }
     };

     private Emitter.Listener onOnlineResponse = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     try {
                         JSONArray jArray = data.getJSONArray("users");
                         onlineClients.clear();
                         if (jArray != null) {
                             for (int i=0;i<jArray.length();i++){
                                 JSONObject jsonObject1 = jArray.getJSONObject(i);
                                 String str = jsonObject1.optString("username");
                                 onlineClients.add(str);
                             }
                             setFriendListOnline();
                         }
                     } catch (JSONException e) {
                     }
                 }
             });
         }
     };


     private Emitter.Listener onAuth = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         if(userName!=null){    //если есть имя пользователя (игрок зарегистрировался)
                             JSONObject jsonData = new JSONObject();
                             jsonData.put("username", userName);
                             mSocket.emit("addUserToOnlineArray", jsonData);        //Обновили массив онлайна
                         }
                     } catch (JSONException e) {
                     }
                 }
             });
         }
     };


    private void setUserName(){
        // если имя уже вводилось - то загрузить его
        if (nSettings.contains("username")){
            this.userName = nSettings.getString("username", "noname");
            userNameView.setText(userName);
            mmButton.setEnabled(false);
            mHandler.postDelayed(openDrawer, 800);
//            friendListFragment.setAddButtonChecked(true);
            try {
                //отправить имя для массива онлайн игроков
                JSONObject jsonData = new JSONObject();
                jsonData.put("username", userName);
                mSocket.emit("addUserToOnlineArray", jsonData);        //Обновили массив онлайна
            } catch (JSONException e) {
            }
        }
        // иначе вызвать окно регистрации
        else {
            showRegisterWindow();
        }
    }

    private void showRegisterWindow(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        blackVeil.setVisibility(View.VISIBLE);
        isBusy = true;
        registerNameInputView.setVisibility(View.VISIBLE);
        registerNameEditView.setVisibility(View.VISIBLE);
        registerWindow.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
//        inviteWindow.setVisibility(View.VISIBLE);
        registerRulesView.setVisibility(View.VISIBLE);
        //заблокировали кнопку поиска
        mmButton.setEnabled(false);
        buttonRegisterListener();
    }

    private void launchGame(String host, String slave, boolean isBot){

        try {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            cancelButton.setVisibility(View.GONE);
            textViewInvitedUser.setVisibility(View.GONE);
            blackVeil.setVisibility(View.GONE);
            inviteWindow.setVisibility(View.GONE);
            textTimerWaiting.setVisibility(View.GONE);
            inviteButton.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);
            isComingAfterGame = true;
            mHandler.removeCallbacks(timerUpdaterRunnable);
            mHandler.removeCallbacks(showTextInviteFriend);
            mHandler.removeCallbacks(addToWaitingList);
            mmButton.setVisibility(View.VISIBLE);
            mmButtonStop.setVisibility(View.GONE);
            mmTextView.setVisibility(View.GONE);
            mmSpinner.setVisibility(View.GONE);
            //разблокировали кнопку поиска
            mmButton.setEnabled(true);

            boolean isHost = false;
            if(host.equals(userName)){
                isHost = true;
                opponent = slave;
            }else{
                opponent = host;
            }

            Intent intent = new Intent(this, OnlineBlitzGame.class);
            intent.putExtra("username", userName);
            intent.putExtra("opponent", opponent);
            intent.putExtra("isHost", isHost);
            intent.putExtra("isBot", isBot);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        } catch (Exception e) {
        }
    }

     private Runnable startBotGame = new Runnable() {
         public void run() {

             try {
                 mSocket.emit("deleteFromWaiters");
                 launchGame(userName, ".Бот", true);
             } catch (Exception e) {
             }
         }
     };

     private Emitter.Listener onRegistration = new Emitter.Listener() {
         @Override
         public void call(final Object... args) {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     JSONObject data = (JSONObject) args[0];
                     Boolean answer;
                     try {
                         answer = Boolean.parseBoolean(data.getString("answer"));
                         String name = data.getString("username");
                         if(answer){    // Если имя доступно
                             drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                             mHandler.postDelayed(openDrawer, 800);
//                             friendListFragment.setAddButtonChecked(true);
                             isBusy = false;
                             userName = name;
                             userNameView.setText(userName);
                             blackVeil.setVisibility(View.GONE);
                             registerNameInputView.setVisibility(View.GONE);
                             registerNameEditView.setVisibility(View.GONE);
                             registerWindow.setVisibility(View.GONE);
                             buttonRegister.setVisibility(View.GONE);
//                             inviteWindow.setVisibility(View.GONE);
                             registerRulesView.setVisibility(View.GONE);
                             //разлокировали кнопку поиска
//                             mmButton.setEnabled(true);
                             try {
                                 //отправить имя для массива онлайн игроков
                                 JSONObject jsonData = new JSONObject();
                                 jsonData.put("username", userName);
                                 mSocket.emit("addUserToOnlineArray", jsonData);        //Обновили массив онлайна
                             } catch (JSONException e) {
                             }
                             //Вошли в комнату
                             //сохранить в savepreferences
                             SharedPreferences.Editor editor = nSettings.edit();
                             editor.putString("username", userName);
                             editor.commit();
                         }
                         else{          // Если имя занято
                             try{
                                 Toast toast =Toast.makeText(getApplicationContext(),
                                         "Никнейм занят", Toast.LENGTH_SHORT);
                                 ViewGroup group = (ViewGroup) toast.getView();
                                 TextView messageTextView = (TextView) group.getChildAt(0);
                                 messageTextView.setTextSize(20);
                                 messageTextView.setTypeface(font);
                                 toast.show();
                             }catch (Exception e){
                                 Toast.makeText(getApplicationContext(),
                                         "Никнейм занят", Toast.LENGTH_SHORT).show();
                             }
                         }

                     } catch (JSONException e) {
                         return;
                     }
                 }
             });
         }
     };

     private void mmButtonListener(){
        mmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBusy = true;
                //Через 2 секунду добавляемся в список ожидающих матча на сервере
                mHandler.postDelayed(addToWaitingList, 2000);
                //Через 7 секунд показываем надпись пригласи друга
                mHandler.postDelayed(showTextInviteFriend,7000);
                //Через 15 сек запускаем игру с ботом
                mHandler.postDelayed(startBotGame, 15000);
                //Обновляем интерфейс
                mmButton.setVisibility(View.GONE);
                mmButtonStop.setVisibility(View.VISIBLE);
                mmSpinner.setVisibility(View.VISIBLE);
                //запрещаем шторку
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            }
        });
     }

     private void MmButtonStopListener(){
        mmButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFinding();
            }
        });
     }

     private Runnable showTextInviteFriend = new Runnable() {
         public void run() {

             try {
                 mmTextView.setVisibility(View.VISIBLE);
             } catch (Exception e) {
             }
         }
     };

     private Runnable addToWaitingList = new Runnable() {
         public void run() {
             //Отправить на сервер имя для записи в список ожидающих
             try {
                 JSONObject jsonData = new JSONObject();
                 jsonData.put("username", userName);
                 mSocket.emit("addToWaiters", jsonData);
             } catch (JSONException e) {
             }
         }
     };

     private Runnable openDrawer = new Runnable() {
         public void run() {
             drawerLayout.openDrawer(findViewById(R.id.left_drawer));
         }
     };

     private void swipeListener(){
         findViewById(R.id.constr_lay_online).setOnTouchListener(new OnSwipeTouchListener(OnlineMenu.this) {
             public void onSwipeTop() {
//                 Toast.makeText(OnlineMenu.this, "top", Toast.LENGTH_SHORT).show();
             }
             public void onSwipeRight() {
                 //Если разрешено изменять шторку
                 if(drawerLayout.getDrawerLockMode(findViewById(R.id.left_drawer)) == DrawerLayout.LOCK_MODE_UNLOCKED)
                     //открываем
                 drawerLayout.openDrawer(findViewById(R.id.left_drawer));
             }
             public void onSwipeLeft() {
//                 Toast.makeText(OnlineMenu.this, "left", Toast.LENGTH_SHORT).show();
             }
             public void onSwipeBottom() {
//                 Toast.makeText(OnlineMenu.this, "bottom", Toast.LENGTH_SHORT).show();
             }

         });

         drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
             @Override
             public void onDrawerSlide(View view, float v) {

             }

             @Override
             public void onDrawerOpened(View view) {
                 mHandler.postDelayed(OnlineRefresh, 200);
             }

             @Override
             public void onDrawerClosed(View view) {
                 // когда закрываем шторку разрешить нажатие кнопки поиск, если не занят
                 if(!isBusy) mmButton.setEnabled(true);
                 mHandler.removeCallbacks(OnlineRefresh);
             }

             @Override
             public void onDrawerStateChanged(int i) {

             }
         });
     }

    private void stopFinding(){
        isBusy = false;
        //Разрешили шторку
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        //Поменяли кнопки
        mmButton.setVisibility(View.VISIBLE);
        mmButtonStop.setVisibility(View.GONE);
        mmSpinner.setVisibility(View.GONE);
        mmTextView.setVisibility(View.GONE);
        //Отправили на сервер
        mSocket.emit("deleteFromWaiters");
        //Убрать колбэки
        mHandler.removeCallbacks(showTextInviteFriend);
        mHandler.removeCallbacks(addToWaitingList);
        mHandler.removeCallbacks(startBotGame);
    }


     public final static boolean isValidNickName(String target) {

        //Длина никнейма 2-10 символов
        //Разрешено использовать латаницу, кириллицу, цифры, символы ".", "_", "-"
        //Символы  ".", "_", "-" не должны быть в начале или конце никнейма

        return Pattern.compile("^[a-zа-яА-ЯA-Z0-9]([._-](?![._-])|[a-zа-яА-ЯA-Z0-9]){0,8}[a-zа-яА-ЯA-Z0-9]$").matcher(target).matches();
     }

     private Runnable timerUpdaterRunnable = new Runnable() {
         public void run() {
             textTimerWaiting.setText(String.valueOf(--timerCounter));
             // повторяем через каждые 1000 миллисекунд
             if(timerCounter>0) mHandler.postDelayed(this, 1000);
             else   {
                 //Если таймер обнулился то отправляем письмо на сервер о прекращении ожидания
                 try {
                     JSONObject jsonData = new JSONObject();
                     jsonData.put("username", userName);
                     jsonData.put("opponent", opponent);
                     mSocket.emit("stopWaiting", jsonData);
                 } catch (JSONException e) {
                     Log.d("zhopa", "error send name " + e.getMessage());
                 }
             }
         }
     };

     private Runnable OnlineRefresh = new Runnable() {
         public void run() {

             //Если шторка открыта, обновляем онлайн друзей каждые 10 секунд
             if(friendListFragment.isVisible()){
                 //запрос серверу на получение списка онлайна
                 try {
                     mSocket.emit("onlineRequest");
                 } catch (Exception e) {
                 }
             }
             mHandler.postDelayed(this, 5000);

         }
     };


    private void hideMatchMakingUI(){
        isBusy = false;
        cancelButton.setVisibility(View.GONE);
        opponent=null;
        textViewInvitedUser.setVisibility(View.GONE);
        blackVeil.setVisibility(View.GONE);
        inviteWindow.setVisibility(View.GONE);
        textTimerWaiting.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);

        mHandler.removeCallbacks(timerUpdaterRunnable);
        try{
            Toast toast =Toast.makeText(getApplicationContext(),
                    "Партия не состоялась", Toast.LENGTH_SHORT);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(20);
            messageTextView.setTypeface(font);
            toast.show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),
                    "Партия не состоялась", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

    //Интерфейс фрагмента
     @Override
     public void onAddFragmentClosedHideKeyboard() {
         hideKeyboard();
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
