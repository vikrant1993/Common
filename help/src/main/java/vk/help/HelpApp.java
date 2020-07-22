package vk.help;

import android.content.Context;
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

    public enum ToastMessageStyle {
        ALL_CAPITAL, FIRST_WORD_CAPITAL, WORDS_CAPITAL, NONE
    }

    public static OkHttpClient client;
    private static final int cacheSize = 10 * 1024 * 1024;
    private HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    private int timeout = 30;
    public static ToastMessageStyle toastMessageStyle;
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Common.sharedPreferences = getSharedPreferences(getString(R.string.shared_vk_offline), Context.MODE_PRIVATE);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(getCacheDir(), cacheSize))
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS);

        builder.addInterceptor(interceptor);
        client = builder.build();
        setToastMessageStyle(ToastMessageStyle.WORDS_CAPITAL);
    }

    public void setToastMessageStyle(ToastMessageStyle style) {
        toastMessageStyle = style;
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

    public void setTimeout(int seconds) {
        timeout = seconds;
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}