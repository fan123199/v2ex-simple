package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import im.fdx.v2ex.utils.ContentUtils;
import im.fdx.v2ex.utils.L;

/**
 * Created by a708 on 15-8-18.
 */
public class TopicModel implements Parcelable {

    public int id;
    public String title;
    public String url;
    public String content;
    public String contentRendered;
    public int replies;
//    public MemberModel member;
//    public NodeModel node;
    public long created;
    public long lastModified;
    public long lastTouched;

    public void parse(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        title = jsonObject.getString("title");
        url = jsonObject.getString("url");
        content = jsonObject.getString("content");
        contentRendered = ContentUtils.formatContent(jsonObject.getString("content_rendered"));
        replies = jsonObject.getInt("replies");
//        member = new MemberModel();
//        member.parse(jsonObject.getJSONObject("member"));
//        node = new NodeModel();
//        node.parse(jsonObject.getJSONObject("node"));
        created = jsonObject.getLong("created");
        lastModified = jsonObject.getLong("last_modified");
        lastTouched = jsonObject.getLong("last_touched");
    }
    protected TopicModel(Parcel in) {

    }

    public static final Creator<TopicModel> CREATOR = new Creator<TopicModel>() {
        @Override
        public TopicModel createFromParcel(Parcel in) {
            L.m("Create from parcel : TopicModel");
            return new TopicModel(in);
        }

        @Override
        public TopicModel[] newArray(int size) {
            return new TopicModel[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{
                id,replies
        });
        dest.writeStringArray(new String[]{
                title, url, content, contentRendered
        });
        dest.writeLongArray(new long[]{
                created, lastModified, lastTouched
        });
//        dest.writeValue(member);
//        dest.writeValue(node);
    }


}
