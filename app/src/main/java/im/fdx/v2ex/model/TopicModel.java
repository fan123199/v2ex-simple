package im.fdx.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import im.fdx.v2ex.R;

/**
 * Created by a708 on 15-8-18.
 * 主题模型
 */



//http://www.v2ex.com/api/topics/show.json?node_id=1
//[
//
//        {
//        "id" : 251393,
//        "title" : "室友是个女 coder",
//        "url" : "http://www.v2ex.com/t/251393",
//        "content" : "454f",
//        "content_rendered" : "吗？",
//        "replies" : 100,
//        "member" : {
//        "id" : 80938,
//        "username" : "boyhailong",
//        "tagline" : "",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/ad59/185e/80938_mini.png?m=1452315358",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/ad59/185e/80938_normal.png?m=1452315358",
//        "avatar_large" : "//cdn.v2ex.co/avatar/ad59/185e/80938_large.png?m=1452315358"
//        },
//        "node" : {
//        "id" : 320,
//        "name" : "wtf",
//        "title" : "不靠谱茶话会",
//        "title_alternative" : "WTF",
//        "url" : "http://www.v2ex.com/go/wtf",
//        "topics" : 212,
//        "avatar_mini" : "//cdn.v2ex.co/navatar/3207/2254/320_mini.png?m=1435210420",
//        "avatar_normal" : "//cdn.v2ex.co/navatar/3207/2254/320_normal.png?m=1435210420",
//        "avatar_large" : "//cdn.v2ex.co/navatar/3207/2254/320_large.png?m=1435210420"
//        },
//        "created" : 1453030019,
//        "last_modified" : 1453044647,
//        "last_touched" : 1453094527
//        },
//        {some of above}
//    ]
public class TopicModel extends BaseModel implements Parcelable {

    private long id;
    private String title;
    private String url;
    private String content;
    private String content_rendered;
    private int replies;
    private MemberModel member;
    private NodeModel node;
    private long created;
    private long last_modified;
    private long last_touched;
//    public String author;
//    public String nodeTitle;
//    public String avatarString;

    //暂时不知道如何传递数据到Model,先用vivz的方法,在activty中设置解析方法


    public TopicModel() {
    }

    //yaoyumeng 喜欢用array,也就是id和replies都是int的话,他就会用readIntArray.我认为不好.我采用vivz.
    protected TopicModel(Parcel in) {
        id = in.readLong();
        replies = in.readInt();
        title = in.readString();
        url = in.readString();
        content = in.readString();
        content_rendered = in.readString();
        member = (MemberModel) in.readValue(MemberModel.class.getClassLoader());
        node = (NodeModel) in.readValue(NodeModel.class.getClassLoader());
        created = in.readLong();
        last_modified = in.readLong();
        last_touched = in.readLong();
//        author = in.readString();
//        nodeTitle = in.readString();
//        avatarString = in.readString();


    }

    public static final Creator<TopicModel> CREATOR = new Creator<TopicModel>() {
        @Override
        public TopicModel createFromParcel(Parcel in) {
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
        dest.writeString(content_rendered);
//        dest.writeString(author);//学devliu
//        dest.writeString(nodeTitle);
//        dest.writeString(avatarString);
        dest.writeValue(member);
        dest.writeValue(node);
        dest.writeLong(created);
        dest.writeLong(last_modified);
        dest.writeLong(last_touched);
    }

    public TopicModel(long id, String title, String content, int replies, String node_title, long created, MemberModel member, NodeModel nodeModel) {
//        this.author = author;
        this.id = id;
        this.title = title;
        this.content = content;
        this.replies = replies;
//        this.nodeTitle = node_title;
        this.created = created;
//        this.avatarString = avatarString;
        this.member = member;
        this.node = nodeModel;
    }

    public TopicModel(long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public String getContent_rendered() {
        return content_rendered;
    }

    public int getReplies() {
        return replies;
    }

    public long getCreated() {
        return created;
    }

    public long getLast_modified() {
        return last_modified;
    }

    public long getLast_touched() {
        return last_touched;
    }

//    public String getAuthor() {
//        return author;
//    }

//    public String getTitle() {
//        return nodeTitle;
//    }

//    public String getAvatarString() {
//        return avatarString;
//    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentRendered(String content_rendered) {
        this.content_rendered = content_rendered;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setLast_modified(long last_modified) {
        this.last_modified = last_modified;
    }

    public void setLast_touched(long last_touched) {
        this.last_touched = last_touched;
    }

//    public void setAuthor(String author) {
//        this.author = author;
//    }

//    public void setTitle(String nodeTitle) {
//        this.nodeTitle = nodeTitle;
//    }

//    public void setAvatarString(String avatarString) {
//        this.avatarString = avatarString;
//    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return R.string.title + title + R.string.content + "\n" + content;
    }


    public NodeModel getNode() {
        return node;
    }

    public MemberModel getMember() {
        return member;
    }

    public void setMember(MemberModel member) {
        this.member = member;
    }

    public void setNode(NodeModel node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicModel that = (TopicModel) o;

        return getTitle().equals(that.getTitle());

    }

    @Override
    public int hashCode() {
        return getTitle().hashCode();
    }
}
