package space.yakimov.firstapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class setLevel extends Activity{

//    boolean isBuying = false;
    int shirina;
    int visota;
    int a;
    int zvezda;
    int ID;
    int expRank;
    int maxExp, oldExp;
    TextView stars, Rank;
    ImageView imageRank, expFront;
    public SharedPreferences nSettings;
    JSONObject ranks;
    Button buttonPlay7x7,buttonPlay5x5,buttonPlay3x3;
    TextView buttonBuyStars;
    private BillingClient billingClient;
//    private String productID = "android.test.purchased";
    private String productID = "buyinghundredstarsmindgames";
    private String licenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq/aOw2NgEFO777BlQQueB+ePdSiMPP27AjQ+2ij8lVG/miOWoBWJHaIPi/XmxIv59M+vZvzctJ2hBloRKGboGNBdfyTEiDXFtZ/GrlUDWA1fC8zselyVoHCNYudLXycIoj1MqVHiVTjHDTA8G8gE5TzzLA39exENySZnQsPR3bZd/ixrOkLcwdJkRfPizP/+jyd1y2WsRDVM/ZlDSjj2Meii+ZsscEycaGSQF89nNBGS87gs1E+7UHOsPXl4fZZ3iWA2r+bopClSh/C4nvTeVVcCbiiE9n2qvdZjNnD4Lyafz4aW3t55DBEOjsEyrSi7EJScwi7dB151LOxFTFl/GQIDAQAB";
    Typeface font;
    private Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_level);

        Display display = getWindowManager().getDefaultDisplay();   //блок получения размера дисплея
        Point size = new Point();
        display.getSize(size);
        shirina=size.x;
        visota=size.y;
        jsonParsing();

//        a = (visota-shirina*7/8-(visota-shirina*7/8)/3)/13;
        font = Typeface.createFromAsset(getAssets(), "fonts/comic.ttf");
        nSettings = getSharedPreferences("ssettings", Context.MODE_PRIVATE);

        stars = (TextView) findViewById(R.id.numStars);
        stars.setTypeface(font,Typeface.BOLD);
        stars.setTextColor(getResources().getColor(R.color.midnightBlue));
        stars.setTextSize(TypedValue.COMPLEX_UNIT_PX, shirina/15);

        Rank = (TextView) findViewById(R.id.textRank);
        Rank.setTypeface(font,Typeface.BOLD);
        Rank.setTextColor(getResources().getColor(R.color.silver));
        Rank.setTextSize(TypedValue.COMPLEX_UNIT_PX, shirina/20);

        imageRank = (ImageView) findViewById(R.id.imageRank);

        expFront = (ImageView) findViewById(R.id.expFront);

        buttonPlay3x3 = (Button) findViewById(R.id.button3);
        buttonPlay5x5 = (Button) findViewById(R.id.button2);
        buttonPlay7x7 = (Button) findViewById(R.id.button);
        buttonBuyStars = (TextView) findViewById(R.id.buy_stars_button);
        buttonBuyStars.setTypeface(font,Typeface.BOLD_ITALIC);

        buttonPlay5x5.setEnabled(false);
        buttonPlay5x5.setAlpha(0.5F);
        buttonPlay7x7.setEnabled(false);
        buttonPlay7x7.setAlpha(0.5F);

        buttonPlay3x3Listener();
        buttonPlay5x5Listener();
        buttonPlay7x7Listener();
        buttonBuyStarsListener();

        try {
            initialize();
        }catch (Exception e){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

        hideSystemUI();

        if (nSettings.contains("counter")) {
            zvezda = nSettings.getInt("counter", 0);
        }
        if (nSettings.contains("exp")){
            expRank = nSettings.getInt("exp", 0);
        }
        if (nSettings.contains("ID")){
            ID = nSettings.getInt("ID", 0);
        }
        if(ID>1) {
            buttonPlay5x5.setEnabled(true);
            buttonPlay5x5.setAlpha(1F);
            buttonPlay7x7.setEnabled(true);
            buttonPlay7x7.setAlpha(1F);
        }
        if(ID>0){
            buttonPlay5x5.setEnabled(true);
            buttonPlay5x5.setAlpha(1F);
        }
        buttonPlay3x3.setEnabled(true);
        //Установить значение звезд если не вернулся с покупки
//        if(!isBuying)
        stars.setText(String.valueOf(zvezda));
//        else{
//            isBuying = false;
        try {
            buttonBuyStars.setEnabled(true);
        }catch(Exception e){}
//        }
//        try {
//
//        }catch (Exception e){}
        setRank();
    }

    private void buttonPlay3x3Listener(){
        buttonPlay3x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n=0, numer=0;
                n = 3;
                // Если есть данные о номере уровня, то загружаем, иначе первый уровень
                if (nSettings.contains("easyLevels"))
                    numer = nSettings.getInt("easyLevels", 0);
                startEasyLvl(n,numer);
            }
        });
    }

    private void buttonPlay5x5Listener(){
        buttonPlay5x5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n=0, numer=0;
                n = 5;
                // Если есть данные о номере уровня, то загружаем, иначе первый уровень
                if (nSettings.contains("normLevels"))
                    numer = nSettings.getInt("normLevels", 0);
                startEasyLvl(n,numer);
            }
        });
    }

    private void buttonPlay7x7Listener(){
        buttonPlay7x7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n=0, numer=0;
                n=7;
                if (nSettings.contains("hardLevels"))
                    numer = nSettings.getInt("hardLevels", 0);
                startEasyLvl(n,numer);
            }
        });
    }

    private void startEasyLvl(int n, int numer){
        buttonPlay3x3.setEnabled(false);
        buttonPlay5x5.setEnabled(false);
        buttonPlay7x7.setEnabled(false);


        Intent intent = new Intent(this, EasyLevel.class);
        intent.putExtra("mapSize", n);
        intent.putExtra("lvlNumber", numer);
        intent.putExtra("ID", ID);
        intent.putExtra("zvezda", zvezda);
        intent.putExtra("expRank", expRank);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

//    public ArrayList<String> getArrayPrefs(String arrayName) {
//        int size = nSettings.getInt(arrayName + "_size", 0);
//        ArrayList<String> array = new ArrayList<>(size);
//        for(int i=0;i<size;i++)
//            array.add(nSettings.getString(arrayName + "_" + i, null));
//        return array;
//    }

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

    private void startCountAnimation(final TextView v, int first, int second) {
        ValueAnimator animator = ValueAnimator.ofInt(first, second);
        animator.setDuration(4000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                v.setText(animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }

    private void buttonBuyStarsListener(){
        buttonBuyStars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBuyStars.setEnabled(false);
//                isBuying = true;
                try {
                    launchBilling();
                }catch (Exception e){}
            }
        });
    }

    public void initialize() {

        billingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (Purchase purchase : list) {
                        handlePurchase(purchase);
                    }
//                    Toast.makeText(getApplicationContext(),
//                            "куплено", Toast.LENGTH_SHORT).show();
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
//                    Toast.makeText(getApplicationContext(),
//                            "отмена", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle any other error codes.
//                    Toast.makeText(getApplicationContext(),
//                            "ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        }).enablePendingPurchases().build();

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                //здесь мы можем запросить информацию о товарах и покупках
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    buttonBuyStars.setVisibility(View.VISIBLE);
                    querySkuDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
            }
        });
    }

    private void handlePurchase(Purchase  purchase){

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    //сюда мы попадем когда будет осуществлена покупка
                    //Добавили звёзд и запустили анимацию

                    setLevel.this.runOnUiThread(new Runnable() {
                        public void run() {
                            int oldStars = zvezda;
                            zvezda += 100;
                            int newStars = zvezda;
                            //Сохранили звёзды
                            SharedPreferences.Editor editor = nSettings.edit();
                            editor.remove("counter");
                            editor.putInt("counter", zvezda);
                            editor.commit();
//                            stars.setText(String.valueOf(zvezda));
                            startCountAnimation(stars, oldStars, newStars);
                        }
                    });
//                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

//    @Override
//    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
//        // Handle consumption.
//
//    }

    private void querySkuDetails() {
        SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
        List<String> skuList = new ArrayList<>();
        skuList.add(productID);
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                for (SkuDetails skuDetails : list) {
                    mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                }
            }
        });
    }

    public void launchBilling() {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mSkuDetailsMap.get(productID))
                .build();
        billingClient.launchBillingFlow(this, billingFlowParams);
    }


    public void changeExpLine( View view, float percent, boolean flagFirstTime){

        if(flagFirstTime) view.setScaleX(0.001F);
        if(percent==0) percent = 0.1F;

        ObjectAnimator animat = ObjectAnimator.ofFloat(view, "scaleX", percent/100);
        animat.setDuration(500);
        animat.start();
    }

    public void setRank(){
        // Цикл по рангам

        maxExp = Integer.parseInt(getRankData(ID,"exp"));
        if(ID>0) oldExp = Integer.parseInt(getRankData(ID-1,"exp"));
        else oldExp = 0;

        Rank.setText(getRankData(ID,"name"));
        int rankRes = getResources().getIdentifier(getRankData(ID, "ImageName"), "drawable", getPackageName());
        Drawable drawable = getResources().getDrawable(rankRes);
        imageRank.setBackground(drawable);
        changeExpLine(expFront, (float)100 * (expRank - oldExp) / (maxExp - oldExp) , true);

    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
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


