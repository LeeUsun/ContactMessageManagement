package cn.bmob.sdkdemo.bean;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.bmob.sdkdemo.adapter.MessageBaseMultiItemQuickAdapter;
import cn.bmob.sdkdemo.dxbmob;

public class dxbmobHeader extends AbstractExpandableItem<dxbmob> implements MultiItemEntity {

    private String person;
    private String code;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return MessageBaseMultiItemQuickAdapter.TYPE_LEVEL_0;
    }
}
