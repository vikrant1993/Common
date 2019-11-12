package vk.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

open class MasterActivity : AppCompatActivity() {

    companion object {
        const val DATA = "DATA"
        const val KEY = "KEY"
        const val ID = "ID"
        const val PHONE_NUMBER = "PHONE_NUMBER"
    }

    public lateinit var context: Context
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    public fun showToast(message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text =
            Common.capitalize(message)
        toast!!.show()
    }

    public fun showErrorToast(message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast(context)
        toast!!.setGravity(Gravity.BOTTOM, 0, 100)
        toast!!.duration = Toast.LENGTH_LONG
        toast!!.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message).text =
            Common.capitalize(message)
        toast!!.view.findViewById<AppCompatTextView>(R.id.toast_message)
            .setTextColor(ContextCompat.getColor(context, R.color.white))
        toast!!.view.findViewById<CardView>(R.id.cardView)
            .setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_red))
        toast!!.show()
    }
}