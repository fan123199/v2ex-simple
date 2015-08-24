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

    public long id;
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
    public String author;
    public String nodeTitle;

    //暂时不知道如何传递数据到Model,先用vivz的方法,在activty中设置解析方法
//    public void parse(JSONObject jsonObject) throws JSONException {
//        id = jsonObject.optInt("id");
//        title = jsonObject.optString("title");
//        url = jsonObject.optString("url");
//        content = jsonObject.optString("content");
//        contentRendered = ContentUtils.formatContent(jsonObject.getString("content_rendered"));
//        replies = jsonObject.optInt("replies");
//
//        created = jsonObject.optLong("created");
//        lastModified = jsonObject.optLong("last_modified");
//        lastTouched = jsonObject.optLong("last_touched");
//        //author学devliu
//        author = jsonObject.optJSONObject("member").optString("author");
////        member = new MemberModel();
////        member.parse(jsonObject.getJSONObject("member"));
////        node = new NodeModel();
////        node.parse(jsonObject.getJSONObject("node"));
//    }

    //yaoyumeng 喜欢用array,也就是id和replies都是int的话,他就会用readIntArray.我认为不好.我采用vivz.
    protected TopicModel(Parcel in) {
        id = in.readLong();
        replies = in.readInt();
        title = in.readString();
        url = in.readString();
        content = in.readString();
        contentRendered = in.readString();
        created = in.readLong();
        lastModified = in.readLong();
        lastTouched = in.readLong();
        author = in.readString();
//        member = (MemberModel) in.readValue(MemberModel.class.getClassLoader());
//        node = (NodeModel) in.readValue(NodeModel.class.getClassLoader());
        nodeTitle = in.readString();


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
        dest.writeLong(id);
        dest.writeInt(replies);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(content);
        dest.writeString(contentRendered);
        dest.writeLong(created);
        dest.writeLong(lastModified);
        dest.writeLong(lastTouched);
        dest.writeString(author);//学devliu
        dest.writeString(nodeTitle);

//        dest.writeValue(member);
//        dest.writeValue(node);
    }

    public TopicModel(long id,String title,String author,String content,int replies,String node_title){
        this.author = author;
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
        this.nodeTitle = node_title;
    }

    public long getId() {
        return id;
    }

}
