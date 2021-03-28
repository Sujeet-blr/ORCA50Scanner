package in.mobiux.android.orca50scanner.api;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import in.mobiux.android.orca50scanner.util.SessionManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class ApiClient {
    static String TAG = ApiClient.class.getCanonicalName();

    //    private static final String BASE_URL = AppPrefs.Instance.getAppConfig().getBaseUrl();
    private static final String BASE_URL = "https://footprints-staging.hito.solutions/api/v1/";

    private static Retrofit retrofit;
    private static ApiService apiService;


    private static Retrofit getClient() {
        Log.i(TAG, "getClient Called " + BASE_URL);

        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(interceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
//        retrofit.create(ApiService.class);
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    static class AuthInterceptor implements Interceptor {

        Context context;
        private SessionManager sessionManager;

        public AuthInterceptor(Context context) {
            this.context = context;
            sessionManager = SessionManager.getInstance(context);
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            Request authRequest = request.newBuilder()
                    .addHeader("Authorization", "Token " + sessionManager.token()).build();
            return chain.proceed(authRequest);
        }
    }
}
