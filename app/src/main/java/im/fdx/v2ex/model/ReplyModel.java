package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 15-9-8.
 * 评论模型，用于传递从JSON获取到的数据。
 * 以后将加入添加评论功能。
 */
public class ReplyModel implements Parcelable {

    public long id;
    public String content;
    public String contentRendered;
    public int thanks;
    public long created;
    public String author;
    public String avatarString;

    protected ReplyModel(Parcel in) {
        id = in.readLong();
        content = in.readString();
        contentRendered = in.readString();
        thanks = in.readInt();
        created = in.readLong();
        author = in.readString();
        avatarString = in.readString();
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
        dest.writeString(contentRendered);
        dest.writeInt(thanks);
        dest.writeLong(created);
        dest.writeString(author);
        dest.writeString(avatarString);

    }

    public ReplyModel(long id,String content,int thanks,long created, String author, String avatarString) {
        this.id = id;
        this.content = content;
        this.thanks = thanks;
        this.created = created;
        this.author = author;
        this.avatarString = avatarString;
    }
}
