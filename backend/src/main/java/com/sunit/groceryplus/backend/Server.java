package com.sunit.groceryplus.backend;

import static spark.Spark.post;
import static spark.Spark.port;
import static spark.Spark.options;
import static spark.Spark.before;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Server {
    private static final Gson gson = new Gson();

    // REPLACE with your actual secret key
    private static final String STRIPE_SECRET_KEY = "sk_test_REPLACE_WITH_YOUR_SECRET_KEY";

    public static void main(String[] args) {
        port(4567);
        Stripe.apiKey = STRIPE_SECRET_KEY;

        // CORS headers
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        post("/create-payment-intent", (request, response) -> {
            response.type("application/json");

            try {
                PaymentIntentRequest req = gson.fromJson(request.body(), PaymentIntentRequest.class);

                PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                        .setAmount((long) req.amount)
                        .setCurrency(req.currency)
                        .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                        )
                        .build();

                PaymentIntent paymentIntent = PaymentIntent.create(params);

                return gson.toJson(new PaymentIntentResponse(paymentIntent.getClientSecret()));
            } catch (Exception e) {
                response.status(400);
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }
        });
        
        System.out.println("Server running on http://localhost:4567");
    }

    static class PaymentIntentRequest {
        @SerializedName("amount")
        long amount;
        @SerializedName("currency")
        String currency;
    }

    static class PaymentIntentResponse {
        @SerializedName("clientSecret")
        String clientSecret;

        public PaymentIntentResponse(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    static class ErrorResponse {
        @SerializedName("error")
        String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
