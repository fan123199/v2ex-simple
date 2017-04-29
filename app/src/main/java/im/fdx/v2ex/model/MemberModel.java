package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by a708 on 16-1-16.
 * V2ex 的 个人信息 模型
 *
 * username 和 id 都是 key value
 */

//{
//        "status" : "found",
//        "id" : 32044,
//        "url" : "http://www.v2ex.com/member/cxshun",
//        "username" : "cxshun",
//        "website" : "http://www.chenxiaoshun.com/",
//        "twitter" : "cxshun",
//        "psn" : "",
//        "github" : "cxshun",
//        "btc" : "",
//        "location" : "",
//        "tagline" : "",
//        "bio" : "",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_mini.png?m=1369031007",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_normal.png?m=1369031007",
//        "avatar_large" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_large.png?m=1369031007",
//        "created" : 1357733451
//        }

@Entity
public class MemberModel extends BaseModel implements Parcelable {

    @Id
    long id;
    private String username;
    private String tagline;
    private String avatar_mini;
    private String avatar_normal;
    private String avatar_large;
    private String github;
    private String btc;
    private String location;
    private String bio;

    public String getGithub() {
        return github;
    }

    public String getBtc() {
        return btc;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getWebsite() {
        return website;
    }

    public String getCreated() {
        return created;
    }

    private String twitter;
    private String website;
    private String created;


    public MemberModel(long id, String username) {
        this.id = id;
        this.username = username;
    }

    @Generated(hash = 2085626243)
    public MemberModel(long id, String username, String tagline, String avatar_mini,
            String avatar_normal, String avatar_large, String github, String btc,
            String location, String bio, String twitter, String website, String created) {
        this.id = id;
        this.username = username;
        this.tagline = tagline;
        this.avatar_mini = avatar_mini;
        this.avatar_normal = avatar_normal;
        this.avatar_large = avatar_large;
        this.github = github;
        this.btc = btc;
        this.location = location;
        this.bio = bio;
        this.twitter = twitter;
        this.website = website;
        this.created = created;
    }

    @Generated(hash = 1847833359)
    public MemberModel() {
    }

    public long getId() {
        return id;
    }

    public String getTagline() {
        return tagline;
    }

    public String getAvatarNormalUrl() {
        return "http:" + avatar_normal;
    }

    public String getAvatarLargeUrl() {
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

    public String getUsername() {
        return this.username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.username);
        dest.writeString(this.tagline);
        dest.writeString(this.avatar_mini);
        dest.writeString(this.avatar_normal);
        dest.writeString(this.avatar_large);
        dest.writeString(this.github);
        dest.writeString(this.btc);
        dest.writeString(this.location);
        dest.writeString(this.bio);
        dest.writeString(this.twitter);
        dest.writeString(this.website);
        dest.writeString(this.created);
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBtc(String btc) {
        this.btc = btc;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    protected MemberModel(Parcel in) {
        this.id = in.readLong();
        this.username = in.readString();
        this.tagline = in.readString();
        this.avatar_mini = in.readString();
        this.avatar_normal = in.readString();
        this.avatar_large = in.readString();
        this.github = in.readString();
        this.btc = in.readString();
        this.location = in.readString();
        this.bio = in.readString();
        this.twitter = in.readString();
        this.website = in.readString();
        this.created = in.readString();
    }

    public static final Creator<MemberModel> CREATOR = new Creator<MemberModel>() {
        @Override
        public MemberModel createFromParcel(Parcel source) {
            return new MemberModel(source);
        }

        @Override
        public MemberModel[] newArray(int size) {
            return new MemberModel[size];
        }
    };

    @Override
    public BaseModel parse() {
        return null;
    }
}
