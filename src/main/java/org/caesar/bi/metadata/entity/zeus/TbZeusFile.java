package org.caesar.bi.metadata.entity.zeus;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "zeus_file")
public class TbZeusFile {
    @Column(name = "id")
    private long id;//主键
    @Column(name = "content")
    private String content;
    @Column(name = "gmt_create")
    private String createTime;
    @Column(name = "gmt_modified")
    private String upateTime;
    @Column(name = "host_group_id")
    private String hostGroupID;
    @Column(name = "name")
    private String name;
    @Column(name = "owner")
    private String owner;
    @Column(name = "parent")
    private Long parent;
    @Column(name = "type")
    private int type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpateTime() {
        return upateTime;
    }

    public void setUpateTime(String upateTime) {
        this.upateTime = upateTime;
    }

    public String getHostGroupID() {
        return hostGroupID;
    }

    public void setHostGroupID(String hostGroupID) {
        this.hostGroupID = hostGroupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TbZeusFile{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createTime='" + createTime + '\'' +
                ", upateTime='" + upateTime + '\'' +
                ", hostGroupID='" + hostGroupID + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", parent=" + parent +
                ", type=" + type +
                '}';
    }
}
