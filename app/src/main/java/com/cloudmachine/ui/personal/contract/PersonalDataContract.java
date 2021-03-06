package com.cloudmachine.ui.personal.contract;

import com.cloudmachine.base.BaseModel;
import com.cloudmachine.base.BasePresenter;
import com.cloudmachine.base.BaseView;

import rx.Observable;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/4/7 下午4:34
 * 修改人：shixionglu
 * 修改时间：2017/4/7 下午4:34
 * 修改备注：
 */

public interface PersonalDataContract {

    interface Model extends BaseModel {
        Observable<String> modifyMemberInfo(String nickName,String logo);
    }

    interface View extends BaseView {
        void returnModifyNickName();

        void returnModifyLogo();
    }

    abstract static class Presenter extends BasePresenter<View,Model> {
        public abstract void modifyMemberInfo(String nickName,String logo);
    }
}
