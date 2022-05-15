package com.example.petdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class imageViewActivity extends AppCompatActivity {

    /*게시글의 이미지 클릭했을때 보여주는 액티비티 */

    private String imageUrl1;
    //    private int currentItem;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_expandimage);
        overridePendingTransition(R.anim.fade_in, R.anim.none_animation);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
//        currentItem = intent.getIntExtra("currentItem", 0);
        imageUrl1 = intent.getStringExtra("imageUrl1");

        imageView = findViewById(R.id.detail_imageView);
        Glide.with(this).load(imageUrl1).into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none_animation, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

