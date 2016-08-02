package org.caesar.hivemeta.entity.metaMang;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by caesar on 2016/4/26.
 */
@Entity
@Table(name = "tb_user", schema = "dbo", catalog = "Customer_DC")
public class TbUser {
    @Column(name = "user_id")
    private String userID;
    @Column(name = "user_name")
    private String userName;
    private String password;
    @Column(name = "mobile_number")
    private Long mobileNumber;
    @Column(name = "create_time")
    private String createTime;
    @Column(name = "update_time")
    private String updateTime;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(Long mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "TbUser{" +
                "userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", mobileNumber=" + mobileNumber +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
