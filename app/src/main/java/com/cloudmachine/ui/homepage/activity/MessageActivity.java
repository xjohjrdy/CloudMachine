package com.cloudmachine.ui.homepage.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.cloudm.autolayout.AutoLayoutFragmentActivity;
import com.cloudmachine.R;
import com.cloudmachine.autolayout.widgets.TitleView;
import com.cloudmachine.utils.Constants;

/**
 * 项目名称：CloudMachine
 * 类描述：消息页面activity
 * 创建人：shixionglu
 * 创建时间：2017/3/27 下午4:09
 * 修改人：shixionglu
 * 修改时间：2017/3/27 下午4:09
 * 修改备注：
 */

public class MessageActivity extends AutoLayoutFragmentActivity implements View.OnClickListener {

    private TitleView titleLayout;
    private Context   mContext;
    private View[]     linearLayouts    = new View[2];
    private TextView[] textViews        = new TextView[2];
    private Fragment   mFragments[]     = new Fragment[2]; // 存储页面的数组
    private boolean    isInitFragment[] = new boolean[2];
    private Fragment mContentFragment; // 当前fragment
    private int currentFragment = 0; // 当前Fragment的索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Constants.MyLog("进入消息页面");
        mContext = this;
        initView();
        initFragmentS(); // 初始化fragment
    }

    private void initFragmentS() {

        /*mFragments[0] = new AvailableCouponsFragment();
        mFragments[1] = new InvalidCouponFragment();*/
        mContentFragment = null;
        switchContent(currentFragment);
    }

    private void switchContent(int n) {

        currentFragment = n;
        int len = mFragments.length;
        if (n < len && n >= 0) {
            // int size = imageViews.length;
            for (int i = 0; i < len; i++) {
                if (i == n) {
                    // imageViews[i].setSelected(true);
                    linearLayouts[i].setSelected(false);
                    textViews[i].setTextColor(getResources().getColor(
                            R.color.main_bar_text_dw));
                } else {
                    // imageViews[i].setSelected(false);
                    linearLayouts[i].setSelected(false);
                    textViews[i].setTextColor(getResources().getColor(
                            R.color.main_bar_text_nm));
                }
            }

            Fragment to = mFragments[n];
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
            }
        }
    }

    private void initView() {

        titleLayout = (TitleView) findViewById(R.id.title_layout);
        titleLayout.setLeftImage(-1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleLayout.setTitle("消息");

        linearLayouts[0] = findViewById(R.id.tab_one_layout);
        linearLayouts[1] = findViewById(R.id.tab_two_layout);
        textViews[0] = (TextView) findViewById(R.id.tab_one_text);
        textViews[1] = (TextView) findViewById(R.id.tab_two_text);
        linearLayouts[0].setOnClickListener(this);
        linearLayouts[1].setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tab_one_layout:
                switchContent(0);
                break;
            case R.id.tab_two_layout:
                switchContent(1);
                break;
        }
    }
}