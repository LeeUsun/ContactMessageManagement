package cn.bmob.sdkdemo;

import cn.bmob.v3.BmobObject;

public class bmob extends BmobObject {

    private String name;
    private long phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }
}
