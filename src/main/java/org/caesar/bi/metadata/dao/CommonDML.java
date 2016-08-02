package org.caesar.bi.metadata.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.caesar.bi.metadata.entity.hive.*;
import org.caesar.bi.metadata.entity.metaMang.TbUser;
import org.caesar.bi.metadata.entity.zeus.TbZeusFile;
import org.caesar.utils.arrays.ArraysUtil;
import org.caesar.utils.db.ExecutorDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caesar on 2016/7/4.
 */
public class CommonDML {
    private static ExecutorDB userDB = new ExecutorDB(1);
    private static ExecutorDB hive = new ExecutorDB(2);
    private static ExecutorDB zeus = new ExecutorDB(3);
    private static Logger logger = LoggerFactory.getLogger(CommonDML.class);

    public static TbUser login(String userName, String password) throws Exception {
//        String sql = String.format("select * from tb_user where user_name='%s' and password='%s'", userName, password);
//        TbUser user = userDB.queryOne(sql,TbUser.class);
//        return user;
        if("admin".equals(userName) && "21232f297a57a5a743894a0e4a801fc3".equals(password)){
            TbUser user = new TbUser();
            user.setUserName("admin");
            return user;
        }else{
            return null;
        }
    }

    public static List<TbTBLS> getTablesListByDBId(long dbID){
        List<TbTBLS> tbTBLSList = hive.queryList(String.format("select * from TBLS where DB_ID='%s' AND TBL_TYPE!='INDEX_TABLE'", dbID), TbTBLS.class);
        return tbTBLSList;
    }

    public static String getFilePath(long id){
        List<String> pathnameslist = new ArrayList<String>();
        Long parentID=id;
        while(parentID!=null){
            TbZeusFile zeusFile=zeus.queryOne(String.format("select parent,name from zeus_file where id='%s'",parentID),TbZeusFile.class);
            if(zeusFile !=null){
                parentID=zeusFile.getParent();
//                System.out.print(zeusFile.getName()+"》");
                pathnameslist.add(zeusFile.getName());
            }else{
                parentID=null;
            }
        }
        pathnameslist = Lists.reverse(pathnameslist);
        String filePath=Joiner.on("/").join(pathnameslist);
        return filePath;
    }

    public static List<TbColumns> getColumnsListByTableID(String tableID){
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
        TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
        List<TbColumns> columns = hive.queryList(String.format("select * from COLUMNS_V2 where CD_ID='%s'", tbSDS.getCdID()), TbColumns.class);
        return columns;
    }

    public static TbTBLS getTableByTableID(String tableID){
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
        return table;
    }

    public static TbDBS getDBByTableID(String tableID){
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
        TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
        return db;
    }

    public static Map<String,Object> getTableParams(String tableID){
        List<TbTableParams> list = hive.queryList(String.format("select * from TABLE_PARAMS where TBL_ID='%s'", tableID), TbTableParams.class);
        Map<String,Object> map = new HashMap<String, Object>();
        for(TbTableParams tbTableParams:list){
            map.put(tbTableParams.getParamKey(),tbTableParams.getParamValue());
        }
        return map;
    }

    public static void main(String[] args) {
        getFilePath(35);
    }

}
