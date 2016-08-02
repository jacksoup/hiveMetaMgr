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
@Table(name = "PARTITION_KEY_VALS")
public class TbPartitionKeyVal {
    @Column(name = "PART_ID")
    private long partID;//主键
    @Column(name = "PART_KEY_VAL")
    private String partkeyVal;
    @Column(name = "INTEGER_IDX")
    private int integerIDX;

    public long getPartID() {
        return partID;
    }

    public void setPartID(long partID) {
        this.partID = partID;
    }

    public String getPartkeyVal() {
        return partkeyVal;
    }

    public void setPartkeyVal(String partkeyVal) {
        this.partkeyVal = partkeyVal;
    }

    public int getIntegerIDX() {
        return integerIDX;
    }

    public void setIntegerIDX(int integerIDX) {
        this.integerIDX = integerIDX;
    }

    @Override
    public String toString() {
        return "TbPartitionKeyVal{" +
                "partID=" + partID +
                ", partkeyVal='" + partkeyVal + '\'' +
                ", integerIDX=" + integerIDX +
                '}';
    }
}
