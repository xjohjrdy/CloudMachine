package com.cloudmachine.ui.home.activity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.base.baserx.RxHelper;
import com.cloudmachine.base.baserx.RxSubscriber;
import com.cloudmachine.bean.AuthBean;
import com.cloudmachine.net.api.Api;
import com.cloudmachine.net.api.ApiConstants;
import com.cloudmachine.net.api.HostType;
import com.cloudmachine.utils.CommonUtils;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.DensityUtil;
import com.cloudmachine.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoAuthActivity extends BaseAutoLayoutActivity implements View.OnClickListener {
    public static final int BNS_TYPE_ENGINEER = 1;
    public static final int BNS_TYPE_INCOME = 2;
    public static final int BNS_TYPE_DEVICE = 3;
    public static final int BNS_TYPE_ADDRESS = 4;
    public static final int BNS_TYPE_CONTACT = 5;
    public static final String IS_NEW_ADD = "is_new_add";
    @BindView(R.id.info_auth_name_tv)
    TextView infoAuthNameTv;
    @BindView(R.id.info_auth_idcard_tv)
    TextView infoAuthIdcardTv;
    @BindView(R.id.info_auth_idcard_status)
    TextView idcardStatus;
    @BindView(R.id.info_auth_idcard_container)
    FrameLayout infoAuthIdcardContainer;
    @BindView(R.id.info_auth_operator_status)
    TextView operatorStatus;
    @BindView(R.id.info_auth_operator_container)
    FrameLayout infoAuthOperatorContainer;
    @BindView(R.id.info_auth_bank_status)
    TextView infoAuthBankStatus;
    @BindView(R.id.info_auth_bank_cotainer)
    FrameLayout infoAuthBankCotainer;
    @BindView(R.id.engineer_contract_status)
    TextView engineerContractStatus;
    @BindView(R.id.info_auth_engineer_contract)
    FrameLayout infoAuthEngineerContract;
    @BindView(R.id.personal_income_status)
    TextView personalIncomeStatus;
    @BindView(R.id.info_auth_personal_income)
    FrameLayout infoAuthPersonalIncome;
    @BindView(R.id.device_owner_status)
    TextView deviceOwnerStatus;
    @BindView(R.id.info_auth_device_owner)
    FrameLayout infoAuthDeviceOwner;
    @BindView(R.id.bankaccount_model_container)
    LinearLayout modelContainer;
    @BindView(R.id.bankaccount_container)
    FrameLayout bankAccountContainer;
    @BindView(R.id.info_auth_relation_container)
    FrameLayout relationContainer;
    @BindView(R.id.info_auth_relation_status)
    TextView relationStatus;
    @BindView(R.id.info_auth_address)
    FrameLayout addressContainer;
    @BindView(R.id.address_status)
    TextView addressStatus;
    String uniqueNo;
    String realName;
    int status1;
    int lStatus0, lStatus1, lStatus2, lStatus3, lStatus4, lStatus5, lStatus6, lStatus7;
    boolean isFResume = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_auth);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        mRxManager.add(Api.getDefault(HostType.HOST_LARK).getAuthInfo().compose(RxHelper.<AuthBean>handleResult()).subscribe(new RxSubscriber<AuthBean>(mContext) {
            @Override
            protected void _onNext(AuthBean authBean) {
                uniqueNo = authBean.getUserUniqueNo();
                realName = authBean.getRealName();
                infoAuthNameTv.setText(realName);
                infoAuthIdcardTv.setText(authBean.getIdCardNo());
                int status0 = authBean.getIdentityCheckStatus();
                status1 = authBean.getRelationAuthStatus();
                int status2 = authBean.getOperatorAuthorizedStatus();
                int status3 = authBean.getCardFourElementAuthStatus();
                int status4 = authBean.getLicenceCheckStatus();
                int status5 = authBean.getIncomeCheckStatus();
                int status6 = authBean.getMachineCheckStatus();
                int status7 = authBean.getResideAddressCheckStatus();
                setAuthStatus(status0, authBean.getIdentityCheckStatusTxt(), idcardStatus);
                setAuthStatus1(status1, authBean.getRelationAuthStatusTxt(), relationStatus);//0未完善1已完善2已过期
                setAuthStatus1(status2, authBean.getOperatorAuthorizedStatusTxt(), operatorStatus);//0未授权1已授权
                setAuthStatus1(status3, authBean.getCardFourElementAuthStatusTxt(), infoAuthBankStatus);//0未验证1已验证
                setAuthStatus(status4, authBean.getLicenceCheckStatusTxt(), engineerContractStatus);
                setAuthStatus(status5, authBean.getIncomeCheckStatusTxt(), personalIncomeStatus);
                setAuthStatus(status6, authBean.getMachineCheckStatusTxt(), deviceOwnerStatus);
                setAuthStatus(status7, authBean.getResideAddressCheckStatusTxt(), addressStatus);
                int platformStatus = authBean.getPlatformStatus();
                if (platformStatus == 1) {//资金账号已开户
                    modelContainer.setVisibility(View.VISIBLE);
                    bankAccountContainer.setTag(ApiConstants.APPRzgj);
                } else {
                    if ((status0 = status1 = status2 = status3) == 1 && (status4 = status5 = status6 = status7) == 2) {
                        obtainOpenAccountUrl();
                    }
                }
                if (isFResume) {
                    isFResume = false;
                } else {
                    if (lStatus0 != status0 || lStatus1 != status1 || lStatus2 != status2 || lStatus3 != status3 || lStatus4 != status4 || lStatus5 != status5 || lStatus6 != status6 || lStatus7 != status7) {
                        setResult(RES_UPDATE_TIKCET);
                    }
                }
                lStatus0 = status0;
                lStatus1 = status1;
                lStatus2 = status2;
                lStatus3 = status3;
                lStatus4 = status4;
                lStatus5 = status5;
                lStatus6 = status6;
                lStatus7 = status7;

            }

            @Override
            protected void _onError(String message) {
                ToastUtils.showToast(mContext, message);

            }
        }));
    }

    private void obtainOpenAccountUrl() {
        mRxManager.add(Api.getDefault(HostType.HOST_LARK).getOpenAccountUrl().compose(RxHelper.<String>handleResult()).subscribe(new RxSubscriber<String>(mContext) {
            @Override
            protected void _onNext(String s) {
                if (!TextUtils.isEmpty(s)) {
                    bankAccountContainer.setTag(s);
                    modelContainer.setVisibility(View.VISIBLE);
                } else {
                    modelContainer.setVisibility(View.GONE);
                }
            }

            @Override
            protected void _onError(String message) {

            }
        }));


    }

    public void setAuthStatus(int status, String statusTxt, TextView tv) {
        ViewGroup container = (ViewGroup) tv.getParent();
        switch (status) {
            case 0://待审
            case 3://失败
                setDefaultStausView(tv, container);
                break;
            case 1://审核中
                if (tv == idcardStatus) {
                    container.setEnabled(false);
                }
                tv.setTextColor(getResources().getColor(R.color.c_ff8901));
                tv.setCompoundDrawables(null, null, null, null);
                tv.setCompoundDrawablePadding(0);
                tv.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case 2://完成
                if (tv == idcardStatus) {
                    container.setEnabled(false);
                }
                setCompleteStatusView(tv, container);
                break;
        }
        tv.setText(statusTxt);
    }

    public void setAuthStatus1(int status, String statusTxt, TextView tv) {
        ViewGroup container = (ViewGroup) tv.getParent();
        switch (status) {
            case 0:
            case 2:
                setDefaultStausView(tv, container);
                break;
            case 1:
                container.setEnabled(false);
                setCompleteStatusView(tv, container);
                break;
//            case 3:
//                if (tv == operatorStatus) {
//                    statusText = "审核中";
//                    container.setEnabled(false);
//                }
//                tv.setTextColor(getResources().getColor(R.color.c_ff8901));
//                tv.setCompoundDrawables(null, null, null, null);
//                tv.setCompoundDrawablePadding(0);
//                tv.setBackgroundColor(getResources().getColor(R.color.transparent));
//                break;
        }
        tv.setText(statusTxt);

    }


    private void setCompleteStatusView(TextView tv, ViewGroup container) {
        tv.setTextColor(getResources().getColor(R.color.c_64baa4));
        Drawable leftDrawable = getResources().getDrawable(R.drawable.ic_item_status_left);
        leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
        tv.setCompoundDrawablePadding(DensityUtil.dip2px(this, 2));
        tv.setCompoundDrawables(leftDrawable, null, null, null);
        int left = DensityUtil.dip2px(this, 4);
        int top = DensityUtil.dip2px(this, 2);
        tv.setPadding(left, top, left, top);
        tv.setBackground(getResources().getDrawable(R.drawable.bg_info_auth_status));
    }

    private void setDefaultStausView(TextView tv, ViewGroup container) {
        container.setEnabled(true);
        tv.setTextColor(getResources().getColor(R.color.c_ff8901));
        Drawable rightDrawable = getResources().getDrawable(R.drawable.arrow_right);
        rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(), rightDrawable.getIntrinsicHeight());
        tv.setCompoundDrawables(null, null, rightDrawable, null);
        tv.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.pad1));
        tv.setBackgroundColor(getResources().getColor(R.color.transparent));
    }


    @Override
    public void initPresenter() {

    }

    @OnClick({R.id.info_auth_address, R.id.info_auth_relation_container, R.id.bankaccount_container, R.id.info_auth_idcard_container, R.id.info_auth_operator_container, R.id.info_auth_bank_cotainer, R.id.info_auth_engineer_contract, R.id.info_auth_personal_income, R.id.info_auth_device_owner})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info_auth_address:
                Bundle addressData = new Bundle();
                addressData.putString(Constants.UNIQUEID, uniqueNo);
                addressData.putBoolean(IS_NEW_ADD, lStatus7 == 0);
                Constants.toActivity(this, AddressActivity.class, addressData);
                break;

            case R.id.info_auth_relation_container://联系人
                Bundle contactData = new Bundle();
                contactData.putString(Constants.UNIQUEID, uniqueNo);
                contactData.putBoolean(IS_NEW_ADD, lStatus1 == 0);
                Constants.toActivity(this, ContactActivity.class, contactData);
                break;
            case R.id.info_auth_idcard_container:
                Bundle idData = new Bundle();
                idData.putString(Constants.UNIQUEID, uniqueNo);
                Constants.toActivity(this, IdCardHandActivity.class, idData);
                break;
            case R.id.info_auth_operator_container:
                if (status1 == 1) {
                    Constants.toActivity(this, OperateActivity.class, null);
                } else {

                    CommonUtils.showDialog(mContext, "您尚未完善联系人信息，请先完善", "取消", "去完善", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle contactData = new Bundle();
                            contactData.putString(Constants.UNIQUEID, uniqueNo);
                            contactData.putBoolean(IS_NEW_ADD, lStatus1 == 0);
                            Constants.toActivity(InfoAuthActivity.this, ContactActivity.class, null);
                        }
                    });
                }

                break;
            case R.id.info_auth_bank_cotainer:
                Bundle bvData = new Bundle();
                bvData.putString(Constants.REAL_NAME, realName);
                Constants.toActivity(this, BankVerifyctivity.class, bvData);
                break;
            case R.id.info_auth_engineer_contract:
                Bundle egData = new Bundle();
                egData.putString(Constants.PAGET_TYPE, IncomeProofActivity.ENGINEER_CONTRACT);
                egData.putString(Constants.UNIQUEID, uniqueNo);
                egData.putBoolean(IS_NEW_ADD, lStatus4 == 0);
                Constants.toActivity(this, IncomeProofActivity.class, egData);
                break;
            case R.id.info_auth_personal_income:
                Bundle piData = new Bundle();
                piData.putString(Constants.UNIQUEID, uniqueNo);
                piData.putBoolean(IS_NEW_ADD, lStatus5 == 0);
                piData.putString(Constants.PAGET_TYPE, IncomeProofActivity.INCOME_PROOF);
                Constants.toActivity(this, IncomeProofActivity.class, piData);
                break;
            case R.id.info_auth_device_owner:
                Bundle deData = new Bundle();
                deData.putString(Constants.UNIQUEID, uniqueNo);
                Constants.toActivity(this, MachineOwnershipActivity.class, deData);
                break;
            case R.id.bankaccount_container:
                Object obj = view.getTag();
                if (obj != null) {
                    Bundle bkData = new Bundle();
                    bkData.putString(QuestionCommunityActivity.H5_URL, (String)obj);
                    bkData.putString(QuestionCommunityActivity.H5_TITLE, "融资管家");
                    Constants.toActivity(this, QuestionCommunityActivity.class, bkData);
                }
                break;
        }
    }

}
