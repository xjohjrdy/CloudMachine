package com.cloudmachine.ui.home.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.alipay.sdk.app.PayTask;
import com.cloudmachine.activities.SearchPoiActivity;
import com.cloudmachine.chart.utils.AppLog;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.ToastUtils;
import com.google.gson.Gson;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by xiaojw on 2017/7/24.
 */

public class JsInteface {
    private Handler mHandler;
    private static final String CLOSE_EVENT = "close_event";
    public static final String Go_Login_Page = "goLoginPage()";
    private static final String CLICK_REPAIR = "click_repair()";
    public static final String ALERT_TYPE = "alert_type";
    public static final String ALERT_TIPS = "alert_tips";
    public static final String ALERT_EVENT = "alert_event";
    public static final String UPLOAD_IMG = "upload_img";
    Context mContext;

    public JsInteface(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;

    }

    @JavascriptInterface
    public void opayOrderPayOk(String str) {
        AppLog.print("opayOrderPayOk___" + str);
        try {
            if (!TextUtils.isEmpty(str)) {
                JSONObject payInfoJobj = new JSONObject(str);
                Gson gson = new Gson();
                String infoJson = payInfoJobj.optString("payInfo");
                PayInfo info = gson.fromJson(infoJson, PayInfo.class);
                if (info != null) {
                    int payType = info.getPayType();
                    if (payType == 101) {
                        payByWeiXin(info);
                    } else if (payType == 102) {
                        payByAliPay(info.getSign());
                    }
                } else {
                    ToastUtils.showToast(mContext, "支付信息不能为空");
                }
            } else {
                ToastUtils.showToast(mContext, "支付信息不能为空");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void payByAliPay(final String orderInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask((Activity) mContext);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = Constants.HANDLER_ALIPAY_RESULT;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };


        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

    }


    //微信支付
    private void payByWeiXin(PayInfo info) {
        IWXAPI api = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID);
        api.registerApp(Constants.APP_ID);
        PayReq payRequest = new PayReq();
        payRequest.appId = Constants.APP_ID;
        payRequest.partnerId = info.getPartnerid();
        payRequest.prepayId = info.getPrepayid();
        payRequest.packageValue = "Sign=WXPay";
        payRequest.nonceStr = info.getNoncestr();
        payRequest.timeStamp = info.getTimestamp();
        payRequest.sign = info.getSign();
        api.sendReq(payRequest);
    }

    @JavascriptInterface
    public void opaySelectAddress(String callBackMethod) {
        AppLog.print("cb method__" + callBackMethod);
        Constants.toActivityForR((Activity) mContext, SearchPoiActivity.class, null, Constants.REQUEST_ToSearchActivity);
    }

    @JavascriptInterface
    public void hideCloseWebPageBtn(boolean flag) {
        AppLog.print("hideCloseWebPageBtn___flag___" + flag);
        if (flag) {
            mHandler.sendEmptyMessage(Constants.HANDLER_HIDEN_CLOSE_BTN);
        } else {
            mHandler.sendEmptyMessage(Constants.HANDLER_SHOW_CLOSE_BTN);
        }

    }

    @JavascriptInterface
    public void postMessage(String jsonStr) throws JSONException {
        AppLog.print("postMessage___" + jsonStr);
        if (TextUtils.isEmpty(jsonStr)) {
            return;
        }
        JSONObject jobj = new JSONObject(jsonStr);
        if (jobj.has(ALERT_TYPE)) {
            Message msg = mHandler.obtainMessage();
            msg.obj = jobj;
            msg.what = Constants.HANDLER_JS_ALERT;
            mHandler.sendMessage(msg);

        } else if (jobj.has(CLOSE_EVENT)) {
            ((Activity)mContext).finish();

        } else {
            Gson gson = new Gson();
            JsBody jsBody = gson.fromJson(jsonStr, JsBody.class);
            Message msg = mHandler.obtainMessage();
            if (jsBody != null) {


                if (Go_Login_Page.equals(jsBody.getLogin_event())) {
                    msg.obj = jsBody.getLogin_event();
                    msg.what = Constants.HANDLER_JS_JUMP;
                    mHandler.sendMessage(msg);
                    return;
                }
                if (CLICK_REPAIR.equals(jsBody.getClick_event())) {
                    mHandler.sendEmptyMessage(Constants.HANDLER_REPAIR);
                    return;
                }
                msg.obj = jsBody;
                msg.what = Constants.HANDLER_JS_BODY;
                mHandler.sendMessage(msg);
            }
        }
    }


}