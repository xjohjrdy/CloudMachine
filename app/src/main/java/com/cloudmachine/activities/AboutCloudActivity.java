package com.cloudmachine.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.autolayout.widgets.RadiusButtonView;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.bean.VersionInfo;
import com.cloudmachine.net.api.ApiConstants;
import com.cloudmachine.net.task.GetVersionAsync;
import com.cloudmachine.ui.homepage.activity.QuestionCommunityActivity;
import com.cloudmachine.utils.CommonUtils;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.ShareDialog;
import com.cloudmachine.utils.UMengKey;
import com.cloudmachine.utils.VersionU;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;

/**
 * 关于与帮助页面
 */
public class AboutCloudActivity extends BaseAutoLayoutActivity implements
        Callback, OnClickListener {

    private Context mContext;
    private Handler mHandler;
    private RadiusButtonView button1;
    private TextView textView;
    // private ImageView backImg;
    private boolean isAuto = false;
    private FrameLayout mFeedback;
    private boolean updateVersion = false;
    private FrameLayout mUseHelp, mShareApp;
//    private Button envirBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_cloud);
        getIntentData();
        mContext = this;
        mHandler = new Handler(this);
        textView = (TextView) findViewById(R.id.version);
        textView.setText("V" + VersionU.getVersionName()+"("+VersionU.getVersionCode()+")");
        initView();
//        envirBtn= (Button) findViewById(R.id.about_envir_btn);
        /*if (isAuto) {
            new GetVersionAsync(mContext, mHandler).execute();
        }*/
//        if (MyApplication.IS_RELEASE){
//            envirBtn.setVisibility(View.GONE);
//        }else{
//            envirBtn.setVisibility(View.VISIBLE);
//            if (ApiConstants.CLOUDM_HOST.contains("test")){
//                envirBtn.setText("测试环境");
//            }else{
//                envirBtn.setText("线上环境");
//            }
//        }
//        long time = VerisonCheckSP.getTime(this);
//        if (time != 0
//                && System.currentTimeMillis() - time < 1000 * 60 * 60 * 24) {
//
//        } else {
            new GetVersionAsync(mContext, mHandler).execute();
//        }

    }
//    public void changeEnvir(View view){
//     String text= envirBtn.getText().toString();
//        if ("测试环境".equals(text)){
//            Api.clearHostType();
//            ApiConstants.CLOUDM_HOST="http://api.cloudm.com/cloudm3/yjx/";
//            ApiConstants.GUOSHUAI_HOST = "http://api.cloudm.com/cloudm3/";
//            ApiConstants.CAITINGTING_HOST = "http://api.cloudm.com/cloudm3/";
//            ApiConstants.XIEXIN_HOST = "http://ask.cloudm.com/";
//            ApiConstants.H5_HOST = "http://h5.cloudm.com/";
//            text="线上环境";
//        }else if ("线上环境".equals(text)){
//            Api.clearHostType();
//            ApiConstants.CLOUDM_HOST = "http://api.test.cloudm.com/cloudm3/yjx/";
//            ApiConstants.GUOSHUAI_HOST = "http://api.test.cloudm.com/cloudm3/";
//            ApiConstants.CAITINGTING_HOST = "http://api.test.cloudm.com/cloudm3/";
//            ApiConstants.XIEXIN_HOST = "http://ask.test.cloudm.com/";
//            ApiConstants.H5_HOST = "http://h5.test.cloudm.com/";
//            text="测试环境";
//        }
//        envirBtn.setText(text);
//
//        ToastUtils.showCenterToast(this,text+"启用，current app process keep valid");
//    }

    @Override
    public void initPresenter() {

    }

    private void initView() {

        mFeedback = (FrameLayout) findViewById(R.id.feedback_fl);
        mFeedback.setOnClickListener(this);
        mUseHelp = (FrameLayout) findViewById(R.id.use_help_fl);
        mUseHelp.setOnClickListener(this);
        mShareApp = (FrameLayout) findViewById(R.id.shareapp_fl);
        mShareApp.setOnClickListener(this);

    }


    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            case Constants.HANDLER_GETVERSION_SUCCESS:
                VersionInfo vInfo = (VersionInfo) msg.obj;
                if (null != vInfo) {
                    boolean isUpdate = CommonUtils.checVersion(VersionU.getVersionName(), vInfo.getVersion());
                    if (isUpdate) {
                        Constants.updateVersion(this, mHandler,
                                vInfo.getMustUpdate(),vInfo.getMessage(),vInfo.getLink());
                    }

                }
                break;
            case Constants.HANDLER_GETVERSION_FAIL:
                Constants.MyToast((String) msg.obj,
                        getResources().getString(R.string.get_version_error));
                break;
            case Constants.HANDLER_VERSIONDOWNLOAD:
                Constants
                        .versionDownload(AboutCloudActivity.this, (String) msg.obj);
                break;
        }
        return false;
    }


    private void getIntentData() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            try {
                String ectra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                if (!TextUtils.isEmpty(ectra)) {
                    isAuto = true;
                }
            } catch (Exception e) {
            }

        }
    }

    @Override
    protected void onResume() {
        //MobclickAgent.onPageStart(UMengKey.time_setting_aboat);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //MobclickAgent.onPageEnd(UMengKey.time_setting_aboat);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feedback_fl:
                Constants.toActivity(AboutCloudActivity.this, SuggestBackActivity.class, null);
                break;
            case R.id.use_help_fl:
//                Constants.toActivity(AboutCloudActivity.this, UseHelpActivity.class, null);
                Bundle bundle=new Bundle();
                bundle.putString(QuestionCommunityActivity.H5_URL, ApiConstants.AppUseHelper);
                Constants.toActivity(AboutCloudActivity.this, QuestionCommunityActivity.class, bundle);
                break;
            case R.id.shareapp_fl:
                ShareDialog shareDialog = new ShareDialog(this, SESSIONURL, SESSIONTITLE, SESSIONDESCRIPTION, -1);
                shareDialog.show();
                MobclickAgent.onEvent(mContext, UMengKey.count_share_app);
                break;

        }
    }
    //分享信息
    private static final String SESSIONTITLE = "云机械";
    private static final String SESSIONDESCRIPTION = "我的工程机械设备都在云机械APP，你的设备在哪里，赶紧加入吧！";
    private static final String SESSIONURL = "http://www.cloudm.com/yjx";
}
