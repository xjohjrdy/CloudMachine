package com.cloudmachine.ui.login.acticity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudmachine.R;
import com.cloudmachine.autolayout.widgets.RadiusButtonView;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.base.baserx.RxHelper;
import com.cloudmachine.base.baserx.RxSubscriber;
import com.cloudmachine.bean.Member;
import com.cloudmachine.bean.UserInfo;
import com.cloudmachine.cache.MySharedPreferences;
import com.cloudmachine.chart.utils.AppLog;
import com.cloudmachine.helper.MobEvent;
import com.cloudmachine.net.api.Api;
import com.cloudmachine.net.api.HostType;
import com.cloudmachine.net.task.ForgetPwdAsync;
import com.cloudmachine.net.task.GetMobileCodeAsync;
import com.cloudmachine.net.task.LoginAsync;
import com.cloudmachine.net.task.RegisterNewAsync;
import com.cloudmachine.ui.homepage.activity.QuestionCommunityActivity;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.MemeberKeeper;
import com.cloudmachine.utils.UMengKey;
import com.cloudmachine.utils.Utils;
import com.cloudmachine.utils.widgets.ClearEditTextView;
import com.cloudmachine.utils.widgets.Dialog.LoadingDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.Timer;
import java.util.TimerTask;

public class FindPasswordActivity extends BaseAutoLayoutActivity implements OnClickListener, Callback, TextWatcher {
    public static final String HASINVITATIONCODE = "hasInvitationCode";
    private Context mContext;
    private Handler mHandler;
    private LoadingDialog progressDialog;


    private TextView title_text, agreement_text;
    private ClearEditTextView phone_string;
    private ClearEditTextView validate_code;
    private ClearEditTextView pwd_string;
    private RadiusButtonView find_btn;

    private TextView validate_text;
    private static final int VALIDATENUM = 60;
    private int validate_num;
    private String message = "正在加载，请稍后";


    private Timer myTimer;

    //  1表示忘记密码 2 表示修改密码  3表示注册
    private int type;

    private View left_layout, validate_layout,
            pwd_layout, agreement_layout;
    private ClearEditTextView cet_invitationCode;
    private String inviteCode = "-1";
    LinearLayout invaiteCodeContainer;
    boolean hasInvatiteCode;
    private String phone;
    private String pwdStr;
    String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_change_pssword);
        this.mContext = this;
        mHandler = new Handler(this);
        type = getIntent().getIntExtra("type", 1);
        hasInvatiteCode = getIntent().getBooleanExtra(HASINVITATIONCODE, false);


        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, MobEvent.TIME_REGISTER);
    }

    @Override
    public void initPresenter() {

    }

    private void initView() {
        invaiteCodeContainer = (LinearLayout) findViewById(R.id.invitation_code_cotainer);
        cet_invitationCode = (ClearEditTextView) findViewById(R.id.invitation_code);
        agreement_layout = findViewById(R.id.agreement_layout);
        left_layout = findViewById(R.id.left_layout);
        left_layout.setOnClickListener(this);
        title_text = (TextView) findViewById(R.id.title_text);
        validate_text = (TextView) findViewById(R.id.validate_text);
        agreement_text = (TextView) findViewById(R.id.agreement_text);
        agreement_text.setOnClickListener(this);

//		backImg  = (ImageView)findViewById(R.id.backImg);
        phone_string = (ClearEditTextView) findViewById(R.id.phone_string);
        validate_code = (ClearEditTextView) findViewById(R.id.validate_code);
        validate_layout = findViewById(R.id.validate_layout);
        validate_layout.setOnClickListener(this);
        pwd_layout = findViewById(R.id.pwd_layout);
        pwd_string = (ClearEditTextView) findViewById(R.id.pwd_string);
        phone_string.addTextChangedListener(this);
        validate_code.addTextChangedListener(this);
        pwd_string.addTextChangedListener(this);
        if (hasInvatiteCode) {
            invaiteCodeContainer.setVisibility(View.GONE);
        }
        find_btn = (RadiusButtonView) findViewById(R.id.find_btn);
        find_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                savePwd();
            }
        });

        switch (type) {
            case 1:
                title_text.setText("忘记密码");
                find_btn.setText("提交");
                pwd_layout.setVisibility(View.VISIBLE);
                agreement_layout.setVisibility(View.GONE);
                cet_invitationCode.setVisibility(View.GONE);
                break;
            case 2:
                title_text.setText("修改密码");
                pwd_layout.setVisibility(View.VISIBLE);
                agreement_layout.setVisibility(View.GONE);
                cet_invitationCode.setVisibility(View.GONE);
                break;
            case 3:
                MobclickAgent.onPageStart(UMengKey.time_register);
                title_text.setText("输入手机号，快速注册");
                find_btn.setText("注册");
                pwd_layout.setVisibility(View.VISIBLE);
                agreement_layout.setVisibility(View.VISIBLE);
                cet_invitationCode.setVisibility(View.VISIBLE);
                break;
            default:
                find_btn.setText("注册");
                title_text.setText("输入手机号，快速注册");
                pwd_layout.setVisibility(View.VISIBLE);
                agreement_layout.setVisibility(View.VISIBLE);
                break;
        }


        if (MemeberKeeper.getOauth(FindPasswordActivity.this) != null) {
            phone_string.setText(MemeberKeeper.getOauth(FindPasswordActivity.this).getMobile());
        }
    }


    private void show() {
        if (progressDialog == null) {
            progressDialog = LoadingDialog.createDialog(this);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void disMiss() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_layout:
                finish();
                break;
            case R.id.validate_layout:
                if (null == myTimer) {
                    phoneNum = phone_string.getText().toString();
                    if (phoneNum.length() == 11) {
                        if (validate_num == 0) {
                            show();
                            String codeType = "1";
                            if (type == 1 || type == 2) {
                                codeType = "2";
                            }
                            //  1表示忘记密码 2 表示修改密码  3表示注册
                            new GetMobileCodeAsync(phoneNum, codeType, mContext, mHandler).execute();
                        }
                    } else {
                        Toast.makeText(FindPasswordActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();

                    }
                }

                break;
            case R.id.agreement_text:
                Bundle bundle = new Bundle();
                bundle.putString(QuestionCommunityActivity.H5_URL, Constants.URL_H5_ARGUMENT);
                bundle.putString(QuestionCommunityActivity.H5_TITLE, "协议条款");
                Constants.toActivity(this, QuestionCommunityActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    private void savePwd() {
        String code = validate_code.getText().toString().trim();
        phone = phone_string.getText().toString().trim();
        pwdStr = pwd_string.getText().toString().trim();
        if (type == 3) {
            if (TextUtils.isEmpty(phone)) {
                Constants.ToastAction("请输入手机号码");
            } else if (TextUtils.isEmpty(code)) {
                Constants.ToastAction("请输入验证码");
            } else if (TextUtils.isEmpty(pwdStr)) {
                Constants.ToastAction("请输入密码");
            } else if (pwdStr.length() < 6) {
                Constants.ToastAction("新密码长度必须大于6位");
            } else {
                //在这里判断是否有邀请码
                if (!TextUtils.isEmpty(cet_invitationCode.getText().toString().trim())) {
                    inviteCode = cet_invitationCode.getText().toString().trim();
                } else {
                    inviteCode = "-1";
                }
                MobclickAgent.onEvent(this, MobEvent.COUNT_LOGIN);
                new RegisterNewAsync(phone, Utils.getPwdStr(pwdStr), code, mContext, mHandler, inviteCode).execute();
            }
        } else {
            if (TextUtils.isEmpty(phone)) {
                Constants.ToastAction("请输入手机号码");
            } else if (TextUtils.isEmpty(code)) {
                Constants.ToastAction("请输入验证码");
            } else if (pwdStr.length() < 6) {
                Constants.ToastAction("新密码长度必须大于6位");
            } else {
//				if(type == 1){
                new ForgetPwdAsync(phone,
                        Utils.getPwdStr(pwdStr),
                        code, mContext, mHandler).execute();
//				}else if(type == 2){
                    /*new UpdatePwdAsync(Utils.getPwdStr(old_password.getText().toString()),
                            Utils.getPwdStr(new_passwrod.getText().toString()),
							Utils.getPwdStr(confirm_passwrod.getText().toString()),mContext,mHandler).execute();*/
//				}
            }
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        AppLog.print("findPassword afterTextChanged___");
        String str1 = phone_string.getText().toString();
        String str2 = validate_code.getText().toString();
        String str3 = pwd_string.getText().toString();
        String text = validate_text.getText().toString();

        if ("获取验证码".equals(text)) {
            if (str1.length() > 0) {
                validate_text.setEnabled(true);
            } else {
                validate_text.setEnabled(false);
            }
        } else {
            if (!str1.equals(phoneNum)) {
                stopTimer();
            }

        }

        if (str1.length() > 0 && str2.length() > 0 && str3.length() > 0) {
            find_btn.setTextColor(getResources().getColor(R.color.cor15));
        } else {
            find_btn.setTextColor(getResources().getColor(R.color.cor2015));
        }
    }

    class ListenerTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = Constants.HANDLER_TIMER;
            mHandler.sendMessage(message);
        }

    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            case Constants.HANDLER_TIMER:
                validate_num++;
                if ((VALIDATENUM - validate_num) < 0) {
                    stopTimer();
                } else {
                    if (validate_text.isEnabled()) {


                        validate_text.setEnabled(false);
                    }
                    validate_text.setText("获取验证码(" + (VALIDATENUM - validate_num) + ")");
                }
                break;
            case Constants.HANDLER_GETCODE_SUCCESS:
                disMiss();
                Toast.makeText(FindPasswordActivity.this, "验证码已发送请注意查收", Toast.LENGTH_SHORT).show();
                validate_text.setText("" + (VALIDATENUM - validate_num));
                if (null != myTimer) {
                    myTimer.cancel();
                    myTimer = null;
                }
                myTimer = new Timer(true);
                myTimer.schedule(new ListenerTimerTask(), 100, 1000);
                break;
            case Constants.HANDLER_GETCODE_FAIL:
                disMiss();
                Toast.makeText(FindPasswordActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                break;
            case Constants.HANDLER_FORGETPWD_SUCCESS:
            case Constants.HANDLER_UPDATEPWD_SUCCESS:
                disMiss();
                Toast.makeText(FindPasswordActivity.this, "修改密码成功", Toast.LENGTH_SHORT).show();
//			MemeberKeeper.clearOauth(this);
                finish();
                break;
            case Constants.HANDLER_FORGETPWD_FAIL:
            case Constants.HANDLER_UPDATEPWD_FAIL:
            case Constants.HANDLER_REGISTER_FAIL:
                disMiss();
                Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                pwd_string.setText("");
                validate_code.setText("");
                break;
            case Constants.HANDLER_REGISTER_SUCCESS:
//                Map<String, String> paramsMap = new HashMap<>();
//                paramsMap.put(phone, pwdStr);
//                mRxManager.post(LoginActivity.RX_LOGIN, paramsMap);
                Toast.makeText(FindPasswordActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                new LoginAsync(phone, pwdStr, mContext, mHandler).execute();
                break;
            case Constants.HANDLER_LOGIN_SUCCESS:
                Constants.isGetScore = true;
                MySharedPreferences.setSharedPInt(MySharedPreferences.key_login_type, 0);
                mMember = (Member) msg.obj;
                if (mMember != null) {
                    excamMaster(mMember.getId());
                } else {
                    loginBack();
                }
                break;
            case Constants.HANDLER_LOGIN_FAIL:
                loginBack();
                break;


        }
        return false;
    }

    private void stopTimer() {
        validate_text.setEnabled(true);
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
        validate_num = 0;
        validate_text.setText("获取验证码");
    }

    Member mMember;

    private void excamMaster(Long id) {

        mRxManager.add(Api.getDefault(HostType.HOST_CLOUDM_ASK).excamMaster(id)
                .compose(RxHelper.<UserInfo>handleResult())
                .subscribe(new RxSubscriber<UserInfo>(mContext, false) {
                    @Override
                    protected void _onNext(UserInfo userInfo) {
                        Long wjdsId = userInfo.userinfo.id;
                        Long status = userInfo.userinfo.status;
                        Long role_id = userInfo.userinfo.role_id;
                        mMember.setWjdsId(wjdsId);
                        mMember.setWjdsStatus(status);
                        mMember.setWjdsRole_id(role_id);
                        mMember.setNum(2L);
                        MemeberKeeper.saveOAuth(mMember, mContext);
                        loginBack();
                    }

                    @Override
                    protected void _onError(String message) {
                        MemeberKeeper.saveOAuth(mMember, mContext);
                        loginBack();
                    }
                }));
    }

    public void loginBack() {
        disMiss();
        Intent intent = new Intent();
        intent.putExtra(Constants.MC_MEMBER, mMember);
        setResult(FP_LOGIN, intent);
        finish();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd(UMengKey.time_register);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (null != myTimer) {
            myTimer.cancel();
            myTimer = null;
        }
    }

    public static final int FP_LOGIN = 0x891;


}