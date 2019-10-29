package vk.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class MasterActivity : AppCompatActivity() {

    companion object {
        const val DATA = "DATA"
        const val KEY = "KEY"
        const val ID = "ID"
        const val PHONE_NUMBER = "PHONE_NUMBER"
    }

    public lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    public fun showToastSimple(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    public fun showToast(message: String) {
        Toast.makeText(context, Common.capitalize(message), Toast.LENGTH_LONG).show()
    }

    public fun showErrorToast(message: String) {
        Toast.makeText(context, Common.capitalize(message), Toast.LENGTH_LONG).show()
    }

}