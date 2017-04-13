package com.cloudmachine.ui.homepage.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cloudmachine.R;
import com.cloudmachine.activities.MessageContentActivity;
import com.cloudmachine.activities.WebviewActivity;
import com.cloudmachine.adapter.MessageAdapter;
import com.cloudmachine.app.MyApplication;
import com.cloudmachine.base.baserx.RxBus;
import com.cloudmachine.base.baserx.RxConstants;
import com.cloudmachine.net.task.AllMessagesListAsync;
import com.cloudmachine.net.task.QuestionMessageAsync;
import com.cloudmachine.struc.MessageBO;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.ResV;
import com.cloudmachine.utils.UIHelper;
import com.cloudmachine.utils.widgets.XListView;
import com.cloudmachine.utils.widgets.XListView.IXListViewListener;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends Fragment implements Callback, IXListViewListener{  

	private XListView listView;
	private Context mContext;
	private Handler mHandler;
	private int pageNo = 1;
	private List<MessageBO> dataList = new ArrayList<MessageBO>();
	private MessageAdapter adapter;
	private View viewParent;
	private View empty_layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != viewParent) {
            ViewGroup parent = (ViewGroup) viewParent.getParent();
            if (null != parent) {
                parent.removeView(viewParent);
            }
        } else {
        	viewParent = inflater.inflate(R.layout.activity_message, null);
        	initRootView();// 控件初始化
        }
		return viewParent;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
	private void initRootView(){
		mContext = getActivity();
		mHandler = new Handler(this);
		initTitleLayout();
		listView = (XListView) viewParent.findViewById(R.id.noti_listview);
		listView.setPullRefreshEnable(true);
		listView.setPullLoadEnable(false);
		listView.setXListViewListener(this);
		adapter = new MessageAdapter(mContext, dataList,mHandler);
		listView.setAdapter(adapter);
		
		empty_layout =  viewParent.findViewById(R.id.empty_layout);
		
		/*acceptlayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (dataList.size()>0) {
					for (int i = 0; i < dataList.size(); i++) {
						MessageBO item = dataList.get(i);
						new MessageUpdateStatusAsync(1,item.getId(),item.getInviterId(),mContext,mHandler).execute();
					}		
				}
			
			}
		});*/
		pageNo = 1;
		
		showData();
	}
	
	private void initTitleLayout(){
		
		/*title_layout = (TitleView)viewParent.findViewById(title_layout);
		title_layout.setTitle(getResources().getString(R.string.main_bar_text2));
		title_layout.setLeftLayoutVisibility(View.GONE);*/
	}
	private void showData(){
		if(null != dataList && dataList.size()>0){
			listView.setVisibility(View.VISIBLE);
			empty_layout.setVisibility(View.GONE);
		}else{
			listView.setVisibility(View.GONE);
			empty_layout.setVisibility(View.VISIBLE);
		}
	}

	private OnClickListener mErrorClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new AllMessagesListAsync(pageNo,mContext, mHandler).execute();
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what){
		case Constants.HANDLER_GETALLMESSAGELIST_SUCCESS:
			listView.stopRefresh();
			listView.stopLoadMore();
			List<MessageBO> messageList = (List<MessageBO>)msg.obj;
			if(pageNo == 1){
				dataList.clear();
			}

			dataList.addAll(messageList);
			if(dataList.size()>=MyApplication.getInstance().getPageSize()){
				listView.setPullLoadEnable(true);
			}
			adapter.notifyDataSetChanged();
			showData();
			break;
		case Constants.HANDLER_GETALLMESSAGELIST_FAIL:
			if(pageNo == 1){
				dataList.clear();
				adapter.notifyDataSetChanged();
			}
			listView.stopRefresh();
			listView.stopLoadMore();
			showData();
			break;
		case Constants.HANDLER_QUESTION_SUCCESS:
			MessageBO questionMessage = (MessageBO)msg.obj;
			if(null != dataList){
				questionMessage.setMessageType(5);
				dataList.add(0, questionMessage);
				adapter.notifyDataSetChanged();
				/*if(null != getActivity())
					((MainActivity)getActivity()).updateQuestion(true);*/
			}else{
				/*if(null != getActivity())
					((MainActivity)getActivity()).updateQuestion(false);*/
			}
			break;
		case Constants.HANDLER_QUESTION_FAIL:
			/*if(null != getActivity())
				((MainActivity)getActivity()).updateQuestion(false);*/
			break;
		case Constants.HANDLER_MESSAGEACCEPTE_SUCCESS:
			/*((MainActivity)getActivity()).updateDevice();*/
			//添加设备成功，通知刷新设备页面
			RxBus.getInstance().post(RxConstants.REFRESH_DEVICE_FRAGMENT,"");
			Constants.isChangeDevice = true;
			Constants.isAddDevice = true;
			UIHelper.ToastMessage(mContext, "已经同意该邀请");
			dataList.get(msg.arg1).setStatus(3);
			adapter.notifyDataSetChanged();
			break;
		case Constants.HANDLER_MESSAGEREFUSE_SUCCESS:
			dataList.get(msg.arg1).setStatus(2);
			adapter.notifyDataSetChanged();
			UIHelper.ToastMessage(mContext, "已经拒绝该邀请");
			break;
		case Constants.HANDLER_MESSAGEUPSTATE_SUCCESS:
			dataList.get(msg.arg1).setStatus(4);
			adapter.notifyDataSetChanged();
			break;
		case Constants.HANDLER_MESSAGEACTION_FAIL:
			break;
		case Constants.HANDLER_GOTO_MESSAGECONTENT:
			if(null!= dataList){
				MessageBO bo = dataList.get(msg.arg1);
				if(null != bo){
					if(bo.getMessageType() == Constants.MESSAGETYPE[5]){
						Bundle bundle = new Bundle();
						bundle.putString(Constants.P_WebView_Url, bo.getMessage());
						bundle.putString(Constants.P_WebView_Title, ResV.getString(R.string.question_title));
						Constants.toActivity(getActivity(), WebviewActivity.class, bundle);
					}else{
						Intent it = new Intent(getActivity(), MessageContentActivity.class);
						it.putExtra("title", bo.getTitle());
						it.putExtra("content", bo.getContent());
						startActivity(it);
					}
				}
			}
			
			
			break;
		default:
			break;
			
		}
		return false;
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		pageNo=1;
		dataList.clear();
		new AllMessagesListAsync(pageNo,mContext, mHandler).execute();
		new QuestionMessageAsync(mContext, mHandler).execute();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		pageNo++;
		new AllMessagesListAsync(pageNo,mContext, mHandler).execute();
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new AllMessagesListAsync(pageNo,mContext, mHandler).execute();
		new QuestionMessageAsync(mContext, mHandler).execute();
	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	



}