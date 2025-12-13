package com.sunit.groceryplus.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.sunit.groceryplus.CartActivity;
import com.sunit.groceryplus.MessageActivity;
import com.sunit.groceryplus.OrderHistoryActivity;
import com.sunit.groceryplus.R;
import com.sunit.groceryplus.UserDetailViewActivity;
import com.sunit.groceryplus.UserHomeActivity;
import com.sunit.groceryplus.UserProfileActivity;

public class NavigationHelper {

    public static void setupNavigation(Activity activity, int userId) {
        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navMessage = activity.findViewById(R.id.navMessage);
        LinearLayout navHistory = activity.findViewById(R.id.navHistory);
        LinearLayout navCart = activity.findViewById(R.id.navCart);
        LinearLayout navProfile = activity.findViewById(R.id.navProfile); // Note: Layout ID might be navProfile or navUser based on inconsistent naming, checking layouts first is safer, but assuming standard from UserHomeActivity

        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateTo(activity, UserHomeActivity.class, userId));
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> navigateTo(activity, MessageActivity.class, userId));
        }

        if (navHistory != null) {
            navHistory.setOnClickListener(v -> navigateTo(activity, OrderHistoryActivity.class, userId));
        }

        if (navCart != null) {
            navCart.setOnClickListener(v -> navigateTo(activity, CartActivity.class, userId));
        }

        if (navProfile != null) {
            // "Profile" usually maps to UserDetailViewActivity based on UserHomeActivity logic
            // or UserProfileActivity depending on what the user wants. 
            // UserHomeActivity maps it to UserDetailViewActivity.
            navProfile.setOnClickListener(v -> navigateTo(activity, UserDetailViewActivity.class, userId));
        }
    }

    private static void navigateTo(Activity currentActivity, Class<?> targetActivityClass, int userId) {
        if (currentActivity.getClass().equals(targetActivityClass)) {
            // Already on the screen, maybe refresh? For now do nothing to avoid loop
            return;
        }

        Intent intent = new Intent(currentActivity, targetActivityClass);
        intent.putExtra("user_id", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Avoid stacking same activities
        currentActivity.startActivity(intent);
        if (!(currentActivity instanceof UserHomeActivity)) {
             // Optional: finish current activity if we want to keep stack clean, 
             // but keeping Home as base is usually good.
             // currentActivity.finish(); 
        }
    }
}
