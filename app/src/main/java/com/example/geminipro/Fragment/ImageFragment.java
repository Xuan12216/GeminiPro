package com.example.geminipro.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.geminipro.R;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.OnViewDragListener;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "imageUri";
    private PhotoView photoView;
    private DialogStatusListener listener;
    private int scale = 1;

    public static ImageFragment newInstance(Uri imageUri) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageFragment newInstance(Uri imageUri, DialogStatusListener listener) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        fragment.setArguments(args);
        fragment.setDialogStatusListener(listener);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        photoView = rootView.findViewById(R.id.photoView_fragment);

        if (getArguments() != null) {
            Uri imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
            Glide.with(requireContext())
                    .load(imageUri)
                    .into(photoView);
        }

        setListener();
        return rootView;
    }

    private void setListener() {
        photoView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (listener != null) listener.DialogDismiss("dismiss");
            }
        });

        photoView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                scale = Math.round(photoView.getScale());
                if (listener != null && scale != 1) listener.DialogDismiss("yes_scale");
            }
        });

        photoView.setOnViewDragListener(new OnViewDragListener() {
            @Override
            public void onDrag(float dx, float dy) {
                if (listener != null && scale == 1) listener.DialogDismiss("no_scale");
            }
        });
    }

    public void setDialogStatusListener(DialogStatusListener listener) {
        this.listener = listener;
    }

    public interface DialogStatusListener{
        void DialogDismiss(String status);
    }
}
