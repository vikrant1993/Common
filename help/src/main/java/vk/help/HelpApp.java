package vk.help;

import android.graphics.Typeface;

import androidx.multidex.MultiDexApplication;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HelpApp extends MultiDexApplication {

    public enum Level {
        NONE, BASIC, HEADERS, BODY
    }

    public static OkHttpClient client;
    private static final int cacheSize = 10 * 1024 * 1024;
    private HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(getCacheDir(), cacheSize))
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);


        builder.addInterceptor(interceptor);
        client = builder.build();
    }

    public void setLogLevel(Level level) {
        if (level == Level.BASIC) {
            interceptor.level(HttpLoggingInterceptor.Level.BASIC);
        } else if (level == Level.HEADERS) {
            interceptor.level(HttpLoggingInterceptor.Level.HEADERS);
        } else if (level == Level.BODY) {
            interceptor.level(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.level(HttpLoggingInterceptor.Level.NONE);
        }
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