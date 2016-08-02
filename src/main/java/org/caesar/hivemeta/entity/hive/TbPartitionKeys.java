package org.caesar.hivemeta.entity.hive;

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
@Table(name = "PARTITION_KEYS")
public class TbPartitionKeys {
    @Column(name = "TBL_ID")
    private long tableID;
    @Column(name = "PKEY_COMMENT")
    private String pKeyComment;
    @Column(name = "PKEY_NAME")
    private String pKeyName;
    @Column(name = "PKEY_TYPE")
    private String pKeyType;
    @Column(name = "INTEGER_IDX")
    private int integerIDX;

    public long getTableID() {
        return tableID;
    }

    public void setTableID(long tableID) {
        this.tableID = tableID;
    }

    public String getpKeyComment() {
        return pKeyComment;
    }

    public void setpKeyComment(String pKeyComment) {
        this.pKeyComment = pKeyComment;
    }

    public String getpKeyName() {
        return pKeyName;
    }

    public void setpKeyName(String pKeyName) {
        this.pKeyName = pKeyName;
    }

    public String getpKeyType() {
        return pKeyType;
    }

    public void setpKeyType(String pKeyType) {
        this.pKeyType = pKeyType;
    }

    public int getIntegerIDX() {
        return integerIDX;
    }

    public void setIntegerIDX(int integerIDX) {
        this.integerIDX = integerIDX;
    }

    @Override
    public String toString() {
        return "TbPartitionKeys{" +
                "tableID=" + tableID +
                ", pKeyComment='" + pKeyComment + '\'' +
                ", pKeyName='" + pKeyName + '\'' +
                ", pKeyType='" + pKeyType + '\'' +
                ", integerIDX=" + integerIDX +
                '}';
    }
}