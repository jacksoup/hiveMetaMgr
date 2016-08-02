package org.caesar.bi.metadata.entity.hive;

/**
 * Created by caesar on 2016/6/28.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * 字段表
 */
@Entity
@Table(name = "COLUMNS_V2")
public class TbColumns {
    @Column(name = "CD_ID")
    private long cdID;
    @Column(name = "COMMENT")
    private String comment;
    @Column(name = "COLUMN_NAME")
    private String columnName;
    @Column(name = "TYPE_NAME")
    private String typeName;
    @Column(name = "INTEGER_IDX")
    private int integerIDX;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getIntegerIDX() {
        return integerIDX;
    }

    public void setIntegerIDX(int integerIDX) {
        this.integerIDX = integerIDX;
    }

    public long getCdID() {
        return cdID;
    }

    public void setCdID(long cdID) {
        this.cdID = cdID;
    }

    @Override
    public String toString() {
        return "TbColumns{" +
                "cdID=" + cdID +
                ", comment='" + comment + '\'' +
                ", columnName='" + columnName + '\'' +
                ", typeName='" + typeName + '\'' +
                ", integerIDX=" + integerIDX +
                '}';
    }
}
