package com.cloudmachine.main;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudm.autolayout.AutoLayoutFragmentActivity;
import com.cloudmachine.R;
import com.cloudmachine.activities.BeginnerGuideActivity;
import com.cloudmachine.cache.MySharedPreferences;
import com.cloudmachine.net.task.AllMessagesCountAsync;
import com.cloudmachine.net.task.GetVersionAsync;
import com.cloudmachine.net.task.QuestionMessageAsync;
import com.cloudmachine.net.task.ScoreInfoAsync;
import com.cloudmachine.struc.McDeviceInfo;
import com.cloudmachine.struc.RepairHistoryInfo;
import com.cloudmachine.struc.ScoreInfo;
import com.cloudmachine.struc.VersionInfo;
import com.cloudmachine.ui.device.fragment.DeviceFragment;
import com.cloudmachine.ui.homepage.fragment.HomePageFragment;
import com.cloudmachine.ui.login.acticity.LoginActivity;
import com.cloudmachine.ui.personal.fragment.PersonalFragment;
import com.cloudmachine.ui.question.activity.QuestionActivity;
import com.cloudmachine.ui.repair.fragment.RepairFragment;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.MemeberKeeper;
import com.cloudmachine.utils.ResV;
import com.cloudmachine.utils.ToastUtils;
import com.cloudmachine.utils.VerisonCheckSP;
import com.cloudmachine.utils.widgets.RadiusButtonView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AutoLayoutFragmentActivity implements OnClickListener,
		Callback {

	public static boolean isForeground = false;
	private MessageReceiver mMessageReceiver;
	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";

	private Handler mHandler;
	private Context mContext;
	private View[] linearLayouts = new View[4];
	private ImageView[] imageViews = new ImageView[4];
	private TextView[] textViews = new TextView[4];
	private String catId;
	private OnActivityBack mListViewListener;
	private ImageView tips;
	// private List<News> displaylist = new ArrayList<News>();
	private Fragment mFragments[] = new Fragment[4]; // 存储页面的数组
	private boolean isInitFragment[] = new boolean[4];//是否被初始化
	private Fragment mContentFragment; // 当前fragment
	private int signBetweenTime; // 签到的时间间隔
	private RadiusButtonView sign_text, question_text;
	private int currentFragment; // 当前Fragment的索引
	private ArrayList<RepairHistoryInfo> repairHistoryList;
	private ImageView main_guide_image;//新手引导图标
	private View g_layout;//新手引导半透明背景
	public static ArrayList<McDeviceInfo> deviceMacList = new ArrayList<>();
	private RelativeLayout mRlQuestionLayout;
	private ImageView mIvQuestionImage;
	private TextView mTvQuestionText;


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SuppressLint("ResourceAsColor")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.enableEncrypt(true); // 友盟统计
		setContentView(R.layout.activity_main);
		Constants.isGetScore = true; // 得到积分？？
		mContext = this;
		mHandler = new Handler(this);
		getIntentData(); // 拿到当前fragment的索引
		initViews(); // 初始化布局，设置点击事件
		initFragmentS(); // 初始化fragment
		registerMessageReceiver(); // 极光推送消息接收
		//向友盟注册用户的id
		if (null != MemeberKeeper.getOauth(mContext))
			MobclickAgent.onProfileSignIn(String.valueOf(MemeberKeeper
					.getOauth(mContext).getId()));
		
	}

	private void getIntentData() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			try {
				currentFragment = bundle
						.getInt(Constants.P_MainActivity_Fragment_Id);
			} catch (Exception e) {
				Constants.MyLog(e.getMessage());
			}

		}
	}

	private void initViews() {
		g_layout = findViewById(R.id.g_layout);
		g_layout.setOnClickListener(this);
		main_guide_image = (ImageView)findViewById(R.id.main_guide_image);
		main_guide_image.setOnClickListener(this);
		
		question_text = (RadiusButtonView) findViewById(R.id.question_text); // 问卷
		//sign_text = (RadiusButtonView) findViewById(R.id.sign_text); // 签到
		linearLayouts[0] = findViewById(R.id.tab_homepage_layout);//主页
		linearLayouts[1] = findViewById(R.id.tab_device_layout);//设备
		//linearLayouts[2] = findViewById(R.id.tab_question_layout);//提问
		mRlQuestionLayout = (RelativeLayout) findViewById(R.id.tab_question_layout);
		linearLayouts[2] = findViewById(R.id.tab_repair_layout); // 报修页面布局
		linearLayouts[3] = findViewById(R.id.tab_personal_layout); //我的页面
		imageViews[0] = (ImageView) findViewById(R.id.tab_homepage_image);
		imageViews[1] = (ImageView) findViewById(R.id.tab_device_image);
		//imageViews[2] = (ImageView) findViewById(R.id.tab_question_iamge);
		mIvQuestionImage = (ImageView) findViewById(R.id.tab_question_iamge);
		imageViews[2] = (ImageView) findViewById(R.id.tab_repair_iamge); // 报修图片
		imageViews[3] = (ImageView) findViewById(R.id.tab_personal_iamge);
		textViews[0] = (TextView) findViewById(R.id.tab_homepage_text);
		textViews[1] = (TextView) findViewById(R.id.tab_device_text);
		//textViews[2] = (TextView) findViewById(R.id.tab_question_text);
		mTvQuestionText = (TextView) findViewById(R.id.tab_question_text);
		textViews[2] = (TextView) findViewById(R.id.tab_repair_text);
		textViews[3] = (TextView) findViewById(R.id.tab_personal_text);
		tips = (ImageView) findViewById(R.id.tips); // 小圆点
		tips.setVisibility(View.GONE);
		linearLayouts[0].setOnClickListener(this);
		linearLayouts[1].setOnClickListener(this);
		//linearLayouts[2].setOnClickListener(this);
		mRlQuestionLayout.setOnClickListener(this);
		linearLayouts[2].setOnClickListener(this);
		linearLayouts[3].setOnClickListener(this);
		//three_r_layout = findViewById(R.id.three_r_layout); // 内部布局（去掉签到标签）
	}

	//新手引导图层
	private void showGuide(){
		if(!MySharedPreferences.getSharedPBoolean(Constants.KEY_isGuide)){
			MySharedPreferences.setSharedPBoolean(Constants.KEY_isGuide, true);
			g_layout.setVisibility(View.VISIBLE);
		}else{
			g_layout.setVisibility(View.GONE);
		}
		
	}
	//在点击切换事件中判断是否需要登录
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_homepage_layout:
			switchContent(0);
			break;
		case R.id.tab_device_layout:
			if (MemeberKeeper.getOauth(mContext) == null) {
				Constants.toLoginActivity(this, 2);
				return;
			} else {
				switchContent(1);
			}
			break;
		case R.id.tab_question_layout:
			if (MemeberKeeper.getOauth(mContext) == null) {
				Constants.toLoginActivity(this, 2);
				return;
			} else {
				//switchContent(2);
				if (MemeberKeeper.getOauth(this).getWjdsStatus() != null && MemeberKeeper.getOauth(this).getWjdsStatus() != 2) {
					Constants.MyLog("拿到的挖机大师id为"+MemeberKeeper.getOauth(MainActivity.this).getWjdsStatus());
					Bundle bundle = new Bundle();
					bundle.putLong("myid",MemeberKeeper.getOauth(this).getWjdsId());
					Constants.toActivity(this, QuestionActivity.class,bundle,false);
				} else {
					ToastUtils.info("您的角色为技师，不能提问哦~",true);
				}

			}
			break;
		case R.id.tab_repair_layout: // 报修页面布局
			if (MemeberKeeper.getOauth(mContext) == null) {
				Constants.toLoginActivity(this, 2);
				return;
			} else {
				switchContent(2);
			}
			break;
			case R.id.tab_personal_layout:
				if (MemeberKeeper.getOauth(mContext) == null) {
					Constants.toLoginActivity(this, 2);
					return;
				} else {
					switchContent(3);
				}
				break;
		case R.id.main_guide_image:
			Constants.toActivity(this, BeginnerGuideActivity.class,
					null);
			break;
		}

	}

	public interface OnActivityBack {
		public void onFresh();
	}

	private long ExitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // 双击退出页面

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - ExitTime) > 2000) {
				Toast.makeText(getApplicationContext(),
						ResV.getString(R.string.main_activity_exit), Toast.LENGTH_SHORT).show();
				ExitTime = System.currentTimeMillis();
			} else {
				clearCache();
				this.finish();
				MobclickAgent.onKillProcess(this);
				System.exit(0);
			}
			return true;
		}
		return true;
	}

	private void clearCache() {
		// FileUtil.delAllFile(Constants.SDCARD_ROOT_FILE);
	}

	public void exit(View v) {
		MemeberKeeper.clearOauth(MainActivity.this);
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		intent.putExtra("flag", 1);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
		startActivity(intent);
		MainActivity.this.finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		JPushInterface.onResume(this);
		isForeground = true;
		if (MemeberKeeper.getOauth(mContext) != null) {
			new AllMessagesCountAsync(mContext, mHandler).execute();
			// new MsgAsync().execute();
		}
		long time = VerisonCheckSP.getTime(this);
		if (time != 0
				&& System.currentTimeMillis() - time < 1000 * 60 * 60 * 24) {

		} else {
			new GetVersionAsync(mContext, mHandler).execute();
		}
		if (Constants.isGetScore) {
			Constants.isGetScore = false;
			new ScoreInfoAsync(0, mContext, mHandler).execute();
		}
		Constants.showTips(tips, 0);

		new QuestionMessageAsync(mContext, mHandler).execute();
		for(int i=0; i<mFragments.length; i++){
			if(null !=mFragments[i] && isInitFragment[i]){
				mFragments[i].onResume();
			}
		}
//		mFragments[currentFragment].onResume();
		super.onResume();
		MobclickAgent.onResume(this);
		showGuide();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isForeground = false;
		JPushInterface.onPause(this);
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (null != mMessageReceiver)
			unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case Constants.HANDLER_GETVERSION_SUCCESS:
			VersionInfo vInfo = (VersionInfo) msg.obj;
			if (null != vInfo) {
				Constants.MyLog(vInfo.getMustUpdate()+"1111111");
				if (vInfo.getMustUpdate() == -1) {
					// Constants.MyToast(vInfo.getMessage());
				} else if (vInfo.getMustUpdate() == 0) {
					Constants.updateVersion(MainActivity.this, mHandler,
							vInfo.getMessage(), vInfo.getLink());
				}
			}
			break;
		case Constants.HANDLER_GETVERSION_FAIL:
			ToastUtils.error("请求版本失败，请检查网络",true);
			break;
		case Constants.HANDLER_QUESTION_SUCCESS:
			//是否展示调查问卷
			if (null != msg.obj) {
				question_text.setVisibility(View.VISIBLE);
			} else {
				question_text.setVisibility(View.GONE);
			}
			break;
		case Constants.HANDLER_VERSIONDOWNLOAD:
			Constants.versionDownload(MainActivity.this, (String) msg.obj);
			break;
		//消息页面小圆点展示
		case Constants.HANDLER_GETALLMESSAGECOUNT_SUCCESS:
		case Constants.HANDLER_GETALLMESSAGECOUNT_FAIL:
			Constants.showTips(tips, 0);
			// tips.setText(((List<MessageBO>)msg.obj).size()+"");
			break;

		//积分
		case Constants.HANDLER_INTEGRAL_SUCCESS:
			ScoreInfo scoreInfo = (ScoreInfo) msg.obj;
			signBetweenTime = 0;//签到间隔时间
			if (null != scoreInfo
					&& !TextUtils.isEmpty(scoreInfo.getServerTime())
					&& null != MemeberKeeper.getOauth(mContext)) {
				String oldTime = MySharedPreferences
						.getSharedPString(MySharedPreferences.key_score_update_time
								+ String.valueOf(MemeberKeeper.getOauth(
										mContext).getId()));
				signBetweenTime = Constants.getDateDays(Constants
						.changeDateFormat(scoreInfo.getServerTime(),
								Constants.DateFormat2, Constants.DateFormat1),
						Constants.changeDateFormat(oldTime,
								Constants.DateFormat2, Constants.DateFormat1),
						Constants.DateFormat1);
			} else {
				signBetweenTime = 1;
			}
			if (signBetweenTime != 0) {
				//sign_text.setVisibility(View.VISIBLE);
			} else {
				//sign_text.setVisibility(View.GONE);
			}
			break;
		case Constants.HANDLER_INTEGRAL_FAIL:
			//sign_text.setVisibility(View.GONE);
			break;
		case Constants.HANDLER_GET_REPAIRHISTORY_SUCCESS:		 									//获取到维修历史成功
//			if(null != msg.obj){
//				repairHistoryList = (ArrayList<RepairHistoryInfo>) msg.obj;							//获取报修数据成功
//				Constants.MyLog(repairHistoryList.toString()+"1111111111111");						//获取数据成功
//				getRepairHistoryList();
//			}  
			break;
		}
		return false;
	}

//	public ArrayList<RepairHistoryInfo> getRepairHistoryList() {
//		// TODO Auto-generated method stub
//		return repairHistoryList;
//	}

	public void registerMessageReceiver() {
		mMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		registerReceiver(mMessageReceiver, filter);
	}

	public class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
				String messge = intent.getStringExtra(KEY_MESSAGE);
				String extras = intent.getStringExtra(KEY_EXTRAS);
				StringBuilder showMsg = new StringBuilder();
				showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
				if (!TextUtils.isEmpty(extras)) {
					showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
				}
				Constants.MyToast(showMsg.toString());
			}
		}
	}

	private void initFragmentS() {
		/*mFragments[0] = new Main1FM();
		mFragments[1] = new MessageActivity();
		mFragments[2] = new UpdateInfoActivity();
		mFragments[3] = new RepairActivity();*/
		mFragments[0] = new HomePageFragment();
		mFragments[1] = new DeviceFragment();
		mFragments[2] = new RepairFragment();
		mFragments[3] = new PersonalFragment();

		mContentFragment = null;
		// mFragments[0].setArguments(b);
		switchContent(currentFragment);
	}

	public void updateDevice() {
		Constants.isAddDevice = true;
		mFragments[0].onResume();
	}

	public void loginOut() { // 退出的时候注销页面的数据
		MobclickAgent.onProfileSignOff();
		// for(int i=0; i<mFragments.length; i++){
		// isUpdate[i] = true;
		// }
		switchContent(0);
		mFragments[0].onResume();
		mFragments[1].onDestroyView();
		this.getSupportFragmentManager().beginTransaction()
				.remove(mFragments[1]).commit();
		mFragments[2].onDestroyView();
		this.getSupportFragmentManager().beginTransaction()
				.remove(mFragments[2]).commit();
		mFragments[3].onDestroyView();
		this.getSupportFragmentManager().beginTransaction()
				.remove(mFragments[3]).commit();
	}

	public int getSignBetweenTime() {
		return signBetweenTime;
	}

	public void updateQuestion(boolean b) { // 问卷调查（不知道在哪里出现的）
		if (b) {
			question_text.setVisibility(View.VISIBLE);
		} else {
			question_text.setVisibility(View.GONE);
		}
	}

	public void setSignBetweenTime(int time) {
		signBetweenTime = time;
		if (signBetweenTime != 0) {
			//sign_text.setVisibility(View.VISIBLE);
		} else {
			//sign_text.setVisibility(View.GONE);
		}
	}

	private void switchContent(int n) {
		if(n<isInitFragment.length)
			isInitFragment[n] = true;
		//签名逻辑
		/*if(currentFragment == 2){
			setSignBetweenTime(0);
		}*/
		currentFragment = n; // 赋值索引
		int len = mFragments.length;
		if (n < len && n >= 0) {
			int size = imageViews.length;
			for (int i = 0; i < size; i++) {
				if (i == n) {
					imageViews[i].setSelected(true);
					linearLayouts[i].setSelected(false);
					/*textViews[i].setTextColor(getResources().getColor(
							R.color.main_bar_text_dw));*/
				} else {
					imageViews[i].setSelected(false);
					linearLayouts[i].setSelected(false);
					/*textViews[i].setTextColor(getResources().getColor(
							R.color.main_bar_text_nm));*/
				}
			}
			Fragment to = mFragments[n];
			if (mContentFragment == mFragments[1]) {
				if (MemeberKeeper.getOauth(mContext) != null) {
					new AllMessagesCountAsync(mContext, mHandler).execute();
				}
			}
			if (mContentFragment != to) {
				FragmentTransaction fragmentTransaction = this
						.getSupportFragmentManager().beginTransaction();
				if (mContentFragment != null) {
					if (!to.isAdded()) { // 先判断是否被add过
						fragmentTransaction.hide(mContentFragment)
								.add(R.id.frame_content, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
					} else {
						fragmentTransaction.hide(mContentFragment).show(to)
								.commit(); // 隐藏当前的fragment，显示下一个
						if (to == mFragments[1]) {
							to.onResume();
						}
					}
				} else {
					fragmentTransaction.add(R.id.frame_content, to).commit();
				}
				mContentFragment = to;
//				if(mContentFragment == mFragments[3]){
//					Constants.MyLog("联网获取数据");													//联网获取报修历史数据
//					if (MemeberKeeper.getOauth(mContext) != null) {
//						new AllRepairHistoryAsync(mContext, mHandler).execute();
//					}
//				}
				if (n == 0) {
					to.onResume();
				}

			}

		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, intent);
		if (null != intent && null != intent.getExtras()) {
			try {
//				currentFragment = intent.getExtras().getInt(
//						Constants.P_MainActivity_Fragment_Id);
//				switchContent(currentFragment);
			} catch (Exception e) {

			}
		}

	}

	public void setStatusbarColor(String rgb) {
		// 4.4及以上版本开启
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
		} else {
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setNavigationBarTintEnabled(true);
			// 自定义颜色
			tintManager.setTintColor(Color.parseColor(rgb));
		}
	}

	@TargetApi(19)
	private void setTranslucentStatus(boolean on){
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

}
