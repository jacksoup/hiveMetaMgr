package org.caesar.hivemeta.entity.hive;

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
@Table(name = "TABLE_PARAMS")
public class TbTableParams {
    @Column(name = "TBL_ID")
    private long tableID;
    @Column(name = "PARAM_KEY")
    private String paramKey;
    @Column(name = "PARAM_VALUE")
    private String paramValue;//hdfs uri

    public long getTableID() {
        return tableID;
    }

    public void setTableID(long tableID) {
        this.tableID = tableID;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public String toString() {
        return "TbTableParams{" +
                "tableID=" + tableID +
                ", paramKey='" + paramKey + '\'' +
                ", paramValue='" + paramValue + '\'' +
                '}';
    }
}
