package com.cloudmachine.ui.home.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cloudmachine.R;
import com.cloudmachine.activities.EvaluationActivity;
import com.cloudmachine.activities.RepairPayDetailsActivity;
import com.cloudmachine.activities.SearchPoiActivity;
import com.cloudmachine.autolayout.widgets.RadiusButtonView;
import com.cloudmachine.bean.ResidentAddressInfo;
import com.cloudmachine.bean.UnfinishedBean;
import com.cloudmachine.chart.utils.AppLog;
import com.cloudmachine.helper.MobEvent;
import com.cloudmachine.helper.OrderStatus;
import com.cloudmachine.helper.UserHelper;
import com.cloudmachine.ui.home.activity.RepairDetailMapActivity;
import com.cloudmachine.ui.home.contract.MSupervisorContract;
import com.cloudmachine.ui.home.model.MSupervisorModel;
import com.cloudmachine.ui.home.model.SiteBean;
import com.cloudmachine.ui.home.presenter.MSupervisorPresenter;
import com.cloudmachine.ui.login.acticity.LoginActivity;
import com.cloudmachine.ui.repair.activity.NewRepairActivity;
import com.cloudmachine.ui.repair.activity.RepairFinishDetailActivity;
import com.cloudmachine.utils.Constants;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xiaojw on 2017/7/19.
 */

public class MaintenanceFragment extends BaseMapFragment<MSupervisorPresenter, MSupervisorModel> implements MSupervisorContract.View, AMapLocationListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {
    private static final int REQCODE_LOC = 0x113;
    @BindView(R.id.repair_btn)
    RadiusButtonView repairBtn;
    @BindView(R.id.maintence_cur_location)
    EditText curLocTv;
    @BindView(R.id.maintenance_order_container)
    ViewGroup orderCotainer;
    @BindView(R.id.device_name)
    TextView modelTv;
    @BindView(R.id.repair_desc)
    TextView descTv;
    @BindView(R.id.repair_history_status_tv)
    TextView statusTv;
    @BindView(R.id.repair_history_time_tv)
    TextView timeTv;
    @BindView(R.id.maintence_cardview)
    FrameLayout curLocCv;
    @BindView(R.id.logo_flag_img)
    ImageView logoImg;


    Marker centerMarker;
    GeocodeSearch geocoderSearch;
    LatLng lastLatLng;
    UnfinishedBean unfinishedBean;
    List<Marker> markerList = new ArrayList<>();
    String mProvince;
    double locLng, locLat;


    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setinfoWIndowHiden(false);
        initGeocoder();
//        if (UserHelper.isLogin(getActivity())) {
//            mPresenter.getRepairItemView(UserHelper.getMemberId(getActivity()));
//        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            MobclickAgent.onEvent(getActivity(), MobEvent.TIME_HOME_REPAIR);
            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            MobclickAgent.onEvent(getActivity(), MobEvent.TIME_HOME_REPAIR);
        }
        loadData();

    }

    private void loadData() {
        startlocaction(this);
        if (UserHelper.isLogin(getActivity())) {
//            mPresenter.getRepairItemView(UserHelper.getMemberId(getActivity()));
            mPresenter.getAllianceItemView(UserHelper.getMemberId(getActivity()));
        }
    }

    @Override
    protected void initView() {
        MobclickAgent.onEvent(getActivity(), MobEvent.TIME_HOME_REPAIR);
    }

    @Override
    protected void setAMap() {
        isFrobidenClick = true;
        setMapZoomTo(ZOOM_DEFAULT);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMarkerClickListener(this);
    }

    @Override
    protected void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_maintenance_supervisor;
    }


    private void initGeocoder() {
        geocoderSearch = new GeocodeSearch(getActivity());
        geocoderSearch.setOnGeocodeSearchListener(this);
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            AppLog.print("onLocationChanged___lat:" + aMapLocation.getLatitude() + "，  lng:" + aMapLocation.getLongitude() + ", address:" + aMapLocation.getAddress());
            mlocClient.stopLocation();
            double lat = aMapLocation.getLatitude();
            double lng = aMapLocation.getLongitude();
            LatLng loclatLng = new LatLng(lat, lng);
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(loclatLng));
        }
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        AppLog.print("onCameraChange______");
        if (centerMarker != null) {
            centerMarker.setPosition(cameraPosition.target);
        } else {
            centerMarker = aMap.addMarker(getMarkerOptions(getActivity(), cameraPosition.target, R.drawable.icon_cur_repair_loc, Constants.CURRENT_LOC));
        }
    }


    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        AppLog.print("onCameraChangeFinish______");
        LatLng lat = cameraPosition.target;
        if (lastLatLng == null) {
            lastLatLng = lat;
        } else {
            float distance = AMapUtils.calculateLineDistance(lastLatLng, lat);
            if (distance > 500) {
                lastLatLng = lat;
                AppLog.print("开始请求站点。。。distance=" + distance);
                for (Marker marker : markerList) {
                    marker.remove();
                }
                if (markerList.size() > 0) {
                    markerList.clear();
                }
                mPresenter.updateStationView(lat.longitude, lat.latitude);
            }
        }
        locLat = lat.latitude;
        locLng = lat.longitude;
        LatLonPoint point = new LatLonPoint(lat.latitude, lat.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
        AppLog.print("onRegeocodeSearched___" + address);
        if (address != null) {
            String formartAddress = address.getFormatAddress();
            String city = address.getTownship();
            String describeAddress;
            int index = formartAddress.indexOf(city) + city.length();
            if (TextUtils.isEmpty(city)) {
                describeAddress = formartAddress;
            } else {
                if (formartAddress.length() > index) {
                    describeAddress = formartAddress.substring(index);
                } else {
                    describeAddress = city;
                }
            }
            mProvince = address.getProvince();
            curLocTv.setText(describeAddress);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }


    @Override
    public void returnStationView(SiteBean siteBean) {
        if (siteBean != null) {
            List<SiteBean.RepairStationListBean> repairsStatiionList = siteBean.getRepairStationList();
            List<SiteBean.RepairStationListBean> serviceStatiionList = siteBean.getServiceSiteList();
//            for (SiteBean.RepairStationListBean bean : repairsStatiionList) {
//                Marker siteMarker = aMap.addMarker(getMarkerOptions(bean.getLat(), bean.getLng(), R.drawable.icon_station, null));
//                markerList.add(siteMarker);
//            }
            for (SiteBean.RepairStationListBean bean : serviceStatiionList) {
                Marker stationMarker = aMap.addMarker(getMarkerOptions(bean.getLat(), bean.getLng(), R.drawable.icon_station, null));
                markerList.add(stationMarker);
            }
        }
    }

    @Override
    public void returnRepairItemView(UnfinishedBean bean, String status) {
        unfinishedBean = bean;
        orderCotainer.setVisibility(View.VISIBLE);
        String logo_flag = bean.getLogo_flag();
        if ("0".equals(logo_flag)) {
            if (logoImg.getVisibility() == View.VISIBLE) {
                logoImg.setVisibility(View.INVISIBLE);
            }
        }
        if ("1".equals(logo_flag)) {
            if (logoImg.getVisibility() != View.VISIBLE) {
                logoImg.setVisibility(View.VISIBLE);
            }
        }
        statusTv.setText(status);
        modelTv.setText(bean.getVbrandname() + " "
                + bean.getVmaterialname());
        descTv.setText(bean.getVdiscription());
        timeTv.setText(bean.getDopportunity());

    }

    @OnClick({R.id.maintence_cur_location, R.id.maintence_cardview, R.id.maintenance_order_container, R.id.radius_button_text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.maintence_cur_location:
                Intent intent = new Intent(getActivity(), SearchPoiActivity.class);
                startActivityForResult(intent, REQCODE_LOC);
                break;
            case R.id.maintence_cardview:
                break;
            case R.id.maintenance_order_container:
                if (unfinishedBean == null) {
                    return;
                }
                String flag = unfinishedBean.getFlag();
                String status = statusTv.getText().toString();
                if (unfinishedBean.isAlliance()) {
                    switch (unfinishedBean.getNstatus()) {
                        case "3"://待付款
                            status = OrderStatus.PAY;
                            break;
                        case "4":
                        case "5":
                        case "6":
                        case "7"://已完工
                            if ("N".equals(unfinishedBean.getIs_EVALUATE())) {
                                status = OrderStatus.EVALUATION;
                            } else {
                                status = OrderStatus.COMPLETED;
                            }
                            break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("isAlliance",unfinishedBean.isAlliance());
                if (OrderStatus.EVALUATION.equals(status)) {
                    bundle.putString("orderNum", unfinishedBean.getOrderNum());
                    bundle.putString("tel", unfinishedBean.getVmacoptel());
                    Constants.toActivity(getActivity(), EvaluationActivity.class, bundle);
                } else if (OrderStatus.COMPLETED.equals(status)) {
                    bundle.putString("orderNum", unfinishedBean.getOrderNum());
                    bundle.putString("flag", unfinishedBean.getFlag());
                    Constants.toActivity(getActivity(), RepairFinishDetailActivity.class, bundle);
                } else if (OrderStatus.PAY.equals(status)) {
                    bundle.putString("orderNum", unfinishedBean.getOrderNum());
                    bundle.putString("flag", flag);
                    Constants.toActivity(getActivity(), RepairPayDetailsActivity.class, bundle);
                } else {
                    bundle.putString("orderNum", unfinishedBean.getOrderNum());
                    bundle.putString("nstatus", status);
                    bundle.putString("flag", flag);
                    Constants.toActivity(getActivity(), RepairDetailMapActivity.class, bundle);
                }

                break;
            case R.id.radius_button_text:
                gotoNewRepair();
                break;
        }
    }

    private void gotoNewRepair() {
        if (UserHelper.isLogin(getActivity())) {
            Bundle bundle = new Bundle();
            bundle.putString(NewRepairActivity.DEFAULT_LOCAITOIN, curLocTv.getText().toString());
            bundle.putString(NewRepairActivity.DEFAULT_PROVINCE, mProvince);
            bundle.putDouble(NewRepairActivity.KEY_LOC_LAT, locLat);
            bundle.putDouble(NewRepairActivity.KEY_LOC_LNG, locLng);
            Constants.toActivity(getActivity(), NewRepairActivity.class, bundle);
        } else {
            Constants.toActivity(getActivity(), LoginActivity.class, null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQCODE_LOC) {
            if (data != null) {
                ResidentAddressInfo info = (ResidentAddressInfo) data.getSerializableExtra(Constants.P_SEARCHINFO);
                if (info != null) {
                    LatLng latLng = new LatLng(info.getLat(), info.getLng());
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                }
            }

        }
    }
}
