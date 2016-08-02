package org.caesar.bi.metadata.entity.hive;

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
@Table(name = "SDS")
public class TbSDS {
    @Column(name = "SD_ID")
    private long sdID;//主键
    @Column(name = "CD_ID")
    private long cdID;
    @Column(name = "INPUT_FORMAT")
    private String inputFormat;
    @Column(name = "IS_COMPRESSED")
    private String isCompressed;
    @Column(name = "IS_STOREDASSUBDIRECTORIES")
    private String IS_STOREDASSUBDIRECTORIES;
    @Column(name = "LOCATION")
    private String location;
    @Column(name = "NUM_BUCKETS")
    private int numBuckets;
    @Column(name = "OUTPUT_FORMAT")
    private String outFormat;
    @Column(name = "SERDE_ID")
    private long serdeID;

    public long getSerdeID() {
        return serdeID;
    }

    public void setSerdeID(long serdeID) {
        this.serdeID = serdeID;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public void setNumBuckets(int numBuckets) {
        this.numBuckets = numBuckets;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIS_STOREDASSUBDIRECTORIES() {
        return IS_STOREDASSUBDIRECTORIES;
    }

    public void setIS_STOREDASSUBDIRECTORIES(String IS_STOREDASSUBDIRECTORIES) {
        this.IS_STOREDASSUBDIRECTORIES = IS_STOREDASSUBDIRECTORIES;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public long getSdID() {
        return sdID;
    }

    public void setSdID(long sdID) {
        this.sdID = sdID;
    }

    public long getCdID() {
        return cdID;
    }

    public void setCdID(long cdID) {
        this.cdID = cdID;
    }

    public String getIsCompressed() {
        return isCompressed;
    }

    public void setIsCompressed(String isCompressed) {
        this.isCompressed = isCompressed;
    }

    public String getOutFormat() {
        return outFormat;
    }

    public void setOutFormat(String outFormat) {
        this.outFormat = outFormat;
    }

    @Override
    public String toString() {
        return "TbSDS{" +
                "sdID=" + sdID +
                ", cdID=" + cdID +
                ", inputFormat='" + inputFormat + '\'' +
                ", isCompressed='" + isCompressed + '\'' +
                ", IS_STOREDASSUBDIRECTORIES='" + IS_STOREDASSUBDIRECTORIES + '\'' +
                ", location='" + location + '\'' +
                ", numBuckets=" + numBuckets +
                ", outFormat='" + outFormat + '\'' +
                ", serdeID=" + serdeID +
                '}';
    }
}
