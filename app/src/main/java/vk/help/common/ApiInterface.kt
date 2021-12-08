package vk.help.common

import retrofit2.http.GET
import vk.help.models.JList
import vk.help.networks.ApiClient

interface ApiInterface {


    @GET("CreateOrder.php")
    suspend fun temp(): JList

    object Factory {
        @Volatile
        var instance: ApiInterface? = null
        fun init(): ApiInterface {
            return instance ?: synchronized(this) {
                instance ?: ApiClient.callRetrofit<ApiInterface>("").also { instance = it }
            }
        }
    }
}