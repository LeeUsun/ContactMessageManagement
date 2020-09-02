package cn.bmob.sdkdemo.adapter;

import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.SmsInboxBean;
import cn.bmob.sdkdemo.dxbmob;

public class MessageAdapter extends BaseQuickAdapter<dxbmob, BaseViewHolder> {

    public MessageAdapter(int layoutResId, @Nullable List<dxbmob> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, dxbmob item) {
        helper.setText(R.id.tv_phone, "号码：" + item.getphone())
                .setText(R.id.tv_content, "内容：" + item.getnr());
    }
}
