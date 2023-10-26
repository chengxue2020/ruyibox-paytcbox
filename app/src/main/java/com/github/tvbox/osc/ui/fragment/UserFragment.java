package com.github.tvbox.osc.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.beanry.AdvBean;
import com.github.tvbox.osc.beanry.ReLevelBean;
import com.github.tvbox.osc.beanry.ReUserBean;
import com.github.tvbox.osc.beanry.SiteBean;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.ui.activity.CollectActivity;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.HistoryActivity;
import com.github.tvbox.osc.ui.activity.LivePlayActivity;
import com.github.tvbox.osc.ui.activity.PushActivity;
import com.github.tvbox.osc.ui.activity.SearchActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.activity.StartActivity;
import com.github.tvbox.osc.ui.activity.UserActivity;
import com.github.tvbox.osc.ui.adapter.HomeHotVodAdapter;
import com.github.tvbox.osc.ui.dialog.TipDialog;
import com.github.tvbox.osc.util.BaseR;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MMkvUtils;
import com.github.tvbox.osc.util.PreferencesUtils;
import com.github.tvbox.osc.util.RoundBitmapTransformation;
import com.github.tvbox.osc.util.ToolUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import java.util.Collections;//用来打乱顺序
/**
 * @author pj567
 * @date :2021/3/9
 * @description:
 */
public class UserFragment extends BaseLazyFragment implements View.OnClickListener {
    private FrameLayout ll_go_play;
    private FrameLayout tvLive;
    private FrameLayout tvCollect;
    private FrameLayout tvPush;

    private TextView tv_video_name;
    public static HomeHotVodAdapter homeHotVodAdapter;

    private List<Movie.Video> homeSourceRec;


    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(List<Movie.Video> recVod) {
        return new UserFragment().setArguments(recVod);
    }

    public UserFragment setArguments(List<Movie.Video> recVod) {
        this.homeSourceRec = recVod;
        return this;
    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord(10);
            List<Movie.Video> vodList = new ArrayList<>();
            for (VodInfo vodInfo : allVodRecord) {
                Movie.Video vod = new Movie.Video();
                vod.id = vodInfo.id;
                vod.sourceKey = vodInfo.sourceKey;
                vod.name = vodInfo.name;
                vod.pic = vodInfo.pic;
                if (vodInfo.playNote != null && !vodInfo.playNote.isEmpty())
                    vod.note = "上次看到" + vodInfo.playNote;
                vodList.add(vod);
            }
            homeHotVodAdapter.setNewData(vodList);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_user;
    }

    private ImageView ad_user_img_0;
    private ImageView ad_user_img_1;
    private ImageView ad_user_img_2;
    private ImageView ad_user_img_3;
    private ImageView ad_user_img_4;

    private ImageView ad_user_img_5;

    private TextView tv_home_ad_0;
    private TextView tv_home_ad_1;
    private TextView tv_home_ad_2;
    private TextView tv_home_ad_3;
    private TextView tv_home_ad_4;

    private TextView tv_home_ad_5;

    private void goSearch(int index) {
        if (advList != null && advList.size() > index) {
            AdvBean.MsgDTO msgDTO = advList.get(index);
            Intent newIntent = new Intent(mContext, SearchActivity.class);
            newIntent.putExtra("title", msgDTO.name);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(newIntent);
        }
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        ll_go_play= findViewById(R.id.ll_go_play);
        tvLive = findViewById(R.id.tvLive);
        tvCollect = findViewById(R.id.tvFavorite);
        tvPush = findViewById(R.id.tvPush);
        ll_go_play.setOnClickListener(this);
        tvLive.setOnClickListener(this);
        tvPush.setOnClickListener(this);
        tvCollect.setOnClickListener(this);
        ll_go_play.setOnFocusChangeListener(focusChangeListener);
        tvLive.setOnFocusChangeListener(focusChangeListener);
        tvPush.setOnFocusChangeListener(focusChangeListener);
        tvCollect.setOnFocusChangeListener(focusChangeListener);
        TvRecyclerView tvHotList = findViewById(R.id.tvHotList);

        //获取view
        ad_user_img_0 = findViewById(R.id.ad_user_img_0);
        ad_user_img_1 = findViewById(R.id.ad_user_img_1);
        ad_user_img_2 = findViewById(R.id.ad_user_img_2);
        ad_user_img_3 = findViewById(R.id.ad_user_img_3);
        ad_user_img_4 = findViewById(R.id.ad_user_img_4);
        ad_user_img_5 = findViewById(R.id.ad_user_img_5);

        tv_home_ad_0 = findViewById(R.id.tv_home_ad_0);
        tv_home_ad_1 = findViewById(R.id.tv_home_ad_1);
        tv_home_ad_2 = findViewById(R.id.tv_home_ad_2);
        tv_home_ad_3 = findViewById(R.id.tv_home_ad_3);
        tv_home_ad_4 = findViewById(R.id.tv_home_ad_4);
        tv_home_ad_5 = findViewById(R.id.tv_home_ad_5);

        tv_video_name = findViewById (R.id.tv_video_name);

        findViewById(R.id.fl_home_ad_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(0);
            }
        });
        findViewById(R.id.fl_home_ad_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(1);
            }
        });
        findViewById(R.id.fl_home_ad_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(2);
            }
        });
        findViewById(R.id.fl_home_ad_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(3);
            }
        });
        findViewById(R.id.fl_home_ad_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(4);
            }
        });
        findViewById(R.id.fl_home_ad_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSearch(5);
            }
        });

        findViewById(R.id.ll_go_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = PreferencesUtils.getString(requireContext(), "last_tv_key");
                String id = PreferencesUtils.getString(requireContext(), "last_tv_id");
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(id)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    bundle.putString("sourceKey", key);
                    jumpActivity(DetailActivity.class, bundle);
                }else {
                    ToastUtils.showShort("暂无观看记录");
                }
            }
        });


        homeHotVodAdapter = new HomeHotVodAdapter();
        homeHotVodAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (ApiConfig.get().getSourceBeanList().isEmpty())
                    return;
                Movie.Video vod = ((Movie.Video) adapter.getItem(position));
                if (vod.id != null && !vod.id.isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", vod.id);
                    bundle.putString("sourceKey", vod.sourceKey);
                    jumpActivity(DetailActivity.class, bundle);
                } else {
                    Intent newIntent = new Intent(mContext, SearchActivity.class);
                    newIntent.putExtra("title", vod.name);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(newIntent);
                }
            }
        });
        tvHotList.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        tvHotList.setAdapter(homeHotVodAdapter);

        initHomeHotVod(homeHotVodAdapter);

        getAdv();

        recordData ();
    }

    @Override
    public void onResume() {
        super.onResume ();
        recordData ();
    }

    private void recordData(){
        String key = PreferencesUtils.getString(requireContext(), "last_tv_key");
        String id = PreferencesUtils.getString(requireContext(), "last_tv_id");
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(id)) {
            VodInfo vodInfoRecord = RoomDataManger.getVodInfo(key, id);
                if (vodInfoRecord != null) {
                    tv_video_name.setText(vodInfoRecord.name+" "+vodInfoRecord.playNote);
                }else{
                    tv_video_name.setText("暂无观看记录");
            }
        }

    }
    private List<AdvBean.MsgDTO> advList;

    /**
     * 调用接口
     */
    private void getAdv() {
        OkGo.<String>post(ToolUtils.setApi("homead"))
                .params("t", System.currentTimeMillis() / 1000)
                .params("sign", ToolUtils.setSign("null"))
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String content = BaseR.decry_R(response.body());
                        AdvBean advBean = new Gson().fromJson(content, AdvBean.class);
                        if (advBean != null) {
                            if (advBean.code == 200) {
                                showAdv(advBean);
                            }
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        assert response.body() != null;
                        return response.body().string();
                    }
                });
    }

    private void showAdv(AdvBean advBean) {
        advList = advBean.msg;      
        if (advList != null) {
        Collections.shuffle(advList); // 打乱列表顺序 
            for (int i = 0; i < advList.size(); i++) {
                showAdvData(advList, i);
            }
        }
    }


    /**
     * 根据顺序加载数据
     *
     * @param msg
     * @param index
     */
    private void showAdvData(List<AdvBean.MsgDTO> msg, int index) {
        AdvBean.MsgDTO msgDTO = msg.get(index);
        switch (index) {
            case 0:
                showView(ad_user_img_0, tv_home_ad_0, msgDTO);
                break;
            case 1:
                showView(ad_user_img_1, tv_home_ad_1, msgDTO);
                break;
            case 2:
                showView(ad_user_img_2, tv_home_ad_2, msgDTO);
                break;
            case 3:
                showView(ad_user_img_3, tv_home_ad_3, msgDTO);
                break;
            case 4:
                showView(ad_user_img_4, tv_home_ad_4, msgDTO);
                break;
            case 5:
                showView(ad_user_img_5, tv_home_ad_5, msgDTO);
                break;
        }

    }

    /**
     * 显示到view上
     *
     * @param iv
     * @param tv
     * @param data
     */
    private void showView(ImageView iv, TextView tv, AdvBean.MsgDTO data) {
        loadImage(iv, data.extend, 10, 10, 0, 0);
        tv.setText(data.name);
    }

    public static void loadImage(ImageView iv, String imgUrl, int leftTopRadius, int rightTopRadius, int leftBottomRadius, int rightBottomRadius) {
        RequestOptions mTransitionOptions = RequestOptions.bitmapTransform(new RoundBitmapTransformation(leftTopRadius, rightTopRadius, leftBottomRadius, rightBottomRadius));
        Glide.with(iv).load(imgUrl).apply(mTransitionOptions).into(iv);
    }

    private void initHomeHotVod(HomeHotVodAdapter adapter) {
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 1) {
            if (homeSourceRec != null) {
                adapter.setNewData(homeSourceRec);
            }
            return;
        } else if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            return;
        }
        try {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            String today = String.format("%d%d%d", year, month, day);
            String requestDay = Hawk.get("home_hot_day", "");
            if (requestDay.equals(today)) {
                String json = Hawk.get("home_hot", "");
                if (!json.isEmpty()) {
                    adapter.setNewData(loadHots(json));
                    return;
                }
            }
            OkGo.<String>get("https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&playable=1&start=0&year_range=" + year + "," + year).execute(new AbsCallback<String>() {
                @Override
                public void onSuccess(Response<String> response) {
                    String netJson = response.body();
                    Hawk.put("home_hot_day", today);
                    Hawk.put("home_hot", netJson);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setNewData(loadHots(netJson));
                        }
                    });
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private ArrayList<Movie.Video> loadHots(String json) {
        ArrayList<Movie.Video> result = new ArrayList<>();
        try {
            JsonObject infoJson = new Gson().fromJson(json, JsonObject.class);
            JsonArray array = infoJson.getAsJsonArray("data");
            for (JsonElement ele : array) {
                JsonObject obj = (JsonObject) ele;
                Movie.Video vod = new Movie.Video();
                vod.name = obj.get("title").getAsString();
                vod.note = obj.get("rate").getAsString();
                vod.pic = obj.get("cover").getAsString();
                result.add(vod);
            }
        } catch (Throwable th) {

        }
        return result;
    }

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            else
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
        }
    };

    private TipDialog dialog;

    @Override
    public void onClick(View v) {
        FastClickCheckUtil.check(v);
        if (v.getId() == R.id.tvLive) {
            ReUserBean userData  = MMkvUtils.loadReUserBean("");
            if (userData!=null){
                jumpActivity(LivePlayActivity.class);//跳转到直播页面
            }else {
                dialog = new TipDialog(mContext,"请登陆后观看直播", "确认","取消", new TipDialog.OnListener() {
                    @Override
                    public void left() {
                        jumpActivity(UserActivity.class);
                        dialog.hide();
                    }
                    @Override
                    public void right() {
                        dialog.hide();
                    }

                    @Override
                    public void cancel() {
                        dialog.hide();
                    }
                });
                if (!dialog.isShowing())
                    dialog.show();
            }

        }
        else if (v.getId() == R.id.tvPush) {
            jumpActivity(PushActivity.class);}
        else if (v.getId() == R.id.tvFavorite) {
            jumpActivity(CollectActivity.class);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent event) {
        if (event.type == ServerEvent.SERVER_CONNECTION) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
