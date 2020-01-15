package com.zpf.bannerdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.SingleLayoutHelper;
import com.zpf.bannerdemo.BannerAdapter;
import com.zpf.bannerdemo.BannerInfo;
import com.zpf.bannerdemo.R;

import java.util.ArrayList;
import java.util.List;

import static com.zpf.bannerdemo.TypeConstant.BANNER_INNER_TYPE;
import static com.zpf.bannerdemo.TypeConstant.BANNER_OUTER_TYPE;

public class SingleBannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_banner);

        List<BannerInfo> bannerList = new ArrayList<>();

        bannerList.add(new BannerInfo(getResources().getDrawable(R.drawable.banner_0)));
        bannerList.add(new BannerInfo(getResources().getDrawable(R.drawable.banner_1)));
        bannerList.add(new BannerInfo(getResources().getDrawable(R.drawable.banner_2)));
        bannerList.add(new BannerInfo(getResources().getDrawable(R.drawable.banner_3)));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(BANNER_OUTER_TYPE, 10);
        viewPool.setMaxRecycledViews(BANNER_INNER_TYPE, 10);

        final VirtualLayoutManager layoutManager = new VirtualLayoutManager(this, LinearLayout.VERTICAL);
        DelegateAdapter delegateAdapter = new DelegateAdapter(layoutManager);
        recyclerView.setLayoutManager(layoutManager);
        delegateAdapter.addAdapter(new BannerAdapter(this, new SingleLayoutHelper(), bannerList,viewPool));
        recyclerView.setAdapter(delegateAdapter);
    }
}
