package com.example.geminipro.Object;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.example.geminipro.Activity.SettingMainActivity;
import com.example.geminipro.Activity.SettingsActivity;
import com.example.geminipro.R;
import com.example.geminipro.Util.ImageDialog;
import com.example.geminipro.Util.MyPopupMenu;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class NavigationLayout {
    private final Context context;
    private final ActivityMainBinding binding;

    public NavigationLayout(ActivityMainBinding binding, Context context){
        this.context = context;
        this.binding = binding;
    }

    public void getAndSetProfilePicture() {
        if (null == context || null == binding) return;

        SharedPreferences preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String storedImagePath = preferences.getString("userImage", "");

        if (!storedImagePath.isEmpty()) Glide.with(context).load(storedImagePath).into(binding.navigationDrawerButton);
        else Glide.with(context).load(R.drawable.baseline_person_24).into(binding.navigationDrawerButton);

        binding.imageViewMore.setOnClickListener(v -> {
            MyPopupMenu popupMenu = new MyPopupMenu(context, R.menu.menu_item, v);
            popupMenu.startPopUp();
        });

        if (!storedImagePath.isEmpty()) {
            Glide.with(context).load(storedImagePath).into(binding.avatarImageView);
            List<Uri> list = new ArrayList<>();
            list.add(Uri.parse(storedImagePath));
            binding.avatarImageView.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                ImageDialog imageDialog = new ImageDialog(context, list, 0);
                imageDialog.show();
            });

        }
        if (!userName.isEmpty()) binding.navUserName.setText(userName);

        binding.navUserName.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent intent = new Intent(context, SettingMainActivity.class);
            intent.putExtra("id", "0");
            context.startActivity(intent);
        });

        binding.cardViewMoreFunc.setOnClickListener(v ->{
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.putExtra("state", true);
            context.startActivity(intent);
        });
    }
}
