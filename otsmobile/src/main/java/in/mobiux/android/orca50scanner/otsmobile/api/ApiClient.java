package in.mobiux.android.orca50scanner.otsmobile.api;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import in.mobiux.android.orca50scanner.otsmobile.utils.SessionManager;
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
//    private static final String BASE_URL = "https://footprints-staging.hito.solutions/api/v1/";

    private static Retrofit retrofit;
    private static ApiService apiService;
    private static ApiAuthService authService;
    private static Retrofit retrofitAuthClient;


    private static Retrofit getAuthClient() {

        if (retrofitAuthClient == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(interceptor).build();

            retrofitAuthClient = new Retrofit.Builder()
                    .baseUrl(Endpoints.getAuthBaseUrl())
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitAuthClient;
    }

    private static Retrofit getClient() {

        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(interceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Endpoints.getBaseUrl())
//                    .client(client)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(Endpoints.BASE_URL)
//                    .client(getUnsafeOkHttpClient().build())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
        }
//        retrofit.create(ApiService.class);
        return retrofit;
    }

    public static ApiAuthService getAuthService() {
        if (authService == null) {
            authService = getAuthClient().create(ApiAuthService.class);
        }
        return authService;
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


    //    added to skip the ssl check
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(interceptor);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
