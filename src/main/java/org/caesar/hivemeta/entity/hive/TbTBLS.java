package org.caesar.hivemeta.entity.hive;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 表
 */
@Entity
@Table(name = "TBLS")
public class TbTBLS {
    @Column(name = "TBL_ID")
    private long tableID;//id
    @Column(name = "CREATE_TIME")
    private int createTime;
    @Column(name = "DB_ID")
    @OneToMany
    private String dbID;//外键
    @Column(name = "LAST_ACCESS_TIME")
    private int lastAccessTime;//
    @Column(name = "OWNER")
    private String owner;
    @Column(name = "RETENTION")//所有者类型
    private int retention;

    @Column(name = "SD_ID")
    private long sdID;//外键
    @Column(name = "TBL_NAME")
    private String tableName;
    @Column(name = "TBL_TYPE")
    private String tableType;
    @Column(name = "VIEW_EXPANDED_TEXT")
    private String viewExpandedText;
    @Column(name = "VIEW_ORIGINAL_TEXT")
    private String viewOriginalText;


    public long getTableID() {
        return tableID;
    }

    public void setTableID(long tableID) {
        this.tableID = tableID;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(int lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public int getRetention() {
        return retention;
    }

    public void setRetention(int retention) {
        this.retention = retention;
    }

    public long getSdID() {
        return sdID;
    }

    public void setSdID(long sdID) {
        this.sdID = sdID;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getViewExpandedText() {
        return viewExpandedText;
    }

    public void setViewExpandedText(String viewExpandedText) {
        this.viewExpandedText = viewExpandedText;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getViewOriginalText() {
        return viewOriginalText;
    }

    public void setViewOriginalText(String viewOriginalText) {
        this.viewOriginalText = viewOriginalText;
    }

    @Override
    public String toString() {
        return "TbTBLS{" +
                "tableID=" + tableID +
                ", createTime=" + createTime +
                ", dbID='" + dbID + '\'' +
                ", lastAccessTime=" + lastAccessTime +
                ", owner='" + owner + '\'' +
                ", retention=" + retention +
                ", sdID=" + sdID +
                ", tableName='" + tableName + '\'' +
                ", tableType='" + tableType + '\'' +
                ", viewExpandedText='" + viewExpandedText + '\'' +
                ", viewOriginalText='" + viewOriginalText + '\'' +
                '}';
    }
}
