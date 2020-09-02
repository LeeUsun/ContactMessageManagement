package cn.bmob.sdkdemo.adapter;

import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.sdkdemo.Contact;
import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.bean.ContactBean;

public class ContactAdapter extends BaseQuickAdapter<Contact, BaseViewHolder> {
    public ContactAdapter(int layoutResId, @Nullable List<Contact> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, Contact item) {
        helper.setText(R.id.tv_phone, "号码：" + item.getPhone())
                .setText(R.id.tv_name, "姓名：" + item.getName());
    }
}
