package vk.help.common

import org.koin.core.module.Module
import org.koin.dsl.module
import vk.help.base.MasterApplication

class BaseApplication : MasterApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun getAppModule(): Module {
        return module {
            super.getAppModule()
            factory { ApiInterface.Factory.init() }
//            viewModel { NetworkViewModel() }
//            viewModel { SignupViewModel() }
//            viewModel { LoginViewModel() }
//            viewModel { ForgotViewModel() }
//            viewModel { HomeViewModel() }
//            viewModel { AddressViewModel() }
//            viewModel { CheckoutViewModel() }
//            viewModel { ProductsViewModel() }
        }
    }
}