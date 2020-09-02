package cn.bmob.sdkdemo;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.bmob.sdkdemo.adapter.ContactBaseMultiItemQuickAdapter;
import cn.bmob.v3.BmobObject;

public class Contact extends BmobObject implements MultiItemEntity {

    private String name;
    private String phone;
    private String person;
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

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
    public String toString() {
        return
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", person='" + person + '\'' +
                ", code='" + code + '\'' ;
    }

    @Override
    public int getItemType() {
        return ContactBaseMultiItemQuickAdapter.TYPE_LEVEL_1;
    }
}
