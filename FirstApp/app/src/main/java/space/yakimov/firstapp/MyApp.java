package space.yakimov.firstapp;

import android.app.Application;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class MyApp extends Application {

    @Override
    public void onCreate( ) {
        super.onCreate();
        metricaInit();
    }

    private void metricaInit(){
        // Creating an extended library configuration.
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder("3f8f84ff-c0f2-4e56-b388-afb33821c160").build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this);
    }
}
