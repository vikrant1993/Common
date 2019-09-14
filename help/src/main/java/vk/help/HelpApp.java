package vk.help;

import android.app.Application;
import android.graphics.Typeface;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class HelpApp extends Application {

    public static OkHttpClient client;
    private static final int cacheSize = 10 * 1024 * 1024;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new OkHttpClient.Builder()
                .cache(new Cache(getCacheDir(), cacheSize))
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();
    }

    public void setFont(Typeface typeface) {
        try {
            Field staticField = Typeface.class.getDeclaredField("MONOSPACE");
            staticField.setAccessible(true);
            staticField.set(null, typeface);// Typeface.createFromAsset(getAssets(), "OpenSansRegular.ttf"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}