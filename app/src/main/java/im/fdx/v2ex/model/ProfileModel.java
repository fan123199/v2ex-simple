package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fdx on 2017/3/24.
 */

public class ProfileModel extends BaseModel implements Parcelable {

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

    public ProfileModel() {
    }

    protected ProfileModel(Parcel in) {
        this.username = in.readString();
        this.avatar = in.readString();
        this.notifications = in.readInt();
        this.followings = in.readInt();
        this.nodeCollections = in.readInt();
        this.topicCollections = in.readInt();
    }

    public static final Parcelable.Creator<ProfileModel> CREATOR = new Parcelable.Creator<ProfileModel>() {
        @Override
        public ProfileModel createFromParcel(Parcel source) {
            return new ProfileModel(source);
        }

        @Override
        public ProfileModel[] newArray(int size) {
            return new ProfileModel[size];
        }
    };

    @Override
    public BaseModel parse() {
        return null;
    }
}
