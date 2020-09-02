package cn.bmob.sdkdemo;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.*;

/** 创建javaBean
 * @ClassName: Person
 * @Description: TODO
 * @author smile
 * @date 2014-5-20 下午4:12:55
 */
class bmobphoto extends BmobObject {


    private BmobFile photo;

    private String name;
    public bmobphoto(){

    }

    public bmobphoto( BmobFile photo,String name){
        this.photo = photo;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BmobFile getFile() {
        return photo;
    }

    public void setFile(BmobFile photo) {
        this.photo = photo;
    }
}

