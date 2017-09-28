package com.cloudmachine.ui.home.contract;

import com.cloudmachine.base.BaseModel;
import com.cloudmachine.base.BasePresenter;
import com.cloudmachine.base.BaseView;
import com.cloudmachine.bean.HomeBannerBean;
import com.cloudmachine.bean.UnReadMessage;
import com.cloudmachine.ui.home.model.PopItem;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by xiaojw on 2017/5/22.
 */

public interface HomeContract {
    interface View extends BaseView {

        void updateMessageCount(int count);


        void updatePromotionInfo(List<PopItem> items);

        void updateActivitySize(int count);


    }

    interface Model extends BaseModel {
        Observable<UnReadMessage> getMessageUntreatedCount(long memberId);

        Observable<List<PopItem>> getPromotionModel(long memberId);
        Observable<JsonObject>   getH5ConfigInfo();
        Observable<ArrayList<HomeBannerBean>> getHomeBannerInfo();

    }

    public abstract class Presenter extends BasePresenter<HomeContract.View, HomeContract.Model> {
        public abstract void updateUnReadMessage(long memberId);


        public abstract void getPromotionInfo(long memberId);
        public abstract  void getH5ConfigInfo();
        public abstract void getHomeBannerInfo();

    }


}