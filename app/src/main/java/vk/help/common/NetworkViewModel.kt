package vk.help.common

import androidx.lifecycle.ViewModel
import org.koin.java.KoinJavaComponent.inject

class NetworkViewModel : ViewModel() {

    private val apiInterface: ApiInterface by inject(ApiInterface::class.java)

    suspend fun temp(){
        apiInterface.temp()
    }

}