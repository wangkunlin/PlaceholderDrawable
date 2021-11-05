package com.wkl.drawabledemo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawableEnjector.enject();
        setContentView(R.layout.activity_main);

//        final Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.common_default_img);

        final ImageView iv = findViewById(R.id.dr);
//        iv.setImageDrawable(drawable);
        // AppCompatImageView 无感使用
        iv.setImageResource(R.drawable.common_default_img);

        ImageView iv2 = findViewById(R.id.iv);
        GlideApp.with(this)
                .load("")
                // 直接 无感使用占位图
                .placeholder(R.drawable.common_default_img2)
                .into(iv2);

        ImageView iv3 = findViewById(R.id.dr1);
        GlideApp.with(this)
                .load("")
                .placeholder(R.drawable.common_default_img2)
                .into(iv3);

    }

}
