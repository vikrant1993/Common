package vk.help.common;

import android.content.Context;
import android.view.View;

import vk.help.network.NetworkRequest;
import vk.help.network.ResultsListener;

public class A {

    void a(Context context, View view, ResultsListener listener) {
        new NetworkRequest(context, listener).execute("");
    }

}