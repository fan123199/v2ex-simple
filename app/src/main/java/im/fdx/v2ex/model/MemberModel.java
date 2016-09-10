package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by a708 on 16-1-16.
 * V2ex 的 个人信息 模型
 */
@Entity
public class MemberModel implements Parcelable {

    @Id
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

    @Generated(hash = 1826227830)
    public MemberModel(long id, String username, String tagline,
            String avatar_mini, String avatar_normal, String avatar_large) {
        this.id = id;
        this.username = username;
        this.tagline = tagline;
        this.avatar_mini = avatar_mini;
        this.avatar_normal = avatar_normal;
        this.avatar_large = avatar_large;
    }

    @Generated(hash = 1847833359)
    public MemberModel() {
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

    public String getAvatar_large() {
        return this.avatar_large;
    }

    public void setAvatar_large(String avatar_large) {
        this.avatar_large = avatar_large;
    }

    public String getAvatar_normal() {
        return this.avatar_normal;
    }

    public void setAvatar_normal(String avatar_normal) {
        this.avatar_normal = avatar_normal;
    }

    public String getAvatar_mini() {
        return this.avatar_mini;
    }

    public void setAvatar_mini(String avatar_mini) {
        this.avatar_mini = avatar_mini;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(long id) {
        this.id = id;
    }
}
