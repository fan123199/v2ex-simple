package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 15-9-8.
 * 评论模型，用于传递从JSON获取到的数据。
 * 以后将加入添加评论功能。
 */

//{
//        "id" : 2826846,
//        "thanks" : 0,
//        "content" : "关键你是男还是女？",
//        "content_rendered" : "关键你是男还是女？",
//        "member" : {
//        "id" : 27619,
//        "username" : "hengzhang",
//        "tagline" : "我白天是个民工，晚上就是个有抱负的IT人士。",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_mini.png?m=1413707431",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_normal.png?m=1413707431",
//        "avatar_large" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_large.png?m=1413707431"
//        },
//        "created" : 1453030169,
//        "last_modified" : 1453030169
//        }

public class ReplyModel extends BaseModel implements Parcelable {

    private long id;
    private String content;
    private String content_rendered;

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent_rendered(String content_rendered) {
        this.content_rendered = content_rendered;
    }

    public void setThanks(int thanks) {
        this.thanks = thanks;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setMember(MemberModel member) {
        this.member = member;
    }

    private int thanks;
    private long created;
    //    private String author;
//    private String avatarString;
    private MemberModel member;

    protected ReplyModel(Parcel in) {
        id = in.readLong();
        thanks = in.readInt();
        content = in.readString();
        content_rendered = in.readString();
        created = in.readLong();
//        author = in.readString();
//        avatarString = in.readString();
        member = in.readParcelable(MemberModel.class.getClassLoader());
    }
    public ReplyModel() {}


    public static final Creator<ReplyModel> CREATOR = new Creator<ReplyModel>() {
        @Override
        public ReplyModel createFromParcel(Parcel in) {
            return new ReplyModel(in);
        }

        @Override
        public ReplyModel[] newArray(int size) {
            return new ReplyModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeString(content_rendered);
        dest.writeInt(thanks);
        dest.writeLong(created);
//        dest.writeString(author);
//        dest.writeString(avatarString);
        dest.writeValue(member);

    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getContent_rendered() {
        return content_rendered;
    }

    public int getThanks() {
        return thanks;
    }

    public long getCreated() {
        return created;
    }

    public MemberModel getMember() {
        return member;
    }

    public ReplyModel(MemberModel user, long replyId, String content, int thanks, String contentRendered, long created) {
        this.member = user;
        this.thanks = thanks;
        this.id = replyId;
        this.content = content;
        this.content_rendered = contentRendered;
        this.created = created;
    }

    @Override
    public String toString() {
        return "ReplyModel{" +
                "content='" + content_rendered + '\'' +
                '}';
    }
}
