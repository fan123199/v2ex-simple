package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import im.fdx.v2ex.ui.main.TopicModel;

/**
 * Created by fdx on 2017/3/24.
 */

public class NotificationModel extends BaseModel implements Parcelable {


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getReplyPosition() {
        return replyPosition;
    }

    public void setReplyPosition(String replyPosition) {
        this.replyPosition = replyPosition;
    }

    //暂时不用，因为网页端也不跳转
    private String replyPosition;
    private String type;
    private TopicModel topic;
    private MemberModel member;
    private String content;
    private String id;
    private String time;


    public NotificationModel() {
    }

    public TopicModel getTopic() {
        return topic;
    }

    public void setTopic(TopicModel topic) {
        this.topic = topic;
    }

    public MemberModel getMember() {
        return member;
    }

    public void setMember(MemberModel member) {
        this.member = member;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "NotificationModel{" +
                "type='" + type + '\'' +
                ", topic=" + topic +
                ", member=" + member +
                ", content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.replyPosition);
        dest.writeString(this.type);
        dest.writeParcelable(this.topic, flags);
        dest.writeParcelable(this.member, flags);
        dest.writeString(this.content);
        dest.writeString(this.id);
        dest.writeString(this.time);
    }

    protected NotificationModel(Parcel in) {
        this.replyPosition = in.readString();
        this.type = in.readString();
        this.topic = in.readParcelable(TopicModel.class.getClassLoader());
        this.member = in.readParcelable(MemberModel.class.getClassLoader());
        this.content = in.readString();
        this.id = in.readString();
        this.time = in.readString();
    }

    public static final Creator<NotificationModel> CREATOR = new Creator<NotificationModel>() {
        @Override
        public NotificationModel createFromParcel(Parcel source) {
            return new NotificationModel(source);
        }

        @Override
        public NotificationModel[] newArray(int size) {
            return new NotificationModel[size];
        }
    };

    @Override
    public BaseModel parse() {
        return null;
    }
}
