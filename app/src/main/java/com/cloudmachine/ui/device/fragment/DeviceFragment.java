package com.cloudmachine.ui.device.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.cloudmachine.R;
import com.cloudmachine.activities.BeginnerGuideActivity;
import com.cloudmachine.activities.DeviceMcActivity;
import com.cloudmachine.activities.MainMapActivity;
import com.cloudmachine.activities.SearchActivity;
import com.cloudmachine.activities.WanaCloudBox;
import com.cloudmachine.adapter.MainListAdapter;
import com.cloudmachine.autolayout.widgets.TitleView;
import com.cloudmachine.base.BaseFragment;
import com.cloudmachine.main.MainActivity;
import com.cloudmachine.net.task.DevicesListAsync;
import com.cloudmachine.struc.McDeviceInfo;
import com.cloudmachine.struc.McDeviceInfoList;
import com.cloudmachine.ui.device.contract.DeviceContract;
import com.cloudmachine.ui.device.model.DeviceModel;
import com.cloudmachine.ui.device.presenter.DevicePresenter;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.MemeberKeeper;
import com.cloudmachine.utils.UMListUtil;
import com.cloudmachine.utils.UMengKey;
import com.cloudmachine.utils.widgets.XListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/3/17 下午10:29
 * 修改人：shixionglu
 * 修改时间：2017/3/17 下午10:29
 * 修改备注：
 */

public class DeviceFragment extends BaseFragment<DevicePresenter, DeviceModel> implements DeviceContract.View ,Handler.Callback, View.OnClickListener, XListView.IXListViewListener, AdapterView.OnItemClickListener {

    private String cloudm_box_url = "http://www.kaigongwuyou.com/kgl/mp/cube/CloudCubeAction/initIWillCube.do?author=y&openid=otxDztwwY7O6gq8eHzzPPOtOD6Io";
    private Handler         mHandler;
    private Context         mContext;
    private TitleView       title_layout;
    private XListView       listView;
    private MainListAdapter adapter;
    private List<McDeviceInfo> deviceList       = new ArrayList<McDeviceInfo>();
    private int                DevicesList_Type = Constants.MC_DevicesList_AllType;
    private View headerView;
    private View empty_layout,beginners_guide,cloudm_box;


    @Override
    protected void initView() {

        mContext = getActivity();
        mHandler = new Handler(this);
        empty_layout = viewParent.findViewById(R.id.empty_layout);
        beginners_guide = viewParent.findViewById(R.id.beginners_guide);
        cloudm_box = viewParent.findViewById(R.id.cloudm_box);
        beginners_guide.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Constants.toActivity(getActivity(), BeginnerGuideActivity.class,
                        null);
            }
        });
        //我要云盒子按钮点击跳转
        cloudm_box.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
				/*Intent cloudmboxIntent = new Intent(getActivity(),
						WebviewActivity.class);
				cloudmboxIntent.putExtra(Constants.P_WebView_Url,cloudm_box_url
						);
				startActivity(cloudmboxIntent);*/
                if (MemeberKeeper.getOauth(mContext) == null) {
                    Constants.toLoginActivity(DeviceFragment.this.getActivity(), 2);
                    return;
                } else {
                    Bundle b = new Bundle();
                    Constants.toActivity(DeviceFragment.this.getActivity(), WanaCloudBox.class, null, false);
                }
            }
        });
        initTitleLayout();
        initList();
        showList();
        new DevicesListAsync(DevicesList_Type, null, mContext, mHandler)
                .execute();
    }

    private void initTitleLayout(){

        title_layout = (TitleView)viewParent.findViewById(R.id.title_layout);
        title_layout.setTitle(getResources().getString(R.string.main_bar_text1));
        title_layout.setLeftLayoutVisibility(View.GONE);
    }

    private void initList() {



        headerView = View.inflate(mContext, R.layout.list_item_header, null);
        ((LinearLayout) headerView.findViewById(R.id.ll_search))
                .setOnClickListener(this);
        ((LinearLayout) headerView.findViewById(R.id.beginners_guide))
                .setOnClickListener(this);
        ((LinearLayout) headerView.findViewById(R.id.cloudm_box))
                .setOnClickListener(this);
        ((LinearLayout) headerView.findViewById(R.id.iv_map_layout))
                .setOnClickListener(this);
        //		((ImageView) headerView.findViewById(R.id.iv_map))
        //				.setOnClickListener(this);

        adapter = new MainListAdapter(getActivity(), mHandler, deviceList);
        listView = (XListView) viewParent.findViewById(R.id.listView);
        listView.addHeaderView(headerView);
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(false);
        listView.setXListViewListener(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void showList(){
        if(null != deviceList && deviceList.size()>0){
            listView.setVisibility(View.VISIBLE);
            empty_layout.setVisibility(View.GONE);

        }else{
            listView.setVisibility(View.GONE);
            empty_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search: // 搜索按钮
                Intent searchIntent = new Intent(getActivity(),
                        SearchActivity.class);
                searchIntent.putExtra(Constants.P_SEARCHLISTTYPE, 0);
                startActivity(searchIntent);
                break;
            case R.id.beginners_guide: // 新手引导
                // MobclickAgent.onEvent(mContext,
                // UMengKey.mc_main_list_cloud_device_click);
                // Bundle b = new Bundle();
                // b.putLong(Constants.P_DEVICEID, 0);
                // b.putString(Constants.P_DEVICENAME, "");
                // b.putString(Constants.P_DEVICEMAC, "");
                // Constants.toActivity(getActivity(), DeviceMcActivity.class, b,
                // false);
                Constants.toActivity(getActivity(), BeginnerGuideActivity.class,
                        null);
                break;
            case R.id.cloudm_box: // 我要云盒子
			/*Intent cloudmboxIntent = new Intent(getActivity(),
					WebviewActivity.class);
			cloudmboxIntent.putExtra(Constants.P_WebView_Url,
					cloudm_box_url);
			startActivity(cloudmboxIntent);*/
                if (MemeberKeeper.getOauth(mContext) == null) {
                    Constants.toLoginActivity(DeviceFragment.this.getActivity(), 2);
                    return;
                } else {
                    Bundle b = new Bundle();
                    Constants.toActivity(DeviceFragment.this.getActivity(), WanaCloudBox.class, null, false);
                }
                break;
            //		case R.id.iv_map: // 跳转地图
            case R.id.iv_map_layout:
                McDeviceInfoList listinfo = new McDeviceInfoList();
                listinfo.setDeviceList(deviceList);
                Bundle b = new Bundle();
                b.putSerializable(Constants.P_DEVICELIST, listinfo);
                Constants
                        .toActivity(getActivity(), MainMapActivity.class, b, false);
                break;
        }

    }

    public interface OnActivityBack {
        public void onFresh();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (Constants.isDeleteDevice || Constants.isAddDevice
                || Constants.isMcLogin || Constants.isChangeDevice) {
            new DevicesListAsync(DevicesList_Type, null, mContext, mHandler)
                    .execute();
        }
        super.onResume();
        //MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onPageStart(UMengKey.time_machine_list);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPageEnd(UMengKey.time_machine_list);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        UMListUtil.getUMListUtil().removeList("Main1FM");
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            case Constants.HANDLER_GETDEVICELIST_SUCCESS:
                listView.stopRefresh();
                listView.stopLoadMore();
                Constants.isDeleteDevice = false;
                Constants.isMcLogin = false;
                Constants.isAddDevice = false;
                Constants.isChangeDevice = false;
                if (deviceList != null)
                    deviceList.clear();
                MainActivity.deviceMacList.clear();
                deviceList.addAll((List<McDeviceInfo>) msg.obj);
                MainActivity.deviceMacList.addAll(deviceList);
                adapter.notifyDataSetChanged();
                showList();
                break;
            case Constants.HANDLER_GETDEVICELIST_FAIL:
                listView.stopRefresh();
                listView.stopLoadMore();
                if (deviceList != null)
                    deviceList.clear();
                adapter.notifyDataSetChanged();
                showList();
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        int temp = position - 2;
        if (null != deviceList && deviceList.size() > 0
                && temp < deviceList.size() && temp >= 0) {
            MobclickAgent.onEvent(mContext, UMengKey.mc_main_list_item_click);
            Bundle b = new Bundle();
            b.putSerializable(Constants.P_DEVICEINFO_MY, deviceList.get(temp));
            Constants.toActivity(getActivity(), DeviceMcActivity.class, b,
                    false);
        }

    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
        MobclickAgent.onEvent(mContext,UMengKey.count_machine_list_pulldown);
        new DevicesListAsync(DevicesList_Type, null, mContext, mHandler)
                .execute();
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main_device_list;
    }




}
