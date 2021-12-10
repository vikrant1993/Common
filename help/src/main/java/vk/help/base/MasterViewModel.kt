package vk.help.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class MasterViewModel : ViewModel() {

    val showProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val showProgressDialog: MutableLiveData<Boolean> = MutableLiveData(false)
}