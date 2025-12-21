package com.sunit.groceryplus.network;

import com.sunit.groceryplus.utils.Config;
import com.sunit.groceryplus.utils.PHPBackendConfig;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static Retrofit phpRetrofit = null;

    // Original Java backend
    public static StripeApi getStripeApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.BACKEND_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(createOkHttpClient())
                    .build();
        }
        return retrofit.create(StripeApi.class);
    }
    
    // Create OkHttpClient with proper timeout settings
    private static okhttp3.OkHttpClient createOkHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // 30 seconds to connect
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)     // 60 seconds to read response
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // 60 seconds to send request
                .retryOnConnectionFailure(true)                            // Retry on connection failure
                .build();
    }
    
    // PHP Backend
    public static PHPStripeApi getPHPStripeApi() {
        if (phpRetrofit == null) {
            phpRetrofit = new Retrofit.Builder()
                    .baseUrl(PHPBackendConfig.BACKEND_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(createOkHttpClient())
                    .build();
        }
        return phpRetrofit.create(PHPStripeApi.class);
    }
    
    // Test backend connection
    public static void testConnection(PHPBackendConnectionCallback callback) {
        getPHPStripeApi().testConnection().enqueue(new retrofit2.Callback<PHPStripeApi.TestResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PHPStripeApi.TestResponse> call, retrofit2.Response<PHPStripeApi.TestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onConnectionSuccess(response.body());
                } else {
                    callback.onConnectionError("Backend response error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<PHPStripeApi.TestResponse> call, Throwable t) {
                callback.onConnectionError("Connection failed: " + t.getMessage());
            }
        });
    }
    
    public interface PHPBackendConnectionCallback {
        void onConnectionSuccess(PHPStripeApi.TestResponse response);
        void onConnectionError(String error);
    }
}
