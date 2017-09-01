package com.cloudmachine.net.task;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.cloudmachine.net.ATask;
import com.cloudmachine.net.HttpURLConnectionImp;
import com.cloudmachine.net.IHttp;
import com.cloudmachine.bean.BaseBO;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.MemeberKeeper;
import com.cloudmachine.utils.Utils;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/3/7 上午12:34
 * 修改人：shixionglu
 * 修改时间：2017/3/7 上午12:34
 * 修改备注：
 */

public class SaveArrivalNoticeAsync extends ATask {

    private Handler handler;
    private Context context;
    private String product_id;

    public SaveArrivalNoticeAsync(Handler handler, Context context, String product_id) {
        this.handler = handler;
        this.context = context;
        this.product_id = product_id;

        try{
            memberId = String.valueOf(MemeberKeeper.getOauth(context).getId());
        }catch(Exception ee){

        }
    }

    @Override
    protected String doInBackground(String... params) {
        IHttp httpRequest = new HttpURLConnectionImp();
        String result = null;
        try {
            result = httpRequest.post(Constants.URL_SAVEARRIVALNOTICE, initListData());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        decodeJson(result);
    }

    @Override
    protected void decodeJson(String result) {
        // TODO Auto-generated method stub
        super.decodeJson(result);

        Message msg = Message.obtain();
        if (isSuccess) {
            try{
                Gson gson = new Gson();
                BaseBO baseBo = gson.fromJson(result,
                        BaseBO.class);
                msg.what = Constants.HANDLER_SAVEARRIVALNOTICE_SUCCESS;
                msg.obj = baseBo.getMessage();
                handler.sendMessage(msg);
                return;
            }catch(Exception e){
            }
        }
        //缓存数据第4步
        if(!isHaveCache){
            msg.what = Constants.HANDLER_SAVEARRIVALNOTICE_FAILD;
            msg.obj = message;
            handler.sendMessage(msg);
        }
    }

    private List<NameValuePair> initListData() {

        List<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(Utils.addBasicValue("memberId", memberId));
        list.add(Utils.addBasicValue("product_id", product_id));

        return list;
    }

}
