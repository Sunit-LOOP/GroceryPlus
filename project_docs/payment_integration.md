# Payment Integration Documentation

## Overview
The GroceryPlus app now supports two payment methods:
1. **Cash on Delivery (COD)** - Pay when you receive your order
2. **Stripe Payment** - Pay online with credit/debit card

---

## Implementation Details

### Files Created/Modified

#### 1. [PaymentActivity.java](file:///d:/6TH%20PROJECT/GroceryPLus/app/src/main/java/com/sunit/groceryplus/PaymentActivity.java)
**Features:**
- Payment method selection (COD or Stripe)
- Card details form (for Stripe)
- Card validation
- Order creation after successful payment
- Cart clearing after order placement

**Payment Flow:**
```
Cart → Checkout → Payment Selection → Order Created → Order History
```

#### 2. [activity_payment.xml](file:///d:/6TH%20PROJECT/GroceryPLus/app/src/main/res/layout/activity_payment.xml)
**UI Components:**
- Order summary card
- Payment method radio buttons
- Card details form (conditional visibility)
- Pay Now button

#### 3. [CartActivity.java](file:///d:/6TH%20PROJECT/GroceryPLus/app/src/main/java/com/sunit/groceryplus/CartActivity.java) - UPDATED
**Changes:**
- Checkout button now navigates to PaymentActivity
- Passes total amount and item count to payment screen

---

## Payment Methods

### 1. Cash on Delivery (COD)
- **Description:** Pay when you receive your order
- **Process:**
  1. User selects COD option
  2. Card fields are hidden
  3. User clicks "Pay Now"
  4. Order is created with status "pending"
  5. Cart is cleared
  6. User redirected to order history

**No validation required** - Order is placed immediately

### 2. Stripe Payment
- **Description:** Pay online with credit/debit card
- **Process:**
  1. User selects Stripe option
  2. Card details form is shown
  3. User enters card information
  4. Validation is performed
  5. Payment is processed (simulated for now)
  6. Order is created
  7. Cart is cleared
  8. User redirected to order history

**Card Validation:**
- Card number: 13-19 digits
- Expiry date: Required (MM/YY format)
- CVV: 3 digits minimum
- Cardholder name: Required

---

## Stripe Integration Setup

### Current Status
The app has **placeholder** Stripe integration. To enable real Stripe payments:

### Step 1: Get Stripe API Keys
1. Sign up at [https://stripe.com](https://stripe.com)
2. Get your API keys from Dashboard
   - **Publishable Key** (starts with `pk_test_` or `pk_live_`)
   - **Secret Key** (starts with `sk_test_` or `sk_live_`)

### Step 2: Add Stripe SDK to Project

Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    // Existing dependencies...
    
    // Stripe Android SDK
    implementation("com.stripe:stripe-android:20.37.0")
}
```

### Step 3: Update PaymentActivity

Replace the placeholder keys in `PaymentActivity.java`:
```java
// Line 23-24
private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_YOUR_ACTUAL_KEY";
private static final String STRIPE_SECRET_KEY = "sk_test_YOUR_ACTUAL_KEY";
```

### Step 4: Implement Stripe Payment Processing

Replace the simulated payment in `processStripePayment()` method:

```java
private void processStripePayment() {
    // Validate card details (already implemented)
    
    // Get card details
    String cardNumber = cardNumberEt.getText().toString().trim();
    String expiryDate = expiryDateEt.getText().toString().trim();
    String cvv = cvvEt.getText().toString().trim();
    String cardholderName = cardholderNameEt.getText().toString().trim();
    
    // Parse expiry date
    String[] expiryParts = expiryDate.split("/");
    int expMonth = Integer.parseInt(expiryParts[0]);
    int expYear = Integer.parseInt("20" + expiryParts[1]);
    
    // Create Stripe card
    Card card = new Card(
        cardNumber,
        expMonth,
        expYear,
        cvv
    );
    card.setName(cardholderName);
    
    // Create payment method
    Stripe stripe = new Stripe(this, STRIPE_PUBLISHABLE_KEY);
    
    PaymentMethodCreateParams params = PaymentMethodCreateParams.create(
        PaymentMethodCreateParams.Card.create(card),
        null
    );
    
    stripe.createPaymentMethod(params, new ApiResultCallback<PaymentMethod>() {
        @Override
        public void onSuccess(PaymentMethod paymentMethod) {
            // Payment method created successfully
            // Now create payment intent on your backend
            createPaymentIntent(paymentMethod.id, totalAmount);
        }
        
        @Override
        public void onError(Exception e) {
            // Handle error
            Toast.makeText(PaymentActivity.this, 
                "Payment failed: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            payNowBtn.setEnabled(true);
            payNowBtn.setText("Pay Now");
        }
    });
}
```

### Step 5: Backend Integration (Required for Production)

For production, you need a backend server to:
1. Create payment intents
2. Confirm payments
3. Handle webhooks

**Example backend endpoint:**
```
POST /create-payment-intent
Body: {
    "amount": 1000,  // in cents
    "currency": "inr",
    "payment_method": "pm_xxx"
}
```

---

## Current Implementation (Without Real Stripe)

### What Works Now:
✅ Payment method selection (COD/Stripe)
✅ Card details form
✅ Card validation
✅ COD order placement
✅ Simulated Stripe payment (2-second delay)
✅ Order creation
✅ Cart clearing
✅ Navigation to order history

### What's Simulated:
⚠️ Stripe payment processing (shows success after 2 seconds)
⚠️ No actual charge to card
⚠️ No payment verification

---

## User Flow

### Complete Checkout Flow:

```
1. User adds items to cart
   ↓
2. User goes to cart
   ↓
3. User reviews cart items
   ↓
4. User clicks "Proceed to Checkout"
   ↓
5. Payment screen opens
   ↓
6. User sees order summary (total amount, item count)
   ↓
7. User selects payment method:
   
   Option A: Cash on Delivery
   - Click "Pay Now"
   - Order created immediately
   
   Option B: Stripe Payment
   - Enter card number
   - Enter expiry date (MM/YY)
   - Enter CVV
   - Enter cardholder name
   - Click "Pay Now"
   - Payment processed
   - Order created
   ↓
8. Cart is cleared
   ↓
9. User redirected to Order History
   ↓
10. Success message shown
```

---

## Testing

### Test COD Payment:
1. Add products to cart
2. Go to cart
3. Click "Proceed to Checkout"
4. Select "Cash on Delivery"
5. Click "Pay Now"
6. Verify order appears in order history

### Test Stripe Payment (Simulated):
1. Add products to cart
2. Go to cart
3. Click "Proceed to Checkout"
4. Select "Credit/Debit Card (Stripe)"
5. Enter test card details:
   - Card Number: 4242424242424242
   - Expiry: 12/25
   - CVV: 123
   - Name: Test User
6. Click "Pay Now"
7. Wait 2 seconds (simulated processing)
8. Verify order appears in order history

---

## Security Notes

### Current Implementation:
⚠️ **NOT PRODUCTION READY** - Stripe keys are hardcoded
⚠️ **NOT SECURE** - No backend validation
⚠️ **SIMULATION ONLY** - No real payment processing

### For Production:
1. ✅ Store Stripe keys securely (use environment variables or secure storage)
2. ✅ Never expose secret key in client app
3. ✅ Implement backend server for payment processing
4. ✅ Use HTTPS for all API calls
5. ✅ Implement payment webhooks
6. ✅ Add fraud detection
7. ✅ Implement 3D Secure authentication
8. ✅ Log all payment attempts
9. ✅ Handle payment failures gracefully
10. ✅ Implement refund functionality

---

## Error Handling

### Validation Errors:
- Empty card number → "Card number is required"
- Invalid card number → "Invalid card number"
- Empty expiry date → "Expiry date is required"
- Empty CVV → "CVV is required"
- Invalid CVV → "Invalid CVV"
- Empty cardholder name → "Cardholder name is required"

### Payment Errors:
- Cart empty → "Cart is empty"
- Order creation failed → "Failed to create order"
- Order items failed → "Error creating order items"
- Payment processing error → "Error processing payment"

---

## Future Enhancements

### Recommended Features:
1. **Multiple Payment Methods:**
   - UPI
   - Net Banking
   - Wallets (Paytm, PhonePe, Google Pay)
   - EMI options

2. **Payment Features:**
   - Save card for future use
   - Payment history
   - Refund processing
   - Payment receipts (PDF)
   - Email confirmations

3. **Security:**
   - PCI DSS compliance
   - Tokenization
   - Biometric authentication
   - OTP verification

4. **UX Improvements:**
   - Card brand detection (Visa, Mastercard, etc.)
   - Auto-format card number
   - Auto-format expiry date
   - CVV tooltip
   - Payment success animation

---

## API Reference

### PaymentActivity Methods

#### `setStripeKeys(String publishableKey, String secretKey)`
**Purpose:** Update Stripe API keys  
**Parameters:**
- `publishableKey`: Stripe publishable key
- `secretKey`: Stripe secret key

**Usage:**
```java
PaymentActivity.setStripeKeys(
    "pk_test_YOUR_KEY",
    "sk_test_YOUR_KEY"
);
```

#### `processStripePayment()`
**Purpose:** Process Stripe payment  
**Returns:** void  
**Side Effects:** Creates order on success

#### `processCODPayment()`
**Purpose:** Process COD order  
**Returns:** void  
**Side Effects:** Creates order immediately

#### `createOrder(String paymentMethod)`
**Purpose:** Create order in database  
**Parameters:**
- `paymentMethod`: "stripe" or "cod"

---

## Summary

✅ **Implemented:**
- Payment screen with 2 options
- Card details form
- Card validation
- COD order placement
- Simulated Stripe payment
- Order creation
- Cart clearing

⏳ **Pending (Requires Your Stripe Keys):**
- Real Stripe SDK integration
- Actual payment processing
- Backend payment server
- Payment verification

📝 **To Enable Real Payments:**
1. Get Stripe API keys
2. Add Stripe SDK dependency
3. Update PaymentActivity with real keys
4. Implement backend server
5. Replace simulated payment with real Stripe API calls

---

**The payment flow is fully functional for testing. Add your Stripe keys when ready for production!**
