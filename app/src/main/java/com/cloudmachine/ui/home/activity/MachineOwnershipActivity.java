package com.cloudmachine.ui.home.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.adapter.BaseRecyclerAdapter;
import com.cloudmachine.adapter.MachineListAdapter;
import com.cloudmachine.adapter.decoration.LineItemDecoration;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.base.baserx.RxHelper;
import com.cloudmachine.base.baserx.RxSubscriber;
import com.cloudmachine.bean.DeviceAuthItem;
import com.cloudmachine.helper.UserHelper;
import com.cloudmachine.net.api.Api;
import com.cloudmachine.net.api.HostType;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MachineOwnershipActivity extends BaseAutoLayoutActivity implements BaseRecyclerAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    MachineListAdapter mAdapter;
    TextView mEmptyTv;
    String uniqueNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_ownership);
        initView();

    }

    private void initView() {
        uniqueNo = getIntent().getStringExtra(Constants.UNIQUEID);
        mRecyclerView = (RecyclerView) findViewById(R.id.machine_owership_rlv);
        mEmptyTv = (TextView) findViewById(R.id.machine_owership_empty);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new LineItemDecoration(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMachineList();
    }

    private void getMachineList() {
        mRxManager.add(Api.getDefault(HostType.HOST_LARK).getDeviceAuthList().compose(RxHelper.<List<DeviceAuthItem>>handleResult()).subscribe(new RxSubscriber<List<DeviceAuthItem>>(mContext) {
            @Override
            protected void _onNext(List<DeviceAuthItem> deviceAuthItems) {
                if (deviceAuthItems != null && deviceAuthItems.size() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyTv.setVisibility(View.GONE);
                    if (mAdapter == null) {
                        mAdapter = new MachineListAdapter(mContext, deviceAuthItems);
                        mAdapter.setOnItemClickListener(MachineOwnershipActivity.this);
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        mAdapter.updateItems(deviceAuthItems);
                    }
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void _onError(String message) {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyTv.setVisibility(View.VISIBLE);
            }
        }));
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onItemClick(View view, int position) {
        Object obj = view.getTag();
        if (obj != null) {
            DeviceAuthItem bean = (DeviceAuthItem) obj;
            Bundle bundle = new Bundle();
            bundle.putString(Constants.UNIQUEID, uniqueNo);
            bundle.putBoolean(InfoAuthActivity.IS_NEW_ADD, bean.getAuditStatus() == 0);
            bundle.putInt(Constants.DEVICE_ID, bean.getDeviceId());
            bundle.putString(Constants.DEVICE_NAME, bean.getBrand());
            bundle.putString(Constants.PAGET_TYPE, IncomeProofActivity.MACHINE_OWERSHIP);
            Constants.toActivity(this, IncomeProofActivity.class, bundle);
        }
    }
}
