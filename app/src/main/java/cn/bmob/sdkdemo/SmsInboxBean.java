package cn.bmob.sdkdemo;

public class SmsInboxBean {

    private String date;
    private String name;
    private String number;
    private String body;
    private String type;

    public SmsInboxBean() {
    }

    public SmsInboxBean(String date, String name, String number, String body, String type) {
        this.date = date;
        this.name = name;
        this.number = number;
        this.body = body;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SmsInboxBean{" +
                "date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", body='" + body + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
