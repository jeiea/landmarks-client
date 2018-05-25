package kr.ac.kw.coms.globealbum.provider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LandmarkClient {
    private OkHttpClient client = new OkHttpClient();
    private static final String authUrl = "https://coms-globe.herokuapp.com/auth/page/email";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Gson gson = new Gson();

    public String register(String email) throws IOException, NullPointerException {
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
}
