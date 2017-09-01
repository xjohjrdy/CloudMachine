package com.cloudmachine.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.bean.CouponInfo;

import java.util.ArrayList;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/2/22 下午2:12
 * 修改人：shixionglu
 * 修改时间：2017/2/22 下午2:12
 * 修改备注：
 */

public class GetCouponAdapter extends BaseAdapter {

    private LayoutInflater         layoutInflater;
    private  Context               context;
    private  ArrayList<CouponInfo> dataList;
    private int couponState;

    public GetCouponAdapter(Context context, ArrayList<CouponInfo> dataList, int couponState) {
        this.context = context;
        this.dataList = dataList;
        this.couponState = couponState;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            if (couponState == 0) {
                convertView = layoutInflater.inflate(R.layout.list_item_coupon, null);
            } else if (couponState == 2) {
                convertView = layoutInflater.inflate(R.layout.list_item_invali_coupon, null);
            }
            holder = new ViewHolder();
            holder.tvPrice = (TextView) convertView.findViewById(R.id.price);
            holder.tvUseType = (TextView) convertView.findViewById(R.id.tv_userType);
            holder.tvlimitInfo = (TextView) convertView.findViewById(R.id.limitInfo);
            holder.validiteTime = (TextView) convertView.findViewById(R.id.tv_valadite_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvPrice.setText("¥ "+dataList.get(position).getAmount());
        holder.tvUseType.setText(dataList.get(position).getUserType());
        holder.tvlimitInfo.setText(dataList.get(position).getLimitInfo());
        holder.validiteTime.setText(new StringBuffer().append(dataList.get(position).getStartTime())
                .append("~").append(dataList.get(position).getEndTime()));
        return convertView;
    }

    static class ViewHolder {

        TextView tvPrice;
        TextView tvUseType;
        TextView tvlimitInfo;
        TextView validiteTime;
    }
}
