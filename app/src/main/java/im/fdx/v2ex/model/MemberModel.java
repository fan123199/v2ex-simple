package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 16-1-16.
 * V2ex 的 个人信息 模型
 */
public class MemberModel implements Parcelable {

    long userId;
    String userName;
    String tagLine;
    String avatarMini;
    String avatarNormal;
    String avatarLarge;


    protected MemberModel(Parcel in) {
        userId = in.readLong();
        userName = in.readString();
        tagLine = in.readString();
        avatarMini = in.readString();
        avatarNormal = in.readString();
        avatarLarge = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(userName);
        dest.writeString(tagLine);
        dest.writeString(avatarMini);
        dest.writeString(avatarNormal);
        dest.writeString(avatarLarge);
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

    public MemberModel(long userId, String username, String tagline) {
        this.userId = userId;
        this.userName = username;
        this.tagLine = tagline;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public String getAvatarMini() {
        return avatarMini;
    }

    public String getAvatarNormal() {
        return avatarNormal;
    }

    public String getAvatarLarge() {
        return avatarLarge;
    }
}
