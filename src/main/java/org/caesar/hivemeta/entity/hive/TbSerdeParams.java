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
@Table(name = "SERDE_PARAMS")
public class TbSerdeParams {
    @Column(name = "SERDE_ID")
    private long serdeID;
    @Column(name = "PARAM_KEY")
    private String paramKey;
    @Column(name = "PARAM_VALUE")
    private String paramValue;

    public long getSerdeID() {
        return serdeID;
    }

    public void setSerdeID(long serdeID) {
        this.serdeID = serdeID;
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
        return "TbSerdeParams{" +
                "serdeID=" + serdeID +
                ", paramKey='" + paramKey + '\'' +
                ", paramValue='" + paramValue + '\'' +
                '}';
    }
}
