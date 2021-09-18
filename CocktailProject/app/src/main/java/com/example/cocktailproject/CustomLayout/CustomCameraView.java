package com.example.cocktailproject.CustomLayout;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otaliastudios.cameraview.CameraView;

import org.jetbrains.annotations.NotNull;

public class CustomCameraView extends CameraView {
    public CustomCameraView(@NonNull @NotNull Context context) {
        super(context);
    }

    public CustomCameraView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
