package com.yt.downloader.spogify.Retrofit;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PrettyLoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String rawJson = responseBody.string();

            try {
                String prettyJson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(JsonParser.parseString(rawJson));

                Log.d("Retrofit", "URL: " + request.url());
                Log.d("Retrofit", "Response:\n" + prettyJson);
            } catch (Exception e) {
                Log.d("Retrofit", "Malformed JSON:\n" + rawJson);
            }

            MediaType contentType = responseBody.contentType();
            ResponseBody newResponseBody = ResponseBody.create(contentType, rawJson);
            return response.newBuilder().body(newResponseBody).build();
        }

        return response;
    }
}