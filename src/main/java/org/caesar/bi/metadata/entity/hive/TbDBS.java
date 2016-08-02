package org.caesar.bi.metadata.entity.hive;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * 数据库表
 */
@Entity
@Table(name = "DBS")
public class TbDBS {
    @Column(name = "DB_ID")
    private long dbID;//id
    @Column(name = "DESC")
    private String desc;
    @Column(name = "DB_LOCATION_URI")
    private String url;//hdfs uri
    @Column(name = "NAME")
    private String name;//数据库名
    @Column(name = "OWNER_NAME")
    private String owerName;//所有者名
    @Column(name = "OWNER_TYPE")//所有者类型
    private String ownerType;

    private List<TbTBLS> tbTBLSList;

    public List<TbTBLS> getTbTBLSList() {
        return tbTBLSList;
    }

    public void setTbTBLSList(List<TbTBLS> tbTBLSList) {
        this.tbTBLSList = tbTBLSList;
    }

    public long getDbID() {
        return dbID;
    }

    public void setDbID(long dbID) {
        this.dbID = dbID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwerName() {
        return owerName;
    }

    public void setOwerName(String owerName) {
        this.owerName = owerName;
    }

    @Override
    public String toString() {
        return "TbDBS{" +
                "dbID='" + dbID + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", owerName='" + owerName + '\'' +
                ", ownerType='" + ownerType + '\'' +
                '}';
    }
}
