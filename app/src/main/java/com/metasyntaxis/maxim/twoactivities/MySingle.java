package com.metasyntaxis.maxim.twoactivities;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by maxim on 27.03.18.
 *
 */

public class MySingle {
    private static MySingle mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private MySingle(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized MySingle getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingle(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
