package com.zpf.bannerdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.RecyclablePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.zpf.bannerdemo.TypeConstant.BANNER_INNER_TYPE;
import static com.zpf.bannerdemo.TypeConstant.BANNER_OUTER_TYPE;
import static com.zpf.bannerdemo.Util.dp2px;

public class BannerAdapter extends DelegateAdapter.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BannerAdapter";

    private Context mContext;
    private LayoutHelper mLayoutHelper;
    private ViewPager mViewPager;
    private Handler mHandler;
    private RecyclerView.RecycledViewPool mViewPool;

    private List<BannerInfo> bannerInfoList;
    private ArrayList<ImageView> pointList;

    private int hashCode;
    private boolean init;

    public BannerAdapter(Context context, LayoutHelper layoutHelper, List<BannerInfo> bannerInfoList,RecyclerView.RecycledViewPool viewPool) {
        hashCode = hashCode();
        this.mLayoutHelper = layoutHelper;
        this.mContext = context;
        this.bannerInfoList = bannerInfoList;
        this.mViewPool = viewPool;
        pointList = new ArrayList<>();
        initPointViewList();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == hashCode) {
                    if (mViewPager != null && BannerAdapter.this.bannerInfoList.size() != 1) {
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                    }
                    sendEmptyMessageDelayed(hashCode, 6000);
                }
            }
        };
        sendEmptyMessageDelayed();
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return mLayoutHelper;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if (type == BANNER_OUTER_TYPE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_viewpager, viewGroup, false);
            return new BannerViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_inner, viewGroup, false);
            return new BannerInnerViewHolder(view);
        }
    }

    //onViewRecycled, 当被创建的一个view被复用的时候被调用。
    // 就是，LayoutManager认为这个View没有价值了
    // 比如在屏幕上不可见，就会复用这个View并且调用这个方法，
    // 可以在这里对该View进行资源释放。
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.itemView instanceof ViewPager) {
            ((ViewPager) holder.itemView).setAdapter(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BannerViewHolder && !init) {
            BannerViewHolder viewHolder = (BannerViewHolder) holder;
            mViewPager = viewHolder.viewPager;
            mViewPager.setAdapter(new PagerAdapter(mContext, this, mViewPool, bannerInfoList));


            mViewPager.clearOnPageChangeListeners();
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                boolean dragging =  false;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }


                @Override
                public void onPageSelected(int position) {
                    //indicator
                    for (int i = 0; i < pointList.size() && pointList.size() != 1; i++) {
                        ImageView point = pointList.get(i);
                        if (i == position % bannerInfoList.size()) {
                            point.setImageResource(R.drawable.banner_indicator_rec_selected);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(mContext, 10), dp2px(mContext, 4));
                            lp.setMargins(dp2px(mContext, 3), 0, dp2px(mContext, 3), 0);
                            point.setLayoutParams(lp);
                        } else {
                            point.setImageResource(R.drawable.banner_indicator_rec_normal);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(mContext, 4), dp2px(mContext, 4));
                            lp.setMargins(dp2px(mContext, 3), 0, dp2px(mContext, 3), 0);
                            point.setLayoutParams(lp);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    /**
                     * 当dragging的时候就把定时任务取消
                     * 下一次idle的时候再开启定时任务
                     */
                    switch (state) {
                        case ViewPager.SCROLL_STATE_IDLE:
                            Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_IDLE" );
                            if (dragging) {
                                sendEmptyMessageDelayed();
                                dragging = false;
                            }
                            break;
                        case ViewPager.SCROLL_STATE_DRAGGING:
                            Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_DRAGGING" );
                            dragging = true;
                            removeHandlerMessage();
                            break;
                        default:
                            break;
                    }
                }
            });


            if (bannerInfoList.size() == 1) {
                mViewPager.setCurrentItem(0);
            } else {
                mViewPager.setCurrentItem(bannerInfoList.size() * 100);
            }


                LinearLayout mPointRealContainerLl = viewHolder.pointLl;
                mPointRealContainerLl.removeAllViews();
                for (ImageView pointItem : pointList) {
                    mPointRealContainerLl.addView(pointItem);
                }

            init = true;
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return BANNER_OUTER_TYPE;
    }


    static class PagerAdapter extends RecyclablePagerAdapter<BannerInnerViewHolder> {
        private List<BannerInfo> bannerInfoList;
        private Context mContext;

        PagerAdapter(Context mContext, RecyclerView.Adapter adapter, RecyclerView.RecycledViewPool pool, List<BannerInfo> bannerInfoList) {
            super(adapter, pool);
            this.bannerInfoList = bannerInfoList;
            this.mContext = mContext;

        }


        @Override
        public int getCount() {
            if (bannerInfoList.size() == 1) {
                return 1;
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public void onBindViewHolder(BannerInnerViewHolder viewHolder, int position) {
            viewHolder.imageView.setImageDrawable(bannerInfoList.get(position % bannerInfoList.size()).drawable);
            final int finalPosition = position % bannerInfoList.size();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"点击了 "+ finalPosition % bannerInfoList.size(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return BANNER_INNER_TYPE;
        }
    }

    public void setBannerInfoList(List<BannerInfo> bannerInfoList) {
        this.bannerInfoList = bannerInfoList;
        init = false;
        notifyDataSetChanged();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private LinearLayout pointLl;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.banner_pager);
            pointLl = itemView.findViewById(R.id.banner_point);
        }
    }

    static class BannerInnerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        BannerInnerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_content);
        }
    }

    private void removeHandlerMessage() {
        if (mHandler != null) {
            mHandler.removeMessages(hashCode);
        }
    }
    private void sendEmptyMessageDelayed() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(hashCode, 6000);
        }

    }


    private void initPointViewList() {
        /**
         * banner数量为1不tianiaiaindicator
         */
        if (bannerInfoList.size() == 1) {
            return;
        }

        for (int i = 0; i < bannerInfoList.size(); i++) {
            ImageView imageView = new ImageView(mContext);
            if (i == 0) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(mContext, 10), dp2px(mContext, 4));
                lp.setMargins(dp2px(mContext, 3), 0, dp2px(mContext, 3), 0);
                imageView.setImageResource(R.drawable.banner_indicator_rec_selected);
                imageView.setLayoutParams(lp);
            } else {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(mContext, 4), dp2px(mContext, 4));
                lp.setMargins(dp2px(mContext, 3), 0, dp2px(mContext, 3), 0);
                imageView.setImageResource(R.drawable.banner_indicator_rec_normal);
                imageView.setLayoutParams(lp);
            }
            pointList.add(imageView);
        }
    }

}
