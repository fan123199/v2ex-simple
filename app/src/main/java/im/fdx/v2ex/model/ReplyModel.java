package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 15-9-8.
 * 评论模型，用于传递从JSON获取到的数据。
 * 以后将加入添加评论功能。
 */
public class ReplyModel implements Parcelable {

    private long replyId;
    private String content;
    private String contentRendered;
    private int thanks;
    private long created;
    //    private String author;
//    private String avatarString;
    private MemberModel user;

    protected ReplyModel(Parcel in) {
        replyId = in.readLong();
        content = in.readString();
        contentRendered = in.readString();
        thanks = in.readInt();
        created = in.readLong();
//        author = in.readString();
//        avatarString = in.readString();
        user = in.readParcelable(MemberModel.class.getClassLoader());
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
        dest.writeLong(replyId);
        dest.writeString(content);
        dest.writeString(contentRendered);
        dest.writeInt(thanks);
        dest.writeLong(created);
//        dest.writeString(author);
//        dest.writeString(avatarString);
        dest.writeValue(user);

    }

    public long getReplyId() {
        return replyId;
    }

    public String getContent() {
        return content;
    }

    public String getContentRendered() {
        return contentRendered;
    }

    public int getThanks() {
        return thanks;
    }

    public long getCreated() {
        return created;
    }

    public MemberModel getUser() {
        return user;
    }

    public ReplyModel(MemberModel user, long replyId, String content, int thanks, String contentRendered, long created) {
        this.user = user;
        this.replyId = replyId;
        this.content = content;
        this.thanks = thanks;
        this.contentRendered = contentRendered;
        this.created = created;
    }
}
