package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 16-1-16.
 * V2ex 的 个人信息 模型
 */
public class MemberModel implements Parcelable {

    long id;
    String username;
    String tagline;
    String avatar_mini;
    String avatar_normal;
    String avatar_large;


    protected MemberModel(Parcel in) {
        id = in.readLong();
        username = in.readString();
        tagline = in.readString();
        avatar_mini = in.readString();
        avatar_normal = in.readString();
        avatar_large = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(username);
        dest.writeString(tagline);
        dest.writeString(avatar_mini);
        dest.writeString(avatar_normal);
        dest.writeString(avatar_large);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MemberModel> CREATOR = new Creator<MemberModel>() {
        @Override
        public MemberModel createFromParcel(Parcel in) {
            return new MemberModel(in);
        }

        @Override
        public MemberModel[] newArray(int size) {
            return new MemberModel[size];
        }
    };

    public MemberModel(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTagline() {
        return tagline;
    }

    public String getAvatarNormal() {
        return "http:" + avatar_normal;
    }

    public String getAvatarLarge() {
        return "http:" + avatar_large;
    }

    public String getAvatarMiniUrl() {
        return "http:" + avatar_mini;
    }
}
