package com.cloudmachine.ui.home.model;

import com.cloudmachine.base.baserx.RxHelper;
import com.cloudmachine.base.baserx.RxSchedulers;
import com.cloudmachine.base.baserx.RxSubscriber;
import com.cloudmachine.base.bean.BaseRespose;
import com.cloudmachine.net.api.Api;
import com.cloudmachine.net.api.HostType;
import com.cloudmachine.ui.home.contract.OperateContact;
import com.google.gson.JsonObject;

import rx.Observable;

/**
 * Created by xiaojw on 2018/7/5.
 */

public class OperateModel implements OperateContact.Model {
    //internalTimeMinutes参数为1表示1分钟
    @Override
    public Observable<String> getVerifyCode(String taskId) {

        return Api.getDefault(HostType.HOST_LARK).retryOperatorCode(taskId,1).compose(RxHelper.<String>handleResult());
    }

    @Override
    public Observable<String> checkVerifyCode(String taskId, String smsCode) {
        return Api.getDefault(HostType.HOST_LARK).checkOperatorCode(taskId,smsCode).compose(RxHelper.<String>handleResult());
    }

    @Override
    public Observable<JsonObject> authOperator(String servicePwd) {

        return Api.getDefault(HostType.HOST_LARK).authOperator(servicePwd);
    }
}
