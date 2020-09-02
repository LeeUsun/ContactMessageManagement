package cn.bmob.sdkdemo.bean;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.bmob.sdkdemo.Contact;
import cn.bmob.sdkdemo.adapter.ContactBaseMultiItemQuickAdapter;

public class ContactHeader extends AbstractExpandableItem<Contact> implements MultiItemEntity {

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
        return ContactBaseMultiItemQuickAdapter.TYPE_LEVEL_0;
    }
}
