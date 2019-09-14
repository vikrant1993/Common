package vk.help.network

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import vk.help.HelpApp
import vk.help.R
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.ConnectException
import java.util.*

class NetworkRequest() : AsyncTask<String, Void, String>() {

    companion object {
        private const val RequestURL = "RequestURL"
        private const val RequestDATA = "RequestDATA"
        private const val OUTPUT = "OUTPUT"
    }

    private var context: WeakReference<Context>? = null
    private var listener: ResultsListener? = null
    private var requestJSON: String = ""
    private var progressMessage = ""
    private var call: Call? = null

    constructor(listener: ResultsListener) : this(null, listener)
    constructor(context: Context?, listener: ResultsListener) : this() {
        this.context = WeakReference(context!!)
        this.listener = listener
        requestJSON = ""
    }

    public fun post(requestJSON: String): NetworkRequest {
        this.requestJSON = requestJSON
        return this
    }

    public fun setListener(listener: ResultsListener): NetworkRequest {
        this.listener = listener
        return this
    }

    fun setProgressMessage(progressMessage: String): NetworkRequest {
        this.progressMessage = progressMessage
        return this
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (context != null) {
//            HelpingClass.showProgress(context, false, if (progressMessage.isEmpty()) context!!.get()!!.getString(R.string.please_wait_) else progressMessage)
        }
    }

    override fun doInBackground(vararg urls: String?): String {
        return try {
            val url = urls[0]
            Log.i(RequestURL, url!!)
            if (requestJSON.isNotEmpty()) {
                Log.d(RequestDATA, requestJSON)
            }
            var builder: Request.Builder = Request.Builder().url(url)
            builder = if (requestJSON.isEmpty()) builder.get() else builder.post(requestJSON.toRequestBody("application/json".toMediaTypeOrNull()))
            call = HelpApp.client.newCall(builder.build())
            String(call!!.execute().body!!.bytes())
        } catch (e: ConnectException) {
            e.printStackTrace()
            "Failed to connect"
        } catch (e: IOException) {
            e.printStackTrace()
            "Server Data Not Found"
        }
    }

    override fun onCancelled() {
        super.onCancelled()
        if (call != null) {
            call!!.cancel()
            call = null
        }
    }

    override fun onPostExecute(output: String) {
        super.onPostExecute(output)
        Log.i(OUTPUT, output)
        if (context != null) {
//            HelpingClass.hideProgress()
        }
        val response: NetworkResponse
        response = when {
            output.isEmpty() -> NetworkResponse(false, "No Data Found", "URL Not Found")
            output.toLowerCase(Locale.ROOT).contains("No HTTP resource".toLowerCase(Locale.ROOT)) -> NetworkResponse(false, "No Data Found", "URL Not Found")
            output.toLowerCase(Locale.ROOT).contains("Failed to connect".toLowerCase(Locale.ROOT)) -> NetworkResponse(false, "", "Failed to connect")
            output.toLowerCase(Locale.ROOT).contains("Server Data Not Found") -> NetworkResponse(false, "", "Data On Server Not Found")
            else -> try {
                val jsonObject = JSONObject(output)
                NetworkResponse(jsonObject.getBoolean("status"), jsonObject.getString("data"), jsonObject.getString("message"))
            } catch (e: JSONException) {
                e.printStackTrace()
                NetworkResponse(false, output, "Data Conversion Error")
            }
        }
        listener!!.onResultsSucceeded(response)
    }
}