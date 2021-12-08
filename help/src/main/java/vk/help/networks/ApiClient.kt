package vk.help.networks

import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun getHttpClient(): OkHttpClient {

        val student: NetworkConnectionInterceptor by inject(NetworkConnectionInterceptor::class.java)

        return OkHttpClient.Builder().connectTimeout(200, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS).addInterceptor(
                student
            ).addInterceptor(interceptor).addInterceptor(Interceptor { chain ->
                val newRequest: Request = chain.request().newBuilder().build()
                chain.proceed(newRequest)
            }).build()
    }

    inline fun <reified T> callRetrofit(base_url: String): T {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder().baseUrl(base_url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(getHttpClient()).build()
        return retrofit.create(T::class.java)
    }

    class NetworkConnectionInterceptor(private val mContext: Context) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!isConnected) {
                throw NoConnectivityException()
                // Throwing our custom exception 'NoConnectivityException'
            }
            val builder = chain.request().newBuilder()
            return chain.proceed(builder.build())
        }

        @Suppress("DEPRECATION")
        private val isConnected: Boolean
            get() {
                val connectivityManager =
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = connectivityManager.activeNetworkInfo
                return netInfo != null && netInfo.isConnected
            }

        class NoConnectivityException : IOException() {
            override val message: String
                get() = "no internet connection"
        }
    }


}