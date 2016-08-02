package org.caesar.bi.metadata.controller;

import co.cask.tephra.persist.HDFSUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.client.HdfsUtils;
import org.apache.http.HttpResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.caesar.bi.metadata.common.CommonUtil;
import org.caesar.bi.metadata.common.Constants;
import org.caesar.bi.metadata.common.Response;
import org.caesar.bi.metadata.dao.CommonDML;
import org.caesar.bi.metadata.entity.hive.*;
import org.caesar.bi.metadata.entity.zeus.TbZeusFile;
import org.caesar.utils.collections.CompartorUtil;
import org.caesar.utils.collections.Data.Sort;
import org.caesar.utils.db.ExecutorDB;
import org.caesar.utils.encryption.MD5;
import org.caesar.utils.hdfs.HDFSUtil2;
import org.caesar.utils.hdfs.HdfsUtil;
import org.caesar.utils.hive.HiveConnectException;
import org.caesar.utils.hive.HiveDBOperator;
import org.caesar.utils.os.Systems;
import org.caesar.utils.properties.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by caesar on 2016/7/4.
 */
@Controller
@RequestMapping("/")
public class CommonController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static Map<String,HiveDBOperator.HiveQueryResult> searchDatacaches = Collections.synchronizedMap(new HashMap<String, HiveDBOperator.HiveQueryResult>());
    ExecutorDB hive = new ExecutorDB(2);
    ExecutorDB zeus = new ExecutorDB(3);

    @RequestMapping(value = "readDatabaseTree", method = RequestMethod.GET)
    @ResponseBody
    public String read() throws Exception {
        JSONObject rootJson = new JSONObject();
        JSONArray arrayJson = new JSONArray();
        rootJson.put("sucess","true");
        String sql = "select * from DBS";
        StringBuffer sb = new StringBuffer();
        sb.append("{sucess:true,children:[");
        List<TbDBS> dbsList = hive.queryList(sql,TbDBS.class);
        for(TbDBS dbs : dbsList) {
            List<TbTBLS> tbTBLSList = CommonDML.getTablesListByDBId(dbs.getDbID());
//            System.out.println(dbs.getName());
            if (tbTBLSList.size() == 0) {
                sb.append("{id:\"" + "DB_" + dbs.getDbID() + "\",name:\"" + dbs.getName() + "\",qtip:\"" + dbs.getName() + "[库]\",icon:'static/images/extIcons/icons/database.png',leaf:true},");
            } else {
                sb.append("{id:\"" + "DB_" + dbs.getDbID() + "\",name:\"" + dbs.getName() + "\",qtip:\"" + dbs.getName() + "[库]\",icon:'static/images/extIcons/icons/database.png',expanded:false,children:[");
                for (TbTBLS tbs : tbTBLSList) {
//                    System.out.println(tbs.getTableName());
                    sb.append("{id:\"" + "TB_"+ tbs.getTableID() + "\",name:\"" + tbs.getTableName() + "\",qtip:\"" + tbs.getTableName() + "[表]\",icon:'static/images/extIcons/icons/table.png',leaf:true},");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("]},");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("]}");
        System.out.println(sb);
        return sb.toString();
    }

    @RequestMapping(value = "readTablePartitionInfo", method = RequestMethod.GET)
    @ResponseBody public String readTablePartition(String tableId) throws Exception {
        /**
         * 目前分区只支持深度为1的分区
         */
        if(tableId.equals("null")){
            return null;
        }
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableId), TbTBLS.class);
        TbPartitionKeys tbPartitionKeys = hive.queryOne(String.format("select * from PARTITION_KEYS where TBL_ID='%s'", tableId), TbPartitionKeys.class);
        if(tbPartitionKeys == null){
            return null;
        }
        List<TbPartitions> tbPartitionsList = hive.queryList(String.format("select * from PARTITIONS where TBL_ID='%s' ORDER BY PART_NAME ASC", tableId), TbPartitions.class);
        StringBuffer sb = new StringBuffer();
        //相关表的创建时间当作该分区的创建时间
        String partitionFeildCreateTime = df.format(CommonUtil.convertHiveDateTime(table.getCreateTime()));
        //相关表的uri路径即为该分区字段的路径
        int id=1953548;//ext中tree类型需要指定id，否则在做节点展开时会有问题
        String partitionFeildURI = hive.queryOne(String.format("select LOCATION from SDS where SD_ID='%s'",table.getSdID()), TbSDS.class).getLocation();
        sb.append("{\"sucess\":true,\"children\":[{\"name\":\""+tbPartitionKeys.getpKeyName()+"\",\"uri\":\""+partitionFeildURI +
                "\",\"createTime\":\"" + partitionFeildCreateTime +
                "\",id:\"" + (id++) +
                "\",\"tableID\":\"" + table.getTableID() +
                "\",\"expanded\":" + true + ",\"children\":[");
        for(TbPartitions tbPartitions : tbPartitionsList) {
            String partitionValue = tbPartitions.getPartName();
            String partitionValueCreateTime = df.format(CommonUtil.convertHiveDateTime(tbPartitions.getCreateTime()));
            String partitionValueURI = hive.queryOne(String.format("select LOCATION from SDS where SD_ID='%s'",
                    tbPartitions.getSdID()), TbSDS.class).getLocation();
            sb.append("{\"name\":\"" + partitionValue + "\",\"uri\":\"" + partitionValueURI +
                    "\",\"tableID\":\"" + table.getTableID() +
                    "\",id:\"" + (id++) +
                    "\",\"partitionID\":\"" + tbPartitions.getPartID() +
                    "\",\"createTime\":\"" + partitionValueCreateTime + "\",\"leaf\":" + true+ "},");
        }
        if(tbPartitionsList.size()!=0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("]}]}");
        System.out.println(sb);
        return sb.toString();
    }

    @RequestMapping(value = "readTableFieldsInfo", method = RequestMethod.GET)
    @ResponseBody
    public String readTableFieldsInfo(String tableId) throws Exception {
        //查看索引字段
        Set<String> indexFields = new HashSet<String>();
        List<TbIDXS> tbIDXSList = hive.queryList(String.format("select * from IDXS where ORIG_TBL_ID='%s'", tableId), TbIDXS.class);
        for(TbIDXS tbIDXS:tbIDXSList) {
            TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", tbIDXS.getSdID()), TbSDS.class);
            TbColumns tbColumns = hive.queryOne(String.format("select * from COLUMNS_V2 where CD_ID='%s'", tbSDS.getCdID()), TbColumns.class);
            indexFields.add(tbColumns.getColumnName());
        }
        List<TbColumns> columns = CommonDML.getColumnsListByTableID(tableId);
        List<TbPartitionKeys> tbPartitionKeysList = hive.queryList(String.format("select * from PARTITION_KEYS where TBL_ID='%s'", tableId), TbPartitionKeys.class);
        StringBuffer json = new StringBuffer();
        json.append("{\"sucess\":true,\"total\":" + (columns.size()+tbPartitionKeysList.size()) + ",\"fields\":[");
        for(TbPartitionKeys tbPartitionKeys:tbPartitionKeysList){
            String comment = tbPartitionKeys.getpKeyComment();
            comment=comment == null||"null".equals(comment)?"":comment;
            comment=CommonUtil.filterSpecialCharactersForJson(comment);
            json.append("{\"field\":\""+tbPartitionKeys.getpKeyName()
                    +"\",\"tableID\":\""+tableId
                    +"\",\"fieldID\":\""+(tbPartitionKeys.getIntegerIDX()+100000000)//为了保证前台按照fieldid排序后，索引字段可以放在最后一个位置
                    +"\",\"type\":\""+tbPartitionKeys.getpKeyType()
                    +"\",\"isPartitionFiled\":\"" + "<font color='red'>true</font>"
                    +"\",\"isIndexFiled\":\"" + (indexFields.contains(tbPartitionKeys.getpKeyName()) ? "<font color='red'>true</font>" : "fasle")
                    +"\",\"comment\":\""+comment+"\"},");
        }
        for(TbColumns column : columns){
            String comment = column.getComment();
            comment=comment == null||"null".equals(comment)?"":comment;
            json.append("{\"field\":\""+column.getColumnName()
                    +"\",\"tableID\":\""+tableId
                    +"\",\"fieldID\":\""+column.getIntegerIDX()
                    +"\",\"type\":\""+column.getTypeName()
                    +"\",\"isPartitionFiled\":\"" + "false"
                    +"\",\"isIndexFiled\":\"" + (indexFields.contains(column.getColumnName()) ? "<font color='red'>true</font>" : "false")
                    +"\",\"comment\":\""+comment+"\"},");
        }
        json.deleteCharAt(json.length()-1);
        json.append("]}");
        System.out.println(json);
        return json.toString();
    }
    @RequestMapping(value = "readTableBaseInfo", method = RequestMethod.POST)
    @ResponseBody
    public Response readTableBaseInfo(String tableId) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableId), TbTBLS.class);
//        List<TbSDS> stores = mysql.queryList(String.format("select * from SDS where CD_ID='%s'", tableId), TbSDS.class);
        TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
        TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
        int partitionsFieldsCount = (int)hive.count(String.format("where TBL_ID='%s'", tableId), TbPartitionKeys.class);
        int tableFieldsCount = hive.queryList(String.format("select * from COLUMNS_V2 where CD_ID='%s'", tbSDS.getCdID()), TbColumns.class).size();
        int fieldsCount = partitionsFieldsCount + tableFieldsCount;
        Map<String,Object> tableParmasMap=CommonDML.getTableParams(tableId);
        System.out.println(tableParmasMap);
        String inputFormat = tbSDS.getInputFormat();
//        if (inputFormat.equals("org.apache.hadoop.mapred.TextInputFormat")) {
//            inputFormat = "TextFile";
//        } else if (inputFormat.equals("org.apache.hadoop.mapred.SequenceFileInputFormat")) {
//            inputFormat = "SequenceFile";
//        } else if (inputFormat.equals("org.apache.hadoop.hive.ql.io.RCFileInputFormat")) {
//            inputFormat = "RCFile";
//        }
        //获取行列分隔符
        long sdID = tbSDS.getSdID();
        List<TbSerdeParams> tbSerdeParamsList = hive.queryList(String.format("select * from SERDE_PARAMS where SERDE_ID='%s'", sdID), TbSerdeParams.class);
        Map<String,Object> SerdeParamsMap = new HashMap<String,Object>();
        for(TbSerdeParams tbSerdeParams : tbSerdeParamsList){
            SerdeParamsMap.put(tbSerdeParams.getParamKey(),tbSerdeParams.getParamValue());
        }
        System.err.println("tbSerdeParamsList:"+tbSerdeParamsList);
        String fieldDelimiter = (String)SerdeParamsMap.get("field.delim");
        if(fieldDelimiter == null){
            fieldDelimiter = "\\u0001";
        }else{
            fieldDelimiter=CommonUtil.convert(fieldDelimiter);
        }
        System.out.println("fieldDelimiter:" + CommonUtil.convert(fieldDelimiter));
        String json = "{\"tableID\":\"" + table.getTableID() + "\",\"tableName\":\"" + table.getTableName() + "\",\"owner\":\"" + table.getOwner() + "\",\"createTime\":\"" +
                df.format(CommonUtil.convertHiveDateTime(table.getCreateTime())) +"\",\"location\":\""+tbSDS.getLocation()
                + "\",\"inputFormat\":\"" + inputFormat + "\",\"dbName\":\"" + db.getName()
                + "\",\"tableType\":\"" + (table.getTableType().equals("MANAGED_TABLE") ? "内部表" : "外部表")
                + "\",\"hasPartition\":\"" + (partitionsFieldsCount == 0 ? "false" : "true")
                + "\",\"fieldsCount\":\"" + fieldsCount
                + "\",\"isCompress\":\"" + tableParmasMap.containsKey("orc.compress")
                + "\",\"compressFormat\":\"" + (tableParmasMap.get("orc.compress") == null ? "无" : tableParmasMap.get("orc.compress"))
                + "\",\"lastUpdateTime\":\"" + df.format(CommonUtil.convertHiveDateTime(Integer.parseInt(tableParmasMap.get("transient_lastDdlTime").toString())))
//                + "\",\"numFiles\":\"" + tableParmasMap.get("numFiles")
//                + "\",\"totalSize\":\"" + tableParmasMap.get("totalSize")
                + "\",\"comment\":\"" + (tableParmasMap.get("comment") == null ? "" : tableParmasMap.get("comment"))
                + "\",\"fieldDelimiter\":\"" + CommonUtil.convert(fieldDelimiter)
                + "\"}";
        System.out.println(json);
        return new Response(json);
    }

    @RequestMapping(value = "readDBInfo", method = RequestMethod.POST)
    @ResponseBody
    public Response readDBInfo(String dbID) throws Exception {
        TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", dbID), TbDBS.class);
        int tableCount = hive.queryList(String.format("select * from TBLS where DB_ID='%s'", db.getDbID()), TbTBLS.class).size();
        String json = "{\"id\":\"" + db.getDbID() + "\",\"name\":\"" + db.getName() + "\",\"owner\":\"" +
                db.getOwerName() +"\",\"location\":\""+db.getUrl()
                + "\",\"tableCount\":\"" + tableCount
                + "\",\"desc\":\"" + (db.getDesc() == null ? "" : db.getDesc())
                + "\"}";
        System.out.println(json);
        return new Response(json);
    }

    @RequestMapping(value = "readTableIndexInfo", method = RequestMethod.GET)
    @ResponseBody
    public String readTableIndexInfo(String tableId) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        StringBuffer json = new StringBuffer("[");
        List<TbIDXS> tbIDXSList = hive.queryList(String.format("select * from IDXS where ORIG_TBL_ID='%s'", tableId), TbIDXS.class);
        for(TbIDXS tbIDXS:tbIDXSList){
            TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", tbIDXS.getSdID()), TbSDS.class);
            TbColumns tbColumns = hive.queryOne(String.format("select * from COLUMNS_V2 where CD_ID='%s'", tbSDS.getCdID()), TbColumns.class);
            long indexID = tbIDXS.getIndexID();
            String indexName = tbIDXS.getIndexName();
            String createTime = df.format(CommonUtil.convertHiveDateTime(tbIDXS.getCreateTime()));
            String handlerClass = tbIDXS.getIndexHandlerClass();
            if(handlerClass != null && handlerClass.split("\\.").length !=0){
                handlerClass=handlerClass.split("\\.")[handlerClass.split("\\.").length-1];
            }
            String fieldName=tbColumns.getColumnName();
            String fieldType=tbColumns.getTypeName();
            json.append("{indexID:"+indexID+",indexName:\""+indexName+"\",createTime:\""+createTime+"\",handlerClass:\""
                    + handlerClass+"\",fieldName:\""+fieldName+"\",fieldType:\""+fieldType+"\"},");

        }
        if(",".equals(json.charAt(json.length()-1)+"")){
            json.deleteCharAt(json.length()-1);
        }
        json.append("]");
        System.out.println(json);
        return json.toString();
    }

    @RequestMapping(value = "updateDatabaseDesc", method = RequestMethod.POST)
    @ResponseBody
    public Response updateDatabaseDesc(String dbID,String desc) throws Exception {
        desc=CommonUtil.filterSpecialCharactersForJson(desc);
        boolean status = hive.update(String.format("update DBS set `DESC`='%s' where DB_ID='%s'",desc,dbID));//DESC为mysql关键字，需要加`
        return new Response(status,"");
    }

    @RequestMapping(value = "updateTableDesc", method = RequestMethod.POST)
    @ResponseBody
    public Response updateTableDesc(String tableID,String desc) throws Exception {
        desc=CommonUtil.filterSpecialCharactersForJson(desc);
        Map<String,Object> tableParmasMap=CommonDML.getTableParams(tableID);
        if(tableParmasMap.containsKey("comment")){
            boolean status = hive.update(String.format("update TABLE_PARAMS set PARAM_VALUE='%s' where TBL_ID='%s' and PARAM_KEY='%s'",desc,tableID,"comment"));//DESC为mysql关键字，需要加`
            return new Response(status,"");
        }else{
            boolean status = hive.update(String.format
                    ("INSERT INTO TABLE_PARAMS (TBL_ID,PARAM_KEY,PARAM_VALUE) VALUES ('%s','%s','%s')",tableID,"comment",desc));
            return new Response(status,"");
        }
    }

    @RequestMapping(value = "search", method = RequestMethod.POST)
    @ResponseBody
    public Response search(String searchDataType,String searchKeys) throws Exception {
        System.err.println("searchDataType:"+searchDataType+",searchKeys:"+searchKeys);
        StringBuffer json = new StringBuffer("[");
        if(searchDataType.equals("1") || searchDataType.equals("0")){//搜索数据库
            List<TbDBS> tbDBSList = hive.queryList("select * from DBS where NAME LIKE '%" + searchKeys.trim() + "%';", TbDBS.class);
            List<String> dbs = new ArrayList<String>();
            for(TbDBS tbDBS : tbDBSList){
                json.append("{dataID:\"DB_"+tbDBS.getDbID()+"\",dataType:\""+"数据库"+"\",name:\""+tbDBS.getName()+"\"},");
            }
        }
        if(searchDataType.equals("2") || searchDataType.equals("0")) {//搜索表
            List<TbTBLS> tbTBLSList = hive.queryList("select * from TBLS where TBL_NAME LIKE '%" + searchKeys.trim() + "%' AND TBL_TYPE !='INDEX_TABLE';", TbTBLS.class);
            for(TbTBLS tbTBLS:tbTBLSList){
                TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", tbTBLS.getDbID()), TbDBS.class);
                json.append("{dataID:\"TB_"+tbTBLS.getTableID()+"\",dataType:\""+"表"+"\",name:\""+tbTBLS.getTableName()+"\",ownDatabase:\""+db.getName()+"\"},");
            }
        }
        if(searchDataType.equals("3") || searchDataType.equals("0")) {//搜索字段
            List<TbColumns> tbColumnsesList = hive.queryList("select * from COLUMNS_V2 where COLUMN_NAME LIKE '%" + searchKeys.trim() + "%';", TbColumns.class);
            for(TbColumns tbColumns:tbColumnsesList){
                //从column逆推找到table要注意，sdID与CDID的对应关系
                long sdID = (Long) hive.queryWithOne(String.format("select MIN(SD_ID) from SDS where CD_ID='%s'", tbColumns.getCdID()));
                TbTBLS table = hive.queryOne(String.format("select * from TBLS where SD_ID='%s'", sdID), TbTBLS.class);
                System.err.println("SDD:"+sdID+","+table);
                if(table==null || table.getTableType().equals("INDEX_TABLE")){//过滤索引表的字段
                    continue;
                }
                TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
                json.append("{dataID:\"TB_"+table.getTableID()+"\",dataType:\""+"字段"+"\",name:\""+tbColumns.getColumnName()+"\",ownTable:\""+table.getTableName()+"\",ownDatabase:\""+db.getName()+"\"},");
            }
        }
        if(json.charAt(json.length()-1) == ','){
            json.deleteCharAt(json.length()-1);
        }
        json.append("]");
        System.out.println(json);
        return new Response(true,json);
    }

    @RequestMapping(value = "getRelatedWord", method = RequestMethod.POST)
    @ResponseBody
    public String getRelatedWord(String tableID,String field) throws Exception {
        TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
        TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
        String tableName = table.getTableName();
        String dbName = db.getName();
        String fieldName = field;
        System.out.println(dbName + "," + tableName+ "," + fieldName);
        String sql = null;
        if(field==null || field.equals("")){
            sql = String.format("select * from zeus_file where type=2 AND locate('%s',content) AND locate('%s',content)" ,dbName,tableName);
        }else{
            sql = String.format("select * from zeus_file where type=2 AND locate('%s',content) AND locate('%s',content) AND locate('%s',content)"
                    ,dbName,tableName,fieldName);
        }
//        System.err.println(sql);
        //初步筛选
        List<TbZeusFile> tbZeusFileList = zeus.queryList(sql,TbZeusFile.class);
        StringBuffer json = new StringBuffer("[");
        String baseZeusDocHref= PropertiesUtil.getProperties(PropertiesUtil.class,"common.properties").getProperty("zeus.url")
                + "/#App:doc/Document:mydoc/Word:";
        for(TbZeusFile file:tbZeusFileList){
            String content = file.getContent();
            boolean isContain;
            String keyWord=null;
            if(field==null || field.equals("")){
                isContain=CommonUtil.equalsDB(content,dbName) && CommonUtil.equalsTable(content,tableName);
                keyWord=tableName;
            }else{
                isContain=CommonUtil.equalsDB(content,dbName) && CommonUtil.equalsTable(content,tableName) && CommonUtil.equalsField(content,fieldName);
                keyWord=fieldName;
            }
            keyWord=keyWord.toLowerCase();
            if(isContain){
                String docURI= CommonDML.getFilePath(file.getId());
                String fileContent=CommonUtil.filterSpecialCharactersForJson(file.getContent()).toLowerCase();
                fileContent=fileContent.replaceAll(keyWord,"<font color='red' style='font-weight:bold'>" + keyWord+"</font>");
                String zeusDocHref="<u><a target='_blank' href='"+baseZeusDocHref+file.getId()+"';>"+"跳转到开发中心</a></u>";
                boolean isAyscSchedule=Pattern.compile("#sync\\[(.*)\\].*").matcher(content.replaceAll("\\n"," ")).matches();
                json.append("{docID:\""+file.getId()+"\",owner:\""+file.getOwner()+"\",docName:\""+file.getName()+"\",docURI:\""+docURI+"\",docContent:\""
                        +fileContent+"\",isAyscSchedule:\""+isAyscSchedule+"\",zeusDocHref:\""+zeusDocHref+"\"},");
            }
        }
        if(json.charAt(json.length()-1) == ','){
            json.deleteCharAt(json.length()-1);
        }
        json.append("]");
        System.out.println("jsonjson:"+json);
        return json.toString();
    }


    @RequestMapping(value = "updateTableField", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateTableField(String tableID,String fieldID,String isPartitionFiledStr,String comment) throws Exception {
        boolean isPartitionFiled = isPartitionFiledStr.contains("true");
        if(comment !=null && !"null".equals(comment) && !"".equals(comment)){
            comment=CommonUtil.filterSpecialCharactersForJson(comment);
            TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
            TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
            if(!isPartitionFiled){//普通字段
                return hive.update(String.format("update COLUMNS_V2 set COMMENT='%s' where CD_ID='%s' AND INTEGER_IDX='%s'",comment,tbSDS.getCdID(),fieldID));
            }else{//分区字段
                return hive.update(String.format("update PARTITION_KEYS set PKEY_COMMENT='%s' where TBL_ID='%s' AND INTEGER_IDX='%s'",comment,tableID,fieldID));
            }
        }
        return false;
    }


    @RequestMapping(value = "exportDataWithExcel", method = RequestMethod.POST)
    @ResponseBody
    public Response exportDataWithExcel(String tableID,String partition) throws Exception {
        TbTBLS table = CommonDML.getTableByTableID(tableID);
        String tableName=table.getTableName();
        TbDBS db = CommonDML.getDBByTableID(tableID);
        logger.info("hive数据表数据导出》dbName:"+db.getName()+",tableName:"+table.getTableName());
        String sql=null;
        if("".equals(partition)){
            sql = "select * from " +tableName+ " limit "+ Constants.exportDataSizeLimit;
        }else{
            String partitionName = partition.split("=")[0];
            String partitionValue = partition.split("=")[1];
            TbPartitionKeys tbPartitionKeys = hive.queryOne(String.format("select * from PARTITION_KEYS where TBL_ID='%s'", tableID), TbPartitionKeys.class);
            //如果索引字段是字符串类型,则需要在值前面加上引号比如：createday='2016-06-13'
            if(tbPartitionKeys.getpKeyType().equalsIgnoreCase("string")){
                sql = "select * from " +tableName+ " where " + partitionName + "='" +partitionValue + "' limit " + Constants.exportDataSizeLimit ;
            }else{
                sql = "select * from " +tableName+ " where " + partition + " limit " + Constants.exportDataSizeLimit ;
            }
        }
        HiveDBOperator.HiveQueryResult result = null;
        try{
            result = HiveDBOperator.query(db.getName(),sql);
        }catch (Exception e){
            return new Response(false,"<font color='red'>SQL执行异常：\n</font>"+e.getMessage());
        }
        String path = CommonUtil.writeXML(result,tableName);
        return new Response(true,path);
    }

    @RequestMapping(value = "downloadHiveTableData", method = RequestMethod.GET)
    public void downloadHiveTableData(String tableID, String partition, HttpServletResponse response) throws Exception {
        TbTBLS table = CommonDML.getTableByTableID(tableID);
        TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
        String rootPath= tbSDS.getLocation().replace("hdfs://mycluster","")+"/"+partition;
        System.out.println("rootPath:"+rootPath);
//        String path = "/user/hive/warehouse/mobile_live.db/gensee_webcast/part-m-00000.snappy";
//        if(System.getProperty(""))
        if(Systems.getSystemType().equals("windows")) {//如果是windows系统(在个人电脑上调试阶段应用)
            System.setProperty("hadoop.home.dir", "E:\\hadoop");
        }
        //Exception ：Failed to set setXIncludeAware(true) for parser org.apache.xerces.jaxp.DocumentBuilderFactoryImpl org.apache.xerces.jaxp.DocumentBuilderFactoryImpl@2662e5cf:java.lang.UnsupportedOperationException
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        Configuration conf = new Configuration();
        String dfsUrl = PropertiesUtil.getProperties(PropertiesUtil.class,"common.properties").getProperty("namenode.url");
        conf.set("fs.defaultFS",dfsUrl);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem hdfs = FileSystem.get(conf);
        List<Path> pathList = new ArrayList<Path>();
        pathList = HDFSUtil2.list(hdfs,rootPath,pathList);;
//        System.err.println("pathList:"+pathList);
        if(pathList.size()==0){
            hdfs.close();
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print("无数据");
            out.flush();
            out.close();
            return;
        }
        response.setContentType("application/octet-stream; charset=utf-8");
//        response.setHeader("Content-disposition", "attachment;filename="+ table.getTableName());
        OutputStream os = response.getOutputStream();
        long contentLength=0;
        for(Path path:pathList){
            long length=hdfs.getFileStatus(new Path(path.toUri().toString())).getLen();
//            System.err.println("length:"+length);
            contentLength+=length;
        }
        System.err.println("contentLength:"+contentLength);
        response.setContentLength((int)contentLength);//抄过int最大值是会成0
        int i=0;
        for(Path path:pathList){
            String pathStr = path.toUri().toString();
            String[] pathStrArr = path.toUri().toString().split("\\/");
            String fileName = pathStrArr[pathStrArr.length-1];
            if(fileName.equalsIgnoreCase("_SUCCESS")){
                continue;
            }
            System.err.println("开始读取：" + path.toUri().toString());
            if(i==0){//第一次设置
                //获取文件后缀名比如，.snappy,只要设置一次就行了
                String fileFormat = ".txt";
                if(fileName.contains(".")){
                    fileFormat="."+fileName.split("\\.")[1];
                }
                response.setHeader("Content-disposition", "attachment;filename="+ table.getTableName()+fileFormat);//设置流类型
            }
            FSDataInputStream hdfsInStream = hdfs.open(path);
            byte[] ioBuffer = new byte[1024];
            int readLen = hdfsInStream.read(ioBuffer);
            while(readLen!=-1)
            {
//                System.out.write(ioBuffer, 0, readLen);//输出
                readLen = hdfsInStream.read(ioBuffer);
                os.write(ioBuffer);
                os.flush();
            }
            os.flush();
            hdfsInStream.close();
            i++;
        }
        os.flush();
        os.close();
        hdfs.close();
    }

    @RequestMapping(value = "exportDataForSearch", method = RequestMethod.POST)
    @ResponseBody
    public Response exportDataForSearch(String codeTxt) throws Exception {
        HiveDBOperator.HiveQueryResult hiveTableTableContent = searchDatacaches.get(new MD5().getMD5ofStr(codeTxt));
        if(hiveTableTableContent.getMetaData().size()==0){
            return new Response(false,"无结果");
        }
        String tableName = "table";
        String path=CommonUtil.writeXML(hiveTableTableContent,tableName);
        return new Response(true,path);
    }

    @RequestMapping(value = "previewHiveTableData", method = RequestMethod.POST)
    @ResponseBody
    public Response hiveDataPreview(String tableID,String partition) throws Exception {//partitionFieldName:createday=2016-06-13
        TbTBLS table = CommonDML.getTableByTableID(tableID);
        String tableName=table.getTableName();
        TbDBS db = CommonDML.getDBByTableID(tableID);
        logger.info("hive数据表预览》dbName:"+db.getName()+",tableName:"+table.getTableName());
        String sql=null;
        if("".equals(partition)){
            sql = "select * from " +tableName+ " limit 200" ;
        }else{
            String partitionName = partition.split("=")[0];
            String partitionValue = partition.split("=")[1];
            TbPartitionKeys tbPartitionKeys = hive.queryOne(String.format("select * from PARTITION_KEYS where TBL_ID='%s'", tableID), TbPartitionKeys.class);
            //如果索引字段是字符串类型,则需要在值前面加上引号比如：createday='2016-06-13'
            if(tbPartitionKeys.getpKeyType().equalsIgnoreCase("string")){
                sql = "select * from " +tableName+ " where " + partitionName + "='" +partitionValue + "' limit 200" ;
            }else{
                sql = "select * from " +tableName+ " where " + partition + " limit 200" ;
            }
        }
        HiveDBOperator.HiveQueryResult result = null;
        try{
            result = HiveDBOperator.query(db.getName(),sql);
        }catch (SQLException e){
            return new Response(false,"<font color='red'>SQL执行异常：\n</font>"+e.getMessage());
        }
        return new Response(true, CommonUtil.assemblyResult(result, tableName));
    }

    @RequestMapping(value = "queryBySQL", method = RequestMethod.POST)
    @ResponseBody
    public Response queryBySQL(String codeText) throws Exception {
        System.err.println("SQL:"+codeText);
        String cacheKey = new MD5().getMD5ofStr(codeText);
        if(codeText != null && !codeText.equals("")){
            if(codeText.endsWith(";")){
                codeText = codeText.substring(0,codeText.length()-1);
            }
            if(codeText.toUpperCase().startsWith("SELECT") && !codeText.toUpperCase().contains("LIMIT")){
                codeText += " limit " + Constants.defaultSearchSizeLimit;
            }
        }
        HiveDBOperator.HiveQueryResult result = null;
        try{
            result = HiveDBOperator.query("",codeText);
        }catch (SQLException e){
            return new Response(false,"<font color='red'>SQL执行异常：\n</font>"+e.getMessage());
        }
        String returnResult = CommonUtil.assemblyResult(result, "");
        //将查询结果放入缓存里，用于可能的下载需要
//        JSONObject jsonObject = JSON.parseObject(returnResult);
//        JSONArray jsonArray = jsonObject.getJSONArray("hiveTableTableContent");
//        searchDatacaches.put(cacheKey,jsonArray);
        searchDatacaches.put(cacheKey,result);
        return new Response(true, returnResult);
    }

    @RequestMapping(value = "deleteTable", method = RequestMethod.POST)
    @ResponseBody
    public Response deleteTable(String tableID,String authCode) throws Exception {
        if(authCode.equals("admin123456")){//通过验证
            TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
            TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
            TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
            String sql = "DROP TABLE " + db.getName() + "." + table.getTableName();
            try{
                boolean status = HiveDBOperator.update(sql);
                if(status){
                    return new Response(true,"删除成功");
                }else{
                    return new Response(false,"删除失败");
                }
            }catch (SQLException e){
                return new Response(false,"<font color='red'>执行异常：\n</font>"+e.getMessage());
            }
        }else{
            return new Response(false,"验证失败，请联系管理员！");
        }
    }

    @RequestMapping(value = "deletePartition", method = RequestMethod.POST)
    @ResponseBody
    public Response deletePartition(String tableID,String partition,String authCode) throws Exception {
        if(authCode.equals("admin123456")){//通过验证
            TbTBLS table = hive.queryOne(String.format("select * from TBLS where TBL_ID='%s'", tableID), TbTBLS.class);
            TbSDS tbSDS = hive.queryOne(String.format("select * from SDS where SD_ID='%s'", table.getSdID()), TbSDS.class);
            TbDBS db = hive.queryOne(String.format("select * from DBS where DB_ID='%s'", table.getDbID()), TbDBS.class);
            //ALTER TABLE  mobile_live.genseedetail_bi DROP PARTITION (pdate='$today')
            String sql = "ALTER TABLE " + db.getName() + "." + table.getTableName() + " DROP PARTITION ("
                    + partition.split("=")[0] + "='" + partition.split("=")[1] + "')";
            System.err.println(sql);
            try{
                boolean status = HiveDBOperator.update(sql);
                if(status){
                    return new Response(true,"删除成功");
                }else{
                    return new Response(false,"删除失败");
                }
            }catch (SQLException e){
                return new Response(false,"<font color='red'>执行异常：\n</font>"+e.getMessage());
            }
        }else{
            return new Response(false,"验证失败，请联系管理员！");
        }
    }

    /**
     * 通过hive sql对 hive进行更新操作，包括建表，建库等
     * @param codeText
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "updateBySQL", method = RequestMethod.POST)
    @ResponseBody
    public Response updateBySQL(String codeText) throws Exception {
        System.err.println("SQL:"+codeText);
        if(codeText != null && !codeText.equals("")){
            if(codeText.endsWith(";")){
                codeText = codeText.substring(0,codeText.length()-1);
            }
        }
        try{
            boolean status = HiveDBOperator.update(codeText);
            if(status){
                return new Response(true,"更新已生效，请刷新相关主题域");
            }else{
                return new Response(false,"更新失败");
            }
        }catch (SQLException e){
            return new Response(false,"<font color='red'>执行异常：\n</font>"+e.getMessage());
        }
    }

    static {
        new Thread(
                new Runnable(){
                    public void run() {
                        System.err.println("启动自动清理缓存线程....");
                        while(true){
                            try {
                                Thread.sleep(1000*60*60);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            searchDatacaches.clear();
                        }
                    }
                }
        ).start();
    }

}
