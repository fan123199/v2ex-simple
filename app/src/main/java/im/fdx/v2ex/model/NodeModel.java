package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a708 on 16-1-17.
 * V2ex 节点模型
 */

//{
//        "id" : 90,
//        "name" : "python",
//        "url" : "http://www.v2ex.com/go/python",
//        "title" : "Python",
//        "title_alternative" : "Python",
//        "topics" : 4272,
//        "stars" : 3234,
//
//        "header" : "这里讨论各种 Python 语言编程话题，也包括 Django，Tornado 等框架的讨论。这里是一个能够帮助你解决实际问题的地方。",
//
//
//        "footer" : null,
//
//        "created" : 1278683336,
//        "avatar_mini" : "//cdn.v2ex.co/navatar/8613/985e/90_mini.png?m=1452823690",
//        "avatar_normal" : "//cdn.v2ex.co/navatar/8613/985e/90_normal.png?m=1452823690",
//        "avatar_large" : "//cdn.v2ex.co/navatar/8613/985e/90_large.png?m=1452823690"
//        }

public class NodeModel implements Parcelable {


    //留个ID见证一下变态的Java封装性
    private Long nodeId;
    private String nodeName;
    private String nodeTitle;
    private String nodeTitleEn;
    private String nodeUrl;
    private int topics;
    private String avatar_mini;
    private String avatar_normal;
    private String avatar_large;


    protected NodeModel(Parcel in) {
        setNodeId(in.readLong());
        nodeName = in.readString();
        nodeTitle = in.readString();
        nodeTitleEn = in.readString();
        nodeUrl = in.readString();
        topics = in.readInt();
        avatar_mini = in.readString();
        avatar_normal = in.readString();
        avatar_large = in.readString();
    }

    public static final Creator<NodeModel> CREATOR = new Creator<NodeModel>() {
        @Override
        public NodeModel createFromParcel(Parcel in) {
            return new NodeModel(in);
        }

        @Override
        public NodeModel[] newArray(int size) {
            return new NodeModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getNodeId());
        dest.writeString(nodeName);
        dest.writeString(nodeTitle);
        dest.writeString(nodeTitleEn);
        dest.writeString(nodeUrl);
        dest.writeInt(topics);
        dest.writeString(avatar_mini);
        dest.writeString(avatar_normal);
        dest.writeString(avatar_large);
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public int getTopics() {
        return topics;
    }

    public String getAvatar_normal() {
        return avatar_normal;
    }

    public String getAvatar_mini() {
        return avatar_mini;
    }

    public String getAvatar_large() {
        return avatar_large;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public String getNodeTitleEn() {
        return nodeTitleEn;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    public void setNodeTitleEn(String nodeTitleEn) {
        this.nodeTitleEn = nodeTitleEn;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public void setTopics(int topics) {
        this.topics = topics;
    }

    public void setAvatar_mini(String avatar_mini) {
        this.avatar_mini = avatar_mini;
    }

    public void setAvatar_normal(String avatar_normal) {
        this.avatar_normal = avatar_normal;
    }

    public void setAvatar_large(String avatar_large) {
        this.avatar_large = avatar_large;
    }
}
