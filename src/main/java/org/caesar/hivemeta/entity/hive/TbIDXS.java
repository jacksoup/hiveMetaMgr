package org.caesar.hivemeta.entity.hive;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * 索引表
 */
@Entity
@Table(name = "IDXS")
public class TbIDXS {
    @Column(name = "INDEX_ID")
    private long indexID;//id
    @Column(name = "CREATE_TIME")
    private int createTime;
    @Column(name = "DEFERRED_REBUILD")
    private int DEFERRED_REBUILD;//hdfs uri
    @Column(name = "INDEX_HANDLER_CLASS")
    private String indexHandlerClass;
    @Column(name = "INDEX_NAME")
    private String indexName;
    @Column(name = "INDEX_TBL_ID")
    private long indexTableID;
    @Column(name = "LAST_ACCESS_TIME")
    private int lastAccessTime;
    @Column(name = "ORIG_TBL_ID")
    private long origTableID;
    @Column(name = "SD_ID")
    private long sdID;

    public long getIndexID() {
        return indexID;
    }

    public void setIndexID(long indexID) {
        this.indexID = indexID;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getDEFERRED_REBUILD() {
        return DEFERRED_REBUILD;
    }

    public void setDEFERRED_REBUILD(int DEFERRED_REBUILD) {
        this.DEFERRED_REBUILD = DEFERRED_REBUILD;
    }

    public String getIndexHandlerClass() {
        return indexHandlerClass;
    }

    public void setIndexHandlerClass(String indexHandlerClass) {
        this.indexHandlerClass = indexHandlerClass;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public long getIndexTableID() {
        return indexTableID;
    }

    public void setIndexTableID(long indexTableID) {
        this.indexTableID = indexTableID;
    }

    public int getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(int lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getOrigTableID() {
        return origTableID;
    }

    public void setOrigTableID(long origTableID) {
        this.origTableID = origTableID;
    }

    public long getSdID() {
        return sdID;
    }

    public void setSdID(long sdID) {
        this.sdID = sdID;
    }

    @Override
    public String toString() {
        return "TbIDXS{" +
                "indexID=" + indexID +
                ", createTime=" + createTime +
                ", DEFERRED_REBUILD=" + DEFERRED_REBUILD +
                ", indexHandlerClass='" + indexHandlerClass + '\'' +
                ", indexName='" + indexName + '\'' +
                ", indexTableID=" + indexTableID +
                ", lastAccessTime=" + lastAccessTime +
                ", origTableID=" + origTableID +
                ", sdID=" + sdID +
                '}';
    }
}
