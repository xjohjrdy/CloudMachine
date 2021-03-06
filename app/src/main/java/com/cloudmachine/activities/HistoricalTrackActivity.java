package com.cloudmachine.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.cloudmachine.R;
import com.cloudmachine.autolayout.widgets.CustomDialog;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.bean.LocationXY;
import com.cloudmachine.chart.utils.AppLog;
import com.cloudmachine.helper.MobEvent;
import com.cloudmachine.net.task.LocusListAsync;
import com.cloudmachine.utils.CommonUtils;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.DensityUtil;
import com.cloudmachine.utils.UMengKey;
import com.cloudmachine.utils.widgets.wheelview.OnWheelScrollListener;
import com.cloudmachine.utils.widgets.wheelview.WheelView;
import com.cloudmachine.utils.widgets.wheelview.adapter.ArrayWheelAdapter;
import com.cloudmachine.utils.widgets.wheelview.adapter.NumericWheelAdapter;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoricalTrackActivity extends BaseAutoLayoutActivity implements AMap.OnMapLoadedListener, OnClickListener,
        Callback {

    //	private static final double[][] data = {{30.273548,119.997108},{30.272933,119.99681},
//		{30.2721,119.996253},{30.271785,119.9972},{30.272174,119.998374},{30.272787,119.999},{30.273305,119.999816}
//		,{30.273788,119.999168},{30.273286,119.998397},{30.272638,119.999259},{30.272027,119.999771},{30.271434,119.99887}
//		,{30.271879,119.997734},{30.273157,119.997627},{30.272933,119.998374}
//	};
    private static String FOMR_TIME_PROCESS = "%1$s / %2$s (秒)";
    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 60;
    private static final double DISTANCE = 0.0001;
    private static final int MapBoundsPadding = 10;
    private static final double MapBoundsLatLng = 0.004;
    private Context mContext;
    private Handler mHandler;
    private MapView mapView;
    private AMap mAmap;
    private List<LocationXY> locationXY = new ArrayList<LocationXY>();
    private Polyline mVirtureRoad;
    private View start_time_layout, end_time_layout, wheelview_layout;
    private TextView start_time, end_time;
    private TextView wheelview_cancel, wheelview_determine, wheelview_title;
    private WheelView year_month_day_week_wheelview, hour_wheelview, minute_wheelview;
    private final int sumDay = 30;
    private String[] dayArray = new String[sumDay];
    private String[] dayArray2 = new String[sumDay];
    private int timeType = 0;
    private final String hour_unit = "时";
    private final String minute_unit = "分";
    private long startTime, endTime;
    private Thread moveThread;
    Marker mMoveMarker;

    Button mPlayBtn, mReduceBtn, mAddBtn;
    TextView mMultipleTv, mCurPosTv;
    SeekBar mProcessSkb;
    RelativeLayout mCtrlLayout;
    String mToalLen;
    int pointSize;
    int curTimeInterval = TIME_INTERVAL;
    long deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_track);

        this.mContext = this;
        mHandler = new Handler(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        mAmap = mapView.getMap();
        mAmap.setOnMapLoadedListener(this);
        UiSettings us = mAmap.getUiSettings();
        us.setMyLocationButtonEnabled(false);
        us.setZoomControlsEnabled(false);
//        getIntentData();
        initView();
        MobclickAgent.onEvent(this, MobEvent.TIME_MACHINE_HISTORYLOCUS);
//        initRoadData();
//        moveLooper();
        deviceId = getIntent().getLongExtra(Constants.DEVICE_ID, -1);
        new LocusListAsync(deviceId, mHandler).execute();
    }

    @Override
    public void initPresenter() {

    }

    private void initView() {
        mCtrlLayout = (RelativeLayout) findViewById(R.id.track_ctrl_layout);
        mPlayBtn = (Button) findViewById(R.id.track_play_btn);
        mReduceBtn = (Button) findViewById(R.id.track_reduce_btn);
        mAddBtn = (Button) findViewById(R.id.track_add_btn);
        mProcessSkb = (SeekBar) findViewById(R.id.track_process_skb);
        mProcessSkb.setEnabled(false);
        mMultipleTv = (TextView) findViewById(R.id.track_multiple_tv);
        mCurPosTv = (TextView) findViewById(R.id.track_cur_pos_tv);
        wheelview_layout = findViewById(R.id.wheelview_layout);
        start_time_layout = findViewById(R.id.start_time_layout);
        end_time_layout = findViewById(R.id.end_time_layout);
        start_time_layout.setOnClickListener(this);
        end_time_layout.setOnClickListener(this);
        start_time = (TextView) findViewById(R.id.start_time);
        end_time = (TextView) findViewById(R.id.end_time);
        wheelview_cancel = (TextView) findViewById(R.id.wheelview_cancel);
        wheelview_determine = (TextView) findViewById(R.id.wheelview_determine);
        wheelview_title = (TextView) findViewById(R.id.wheelview_title);
        wheelview_cancel.setOnClickListener(this);
        wheelview_determine.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mAddBtn.setOnClickListener(this);
        mReduceBtn.setOnClickListener(this);
        mProcessSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurPosTv.setText(String.format(FOMR_TIME_PROCESS, CommonUtils.formartTime(progress * curTimeInterval), mToalLen));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        initWheelView();
    }


    private void initRoadData() {
        if (locationXY.size() <= 0)
            return;
        PolylineOptions polylineOptions = new PolylineOptions();
        for (int i = 0; i < locationXY.size(); i++) {
            polylineOptions.add(new LatLng(locationXY.get(i).getY(), locationXY.get(i).getX()));
        }
        polylineOptions.width(DensityUtil.dip2px(this, 3));
        polylineOptions.color(Color.parseColor("#7bb4f5"));
        mVirtureRoad = mAmap.addPolyline(polylineOptions);
        pointSize = mVirtureRoad.getPoints().size();
        mProcessSkb.setMax((pointSize - 1));
        mToalLen = CommonUtils.formartTime((pointSize - 1) * TIME_INTERVAL);
        mCurPosTv.setText(String.format(FOMR_TIME_PROCESS, CommonUtils.formartTime(0), mToalLen));
        MarkerOptions markerOptions = new MarkerOptions();
//		markerOptions.setFlat(true);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(BitmapDescriptorFactory
                .fromView(CommonUtils.getMarkerView(this, 32, 32, R.drawable.location_marker)));
        markerOptions.position(polylineOptions.getPoints().get(0));
        mMoveMarker = mAmap.addMarker(markerOptions);
        mMoveMarker.setRotateAngle((float) getAngle(0));
//		mAmap.moveCamera(CameraUpdateFactory.changeLatLng(mMoveMarker.getPosition()));


        if (polylineOptions.getPoints().size() > 2) {
            mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                    .fromView(CommonUtils.getMarkerView(this, 19, 25, R.drawable.location_start)))
                    .position(polylineOptions.getPoints().get(0)));
            mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                    .fromView(CommonUtils.getMarkerView(this, 19, 25, R.drawable.location_end)))
                    .position(polylineOptions.getPoints().get(polylineOptions.getPoints().size() - 1)));
        }
        initMapMarker();
    }


    private void initMapMarker() {
        LatLngBounds.Builder bu = new LatLngBounds.Builder();
        double latMax = 0, latMin = 0, lngMax = 0, lngMin = 0;
        double lat = -1, lng = -1;
        if (null != locationXY && locationXY.size() > 0) {
            int len = locationXY.size();
            for (int i = 0; i < len; i++) {
                lat = locationXY.get(i).getY();
                lng = locationXY.get(i).getX();
                if (latMax == 0) {
                    latMax = latMin = lat;
                    lngMax = lngMin = lng;
                }
                latMax = latMax > lat ? latMax : lat;
                latMin = latMin < lat ? latMin : lat;
                lngMax = lngMax > lng ? lngMax : lng;
                lngMin = lngMin < lng ? lngMin : lng;
                bu.include(new LatLng(lat, lng));
            }
            if (len > 0) {
                bu.include(new LatLng(latMax + MapBoundsLatLng, lngMin - MapBoundsLatLng));
                bu.include(new LatLng(latMin - MapBoundsLatLng, lngMax + MapBoundsLatLng));
                mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bu.build(), MapBoundsPadding));
                mAmap.moveCamera(CameraUpdateFactory.zoomTo(18));
            }
        }
    }

    private void initWheelView() {
//		Calendar c = Calendar.getInstance();
//		int norYear = c.get(Calendar.YEAR);
//		int week = c.get(Calendar.DAY_OF_WEEK);

        year_month_day_week_wheelview = (WheelView) findViewById(R.id.day_wheelview);
        initDay();

        hour_wheelview = (WheelView) findViewById(R.id.hour_wheelview);
        initHour();

        minute_wheelview = (WheelView) findViewById(R.id.minute_wheelview);
        initMinute();
    }

    private void initDay() {

        for (int i = 0; i < sumDay; i++) {
            dayArray[sumDay - i - 1] = Constants.getDateBefore(i);
            dayArray2[sumDay - i - 1] = Constants.getDateBefore2(i);
        }
        ArrayWheelAdapter dayAdapter = new ArrayWheelAdapter(this, dayArray);
        year_month_day_week_wheelview.setViewAdapter(dayAdapter);
        year_month_day_week_wheelview.setCyclic(false);//是否可循环滑动
        year_month_day_week_wheelview.setCurrentItem(sumDay - 1);
        year_month_day_week_wheelview.setVisibleItems(7);
        year_month_day_week_wheelview.addScrollingListener(scrollListener);
    }

    private void initHour() {
        NumericWheelAdapter numericWheelAdapter3 = new NumericWheelAdapter(this, 0, 23, "%02d");
        numericWheelAdapter3.setLabel(hour_unit);
        hour_wheelview.setViewAdapter(numericWheelAdapter3);
        hour_wheelview.setCyclic(true);
        hour_wheelview.setVisibleItems(7);
        hour_wheelview.addScrollingListener(scrollListener);
    }

    private void initMinute() {
        NumericWheelAdapter numericWheelAdapter4 = new NumericWheelAdapter(this, 0, 59, "%02d");
        numericWheelAdapter4.setLabel(minute_unit);
        minute_wheelview.setViewAdapter(numericWheelAdapter4);
        minute_wheelview.setCyclic(true);
        minute_wheelview.setVisibleItems(7);
        minute_wheelview.addScrollingListener(scrollListener);
    }

    private void showWheelView() {
        wheelview_layout.setVisibility(View.VISIBLE);
    }

    private void hideWheelView() {
        wheelview_layout.setVisibility(View.GONE);
    }

    OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            Calendar now = Calendar.getInstance();
            Date date = now.getTime();
            int hour = date.getHours();
            int minute = date.getMinutes();

            if (Constants.getDateDays(Constants.getNowData(),
                    dayArray2[year_month_day_week_wheelview.getCurrentItem()],
                    Constants.DateFormat1) == 0) {//今天
//				Constants.MyLog("hour: "+ hour + " minute :"+minute
//						+" hh:"+(hour_wheelview.getCurrentItem()+1)+" mm："+(minute_wheelview.getCurrentItem()+1));
                if (hour_wheelview.getCurrentItem() > hour) {
                    hour_wheelview.setCurrentItem(hour);
                } else if ((hour_wheelview.getCurrentItem()) == hour) {
                    if ((minute_wheelview.getCurrentItem()) > minute) {
                        minute_wheelview.setCurrentItem(minute);
                    }
                }

            }
//			hour_wheelview
        }
    };

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
        if (mVirtureRoad != null && mVirtureRoad.getPoints() != null) {
            mProcessSkb.setProgress(0);
            moveThread = new Thread() {

                public void run() {


                    synchronized (moveThread) {

                        for (int i = 0; i < mVirtureRoad.getPoints().size() - 1; i++) {
                            LatLng startPoint = mVirtureRoad.getPoints().get(i);
                            LatLng endPoint = mVirtureRoad.getPoints().get(i + 1);

                            mMoveMarker.setPosition(startPoint);

                            mMoveMarker.setRotateAngle((float) getAngle(startPoint,
                                    endPoint));
                            double slope = getSlope(startPoint, endPoint);
                            if (slope == 0)
                                continue;
                            //是不是正向的标示（向上设为正向）
                            boolean isReverse = (startPoint.latitude > endPoint.latitude);

                            double intercept = getInterception(slope, startPoint);

                            double xMoveDistance = isReverse ? getXMoveDistance(slope)
                                    : -1 * getXMoveDistance(slope);
                            for (double j = startPoint.latitude;
                                 !((j > endPoint.latitude) ^ isReverse);

                                 j = j
                                         - xMoveDistance) {
                                LatLng latLng = null;
                                if (slope != Double.MAX_VALUE) {
                                    latLng = new LatLng(j, (j - intercept) / slope);
                                } else {
                                    latLng = new LatLng(j, startPoint.longitude);
                                }
                                if (null != latLng &&
                                        latLng.latitude != Double.NaN &&
                                        latLng.longitude != Double.NaN) {
                                    mMoveMarker.setPosition(latLng);
                                } else {
                                    latLng = new LatLng(j, startPoint.longitude);
                                    mMoveMarker.setPosition(latLng);
                                    break;
                                }

                                mAmap.reloadMap();// 刷新地图

                            }
                            Message msg = mHandler.obtainMessage();
                            msg.arg1 = i + 1;
                            msg.what = Constants.HANDLER_UPDATE_PROGRESS;
                            mHandler.sendMessage(msg);
                            if (i < mVirtureRoad.getPoints().size() - 2) {
                                try {
                                    Thread.sleep(curTimeInterval);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (pause) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    }
                }

            };
            moveThread.start();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        //MobclickAgent.onPageStart(UMengKey.time_machine_historylocus);
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        //MobclickAgent.onPageEnd(UMengKey.time_machine_historylocus);
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
//			throw new RuntimeException("index out of bonds");
            return 0;
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算取斜率
     */
    private double getSlope(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getSlope(startPoint, endPoint);
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    @Override
    public void onMapLoaded() {
        // TODO Auto-generated method stub
//		initMapMarker();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.track_add_btn:
                String text = mMultipleTv.getText().toString();
                switch (text) {
                    case "1X":
                        curTimeInterval = TIME_INTERVAL / 2;
                        mMultipleTv.setText("2X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "2X":
                        curTimeInterval = TIME_INTERVAL / 3;
                        mMultipleTv.setText("3X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "3X":
                        curTimeInterval = TIME_INTERVAL / 4;
                        mMultipleTv.setText("4X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "4X":
                        curTimeInterval = TIME_INTERVAL / 5;
                        mMultipleTv.setText("5X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                }
                break;
            case R.id.track_reduce_btn:
                String reduceText = mMultipleTv.getText().toString();
                switch (reduceText) {
                    case "5X":
                        curTimeInterval = TIME_INTERVAL / 4;
                        mMultipleTv.setText("4X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "4X":
                        curTimeInterval = TIME_INTERVAL / 3;
                        mMultipleTv.setText("3X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "3X":
                        curTimeInterval = TIME_INTERVAL / 2;
                        mMultipleTv.setText("2X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                    case "2X":
                        curTimeInterval = TIME_INTERVAL / 1;
                        mMultipleTv.setText("1X");
                        mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                        if (pause) {
                            startMoveThread();
                        }
                        break;
                }

                break;
            case R.id.track_play_btn:
                if (!pause) {
                    stopMoveThread();
                } else {
                    startMoveThread();
                }
                break;

            case R.id.start_time_layout:
                MobclickAgent.onEvent(mContext, UMengKey.count_historylocus_start);
                stopMoveThread();

                wheelview_title.setText(getResources().getString(R.string.historical_track_wheelview_time_start));
                timeType = 0;
                showWheelView();
                initDay();
                initHour();
                initMinute();
                break;
            case R.id.end_time_layout:
                MobclickAgent.onEvent(mContext, UMengKey.count_historylocus_end);
                stopMoveThread();
                wheelview_title.setText(getResources().getString(R.string.historical_track_wheelview_time_end));
                timeType = 1;
                showWheelView();
                initDay();
                initHour();
                initMinute();
                break;
            case R.id.wheelview_cancel:
                hideWheelView();
                break;
            case R.id.wheelview_determine:
                if (timeType == 0) {
                    start_time.setText(dayArray[year_month_day_week_wheelview.getCurrentItem()] +
                            " " + hour_wheelview.getCurrentItem() + hour_unit +
                            " " + minute_wheelview.getCurrentItem() + minute_unit);
                    startTime = Constants.getDatetolong(dayArray2[year_month_day_week_wheelview.getCurrentItem()] +
                                    " " + hour_wheelview.getCurrentItem() + ":" + minute_wheelview.getCurrentItem(),
                            "yyyy-MM-dd HH:mm");
                    wheelview_title.setText(getResources().getString(R.string.historical_track_wheelview_time_end));
                    timeType = 1;
                    showWheelView();
                    initDay();
                    initHour();
                    initMinute();
                } else {
                    end_time.setText(dayArray[year_month_day_week_wheelview.getCurrentItem()] +
                            " " + hour_wheelview.getCurrentItem() + hour_unit +
                            " " + minute_wheelview.getCurrentItem() + minute_unit);
                    endTime = Constants.getDatetolong(dayArray2[year_month_day_week_wheelview.getCurrentItem()] +
                                    " " + hour_wheelview.getCurrentItem() + ":" + minute_wheelview.getCurrentItem(),
                            "yyyy-MM-dd HH:mm");
                    hideWheelView();

                    if (startTime > 0 && endTime > 0) {
                        if (startTime > endTime) {
                            showPromptDialog(getResources().getString(R.string.start_end_time_error));
                            return;
                        }
                        new LocusListAsync(deviceId, startTime, endTime, mHandler).execute();
                    } else {
                        showPromptDialog(getResources().getString(R.string.start_end_time_null));
                    }

                }
                break;
        }
    }

    private void showPromptDialog(String message) {
        if (isFinishing()) {
            return;
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("好", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            case Constants.HANDLER_UPDATE_PROGRESS:
                int position = msg.arg1;
                mProcessSkb.setProgress(position);
                if (position == pointSize - 1) {
                    isComlete = true;
                    curTimeInterval = TIME_INTERVAL;
                    mMultipleTv.setText("1X");
                    mToalLen = CommonUtils.formartTime((pointSize - 1) * curTimeInterval);
                    stopMoveThread();
                }
                break;

            case Constants.HANDLER_LOCUS_SUCCESS:
            /*isMove = false;
            if(null != pausableThreadPoolExecutor){
				pausableThreadPoolExecutor.shutdownNow();
				pausableThreadPoolExecutor = null;
			}
			pausableThreadPoolExecutor = new PausableThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new PriorityBlockingQueue());
			if(null != pRunnable)
				pausableThreadPoolExecutor.remove(pRunnable);*/
                Object obj = msg.obj;
                if (obj != null) {
                    locationXY.clear();
                    locationXY.addAll((List<LocationXY>) obj);
                    mAmap.clear();
                    mAmap.reloadMap();
                    if (null != locationXY && locationXY.size() > 2) {
                        //// TODO: 2017/12/27
                        mCtrlLayout.setVisibility(View.VISIBLE);
                        mPlayBtn.setBackground(getResources().getDrawable(R.drawable.ic_route_play));
                        pause = false;
                        initRoadData();
                        moveLooper();
                    } else {
                        mCtrlLayout.setVisibility(View.GONE);
                        showPromptDialog("定位点过少，无法形成轨迹，请扩大选择范围");

                    }
                }
                break;
            case Constants.HANDLER_LOCUS_FAIL:
                Object failedObj = msg.obj;
                if (failedObj != null) {
                    showPromptDialog((String) failedObj);
                }
                break;
        }
        return false;
    }

    private boolean pause;

    private void stopMoveThread() {
        pause = true;
        mPlayBtn.setBackground(getResources().getDrawable(R.drawable.ic_route_pause));
    }

    public boolean isComlete = false;

    private void startMoveThread() {
        pause = false;
        mPlayBtn.setBackground(getResources().getDrawable(R.drawable.ic_route_play));
        if (null != moveThread) {
            if (!isComlete) {
                AppLog.print("isAlive will resume");
                synchronized (moveThread) {
                    moveThread.notify();
                }
            } else {
                AppLog.print("no isAlive restart");
                isComlete = false;
//                moveThread.start();
                moveLooper();
            }
        }

    }


}
