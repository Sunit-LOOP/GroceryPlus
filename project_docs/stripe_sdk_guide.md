# Stripe SDK Production Integration Guide

## ✅ Current Project Status
**Stripe Android SDK:** v20.37.0 ✅ **INSTALLED**  
**Build Status:** ✅ **SUCCESSFUL**  
**Your API Keys:** ✅ **READY**

---

## 🎯 Goal: Production-Ready Payments
To move from the current test implementation to a real, secure payment system using Stripe's official UI, you need to implement a backend server. This guide provides **every single file** you need.

---

## 🏗️ PART 1: The Backend Server
*You must host this securely. Do not skip this.*

### Option A: Node.js (Recommended)

**1. Initialize Project**
```bash
mkdir stripe-server
cd stripe-server
npm init -y
npm install express stripe cors dotenv
```

**2. Create `server.js`**
```javascript
require('dotenv').config();
const express = require('express');
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// Endpoint to create Payment Intent
app.post('/create-payment-intent', async (req, res) => {
  try {
    const { amount, currency } = req.body;
    
    // Create a PaymentIntent with the order amount and currency
    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount, // Amount in cents (e.g. 1000 = $10.00)
      currency: currency || 'npr',
      automatic_payment_methods: {
        enabled: true,
      },
    });

    res.json({
      clientSecret: paymentIntent.client_secret,
    });
  } catch (e) {
    res.status(400).json({ error: { message: e.message } });
  }
});

app.listen(3000, () => console.log('Node server running on port 3000...'));
```

**3. Create `.env` file**
```env
STRIPE_SECRET_KEY=sk_test_REPLACE_WITH_YOUR_SECRET_KEY
```

**4. Deploy**
Deploy this folder to **Heroku**, **Render**, or **DigitalOcean**. You will get a URL like `https://your-app.onrender.com`.

---

## 📱 PART 2: Android Implementation

### 1. Retrofit Dependencies
Start by adding Retrofit to `app/build.gradle.kts` if not already present:
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

### 2. API Interface (`StripeApi.java`)
```java
package com.sunit.groceryplus.network;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StripeApi {
    @POST("create-payment-intent")
    Call<PaymentIntentResponse> createPaymentIntent(@Body PaymentIntentRequest request);
}

class PaymentIntentRequest {
    int amount;
    String currency;

    public PaymentIntentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
}

class PaymentIntentResponse {
    @SerializedName("clientSecret")
    String clientSecret;
}
```

### 3. Payment Activity Implementation (`PaymentActivity.java`)
Replace your current simulation logic with this:

```java
// ... imports ...
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

public class PaymentActivity extends AppCompatActivity {
    
    private PaymentSheet paymentSheet;
    private String clientSecret;
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_REPLACE_WITH_YOUR_PUBLISHABLE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... setup code ...

        // 1. Initialize Stripe Configuration
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLISHABLE_KEY);

        // 2. Initialize PaymentSheet
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        
        // 3. Prepare the payment intent immediately (fetch from backend)
        fetchPaymentIntent(); 
    }

    private void fetchPaymentIntent() {
        // Assume you calculate total in cents (e.g., 500 = 5.00)
        int amountInCents = (int) (totalAmount * 100); 

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://your-deployed-backend.com/") // REPLACE THIS WITH YOUR SERVER URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StripeApi api = retrofit.create(StripeApi.class);
        Call<PaymentIntentResponse> call = api.createPaymentIntent(new PaymentIntentRequest(amountInCents, "npr"));

        call.enqueue(new Callback<PaymentIntentResponse>() {
            @Override
            public void onResponse(Call<PaymentIntentResponse> call, Response<PaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clientSecret = response.body().clientSecret;
                    Log.d("Stripe", "Payment Intent fetched");
                }
            }

            @Override
            public void onFailure(Call<PaymentIntentResponse> call, Throwable t) {
                Log.e("Stripe", "Error: " + t.getMessage());
            }
        });
    }

    private void processStripePayment() { // Called when user clicks "Pay"
        if (clientSecret != null) {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("GroceryPlus")
                    .merchantDisplayName("GroceryPlus Inc.")
                    .build();
            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
        } else {
            Toast.makeText(this, "Please wait, initializing payment...", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // ✅ SUCCESS
            createOrder("stripe");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            // ❌ CANCELED
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            // ❌ FAILED
            Toast.makeText(this, "Payment Failed: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
```

---

## 🛡️ PART 3: Security & Release

### ProGuard Rules (`proguard-rules.pro`)
When you build the release APK, add these to prevent the SDK from breaking:

```pro
-keep class com.stripe.android.** { *; }
-dontwarn com.stripe.android.**
```

### Best Practices
1. **Never commit `.env` files** to GitHub.
2. **Use HTTPS** for your backend URL.
3. **Verify amount on backend**: Don't trust the amount sent from the app. Recalculate it on the server using the cart items.

---

## 🧪 Testing Credentials

| Card Type | Card Number | Expiry | CVV |
|-----------|-------------|--------|-----|
| **Visa (Success)** | `4242 4242 4242 4242` | Future | 123 |
| **Visa (Decline)** | `4000 0000 0000 0002` | Future | 123 |
| **Mastercard** | `5555 5555 5555 4444` | Future | 123 |

---

## ✅ Summary Checklist

- [x] Android SDK Installed
- [x] Test Keys Integrated
- [ ] Backend Server Deployed (User Action Required)
- [ ] Retrofit Integration in App (User Action Required)
- [ ] ProGuard Rules Added (For Release)

Follow this guide to transition from the current test implementation to a fully secure production environment.
