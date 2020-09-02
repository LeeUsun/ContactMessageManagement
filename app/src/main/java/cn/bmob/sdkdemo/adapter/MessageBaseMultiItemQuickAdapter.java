package cn.bmob.sdkdemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.bean.dxbmobHeader;
import cn.bmob.sdkdemo.dxbmob;

public class MessageBaseMultiItemQuickAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    private Context context;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public MessageBaseMultiItemQuickAdapter(Context context, List<MultiItemEntity> data) {
        super(data);
        this.context = context;
        addItemType(TYPE_LEVEL_0, R.layout.item_message_header);
        addItemType(TYPE_LEVEL_1, R.layout.item_message);
    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_0:
                final dxbmobHeader lv0 = (dxbmobHeader) item;
                helper.setText(R.id.tv_phone, lv0.getPerson())
                        .setText(R.id.tv_code, lv0.getCode());
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = helper.getAdapterPosition();
                        if (lv0.isExpanded()) {
                            collapse(pos);
                        } else {
//                            if (pos % 3 == 0) {
//                                expandAll(pos, false);
//                            } else {
                            expand(pos);
//                            }
                        }
                    }
                });
                helper.addOnClickListener(R.id.btn_delete, R.id.btn_download);

                break;
            case TYPE_LEVEL_1:
                final dxbmob dxbmob1 = (dxbmob) item;
                helper.setText(R.id.tv_phone, "手机：" + dxbmob1.getphone())
                        .setText(R.id.tv_content, "内容：" + dxbmob1.getnr());
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("短信详情")
                                .setMessage("号码：" + dxbmob1.getphone() + "\n\n"
                                        + "内容：" + dxbmob1.getnr())
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        builder.show();
                    }
                });


                break;
        }
    }
}
