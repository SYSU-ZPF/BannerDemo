# BannerDemo
A banner demo is based on vlayout

一个基于阿里RecyclerView框架[Vlayout](https://github.com/alibaba/vlayout)的Banner，可实现自动轮播，滑动暂停轮播，底部indicator指示器。

大体思路：把ViewPager用一个ViewHolder包起来，做成一个size为1的adapter放在RecyclerView中。使用Handler处理轮播，在ViewPager的监听器中改变底部indicator，以及控制滑动暂定轮播--当ScrollState为SCROLL_STATE_DRAGGING的时候把定时任务取消，下一次ScrollState为SCROLL_STATE_IDLE的时候再开启定时任务。ViewPager的PagerAdapter使用Vlayout的RecyclablePagerAdapter。由于RecyclerView的Adapter 的Item在滑出屏幕再滑入屏幕的时候onBindViewHolder会重复调用，如果不加以控制的化就会每次onBindViewHolder调用都初始化一遍ViewPager，导致banner滑回来的时候就跳到了第一张，其实banner只需要初始化一次就够了，为了解决这个问题，再onBindViewHolder的时候通过标志位判断，如果未初始化才走初始化过程，当数据变化时，改变标志位重新初始化banner。


两个ViewHolder

①BannerViewHolder：外层存放ViewPager和指示器LinearLayout的viewHolder（itemCount = 1）
```
    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private LinearLayout pointLl;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.banner_pager);
            pointLl = itemView.findViewById(R.id.banner_point);
        }
    }
```
②BannerInnerViewHolder: 内层存放轮播图的ViewHolder
```
    static class BannerInnerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        BannerInnerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_content);
        }
    }
```

onCreateViewHolder根据不同的itemType返回不同的ViewHolder
```
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if (type == BANNER_OUTER_TYPE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_viewpager, viewGroup, false);
            return new BannerViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_inner, viewGroup, false);
            return new BannerInnerViewHolder(view);
        }
    }
```

继承Vlayout RecyclablePagerAdapter的PagerAdater
为了让banner无限滑动这里把ViewPager的count设置为MAX_VALUE,并在viewPager初始化时把CurrentItem设置为一个中间的的数
```

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
        }

        @Override
        public int getItemViewType(int position) {
            return BANNER_INNER_TYPE;
        }
```

初始化indicator的pointList，initPointViewList，根据banner数量添加ImageView圆点。

在外层Adapter的onBindViewHolder中初始化ViewPager，设置mViewpager的CurrentItem，
把pointList添加到在线性布局中，并改变初始化标志位
```
@Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BannerViewHolder && !init) {
            BannerViewHolder viewHolder = (BannerViewHolder) holder;
            mViewPager = viewHolder.viewPager;
            mViewPager.setAdapter(new PagerAdapter(mContext, this, mViewPool, bannerInfoList));
    
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
```
在ViewPager的PageChangeListener中主要时①在图片切换时改变指示器②在拖动时removeHandlerMessage，
在拖动完成变成idle状态时候再开启定时任务sendEmptyMessageDelayed，每次被用户拖动的时候state就变成DRAGGING，在放开归位之后就变成IDLE
，看官方关于ViewPager.STATE 的解释
```
    /**
     * Indicates that the pager is in an idle, settled state. The current page
     * is fully in view and no animation is in progress.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * Indicates that the pager is currently being dragged by the user.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * Indicates that the pager is in the process of settling to a final position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;
```
我对三个状态的理解为

SCROLL_STATE_IDLE:此时处于归位、呆滞的状态，没有动画，完全展示

SCROLL_STATE_DRAGGING:被用户拖动

SCROLL_STATE_SETTLING:在过渡到另外一个position的过程中

```
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
```

Handler轮询,改变CurrentItem，并延迟6s（banner跳转间隔）再发message,使用hashCode是为了防止实例化多个
BannerAdapter导致一个banner处理其他banner发来的消息
```
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
```

```
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
```

当banner数据改变例如网络数据刷新需要重新初始化banner
```
    public void setBannerInfoList(List<BannerInfo> bannerInfoList) {
        this.bannerInfoList = bannerInfoList;
        init = false;
        notifyDataSetChanged();
    }
```
