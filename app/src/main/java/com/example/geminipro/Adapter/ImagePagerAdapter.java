package com.example.geminipro.Adapter;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.geminipro.Fragment.ImageFragment;
import java.util.List;

public class ImagePagerAdapter extends FragmentStateAdapter {

    private List<Uri> imageUris;
    private ImageFragment.DialogStatusListener listener;

    public ImagePagerAdapter(FragmentActivity fragmentActivity, List<Uri> imageUris) {
        super(fragmentActivity);
        this.imageUris = imageUris;
    }

    public ImagePagerAdapter(FragmentActivity fragmentActivity, List<Uri> imageUris, ImageFragment.DialogStatusListener listener) {
        super(fragmentActivity);
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ImageFragment.newInstance(imageUris.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }
}
