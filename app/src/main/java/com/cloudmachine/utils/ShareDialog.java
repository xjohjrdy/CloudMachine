package com.cloudmachine.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.cloudmachine.R;
import com.cloudmachine.chart.utils.AppLog;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import rx.functions.Action1;

import static com.cloudmachine.utils.WeChatShareUtil.weChatShareUtil;


public class ShareDialog extends Dialog implements View.OnClickListener, UMShareListener {
    private final View view;
    private String webpageUrl;
    private String msgTitle;
    private String msgDesc;
    private Context mContext;
    private int imageSource = -1;
    private String iconUrl;
    private SHARE_MEDIA mMedia;

    public ShareDialog(Context context, String webpageUrl, String msgTitle, String msgDesc, int resource) {
        super(context, R.style.ShareDialog);

        this.webpageUrl = webpageUrl;
        this.msgTitle = msgTitle;
        this.msgDesc = msgDesc;
        this.imageSource = resource;
        this.mContext = context;
        view = getLayoutInflater().inflate(R.layout.widget_dialog_share, null);
        setContentView(view);
        initWindow();
        initView();
    }

    public ShareDialog(Context context, String webpageUrl, String msgTitle, String msgDesc, String icon) {
        super(context, R.style.ShareDialog);

        this.webpageUrl = webpageUrl;
        this.msgTitle = msgTitle;
        this.msgDesc = msgDesc;
        this.iconUrl = icon;
        this.mContext = context;

        view = getLayoutInflater().inflate(R.layout.widget_dialog_share, null);
        setContentView(view);
        initWindow();
        initView();
    }

    /**
     * 初始化window窗口
     */
    private void initWindow() {
        Window window = getWindow();//当前弹窗所在的窗口对象
        WindowManager.LayoutParams attributes = window.getAttributes();

        attributes.gravity = Gravity.BOTTOM;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;

        window.setAttributes(attributes);
    }

    private void initView() {
        if (weChatShareUtil == null) {
            weChatShareUtil = WeChatShareUtil.getInstance(mContext);
        }
        ImageView ivQq = (ImageView) view.findViewById(R.id.iv_qq);
        ImageView ivWeibo = (ImageView) view.findViewById(R.id.iv_weibo);
        ImageView ivQqZone = (ImageView) view.findViewById(R.id.iv_qq_zone);
        ImageView ivSession = (ImageView) view.findViewById(R.id.iv_session);
        ImageView ivTimeline = (ImageView) view.findViewById(R.id.iv_timeline);
        ImageView ivCopyLink = (ImageView) view.findViewById(R.id.iv_copylink);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        ivQq.setOnClickListener(this);
        ivQqZone.setOnClickListener(this);
        ivWeibo.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        ivSession.setOnClickListener(this);
        ivTimeline.setOnClickListener(this);
        ivCopyLink.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.iv_copylink:
//                webpageUrl
                ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(webpageUrl);
                ToastUtils.showCenterToast(v.getContext(), "复制成功");
                dismiss();
                break;
            case R.id.iv_qq:
                mMedia = SHARE_MEDIA.QQ;
                shareSocialUrl();
                dismiss();
                break;
            case R.id.iv_qq_zone:
                mMedia = SHARE_MEDIA.QZONE;
                shareSocialUrl();
                dismiss();
                break;
            case R.id.iv_weibo:
                mMedia = SHARE_MEDIA.SINA;
                shareSocialUrl();
                dismiss();
                break;

            case R.id.iv_session:  // 分享给微信朋友
                mMedia = SHARE_MEDIA.WEIXIN;
                shareSocialUrl();
                dismiss();
                break;
            case R.id.iv_timeline: // 分享到朋友圈
                AppLog.print("case1 分享--->wechat");
                mMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
                shareSocialUrl();
                dismiss();
                break;
        }


    }

    public void shareSocialUrl() {
        //初试话一个WXWebpageObject对象，填写url
        RxPermissions.getInstance(mContext).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean grant) {
                if (grant) {
                    UMWeb web = new UMWeb(webpageUrl);
                    web.setTitle(msgTitle);
                    web.setThumb(new UMImage(mContext, R.drawable.corner));
                    web.setDescription(msgDesc);
                    ShareAction shareAction = new ShareAction((Activity) mContext);
                    AppLog.print("setPlatform__");
                    shareAction.setPlatform(mMedia).withMedia(web).setCallback(ShareDialog.this).share();
                } else {
                    ToastUtils.showToast(mContext, "需要相关权限，请到设置/权限管理中设置允许");
                }
            }
        });
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {

    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {
        ToastUtils.showToast(mContext, "分享成功！！");
    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
        ToastUtils.showToast(mContext, "分享失败！！" + throwable.getMessage());
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
        ToastUtils.showToast(mContext, "取消分享！！");
    }
}
