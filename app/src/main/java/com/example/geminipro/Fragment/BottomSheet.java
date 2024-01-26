package com.example.geminipro.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.example.geminipro.databinding.PickImageBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.annotation.Nullable;

public class BottomSheet extends BottomSheetDialogFragment {
    private PickImageBottomSheetBinding binding;
    private BottomSheetCallback callback;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PickImageBottomSheetBinding.inflate(inflater, container, false);

        binding.btnOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCameraClicked();
                }
                dismiss();
            }
        });

        binding.btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onGalleryClicked();
                }
                dismiss();
            }
        });

        return binding.getRoot();
    }

    public void setCallback(BottomSheetCallback callback) {
        this.callback = callback;
    }

    public interface BottomSheetCallback {
        void onCameraClicked();
        void onGalleryClicked();
    }
}
