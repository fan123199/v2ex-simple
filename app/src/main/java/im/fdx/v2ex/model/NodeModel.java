package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.w3c.dom.Node;

/**
 * Created by a708 on 16-1-17.
 * V2ex 节点模型
 * 其中 name 是 key value
 */

///api/topics/show.json
//
//        参数（选其一）
//        username	根据用户名取该用户所发表主题
//        node_id	根据节点id取该节点下所有主题
//        node_name	根据节点名取该节点下所有主题

//------------------
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


    private long id;
    private String name;
    private String url;
    private String title;
    private String title_alternative;
    private int topics;
    private int stars;

    private long created;

    public void setHeader(String header) {
        this.header = header;
    }

    private String header;
    private String avatar_mini;
    private String avatar_normal;
    private String avatar_large;

    public NodeModel() {
    }


    /**
     * @param nodeName 唯一确定一个node模型
     */
    public NodeModel(String nodeName) {
        name = nodeName;
    }


    public String getName() {
        return name;
    }


    public long getCreated() {
        return created;
    }

    public String getTitle() {
        return title;
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

    public String getUrl() {
        return url;
    }

    public String getTitle_alternative() {
        return title_alternative;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle_alternative(String title_alternative) {
        this.title_alternative = title_alternative;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getAvatarMiniUrl() {
        return "http:" + getAvatar_mini();
    }

    public String getAvatarLargeUrl() {
        return "http:" + getAvatar_large();
    }

    public String getHeader() {
        return header;
    }

    public int getStars() {
        return stars;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeString(this.title);
        dest.writeString(this.title_alternative);
        dest.writeInt(this.topics);
        dest.writeInt(this.stars);
        dest.writeLong(this.created);
        dest.writeString(this.header);
        dest.writeString(this.avatar_mini);
        dest.writeString(this.avatar_normal);
        dest.writeString(this.avatar_large);
    }

    protected NodeModel(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.url = in.readString();
        this.title = in.readString();
        this.title_alternative = in.readString();
        this.topics = in.readInt();
        this.stars = in.readInt();
        this.created = in.readLong();
        this.header = in.readString();
        this.avatar_mini = in.readString();
        this.avatar_normal = in.readString();
        this.avatar_large = in.readString();
    }

    public static final Creator<NodeModel> CREATOR = new Creator<NodeModel>() {
        @Override
        public NodeModel createFromParcel(Parcel source) {
            return new NodeModel(source);
        }

        @Override
        public NodeModel[] newArray(int size) {
            return new NodeModel[size];
        }
    };
}
