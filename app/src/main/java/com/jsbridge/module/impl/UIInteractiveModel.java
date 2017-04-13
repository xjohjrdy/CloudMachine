package com.jsbridge.module.impl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.cloudmachine.activities.WanaCloudBox;
import com.cloudmachine.main.MainActivity;
import com.cloudmachine.ui.homepage.activity.InsuranceActivity;
import com.cloudmachine.ui.homepage.activity.QuestionCommunityActivity;
import com.cloudmachine.ui.personal.activity.AskMyQuestionActicity;
import com.cloudmachine.ui.question.activity.QuestionActivity;
import com.cloudmachine.utils.Constants;
import com.jsbridge.core.JsCallback;
import com.jsbridge.module.IModule;

import org.json.JSONObject;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/3/5 下午11:29
 * 修改人：shixionglu
 * 修改时间：2017/3/5 下午11:29
 * 修改备注：
 */

public class UIInteractiveModel implements IModule {

    public static void showBuyBtn(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");
            WanaCloudBox wanaCloudBoxActivity = (WanaCloudBox) activity;
            switch (action) {
                case "show":
                    wanaCloudBoxActivity.showBuyBtn("show");
                    break;
                case "hide":
                    wanaCloudBoxActivity.showBuyBtn("hide");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showBackBtn(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");
            WanaCloudBox wanaCloudBoxActivity = (WanaCloudBox) activity;
            switch (action) {
                case "show":
                    wanaCloudBoxActivity.showBackBtn("show");
                    break;
                case "hide":
                    wanaCloudBoxActivity.showBackBtn("hide");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backPage(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");
            InsuranceActivity insuranceActivity = (InsuranceActivity) activity;
            switch (action) {
                case "back":
                    insuranceActivity.backPage("back");
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goLoginPage(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");
            QuestionCommunityActivity questionCommunityActivity = (QuestionCommunityActivity) activity;
            switch (action) {
                case "login":
                    Constants.toLoginActivity(questionCommunityActivity,3);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeAskPage(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");

            if (activity instanceof QuestionCommunityActivity) {
                QuestionCommunityActivity questionCommunityActivity = (QuestionCommunityActivity) activity;
                switch (action) {
                    case "close":
                        questionCommunityActivity.finish();
                        break;
                }
            } else if (activity instanceof AskMyQuestionActicity) {
                switch (action) {
                    case "close":
                        AskMyQuestionActicity askMyQuestionActicity = (AskMyQuestionActicity) activity;
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.P_MainActivity_Fragment_Id,3);
                        Constants.toActivity(askMyQuestionActicity, MainActivity.class,bundle,true);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void goWebViewMyqPage(AppCompatActivity activity, WebView webView, JSONObject params, final JsCallback callback) {
        try {
            String action = params.getString("action");
            QuestionActivity questionActivity = (QuestionActivity) activity;
            switch (action) {
                case "myPage":
                    Bundle bundle = new Bundle();
                    bundle.putString("url","http://h5.test.cloudm.com/n/ask_myq");
                    Constants.toActivity(questionActivity, AskMyQuestionActicity.class,bundle,true);
                    questionActivity.finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
