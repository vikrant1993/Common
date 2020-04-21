package vk.help.network

import android.content.res.Resources
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
import vk.help.Common
import vk.help.HelpApp
import vk.help.R
import java.io.IOException
import java.net.ConnectException
import java.util.*
import kotlin.collections.HashMap

open class NetworkRequest @JvmOverloads constructor(
    private val listener: ResultsListener,
    private val requestJSON: String = "",
    private var requestBody: RequestBody? = null,
    private val requestMap: HashMap<String, String>? = null
) : AsyncTask<String, Void, String>() {

    companion object {
        private const val RequestURL = "Request URL"
        private const val RequestDATA = "Request Data"
        private const val OUTPUT = "output"
        private var MEDIA_TYPE = "multipart/form-data"
    }

    private var call: Call? = null
    private var headerMap = HashMap<String, String>()

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

    public fun setHeader(_headerMap: HashMap<String, String>): NetworkRequest {
        headerMap = _headerMap
        return this
    }

    public fun setMediaType(type: String) {
        MEDIA_TYPE = type
    }

    override fun doInBackground(vararg urls: String?): String {
        return try {
            val url = urls[0]!!
            Log.i(RequestURL, url)
            val request: Request
            val builder = Request.Builder().url(url)

            if (headerMap.isNotEmpty()) {
                for (singleHeader in headerMap) {
                    builder.addHeader(singleHeader.key, singleHeader.value)
                    Common.longLog("SOMETAG", singleHeader.key + "-" + singleHeader.value)
                }
            }

            if (requestBody != null) {
                request = builder.post(requestBody!!).build()
            } else {
                if (requestJSON.isEmpty()) {
                    request = builder.get().build()
                } else {
                    request = builder.post(
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
        Common.longLog(OUTPUT, output_)
        val response = try {
            when {
                output_.isEmpty() -> NetworkResponse(
                    false,
                    Resources.getSystem().getString(R.string.no_data_found),
                    Resources.getSystem().getString(R.string.url_not_found)
                )
                output_.toLowerCase(Locale.getDefault())
                    .contains("No HTTP resource".toLowerCase(Locale.getDefault())) -> NetworkResponse(
                    false,
                    Resources.getSystem().getString(R.string.no_data_found),
                    Resources.getSystem().getString(R.string.url_not_found)
                )
                output_.toLowerCase(Locale.getDefault()).contains(
                    "Failed to connect".toLowerCase(
                        Locale.getDefault()
                    )
                ) -> NetworkResponse(
                    false,
                    "",
                    Resources.getSystem().getString(R.string.failed_to_connect)
                )
                output_.toLowerCase(Locale.getDefault())
                    .contains("Server Data Not Found") -> NetworkResponse(
                    false,
                    "",
                    Resources.getSystem().getString(R.string.data_on_server_not_found)
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
                    NetworkResponse(
                        false,
                        output_,
                        Resources.getSystem().getString(R.string.data_conversion_error)
                    )
                }
            }
        } catch (ignored: Exception) {
            NetworkResponse(false, output_, ignored.message!!)
        }
        listener.invoke(response)
    }
}