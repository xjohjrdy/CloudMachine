package com.cloudmachine.ui.home.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.adapter.AddressBookAdapter;
import com.cloudmachine.adapter.BaseRecyclerAdapter;
import com.cloudmachine.base.BaseAutoLayoutActivity;
import com.cloudmachine.bean.AddressBookItem;
import com.cloudmachine.chart.utils.AppLog;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.DensityUtil;
import com.cloudmachine.utils.locatecity.City;
import com.cloudmachine.utils.locatecity.PingYinUtil;
import com.cloudmachine.widget.LetterIndexView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddressBookActivity extends BaseAutoLayoutActivity implements LetterIndexView.WordChangeListener, BaseRecyclerAdapter.OnItemClickListener {
    private static int MSG_UPDATE_WORD = 0x11;
    @BindView(R.id.address_book_xrlv)
    XRecyclerView addressBookRlv;
    @BindView(R.id.address_book_liv)
    LetterIndexView letterView;
    @BindView(R.id.address_word_tv)
    TextView wordTv;
    @BindView(R.id.address_book_container)
    RelativeLayout addressBookCotainer;
    @BindView(R.id.address_book_empty)
    TextView emtyTv;
    List<AddressBookItem> mItems;
    LinearLayoutManager lm;
    RecyclerView.SmoothScroller smoothScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);
        ButterKnife.bind(this);
        smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        initView();
    }

    private void initView() {
        mItems = getItemList();
        if (mItems != null && mItems.size() > 0) {
            addressBookCotainer.setVisibility(View.VISIBLE);
            emtyTv.setVisibility(View.GONE);
            letterView.setOnWordChangeListener(this);
            lm = new LinearLayoutManager(mContext);
            addressBookRlv.setLayoutManager(lm);
            addressBookRlv.setPullRefreshEnabled(false);
            addressBookRlv.setLoadingMoreEnabled(false);
            AddressBookAdapter adapter = new AddressBookAdapter(mContext, mItems);
            adapter.setOnItemClickListener(this);
            addressBookRlv.setAdapter(adapter);
            final StickyRecyclerHeadersDecoration decoration = new StickyRecyclerHeadersDecoration(adapter);
            addressBookRlv.addItemDecoration(decoration);
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    decoration.invalidateHeaders();
                }
            });
        } else {
            addressBookCotainer.setVisibility(View.GONE);
            emtyTv.setVisibility(View.VISIBLE);


        }
    }

    private List<AddressBookItem> getItemList() {
        List<AddressBookItem> itemList = new ArrayList<>();
        String[] projection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        String[] phoneProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                if (!isAddressName(name)) {
                    continue;
                }
                List<String> mobileList = new ArrayList<>();
                Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneProjection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    do {
                        String phone = phoneCursor.getString(0);
                        String formartPhone = formartMobile(phone);
                        if (!TextUtils.isEmpty(formartPhone)) {
                            mobileList.add(formartPhone);
                        }
                    } while (phoneCursor.moveToNext());
                    phoneCursor.close();
                }
                if (mobileList.size() <= 0) {
                    continue;
                }
                AddressBookItem item = new AddressBookItem();
                item.setName(name);
                item.setMobile(mobileList);
                item.setFirstLetter(getLetterStr(name));
                AppLog.print("name___" + name + "__firstLetter__" + item.getFirstLetter());
                itemList.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        Collections.sort(itemList, comparator);
        return itemList;
    }

    public boolean isAddressName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[A-Za-z0-9\\u4e00-\\u9fa5]+$");
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        // 字符串是否与正则表达式相匹配
        return matcher.matches();
    }

    public String formartMobile(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return mobile;
        }
        mobile = mobile.trim();
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace("+86", "");
        }
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace("-", "");
        }
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace(" ", "");
        }
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace("(", "");
        }
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace(")", "");
        }
        if (!TextUtils.isEmpty(mobile)) {
            mobile = mobile.replace("#", "");
        }
        return mobile;
    }

    public String getLetterStr(String name) {
        char c = name.charAt(0);
        if (c >= 'A' && c < 'Z') {
            return String.valueOf(c);
        } else if (c >= 'a' && c < 'z') {
            return String.valueOf(c).toUpperCase();
        }
        String pyStr = PingYinUtil.getPingYin(name);
        if (TextUtils.isEmpty(pyStr)) {
            return "#";
        }
        return pyStr.substring(0, 1).toUpperCase();
    }


    @Override
    public void initPresenter() {

    }

    @Override
    public void wordChagne(String word) {
        wordTv.setText(word);
        wordTv.setVisibility(View.VISIBLE);
        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++) {
                AddressBookItem item = mItems.get(i);
                if (word.equals(item.getFirstLetter())) {
                    if (lm != null) {
//                        lm.scrollToPosition(i);
                        smoothScroller.setTargetPosition(i);
                        lm.startSmoothScroll(smoothScroller);
                    }
                    break;
                }

            }
        }
        if (mHandler.hasMessages(MSG_UPDATE_WORD)) {
            mHandler.removeMessages(MSG_UPDATE_WORD);
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_WORD, 200);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_WORD) {
                wordTv.setVisibility(View.GONE);
            }
        }
    };
    Comparator comparator = new Comparator<AddressBookItem>() {
        @Override
        public int compare(AddressBookItem lhs, AddressBookItem rhs) {
            String a = lhs.getFirstLetter();
            String b = rhs.getFirstLetter();
            int flag = a.compareTo(b);
            if (flag == 0) {
                return a.compareTo(b);
            } else {
                return flag;
            }
        }
    };

    @Override
    public void onItemClick(View view, int position) {
        Object tagObj = view.getTag();
        if (tagObj instanceof AddressBookItem) {
            AddressBookItem item = ((AddressBookItem) tagObj);
            Intent intent = new Intent();
            intent.putExtra(Constants.NAME, item.getName());
            List<String> mobiles = item.getMobile();
            if (mobiles != null && mobiles.size() > 0) {
                intent.putExtra(Constants.MOBILE, mobiles.get(0));
            }
            setResult(RESULT_OK, intent);
            finish();
        }

    }

}
