package cn.bmob.sdkdemo;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.bmob.sdkdemo.adapter.MessageBaseMultiItemQuickAdapter;
import cn.bmob.v3.BmobObject;

/**
 * 创建javaBean
 *
 * @author smile
 * @ClassName: Person
 * @Description: TODO
 * @date 2014-5-20 下午4:12:55
 */
public class dxbmob extends BmobObject implements MultiItemEntity {

    private String phone;
    private String nr;
    private String person;
    private String code;

    public String getphone() {
        return phone;
    }

    public void setphone(String phone) {
        this.phone = phone;
    }

    public String getnr() {
        return nr;
    }

    public void setnr(String nr) {
        this.nr = nr;
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
                "phone='" + phone + '\'' +
                ", nr='" + nr + '\'' +
                ", person='" + person + '\'' +
                ", code='" + code + '\'' ;
    }

    @Override
    public int getItemType() {
        return MessageBaseMultiItemQuickAdapter.TYPE_LEVEL_1;
    }
}
