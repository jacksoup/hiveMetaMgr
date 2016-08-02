package org.caesar.bi.metadata.entity.hive;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "PARTITIONS")
public class TbPartitions {
    @Column(name = "PART_ID")
    private long partID;
    @Column(name = "CREATE_TIME")
    private int createTime;
    @Column(name = "LAST_ACCESS_TIME")
    private int lastAccessTime;
    @Column(name = "PART_NAME")
    private String partName;
    @Column(name = "SD_ID")
    private long sdID;
    @Column(name = "TBL_ID")
    private String tableID;

    public long getPartID() {
        return partID;
    }

    public void setPartID(long partID) {
        this.partID = partID;
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

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public long getSdID() {
        return sdID;
    }

    public void setSdID(long sdID) {
        this.sdID = sdID;
    }

    public String getTableID() {
        return tableID;
    }

    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    @Override
    public String toString() {
        return "TbPartitions{" +
                "partID=" + partID +
                ", createTime=" + createTime +
                ", lastAccessTime=" + lastAccessTime +
                ", partName='" + partName + '\'' +
                ", sdID=" + sdID +
                ", tableID='" + tableID + '\'' +
                '}';
    }
}