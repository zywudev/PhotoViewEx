package com.wuzy.photoviewex.sample;

import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.wuzy.photoviewex.PhotoView;

import androidx.appcompat.app.AppCompatActivity;

public class PicassoSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final PhotoView photoView = findViewById(R.id.iv_photo);

        Picasso.with(this)
                .load("http://pbs.twimg.com/media/Bist9mvIYAAeAyQ.jpg")
                .into(photoView);
    }
}
