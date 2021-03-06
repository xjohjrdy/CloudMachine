package com.cloudmachine.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.adapter.McSearchMemberAdapter;
import com.cloudmachine.autolayout.widgets.CustomDialog;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.bean.SearchMemberItem;
import com.cloudmachine.helper.MobEvent;
import com.cloudmachine.net.task.GivePermissionNewAsync;
import com.cloudmachine.net.task.SearchMemberAsync;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.ToastUtils;
import com.cloudmachine.utils.UIHelper;
import com.cloudmachine.utils.UMengKey;
import com.cloudmachine.utils.widgets.XListView;
import com.cloudmachine.utils.widgets.XListView.IXListViewListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseAutoLayoutActivity implements OnClickListener, IXListViewListener,
        Callback {

    private Context mContext;
    private Handler mHandler;
    private EditText searchText;
    private ImageView icon_clear;
    private boolean isLoading;
    private InputMethodManager imm;
    private XListView listview;
    //    private McSearchAdapter mAdapter;
    private McSearchMemberAdapter mMemberAdapter;
    private List<SearchMemberItem> memberList = new ArrayList<SearchMemberItem>();
    private int pageNo = 1;
    private int searchListType = -1; //0:搜索机器 1:搜索成员  2:搜索成员移交机主
    private TextView button_cancel;
    private long deviceId;
    private ImageView mBtnBack;


    SearchMemberItem mInfo;
    private TextView emptTv;
    private TextView contentTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;
        mHandler = new Handler(this);
        getIntentData();
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onEvent(this, MobEvent.SEARCH);
    }

    @Override
    public void initPresenter() {

    }

    private void getIntentData() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            searchListType = bundle.getInt(Constants.P_SEARCHLISTTYPE);
            deviceId = bundle.getLong(Constants.DEVICE_ID);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_back:
                this.finish();
                MobclickAgent.onEvent(mContext, UMengKey.time_search_cancel);
                break;
            case R.id.button_cancel:
//                finish();
                clickSearch();
                break;
        }
    }


    private void initView() {
        emptTv = (TextView) findViewById(R.id.noti_empt_tv);
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(this);
        button_cancel = (TextView) findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(this);
        searchText = (EditText) findViewById(R.id.et_search_keyword);
        icon_clear = (ImageView) findViewById(R.id.icon_clear);
        listview = (XListView) findViewById(
                R.id.noti_listview);
        listview.setVisibility(View.GONE);
        imm = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        searchText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        searchText.setHint(getResources().getString(R.string.search_member_hint_message));
        mMemberAdapter = new McSearchMemberAdapter(memberList, searchListType, this, mHandler, deviceId);
        listview.setAdapter(mMemberAdapter);
//		listview.setShowFooterView(false);
        listview.setPullLoadEnable(false);
        listview.setPullRefreshEnable(false);
        listview.setXListViewListener(this);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                icon_clear.setVisibility(View.VISIBLE);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                icon_clear.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    listview.setVisibility(View.GONE);
                    emptTv.setVisibility(View.GONE);
                }
                icon_clear.setVisibility(View.VISIBLE);

            }
        });
        icon_clear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                icon_clear.setVisibility(View.GONE);
            }

        });

        searchText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER & !isLoading) {
                        clickSearch();
                        MobclickAgent.onEvent(mContext, UMengKey.count_search_keyboard);
                    }
                }
                return false;
            }

        });
    }


    public void showAlertView(SearchMemberItem info) {
        mInfo = info;
        Spanned spannd = Html.fromHtml("确定将设备移交给<font color=\"#333333\">\"" + info.getNickName() + "\"</font>，移交之后您将无法管理此设备");
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage(spannd);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("移交", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.sendEmptyMessage(Constants.HANDLER_ADDMEMBER);
            }
        });
        builder.create().show();
    }

    public void addMember(SearchMemberItem info) {
        MobclickAgent.onEvent(this, MobEvent.COUNT_MEMEBER_ADD);
        mInfo = info;
        mHandler.sendEmptyMessage(Constants.HANDLER_ADDMEMBER);

    }


    private Handler blogHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.HANDLER_BLOGSUPPORTLIST_SUCCESS:
                    break;
                case Constants.HANDLER_BLOGSUPPORTLIST_FAIL:
                    Constants.ToastAction((String) msg.obj);
                    break;
            }
        }

    };

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            case Constants.HANDLER_SEARCHMEMBER_SUCCESS:
                isLoading = false;
                if (null != memberList) {
                    memberList.clear();
                    SearchMemberItem info = (SearchMemberItem) msg.obj;
                    if (info != null) {
                        memberList.add(info);
                    }
                    if (memberList.size() > 0) {
                        listview.setVisibility(View.VISIBLE);
                        emptTv.setVisibility(View.GONE);
                        mMemberAdapter.notifyDataSetChanged();
                    } else {
                        listview.setVisibility(View.GONE);
                        emptTv.setVisibility(View.VISIBLE);
                    }
                }
//			listview.setPullLoadEnable(true);
//			listview.setPullLoadEnable(false);
                break;
            case Constants.HANDLER_SEARCHMEMBER_FAIL:
                isLoading = false;
                if (memberList != null)
                    memberList.clear();
                listview.setVisibility(View.GONE);
                emptTv.setVisibility(View.VISIBLE);
                break;
            case Constants.HANDLER_SEARCHBLOG_SUCCESS:
                break;
            case Constants.HANDLER_SEARCHBLOG_FAIL:
                isLoading = false;
                listview.stopRefresh();
                listview.stopLoadMore();
//			mEmptyLayout.showError();
                UIHelper.ToastMessage(mContext, (String) msg.obj);
                break;
            case Constants.HANDLER_ADDMEMBER:
                new GivePermissionNewAsync(mInfo.getMemberId(), deviceId, searchListType, mHandler).execute();
                break;
            case Constants.HANDLER_ADDMEMBER_SUCCESS:
                if (searchListType ==1) {
                    ToastUtils.showToast(this, "添加成功，等待对方确认");
                } else {
                    ToastUtils.showToast(this, "移交成功，等待对方确认");
                }
                finish();
                break;
            case Constants.HANDLER_ADDMEMBER_FAIL:
                Constants.MyToast((String) msg.obj);
                break;


        }
        return false;
    }

    private void clickSearch() {
        String sText = searchText.getText().toString().replaceAll(" ", "");
        if (TextUtils.isEmpty(sText)) {
            ToastUtils.showCenterToast(this, "请输入要搜索的内容");
            return;
        }
        listview.setVisibility(View.VISIBLE);
        isLoading = true;
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        sText = sText.replace("-", "").trim();
        new SearchMemberAsync(mContext, mHandler).execute(sText);

    }

}
