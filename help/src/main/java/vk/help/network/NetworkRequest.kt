package vk.help.network

import android.os.AsyncTask
import android.util.Log
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import vk.help.HelpApp
import java.io.IOException
import java.net.ConnectException
import java.util.*

//@SuppressLint("StaticFieldLeak")

class NetworkRequest @JvmOverloads constructor(
    private val listener: ResultsListener,
    private val requestJSON: String = "",
    private val requestBody: RequestBody? = null
) : AsyncTask<String, Void, String>() {

    companion object {
        private const val RequestURL = "Request URL"
        private const val RequestDATA = "Request Data"
        private const val OUTPUT = "Output"
    }

    private var call: Call? = null

//    public fun post(requestJSON: String): NetworkRequest {
//        this.requestJSON = requestJSON
//        return this
//    }
//
//    fun post(requestBody: RequestBody): NetworkRequest {
//        this.requestBody = requestBody
//        return this
//    }

//    public fun setListener(listener: ResultsListener): NetworkRequest {
//        this.listener = listener
//        return this
//    }

//    fun setProgressMessage(progressMessage: String): NetworkRequest {
//        this.progressMessage = progressMessage
//        return this
//    }
//
//    override fun onPreExecute() {
//        super.onPreExecute()
//        if (context != null) {
////            HelpingClass.showProgress(context, false, if (progressMessage.isEmpty()) context!!.get()!!.getString(R.string.please_wait_) else progressMessage)
//        }
//    }

    override fun doInBackground(vararg urls: String?): String {
        return try {
            val url = urls[0]
            Log.i(RequestURL, url!!)
            val request: Request
            if (requestBody != null) {
                request = Request.Builder().url(url).post(requestBody).build()
            } else {
                if (requestJSON.isEmpty()) {
                    request = Request.Builder().url(url).get().build()
                } else {
                    request = Request.Builder().url(url).post(
                        requestJSON.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                    ).build()
                    Log.i(RequestDATA, requestJSON)
                }
            }
            call = HelpApp.client.newCall(request)
            String(call!!.execute().body!!.bytes())
        } catch (e: ConnectException) {
            e.printStackTrace()
            "Failed to connect"
        } catch (e: IOException) {
            e.printStackTrace()
            "Server Data Not Found"
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            "Url not found"
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
//        if (context != null) {
////            HelpingClass.hideProgress()
//        }
        val response: NetworkResponse
        response = when {
            output.isEmpty() -> NetworkResponse(false, "No Data Found", "URL Not Found")
            output.toLowerCase(Locale.ROOT).contains("No HTTP resource".toLowerCase(Locale.ROOT)) -> NetworkResponse(
                false,
                "No Data Found",
                "URL Not Found"
            )
            output.toLowerCase(Locale.ROOT).contains("Failed to connect".toLowerCase(Locale.ROOT)) -> NetworkResponse(
                false,
                "",
                "Failed to connect"
            )
            output.toLowerCase(Locale.ROOT).contains("Server Data Not Found") -> NetworkResponse(
                false,
                "",
                "Data On Server Not Found"
            )
            else -> try {
                val jsonObject = JSONObject(output)
                NetworkResponse(
                    jsonObject.getBoolean("status"),
                    jsonObject.getString("data"),
                    jsonObject.getString("message")
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                NetworkResponse(false, output, "Data Conversion Error")
            }
        }
        listener.invoke(response)
    }
}