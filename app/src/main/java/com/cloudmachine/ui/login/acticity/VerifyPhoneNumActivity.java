package com.cloudmachine.ui.login.acticity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.autolayout.widgets.RadiusButtonView;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.recyclerbean.CheckNumBean;
import com.cloudmachine.struc.Member;
import com.cloudmachine.ui.login.contract.VerifyPhoneNumContract;
import com.cloudmachine.ui.login.model.VerifyPhoneNumModel;
import com.cloudmachine.ui.login.presenter.VerifyPhoneNumPresenter;
import com.cloudmachine.utils.ToastUtils;
import com.cloudmachine.utils.widgets.ClearEditTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * 项目名称：CloudMachine
 * 类描述：验证手机号页面
 * 创建人：shixionglu
 * 创建时间：2017/3/27 下午2:39
 * 修改人：shixionglu
 * 修改时间：2017/3/27 下午2:39
 * 修改备注：
 */

public class VerifyPhoneNumActivity extends BaseAutoLayoutActivity<VerifyPhoneNumPresenter, VerifyPhoneNumModel>
        implements VerifyPhoneNumContract.View, View.OnClickListener {


    @BindView(R.id.ll_back)
    LinearLayout      mLlBack;//返回按钮
    @BindView(R.id.phone_string)//手机号码
    ClearEditTextView mPhoneString;
    @BindView(R.id.validate_text)
    TextView          mValidateText;
    @BindView(R.id.validate_layout)
    RelativeLayout    mValidateLayout;
    @BindView(R.id.validate_code)
    ClearEditTextView mValidateCode;
    @BindView(R.id.code_layout)
    RelativeLayout    mCodeLayout;
    @BindView(R.id.invitation_code)
    ClearEditTextView mInvitationCode;
    @BindView(R.id.pwd_string)
    ClearEditTextView mPwdString;
    @BindView(R.id.agreement_text)
    TextView          mAgreementText;
    @BindView(R.id.agreement_layout)
    LinearLayout      mAgreementLayout;
    @BindView(R.id.ll_invitation)
    LinearLayout      mLlInvitation;
    @BindView(R.id.ll_password)
    LinearLayout      mLlPassword;
    private int mobileType = 2;
    private String mNickname;
    private String mUnionid;
    private String mOpenid;
    private int    mSex;
    private String mAccount;
    private String mInvitationValue;
    private String mPwd;
    private String mHeadimgurl;
    private RadiusButtonView mFindBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phonenum);
        ButterKnife.bind(this);
        getIntentData();
        initView();
        initData();
    }

    private void initData() {

        mAccount = mPhoneString.getText().toString().trim();
    }

    private void getIntentData() {

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mNickname = bundle.getString("nickname");
            mUnionid = bundle.getString("unionid");
            mOpenid = bundle.getString("openid");
            mHeadimgurl = bundle.getString("headimgurl");
            mSex = bundle.getInt("sex");
        }
    }

    private void initView() {

        switchLayout();
        mFindBtn = (RadiusButtonView) findViewById(R.id.find_btn);
        mFindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mPhoneString.getText().toString().trim())) {
                    ToastUtils.info("手机号码不能为空", true);
                    return;
                }
                if (TextUtils.isEmpty(mValidateCode.getText().toString().trim())) {
                    ToastUtils.info("验证码不能为空", true);
                }
                if (mobileType == 1) {
                    if (TextUtils.isEmpty(mPwdString.getText().toString().trim())) {
                        ToastUtils.info("密码不能为空", true);
                    } else {
                        mPwd = mPwdString.getText().toString().trim();
                    }
                } else {
                    mPwd = null;
                }
                if (TextUtils.isEmpty(mInvitationCode.getText().toString().trim())) {
                    mInvitationValue = mInvitationCode.getText().toString().trim();
                } else {
                    mInvitationValue = null;
                }

                mPresenter.bindWx(mUnionid, mOpenid, mAccount, mInvitationValue,mPwd
                        ,mNickname,mHeadimgurl,mHeadimgurl,mobileType);
            }
        });
        mValidateLayout.setOnClickListener(this);
    }

    private void switchLayout() {

        if (mobileType == 1) {
            mLlPassword.setVisibility(View.VISIBLE);
            mLlInvitation.setVisibility(View.VISIBLE);
        } else {
            mLlInvitation.setVisibility(View.GONE);
            mLlPassword.setVisibility(View.GONE);
        }
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }


    private void sendMessage() {
        final int count = 60;
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return count - aLong;
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mValidateLayout.setEnabled(false);
                        mValidateText.setTextColor(Color.BLUE);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        mValidateText.setText("获取验证码");
                        mValidateText.setTextColor(Color.GRAY);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        mValidateText.setText("重新发送" + aLong + "秒");
                        mValidateText.setTextColor(Color.BLUE);
                        mValidateLayout.setEnabled(true);
                    }
                });

    }

    //拿到绑定成功的信息（失败结果已在presenter页面先行进行处理）
    @Override
    public void returnWXBindMobile(String message) {
        ToastUtils.success(message, true);
    }

    @Override
    public void returnCheckNum(CheckNumBean checkNumBean) {
        //账号未注册
        if (checkNumBean.type == 1) {
            changeMobileState(false, 1);
        }
        //账号已注册，但是未绑定微信号
        else if (checkNumBean.type == 2) {
            changeMobileState(true, 2);
        }
        //账号已注册，绑定了其他微信
        else if (checkNumBean.type == 3) {
            changeMobileState(true, 3);
        }
        //获取验证码
        mPresenter.wxBindMobile(Long.parseLong(mPhoneString.getText().toString().trim()), 3);
    }

    @Override
    public void returnBindWx(Member member) {

    }


    //赋值状态，
    private void changeMobileState(boolean b, int mobileType) {
        this.mobileType = mobileType;
        switchLayout();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.validate_layout:
                if (TextUtils.isEmpty(mPhoneString.getText().toString().trim())) {
                    ToastUtils.info("手机号码不能为空", true);
                } else {
                    sendMessage();
                    mPresenter.checkNum(Long.parseLong(mPhoneString.getText().toString().trim()));
                }
                break;
        }
    }
}