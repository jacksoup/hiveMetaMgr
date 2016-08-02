#hive仓库元数据管理系统
##有如下功能：
*   1.hive元数据信息的查看，包括表基本信息，数据库基本信息，字段信息，分区信息，索引信息等；
*   2.对hive元数据的检索，包括表、字段、数据库等内容的检索
*   3.元数据信息更新（目前只提供对库、表、字段的描述信息进行更新，通过单击或者双击相应字段可进行相应编辑）；
*   4.对表数据或按分区进行预览
*   5.对表数据或按分区进行数据EXCEl格式导出
*   6.数据下载，直接从hdfs上下载原始数据，可以对整表或者分区进行下载
*   7.表或该表相应分区的删除（需要数据管理员验证码）
*   8.执行SQl查询与更新操作，并且可以进行查询结果的导出
*   9.执行SQl查询与更新操作，并且可以进行查询结果的导出
*   10.如果你的调度系统是zeus，那么可以通过本系统和zeus结合，实现对字段与表在zeus中的开发中心引用关系的查看

##系统使用截图如下：
![image](https://github.com/cnfire/hiveMetaMgr/tree/master/src/main/webapp/doc/1.jpg)

##使用说明：
*   1.下载
*   2.修改配置文件参考：
    *  (1).hivemeta/WEB-INF/classes/database.properties：
```
        #数据库连接池配置
        initialPoolSize = 10
        maxPoolSize = 50
        minPoolSize = 10
        #hive元数据库相关配置
        2.mysql.url = jdbc:mysql://192.168.8.46:3306/hive
        2.mysql.user = hive
        2.mysql.password = hive
        #hiveJdbc(hiveServer连接)配置
        hive.jdbc.url.pre=jdbc:hive2://namenode1:10000
        hive.user=hadoop
        hive.password=
```
    *  (2).hivemeta/WEB-INF/classes/common.properties：
```
    #hdfs连接配置
    namenode.url=hdfs://192.168.8.45:9000
```
    *  (3).使用maven命令编译并打成war包：
```
    mvn compile war:war
```
    *  (4).部署至tomcat并将目录修改为hivemeta
    *  (5).访问：eg：http://localhost:8080/hivemeta
    *  (6).默认的登录用户名和密码均为admin，删除表时输入的管理员验证码为admin123456

*  3.注意：
    *  需要确保hiveserver服务已开启，并且程序所在主机可以正常访问hiverserver与hdfs、hive请使用1.0以上版本，否则不知道能否兼容

