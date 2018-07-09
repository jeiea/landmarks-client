package kr.ac.kw.coms.globealbum.provider;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LandmarkClientJava {

    private OkHttpClient client = new OkHttpClient();

    private static final String authUrl = "https://coms-globe.herokuapp.com/auth/page/email";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Gson gson = new Gson();

    public String register(String url, String email, String password) throws IOException, NullPointerException {
        JsonObject obj = new JsonObject();
        obj.addProperty("email", email);

        RequestBody body = RequestBody.create(JSON, gson.toJson(obj));
        Request request = new Request.Builder()
                .url(authUrl + "/register")
                .addHeader("User-Agent", "landmarks-client")
                .addHeader("Accept", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    public void login (String url, String id, String password){
        RequestBody requestBody = new FormBody.Builder().add("userId", id).add("userPassword", password).build();

        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("error", "Connect Server Error is " + e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("aaaa", "Response Body is " + response.body().string());
            }
        });
    }
}