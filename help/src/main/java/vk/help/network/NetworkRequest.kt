package vk.help.network

import android.os.AsyncTask
import android.util.Log
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import vk.help.HelpApp
import java.io.IOException
import java.net.ConnectException
import java.util.*

open class NetworkRequest @JvmOverloads constructor(
    private val listener: ResultsListener,
    private val requestJSON: String = "",
    private var requestBody: RequestBody? = null,
    private val requestMap: HashMap<String, String>? = null
) : AsyncTask<String, Void, String>() {

    companion object {
        private const val RequestURL = "Request URL"
        private const val RequestDATA = "Request Data"
        private const val OUTPUT = "ouput"
        private var MEDIA_TYPE = "multipart/form-data"
    }

    private var call: Call? = null

    override fun onPreExecute() {
        super.onPreExecute()

        val requestLogJSON = JSONObject()

        if (requestMap != null) {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            val iterator = requestMap.entries.iterator()

            while (iterator.hasNext()) {
                val temp = iterator.next()
                builder.addFormDataPart(temp.key, temp.value)
                requestLogJSON.put(temp.key, temp.value)
            }
            requestBody = builder.build()
        }
//        if (requestJSON.isNotEmpty()) {
//            Log.i("Request Data", requestJSON)
//        } else if (requestMap != null) {
//            Log.i("Request Data", requestLogJSON.toString())
//        }
    }

    public fun setMediaType(type: String) {
        MEDIA_TYPE = type
    }

    override fun doInBackground(vararg urls: String?): String {
        return try {
            val url = urls[0]!!
            Log.i(RequestURL, url)
            val request: Request
            if (requestBody != null) {
                request = Request.Builder().url(url).post(requestBody!!).build()
            } else {
                if (requestJSON.isEmpty()) {
                    request = Request.Builder().url(url).get().build()
                } else {
                    request = Request.Builder().url(url).post(
                        requestJSON.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())
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

    override fun onPostExecute(output_: String) {
        super.onPostExecute(output_)
        Log.i(OUTPUT, output_)
        val response = try {
            when {
                output_.isEmpty() -> NetworkResponse(false, "No Data Found", "URL Not Found")
                output_.toLowerCase(Locale.getDefault()).contains(
                    "No HTTP resource".toLowerCase(
                        Locale.getDefault()
                    )
                ) -> NetworkResponse(
                    false,
                    "No Data Found",
                    "URL Not Found"
                )
                output_.toLowerCase(Locale.getDefault()).contains(
                    "Failed to connect".toLowerCase(
                        Locale.getDefault()
                    )
                ) -> NetworkResponse(
                    false,
                    "",
                    "Failed to connect"
                )
                output_.toLowerCase(Locale.getDefault()).contains("Server Data Not Found") -> NetworkResponse(
                    false,
                    "",
                    "Data On Server Not Found"
                )
                else -> try {
                    val jsonObject = JSONObject(output_)
                    NetworkResponse(
                        jsonObject.getBoolean("status"),
                        jsonObject.getString("data"),
                        jsonObject.getString("message")
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                    NetworkResponse(false, output_, "Data Conversion Error")
                }
            }
        } catch (ignored: java.lang.Exception) {
            NetworkResponse(false, ignored.toString(), ignored.message!!)
        }
        listener.invoke(response)
    }
}