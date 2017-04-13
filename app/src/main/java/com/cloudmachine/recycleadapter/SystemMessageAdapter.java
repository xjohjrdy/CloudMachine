package com.cloudmachine.recycleadapter;

import android.content.Context;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudmachine.R;
import com.cloudmachine.struc.MessageBO;
import com.cloudmachine.utils.Constants;
import com.cloudmachine.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2017/4/12 下午8:47
 * 修改人：shixionglu
 * 修改时间：2017/4/12 下午8:47
 * 修改备注：
 */

public class SystemMessageAdapter extends RecyclerView.Adapter<SystemMessageAdapter.SystemMessageHolder> {

    private ArrayList<MessageBO> dataList;
    private Context mContext;
    private LayoutInflater inflater;

    public SystemMessageAdapter(Context context, ArrayList<MessageBO> dataList) {
        this.dataList = dataList;
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public SystemMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = inflater.inflate(R.layout.inner_message_list1, parent, false);
        return new SystemMessageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SystemMessageHolder holder, int position) {
        MessageBO item = dataList.get(position);
        if(item.getMessageType() == Constants.MESSAGETYPE[3]
                || item.getMessageType() == Constants.MESSAGETYPE[4]
                ||item.getMessageType() == Constants.MESSAGETYPE[5]){//系统消息
            holder.arrowRight.setVisibility(View.VISIBLE);
            holder.tv2.setVisibility(View.GONE);
            holder.tv3.setVisibility(View.GONE);
            holder.llSelect.setVisibility(View.GONE);
            holder.dividerBottom.setVisibility(View.GONE);
            holder.dividerTop.setVisibility(View.VISIBLE);
            holder.llDetail.setOnClickListener(new ActionClickListener(3, position, item.getId(), item.getInviterId()));

            switch(item.getStatus()){
                case 1:
                    holder.messageOval.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    holder.messageOval.setVisibility(View.INVISIBLE);
                    break;
            }

            if(item.getMessageType() == Constants.MESSAGETYPE[5]){
                holder.invite_name.setText(item.getTitle());
                holder.time.setText(item.getInviteTime());
                holder.info_inv.setVisibility(View.INVISIBLE);
            }else{
                holder.invite_name.setSingleLine(true);
                holder.invite_name.setText(item.getTitle());
                holder.time.setText(item.getInviteTime());
                holder.info_inv.setText(item.getContent());
            }
            holder.imageView.setImageResource(R.drawable.system1);
        }else{
            holder.arrowRight.setVisibility(View.INVISIBLE);
            holder.tv2.setVisibility(View.VISIBLE);
            holder.tv3.setVisibility(View.VISIBLE);
            holder.llSelect.setVisibility(View.VISIBLE);
            holder.dividerBottom.setVisibility(View.VISIBLE);

            switch(item.getStatus()){
                case 1:
                    holder.messageOval.setVisibility(View.VISIBLE);
                    holder.llSelect.setVisibility(View.VISIBLE);
                    holder.dividerBottom.setVisibility(View.VISIBLE);
                    if (item.getMessageType() == 1) {
                        holder.info_inv.setText("邀请你共享 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 该设备的数据");
                    }else if(item.getMessageType() == 2){
                        holder.info_inv.setText("技师移交 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 给您，请接受");
                    }
                    break;

                case 2:

                    holder.messageOval.setVisibility(View.INVISIBLE);
                    holder.llSelect.setVisibility(View.GONE);
                    holder.dividerTop.setVisibility(View.GONE);
                    if (item.getMessageType() == 1) {
                        holder.info_inv.setText("邀请你共享 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 该设备的数据，已拒绝");
                    }else if(item.getMessageType() == 2){
                        holder.info_inv.setText("技师移交 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 给您，已拒绝");
                    }
                    break;
                case 3:
                    holder.dividerTop.setVisibility(View.GONE);
                    holder.messageOval.setVisibility(View.INVISIBLE);
                    holder.llSelect.setVisibility(View.GONE);

                    if (item.getMessageType() == 1) {
                        holder.info_inv.setText("邀请你共享 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 该设备的数据，已接受");
                    }else if(item.getMessageType() == 2){
                        holder.info_inv.setText("技师移交 ");
                        holder.tv2.setText(item.getDeviceName());
                        holder.tv3.setText(" 给您，已接受");
                    }
                    break;
            }

            holder.invite_name.setText(item.getInviterNickname());
            holder.time.setText(item.getInviteTime());
            ImageLoader.getInstance().displayImage(item.getImgpath(), holder.imageView, Utils.displayImageOptions);
        }

        holder.llaccept.setOnClickListener(new ActionClickListener(1, position, item.getId(), item.getInviterId()));//点击事件
        holder.llrefuse.setOnClickListener(new ActionClickListener(2, position, item.getId(), item.getInviterId()));

    }

    class ActionClickListener implements View.OnClickListener {
        private int position;
        private int type ;
        private long itemId;
        private long inviteId;
        ActionClickListener(int type,int position,long itemId, long inviteId){
            this.type = type;
            this.position = position;
            this.itemId = itemId;
            this.inviteId = inviteId;
        }
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(type == 3){
                Message msg = Message.obtain();
                msg.what = Constants.HANDLER_GOTO_MESSAGECONTENT;
                msg.arg1 = position;
                //myHandler.sendMessage(msg);
            }
            if(null != dataList){
                if (dataList.get(position).getMessageType() != Constants.MESSAGETYPE[5]) {
                    //  new MessageUpdateStatusAsync(type,itemId,inviteId,position,mContext,myHandler).execute();
                }

            }
        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class SystemMessageHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView     time;//时间复用
        private LinearLayout llSelect; //下半部分，接受或者拒绝布局
        private View         dividerBottom;    //底部分割线
        private LinearLayout llDetail; //上半部分，消息详情布局
        private View         messageOval;  //小圆点，标识是否阅读过消息
        private TextView     invite_name;//邀请人昵称，消息页面中标识消息状态
        private TextView     info_inv;    //昵称下面的文字，第一段
        private LinearLayout llaccept;
        private LinearLayout llrefuse;
        private TextView     tv2; //昵称下面的文字，第二段
        private TextView     tv3; //昵称下面的文字，第三段
        private View         dividerTop;
        private ImageView    arrowRight;

        public SystemMessageHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.message_body);//照片
            time = (TextView) itemView.findViewById(R.id.message_time);//系统通知时间
            llSelect = (LinearLayout) itemView.findViewById(R.id.ll_select);
            dividerBottom = itemView.findViewById(R.id.divider2);
            llDetail = (LinearLayout) itemView.findViewById(R.id.ll_detail);
            messageOval = itemView.findViewById(R.id.message_oval);
            invite_name = (TextView) itemView.findViewById(R.id.invite_name);
            info_inv = (TextView) itemView.findViewById(R.id.info_inv);
            llrefuse = (LinearLayout) itemView.findViewById(R.id.ll_resfuse);
            llaccept = (LinearLayout) itemView.findViewById(R.id.ll_accept);
            tv2 = (TextView) itemView.findViewById(R.id.device_name_message);
            tv3 = (TextView) itemView.findViewById(R.id.tv3);
            dividerTop = itemView.findViewById(R.id.divider1);
            arrowRight = (ImageView) itemView.findViewById(R.id.arrow_right);
        }
    }
}
