package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fdx on 2017/3/24.
 */

public class ProfileMode extends BaseModel implements Parcelable {

    private String username;
    private String avatar;
    private int notifications;
    private int followings;
    private int nodeCollections;
    private int topicCollections;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.avatar);
        dest.writeInt(this.notifications);
        dest.writeInt(this.followings);
        dest.writeInt(this.nodeCollections);
        dest.writeInt(this.topicCollections);
    }

    public ProfileMode() {
    }

    protected ProfileMode(Parcel in) {
        this.username = in.readString();
        this.avatar = in.readString();
        this.notifications = in.readInt();
        this.followings = in.readInt();
        this.nodeCollections = in.readInt();
        this.topicCollections = in.readInt();
    }

    public static final Parcelable.Creator<ProfileMode> CREATOR = new Parcelable.Creator<ProfileMode>() {
        @Override
        public ProfileMode createFromParcel(Parcel source) {
            return new ProfileMode(source);
        }

        @Override
        public ProfileMode[] newArray(int size) {
            return new ProfileMode[size];
        }
    };
}
