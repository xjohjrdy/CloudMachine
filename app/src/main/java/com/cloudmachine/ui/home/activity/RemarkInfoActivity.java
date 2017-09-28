package com.cloudmachine.ui.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.ui.home.contract.RemarkInfoContract;
import com.cloudmachine.ui.home.model.RemarkInfoModel;
import com.cloudmachine.ui.home.model.RoleBean;
import com.cloudmachine.ui.home.presenter.RemarkInfoPresenter;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.widgets.wheelview.OnWheelScrollListener;
import com.cloudmachine.utils.widgets.wheelview.WheelView;
import com.cloudmachine.widget.CommonTitleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RemarkInfoActivity extends BaseAutoLayoutActivity<RemarkInfoPresenter, RemarkInfoModel> implements RemarkInfoContract.View, OnWheelScrollListener, TextWatcher {
    public static final String REMARK_INFO = "remark_info";
    public static final String ROLEIDS = "roleIdS";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ROLEREMARK = "roleRemark";
    public static final String ROLE = "role";
    public static final String MEMBER_ID = "memberId";
    @BindView(R.id.nickname_edt)
    EditText nicknameEdt;
    @BindView(R.id.remarkname_edt)
    EditText remarknameEdt;
    @BindView(R.id.role_container)
    FrameLayout roleContainer;
    @BindView(R.id.remarkinfo_titleview)
    CommonTitleView remarkinfoTitleview;
    @BindView(R.id.remarkinfo_role_name)
    TextView roleNameTv;
    int selectIndex;
    ArrayList<RoleBean> mRoleList;
    HashMap<String, Long> mRoleMap = new HashMap<>();
    long deviceId;
    String role;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark_info);
        ButterKnife.bind(this);
        initView();
        mPresenter.updateRoleListView();
    }


    private void initView() {
        deviceId = getIntent().getLongExtra(Constants.P_DEVICEID, 0);
        remarkinfoTitleview.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        final long id = intent.getLongExtra(ID, 0);
        final long memberId = intent.getLongExtra(MEMBER_ID, 0);
        String name = intent.getStringExtra(NAME);
        role = intent.getStringExtra(ROLE);
        String roleRemark = intent.getStringExtra(ROLEREMARK);
        final long rolesIds = intent.getLongExtra(ROLEIDS, 0);
        remarkinfoTitleview.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remark = remarknameEdt.getText().toString();
                String roleName = roleNameTv.getText().toString();
                long roleId;
                if (mRoleMap.containsKey(roleName)) {
                    roleId = mRoleMap.get(roleName);
                } else {
                    roleId = rolesIds;
                }
                mPresenter.updateRemarkInfo(id, memberId, deviceId, remark, roleId);
            }
        });
        nicknameEdt.setText(name);
        remarknameEdt.setText(roleRemark);
        roleNameTv.setText(role);
        remarknameEdt.addTextChangedListener(this);
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @OnClick({R.id.nickname_edt, R.id.remarkname_edt, R.id.role_container})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nickname_edt:
                break;
            case R.id.remarkname_edt:
                break;
            case R.id.role_container:
                String roleName = roleNameTv.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(RoleSelActiviy.ROLE_LIST, mRoleList);
                bundle.putString(RoleSelActiviy.ROLE_NAME, roleName);
                Constants.toActivityForR(this, RoleSelActiviy.class, bundle);
                break;
        }
    }

    @Override
    public void onScrollingStarted(WheelView wheel) {
    }

    @Override
    public void onScrollingFinished(WheelView wheel) {
        selectIndex = wheel.getCurrentItem();
    }

    @Override
    public void returnRoleListView(List<RoleBean> roleList) {
        mRoleList = (ArrayList<RoleBean>) roleList;
        for (RoleBean bean : mRoleList) {
            mRoleMap.put(bean.getType(), bean.getId());
        }
    }

    @Override
    public void updateRemarkSuccess() {
        finish();
    }

    @Override
    public void updateRemarkFailed() {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String content = s.toString();
        if (content.length() > 0) {
            remarkinfoTitleview.setRighteTextEnalbe(true);
            remarkinfoTitleview.setRightTextColor(getResources().getColor(R.color.cor9));
        } else {
            remarkinfoTitleview.setRighteTextEnalbe(false);
            remarkinfoTitleview.setRightTextColor(getResources().getColor(R.color.cor10));
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            RoleBean bean = data.getParcelableExtra(RoleSelActiviy.ROLE_BEAN);
            if (bean != null) {
                String roleType = bean.getType();
                if (!role.equals(roleType)) {
                    remarkinfoTitleview.setRighteTextEnalbe(true);
                    remarkinfoTitleview.setRightTextColor(getResources().getColor(R.color.cor9));
                } else {
                    remarkinfoTitleview.setRighteTextEnalbe(false);
                    remarkinfoTitleview.setRightTextColor(getResources().getColor(R.color.cor10));
                }
                roleNameTv.setText(roleType);
            }
        }


    }
}