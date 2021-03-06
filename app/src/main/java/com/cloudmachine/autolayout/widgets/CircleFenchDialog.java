package com.cloudmachine.autolayout.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.cloudmachine.R;

/**
 * 项目名称：CloudMachine
 * 类描述：
 * 创建人：shixionglu
 * 创建时间：2016/12/9 上午10:18
 * 修改人：shixionglu
 * 修改时间：2016/12/9 上午10:18
 * 修改备注：
 */

public class CircleFenchDialog  implements View.OnClickListener{

    private Context context;
    public Dialog dialog;
    public TextView title, message, cancle, ok;
    private CircleFenchDialogOnClick mCircleFenchDialogOnClick;
    private CancelFenchDialogOnClick mCancelFenchDialogOnClick;
    private final EditText mRange;

    public CircleFenchDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context, R.style.CircleFenchDialog);//Dialog的Style
        Window window = dialog.getWindow();
        window.setContentView(R.layout.circle_fench_dialog);//引用Dialog的布局

        mRange = (EditText) window.findViewById(R.id.et_circle_fench);
        cancle = (TextView) window.findViewById(R.id.btn_cancel);
        ok = (TextView) window.findViewById(R.id.btn_ok);

        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
                mCircleFenchDialogOnClick.ok(mRange);
                dialog.dismiss();
                break;
            case R.id.btn_cancel:
                mCancelFenchDialogOnClick.cancel();
                dialog.dismiss();
                break;

        }
    }
    //给确认按钮设置回调的接口
    public interface CircleFenchDialogOnClick{
        void ok(View view);
    }

    public void setMyDialogOnClick(CircleFenchDialogOnClick mCircleFenchDialogOnClick){
        this.mCircleFenchDialogOnClick =mCircleFenchDialogOnClick;
    }

    public interface CancelFenchDialogOnClick {
        void cancel();
    }

    public void setMyDialogCancelOnClick(CancelFenchDialogOnClick mCancelFenchDialogOnClick) {
        this.mCancelFenchDialogOnClick = mCancelFenchDialogOnClick;
    }

    public void setEditText(String range) {
        mRange.setText(range);
    }
}
