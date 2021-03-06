package com.cloudmachine.ui.home.model;

import com.cloudmachine.base.baserx.RxHelper;
import com.cloudmachine.base.bean.BaseRespose;
import com.cloudmachine.bean.VideoBean;
import com.cloudmachine.net.api.Api;
import com.cloudmachine.net.api.HostType;
import com.cloudmachine.ui.home.contract.WorkVideoContract;

import rx.Observable;

/**
 * Created by xiaojw on 2018/3/9.
 */

public class WorkVideoModel  implements WorkVideoContract.Model{


    @Override
    public Observable<BaseRespose<String>> videoUpload(String deviceId, String id) {
        return Api.getDefault(HostType.HOST_LARK).videoUpload(deviceId,id).compose(RxHelper.<String>handleBaseResult());
    }

    @Override
    public Observable<VideoBean> getVideoList(String deviceId,String startTime,String endTime) {
        return Api.getDefault(HostType.HOST_LARK).getVideoList(deviceId,startTime,endTime).compose(RxHelper.<VideoBean>handleResult());
    }
}
