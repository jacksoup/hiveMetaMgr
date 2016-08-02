<%@ page import="org.caesar.bi.metadata.entity.metaMang.TbUser" %>
<%@ page import="org.caesar.utils.properties.PropertiesUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/common/tag.jsp" %>
<%@ include file="/common/header.jsp" %>
<html>
<title>数据仓库元数据管理系统</title>
<script type="text/javascript" src="static/lib/codemirror/codemirror.js"></script>
<link type="text/css" rel="stylesheet" href="static/lib/codemirror/codemirror.css"/>
<script type="text/javascript" src="static/lib/codemirror/mysql/mysql.js"></script>
<head>
    <style>

    </style>
    <script type="text/javascript">
//        Ext.require(['*']);
        Ext.Loader.setConfig({enabled: true});
        Ext.Loader.setPath('Ext.ux', 'static/lib/ext-4.2.1/examples/ux');
        Ext.require([
            'Ext.ux.form.SearchField',
        ]);
        Ext.define('Database', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'id', type: 'string'},
                {name: 'name', type: 'string'}
            ]
        });

        var myTreeStore = Ext.create('Ext.data.TreeStore', {
            model: 'Database',
            proxy: {
                type: 'ajax',
                url: '/hivemeta/readDatabaseTree'
            },
            root: {
                name: '根节点',
                qtip : '树节点',
                expanded: true,//根节点是否展开
                id:'-1'
            }
        });

        var databaseTree=Ext.create('Ext.tree.Panel', {
            width: 350,
            height: 500,
            title: '数据树形浏览',
            store: myTreeStore,
            rootVisible: false,//隐藏根节点
        //  useArrows:true,//是否在树中使用Vista样式箭头，默认为false。
//            viewConfig:{
//                stripeRows:true,//在表格中显示斑马线
//                enableTextSelection:true //可以复制单元格文字
//            },
            listeners: {
                itemclick : tree_itemclick,
                itemcontextmenu: function ( view, record, item, index, e, eOpts) {
                    e.preventDefault();  //屏蔽默认右键菜单
                    var idStr = record.data.id;
                    var tableName=record.data.name;
                    var txt;
                    if(idStr.split("_")[0]=="DB"){//过滤掉非叶子节点（数据库节点）的右击事件
                        txt="删除该数据库";
                        return;
                    }else{
                        txt="删除该表"
                    }
                    var tableID=idStr.split("_")[1];
                    var rightMenu = new Ext.menu.Menu({
                        items: [
                            {
                                text: txt,
                                icon:'static/images/extIcons/icons/delete2.png',
                                handler: function(){
                                    Ext.Msg.prompt('删除表"'+tableName+'"确认', '请输入管理员验证码:', function(btn, text){
                                        if (btn == 'ok'){
                                            var myMask = new Ext.LoadMask(Ext.getBody(), {
                                                msg: '正在删除，请稍后...',
                                                removeMask: true     //完成后移除
                                            });
                                            myMask.show();
                                            Ext.Ajax.request({
                                                url: '/hivemeta/deleteTable',
                                                params: {
                                                    tableID: tableID,
                                                    authCode:text
                                                },
                                                success: function (response) {
                                                    myMask.hide();
                                                    var obj = Ext.decode(response.responseText);
                                                    if(obj.success==false){
                                                        Ext.Msg.alert('错误提示',obj.msg);
                                                    }else{
                                                        databaseTree.getStore().reload();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            },
                            {
                                text: '数据预览',
                                icon: 'static/images/extIcons/icons/application_form_magnify.png',
                                handler: function () {
                                    previewHiveTableData(tableID, "");
                                }
                            },
                            {
                                text: 'EXCEL导出',
                                icon: 'static/images/extIcons/icon/page_excel.png',
                                handler: function () {
                                    exportDataWithExcel(tableID, "");
                                }
                            }
                        ]
                    });
                    rightMenu.showAt(e.getXY());
                },
            },
            columns: [{
                xtype: 'treecolumn',
                sortable:true,
                header: '点此按名称进行排序',
                dataIndex: 'name',
                flex: 1
            }],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'toolbar',
                    items: [
        //                                    '->',
                        {
                            xtype: 'button',
                            text: '展开',
                            icon: 'static/images/extIcons/icons/arrow_inout.png',  // Use a URL in the icon config
                            tooltip: '展开所有节点',
                            handler: function () {
                                this.ownerCt.ownerCt.expandAll();//获得当前节点对象的父父节点对象（树对象）
                            }
                        },
                        {
                            xtype: 'button',
                            text: '收缩',
                            icon: 'static/images/extIcons/icons/arrow_in.png',  // Use a URL in the icon config
                            tooltip: '收缩所有节点',
                            handler: function () {
                                this.ownerCt.ownerCt.collapseAll();
                            }
                        },
                        {
                            xtype: 'button',
                            text: '刷新',
                            icon: 'static/images/extIcons/icons/arrow_refresh.png',  // Use a URL in the icon config
                            tooltip: '刷新树形数据库',
                            handler: function () {
                                this.ownerCt.ownerCt.getStore().reload();
                            }
                        },
                    ]
                }],
        })

        var tabs=Ext.create('Ext.tab.Panel', {
            region: 'center', // a center region is ALWAYS required for border layout
//            title:'hehehe',
            deferredRender: false,//是否延迟渲染，默认为true。
            enableTabScroll:true,//是否允许Tab溢出时可以滚动，默认为false。
            activeTab: 0,     // 初始激活的tab，索引或者id值，默认为none
//            autoDestroy:false,
            items: [
                {
                contentEl: 'center1',
                title: '主页',
                closable: false,//tab是否可关闭，默认为false
                autoScroll: true
            },
//                {
//                contentEl: 'center2',
//                title: 'Center Panel',
//                autoScroll: true
//            }
            ],
            plugins : Ext.create('Ext.ux.TabCloseMenu', {//右键菜单
                closeTabText : '关闭当前页',
                closeOthersTabsText : '关闭其他页',
                closeAllTabsText : '关闭所有页'
            }),
        });


        Ext.define('TablePartition', {
            extend: 'Ext.data.Model',
            fields: ['name','uri','createTime','tableID','partitionID']
        });
        var partitionStore = Ext.create('Ext.data.TreeStore', {
            model: 'TablePartition',
            proxy: {
                type: 'ajax',
                url: '/hivemeta/readTablePartitionInfo?tableId=null'//proxy不设置会报错（奇怪），在这里设置读取空值
            },
            root: {
                name: '根节点',
                uri : 'uri :',
                expanded: true,//根节点是否展开
//                id:'-100000'
            }
        });

        function addTab (idStr,nodeName) {
            //截取tableID并过滤文件夹节点点击事件
            strs=idStr.split("_");
            if(strs[0]=="TB"){
                var tableId = strs[1];
                Ext.define('Table', {
                    extend: 'Ext.data.Model',
                    //默认字段类型是string，因为下面要按照fieldID排序，因此指明为int
                    fields: [ 'tableID','field',{name: 'fieldID', type: 'int'}, 'type','isPartitionFiled','isIndexFiled','comment' ]
                });
                var tableStore = Ext.create('Ext.data.Store', {
                    model: 'Table',
//                autoLoad: true,
                    pageSize: 30,//TO DO 分页有bug
                    proxy: {
                        type: 'ajax',
                        url: '/hivemeta/readTableFieldsInfo?tableId='+tableId,
                        reader: {
                            type: 'json',
                            root: 'fields',
                            totalProperty: 'total'
                        }
                    },
                    sorters: [{
                        property: 'fieldID',
                        direction: 'ASC'
                    }]
                });
                tableStore.loadPage(1);
//                tableStore.reload(1);

                var tableGrid=Ext.create('Ext.grid.Panel', {
                    store: tableStore,
//                    closable: true,
//            width: 250,
//            height: 200,
                    maxHeight: 600,
                    title: '表字段信息',
                    icon: 'static/images/extIcons/icons/information.png',
                    //在默认情况下，Extjs GridPanel不允许进行选中单元格中的内容，由于不能选中，我们就不可能来复制单元格中的内容。如果要实现这种功能，我们需要通过viewConfig来实现。
                    viewConfig:{
                        stripeRows:true,//在表格中显示斑马线
                        enableTextSelection:true //可以复制单元格文字
                    },
                    columns: [
                        { xtype:"rownumberer", text: "序号", width:40 },
                        {
                            text: 'tableID',
//                    sortable: false,
                            hidden: true,
                            dataIndex: 'tableID'
                        },
                        {
                            text: '字段ID',
                            width: 60,
                            dataIndex: 'fieldID',
                            hidden: true
                        },
                        {
                            text: '字段',
                            width: 100,
                            dataIndex: 'field',
                        },
                        {
                            text: '类型',
                            width: 100,
//                    flex: 1,
                            dataIndex: 'type'
                        },
                        {
                            text: '分区字段',
                            width: 70,
                            dataIndex: 'isPartitionFiled',
//                    hidden: true
                        },
                        {
                            text: '索引字段',
                            width: 70,
                            dataIndex: 'isIndexFiled',
                        },
                        {
                            text: '描述',
                            width: 200,
                            editor: 'textfield',
                            dataIndex: 'comment',
                            tooltip:'描述',
//                            listeners : {
//                                mouseover : function (view, record, item, index, e, eOpts) {
//                                    if (view.tip == null) {  //这块判断很重要，不能每次都创建个tooltip，要不显示会有问题。
//                                        view.tip = Ext.create('Ext.tip.ToolTip', {
//                                            text:'fsf',
//                                            // The overall target element.
//                                            target: view.el,
//                                            // Each grid row causes its own separate show and hide.
//                                            delegate: view.itemSelector,
//                                            // Moving within the row should not hide the tip.
//                                            //  trackMouse: false,
//                                            // Render immediately so that tip.body can be referenced prior to the first show.
//                                            renderTo: Ext.getBody()
//                                        });
//                                    }
//                                    var gridColums = view.getGridColumns();
//                                    var column = gridColums[e.getTarget(view.cellSelector).cellIndex];
//                                    view.el.clean();
//                                    view.tip.update(record.data[column.dataIndex]);
//                                }
//                            }
                        },
//                        {
//                            xtype:'actioncolumn',
//                            header:"操作",
//                            width: 50,
//                            items: [{
////                                text:'ff',//Extjs4 actioncolumn只能显示图标，不能显示文字
//                                icon: 'static/images/extIcons/icons/icon_jump.png',  // Use a URL in the icon config
//                                tooltip: '查看该字段在开发中心的引用文档',
//                                handler: function (grid, rowIndex, colIndex) {
//                                    var reord = grid.getStore().getAt(rowIndex);
//                                    getRelatedZeusDoc(reord.get('tableID'),reord.get('field'));
//                                }
//                            }
//                            ]
//                        },
                    ],
                    dockedItems: [
//                        {
//                            dock: 'bottom',
//                            xtype: 'toolbar',
//                            items: [
//        //                    '->',
//        //                 {
//        //                    xtype: 'button',
//        //                    text: '搜索',
//        //                    tooltip: '点此进行搜索'
//        //                },
//                                {//分页
//                                    xtype: 'pagingtoolbar',
//                                    store: tableStore,   // same store GridPanel is using
//                                    dock: 'bottom',//按钮在底部显示
//                                    displayInfo: true
//                                }
//                            ]
//                         },
                       {
                            dock: 'top',
                            xtype: 'toolbar',
                            items: [
//                                {
//                                    text:'<u><font color="blue">查看引用文档</font></u>',
//                                    icon: 'static/images/extIcons/icons/icon_jump.png',  // Use a URL in the icon config
//                                    tooltip: '查看该表在开发中心的引用文档',
//                                    handler: function () {
//    //                                    var tableID = tableGrid.getStore().getAt(0).get("tableID");
//                                        var tableID = this.ownerCt.ownerCt.getStore().getAt(0).get("tableID");
//                                        getRelatedZeusDoc(tableID,null);
//                                    }
//                                },
                                {
                                    text:'<u><font color="blue">数据预览</font></u>',
                                    icon: 'static/images/extIcons/icons/application_form_magnify.png',  // Use a URL in the icon config
                                    tooltip: '预览数据',
                                    handler: function () {
    //                                    var tableID = tableGrid.getStore().getAt(0).get("tableID");
                                        var tableID = this.ownerCt.ownerCt.getStore().getAt(0).get("tableID");
                                        previewHiveTableData(tableID,"");
                                    }
                                },
                                {
                                    text:'<u><font color="blue">EXCEL导出</font></u>',
                                    icon: 'static/images/extIcons/icon/page_excel.png',  // Use a URL in the icon config
                                    tooltip: '以EXCEL格式导出数据',
                                    handler: function () {
                                        var tableID = this.ownerCt.ownerCt.getStore().getAt(0).get("tableID");
                                        exportDataWithExcel(tableID,"");
                                    }
                                },
                                {
                                    text:'<u><font color="blue">数据下载</font></u>',
                                    icon: 'static/images/extIcons/icons/download.png',  // Use a URL in the icon config
                                    tooltip: '数据下载',
                                    handler: function () {
                                        var tableID = this.ownerCt.ownerCt.getStore().getAt(0).get("tableID");
                                        downloadData(tableID,"");
                                    }
                                },
                            ]
                        },
                    ],
                    plugins: [
//                        Ext.create('Ext.grid.plugin.RowEditing', {
//                            clicksToEdit: 2,  //双击进行修改  1-单击   2-双击    0-可取消双击/单击事件
//                            saveBtnText: '保存',
//                            cancelBtnText: "取消",
//                        }),
                        Ext.create('Ext.grid.plugin.CellEditing', {
                            clicksToEdit: 2,  //双击进行修改  1-单击   2-双击    0-可取消双击/单击事件
                        })
                    ],
                    listeners:{
//                        itemclick : function(){},//单击行事件
//                        itemdblclick : function() {//双击行事件
//                            var record = this.getSelectionModel().getLastSelected();
////                    alert(record.get("tableID") + "," + record.get("field"));
//                            getRelatedZeusDoc(record.get("tableID"),record.get("field"));
//                        }
                        edit:function(rowIndex, columnIndex, e) {
//                            alert(tableGrid.getStore().getAt(rowIndex).get("comment"));
                            var comment = this.getSelectionModel().getLastSelected().get("comment");
                            if(comment==null || comment==""){
                                return;
                            }
                            var myMask = new Ext.LoadMask(Ext.getBody(), {
                                msg: '正在修改，请稍后...',
                                removeMask: true     //完成后移除
                            });
                            myMask.show();
                            Ext.Ajax.request({
                                url: '/hivemeta/updateTableField',
                                params: {
                                tableID: this.getSelectionModel().getLastSelected().get("tableID"),
                                fieldID: this.getSelectionModel().getLastSelected().get("fieldID"),
                                isPartitionFiledStr: this.getSelectionModel().getLastSelected().get("isPartitionFiled"),
                                comment: this.getSelectionModel().getLastSelected().get("comment"),
                            },
                            success: function (response) {
                                    myMask.hide();
                                    var obj = Ext.decode(response.responseText);
//                                    var data = JSON.parse(obj.msg);
                                    if(obj==true){
                                        tableGrid.getStore().reload();
                                    }else{
                                        Ext.Msg.show({
                                            title:'操作提示',
                                            msg: '修改失败（提示：值不能为空或null)',
                                            buttons: Ext.Msg.YES,
                                            icon: Ext.Msg.WARNING
                                        });
                                    }
                                }
                            });


                        }

                    },
                });

                var PropertyGrid=Ext.create('Ext.grid.PropertyGrid', {
                    title: '表基本信息',
                    icon: 'static/images/extIcons/icons/information.png',
                    sortableColumns:false,//禁止对属性表格字段的自动排序
                    customEditors:{//禁止相关字段的可编辑功能（注意，用beforeedit的cancel方法会让所有字段不可编辑）
                        "表ID":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "表名":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "所属数据库":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "所有者":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "创建时间":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "最近一次更新时间":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "存储格式":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "存储路径":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "表类型":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "是否为分区表":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "字段分隔符":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "是否压缩":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "压缩格式":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "拥有字段数量":new Ext.form.TextField({disabled:true,disabledClass:""}),
//                        "文件分片数量":new Ext.form.TextField({disabled:true,disabledClass:""}),
//                        "该表总的记录数":new Ext.form.TextField({disabled:true,disabledClass:""}),
                        "描述":new Ext.form.TextField({disabled:false,disabledClass:""}),
                    },
                    listeners:{
                        propertychange: function (source, name, newValue, oldValue) {//属性值被修改（发生变化）事件
//                            alert(Ext.encode(source));
                            if(name=="描述" && (newValue!="null"||newValue!="")){
                                var myMask = new Ext.LoadMask(Ext.getBody(), {
                                    msg: '正在修改，请稍后...',
                                    removeMask: true     //完成后移除
                                });
                                myMask.show();
                                Ext.Ajax.request({
                                    url: '/hivemeta/updateTableDesc',
                                    params: {
                                        tableID: source["表ID"],
                                        desc: newValue
                                    },
                                    success: function (response) {
                                        myMask.hide();
                                        var data = Ext.decode(response.responseText);
                                        if (data.success == false) {
                                            Ext.Msg.alert('错误提示', "修改失败");
                                            return;
                                        }
                                    }
                                });
                            }
                        }
                    }
//                    closable: true,
//            source: {
//                "(name)": "Properties Grid",
//                "grouping": false,
//                "autoFitColumns": true,
//                "productionQuality": false,
//                "created": Ext.Date.parse('10/15/2006', 'm/d/Y'),
//                "tested": false,
//                "version": 0.01,
//                "borderWidth": 1
//            }
//                    dockedItems: [{
//                        dock: 'bottom',
//                        xtype: 'toolbar',
//                        items: [
////                    '->',
////                 {
////                    xtype: 'button',
////                    text: '搜索',
////                    tooltip: '点此进行搜索'
////                },
//                            {
//                                text:'<u><font color="blue">查看该表在开发中心的引用文档</font></u>',
//                                icon: 'static/images/extIcons/icon/arrow_switch.png',  // Use a URL in the icon config
//                                tooltip: '查看该表在开发中心的引用文档',
//                                handler: function () {
//                                    var obj=PropertyGrid.getSource();//map结构
//                                    var tableID=null;
//                                    for(var item in obj){
//                                        if(item=="表ID"){
//                                            //alert(item + ":" + obj[item]);
//                                            tableID=obj[item];
//                                            break;
//                                        }
//                                    }
////                    alert(tableID);
//                                    getRelatedZeusDoc(tableID,null);
//                                }
//                            }
//                        ]
//                    }],
//                    listeners:{
//                        itemdblclick : function(){//双击行事件
////                    alert(PropertyGrid.getId());
//                            var obj=PropertyGrid.getSource();//map结构
//                            var tableID=null;
//                            for(var item in obj){
//                                if(item=="表ID"){
//                                    //alert(item + ":" + obj[item]);
//                                    tableID=obj[item];
//                                    break;
//                                }
//                            }
////                    alert(tableID);
//                            getRelatedZeusDoc(tableID,null);
//                        }
//                    },
                });
//       PropertyGrid默认情况下属性字段是可以编辑的，通过这个方法设置属性值字段只能看不能动
//        PropertyGrid.on("beforeedit",function(e){
//            e.cancel = true;
//            return false;
//        });

                var tableIndexInfoGrid = Ext.create('Ext.grid.Panel', {
                    title: '表索引信息',
                    icon: 'static/images/extIcons/icons/information.png',
//                    closable: true,
                    store: Ext.create('Ext.data.Store', {
                        model: Ext.define('tableIndexInfoModel', {
                            extend: 'Ext.data.Model',
                            fields: [ 'indexID','indexName', 'createTime','fieldName','fieldType','handlerClass' ]
                        }),
//            proxy: {
//                    type: 'ajax',
//                    url: '/hivemeta/readTableIndexInfo',
//                    reader: {
//                        type: 'json',
//                        root: 'fields',
//                        totalProperty: 'total'
//                    }
//                }
                    }),
                    columns: [
                        { text: '索引ID',  dataIndex: 'indexID',width:50 },
                        { text: '索引名', dataIndex: 'indexName',width:100},
                        { text: '创建时间', dataIndex: 'createTime',width:120},
                        { text: '作用字段名', dataIndex: 'fieldName',width:80},
                        { text: '字段类型', dataIndex: 'fieldType',width:80},
                        { text: '索引处理类', dataIndex: 'handlerClass',width:150}
                    ],
//            height: 200,
//            width: 400,
                });

                var partitionTree = Ext.create('Ext.tree.Panel', {
                    store: partitionStore,
                    title: '表分区信息',
                    icon: 'static/images/extIcons/icons/information.png',
                    rootVisible:false,
//                    closable: true,
//            width: 300,
//            height: 300,
                    maxHeight: 600,
//            fields: ['name', 'description'],
                    viewConfig:{
                        stripeRows:true,//在表格中显示斑马线
                        enableTextSelection:true //可以复制单元格文字
                    },
                    columns: [
                        {
                            xtype: 'treecolumn',
                            text: '分区字段',
                            dataIndex: 'name',
                            width: 220,
                            sortable: true
                        },
                        {
                            text: '表ID',
                            dataIndex: 'tableID',
                            hidden: true
                        },
                        {
                            text: '分区ID',
                            dataIndex: 'partitionID',
                            hidden: true
                        },
                        {
                            text: '创建时间',
                            dataIndex: 'createTime',
                            width: 150,
//                            hidden:true,
                            sortable: true
                        },
                        {
                            text: '分区路径',
                            dataIndex: 'uri',
                            width: 400,
                            sortable: false
                        },
                        {
                            xtype:'actioncolumn',
                            header:"操作",
                            width: 100,
                            items: [
                                {
//                                    text:'<u><font color="blue">数据预览</font></u>',
                                    icon: 'static/images/extIcons/icons/application_form_magnify.png',  // Use a URL in the icon config
                                    tooltip: '分区预览数据',
                                    handler: function(grid, rowIndex, colIndex, actionItem, event, record, row) {
                                        var tableID = record.get("tableID");
                                        var partitionID = record.get("partitionID");
                                        var partition = record.get("name");
                                        if(partitionID==null||partitionID==""){
//                                            previewHiveTableData(tableID,"");
                                            Ext.Msg.alert("提示","请选择一个分区进行预览！");
                                        }else{
                                            previewHiveTableData(tableID,partition);
                                        }
                                    }
                                },
                                {
                                    icon: 'static/images/extIcons/icon/page_excel.png',  // Use a URL in the icon config
                                    tooltip: '以Excel格式导出数据',
                                    handler: function(grid, rowIndex, colIndex, actionItem, event, record, row) {
                                        var tableID = record.get("tableID");
                                        var partitionID = record.get("partitionID");
                                        var partition = record.get("name");
                                        if(partitionID==null||partitionID==""){
                                            Ext.Msg.alert("提示","请选择一个分区进行数据导出！");
                                        }else{
                                            exportDataWithExcel(tableID,partition);
                                        }
                                    }
                                },
                                {
                                    icon: 'static/images/extIcons/icons/download.png',  // Use a URL in the icon config
                                    tooltip: '下载分区数据',
                                    handler: function(grid, rowIndex, colIndex, actionItem, event, record, row) {
                                        var tableID = record.get("tableID");
                                        var partitionID = record.get("partitionID");
                                        var partition = record.get("name");
                                        if(partitionID==null||partitionID==""){
                                            Ext.Msg.alert("提示","请选择一个分区进行数据下载！");
                                        }else{
                                            downloadData(tableID,partition);
                                        }
                                    }
                                },
                                {
                                    icon:'static/images/extIcons/icons/delete2.png',
                                    tooltip: '删除分区数据',
                                    handler: function(grid, rowIndex, colIndex, actionItem, event, record, row){
                                        var tableID = record.get("tableID");
                                        var partitionID = record.get("partitionID");
                                        var partition = record.get("name");
                                        if(partitionID==null||partitionID==""){
                                            Ext.Msg.alert("提示","请选择一个分区进行删除！");
                                        }else{
                                            Ext.Msg.prompt('删除分区"'+partition+'"确认', '请输入管理员验证码:', function(btn, text){
                                                if (btn == 'ok'){
                                                    var myMask = new Ext.LoadMask(Ext.getBody(), {
                                                        msg: '正在删除，请稍后...',
                                                        removeMask: true     //完成后移除
                                                    });
                                                    myMask.show();
                                                    Ext.Ajax.request({
                                                        url: '/hivemeta/deletePartition',
                                                        params: {
                                                            tableID: tableID,
                                                            partition:partition,
                                                            authCode:text
                                                        },
                                                        success: function (response) {
                                                            myMask.hide();
                                                            var obj = Ext.decode(response.responseText);
                                                            if(obj.success==false){
                                                                Ext.Msg.alert('错误提示',obj.msg);
                                                            }else{
                                                                partitionTree.getStore().reload();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                },
                            ],
                            renderer:function(val, m, rec) {//如果为父节点就不显示操作按钮（父节点分区ID为空）
                                if (rec.get('partitionID')=="" || rec.get('partitionID')==null){
                                    return (new Ext.grid.column.actioncolumn).renderer(val);
                                }else{
                                    return '';
                                }
                            }
                        },
                    ],
                });

//                tableStore.setProxy({
//                    type: 'ajax',
//                    url: '/hivemeta/readTableFieldsInfo?tableId='+tableId,
//                    reader: {
//                        type: 'json',
//                        root: 'fields',
//                        totalProperty: 'total'
//                    }
//                });
//                tableStore.reload();
////                tableStore.loadPage(1);

                tableIndexInfoGrid.getStore().setProxy({
                    type: 'ajax',
                    url: '/hivemeta/readTableIndexInfo?tableId='+tableId,
//                    params:{
//                        tableId:tableId
//                    }
                });
                tableIndexInfoGrid.getStore().reload();

                partitionStore.setProxy({
                    type: 'ajax',
                    url: '/hivemeta/readTablePartitionInfo?tableId='+tableId
                });
                partitionStore.reload();

                Ext.Ajax.request({
                    url:'/hivemeta/readTableBaseInfo',
                    params:{
                        tableId:tableId
                    },
                    success: function(response) {
                        var obj = Ext.decode(response.responseText);
                        var data = JSON.parse(obj.msg);
                        PropertyGrid.setSource({
                            "表ID": data.tableID,
                            "表名": data.tableName,
                            "所属数据库": data.dbType,
                            "所有者": data.owner,  // date type
                            "创建时间": data.createTime,  // boolean type
                            "最近一次更新时间": data.lastUpdateTime,
                            "存储格式": data.inputFormat,      // decimal type
                            "存储路径": data.location,
                            "表类型": data.tableType,
                            "是否为分区表": data.hasPartition,
                            "字段分隔符": data.fieldDelimiter,
                            "是否压缩": data.isCompress,
                            "压缩格式": data.compressFormat,
                            "拥有字段数量": data.fieldsCount,
//                            "文件分片数量": data.numFiles,
//                            "该表总的记录数": data.totalSize,
                            "描述": data.comment,
                        });
                        PropertyGrid.updateLayout();
                    }
                });
//                var tab=
                    tabs.add({
                    id:idStr,
                    title: nodeName,
                    icon:'static/images/extIcons/icons/table.png',
                    closable: true,
                    autoScroll: true,
//                iconCls: 'tabs'
//                html: 'Tab Body ',
//                    items: [tableGrid,PropertyGrid,tableIndexInfoGrid,partitionTree],
                    items: [
                        Ext.create('Ext.tab.Panel', {
//                        height:600,
                        tabPosition: 'top',
//                        tabPosition: 'bottom',
                        items: [tableGrid,PropertyGrid,tableIndexInfoGrid,partitionTree]
                    })
                    ]
                }).show();
//                });
//                tabs.setActiveTab(tab);
//                tabs.doLayout();
            }else if(strs[0]=="DB"){
                var dbId = strs[1];
                Ext.Ajax.request({
                    url:'/hivemeta/readDBInfo',
                    params:{
                        dbID:dbId
                    },
                    success: function(response,opts) {
                        var obj = Ext.decode(response.responseText);
                        var data = JSON.parse(obj.msg);
                        var dataBasePropertyGrid=Ext.create('Ext.grid.PropertyGrid', {
                            title: '数据库基本信息',
                            sortableColumns:false,//禁止对属性表格字段的自动排序
                            closable: true,
                            source: {
                                "数据库ID": data.id,
                                "数据库名": data.name,
                                "所有者": data.owner,  // date type
                                "存储路径": data.location,
                                "子表数量": data.tableCount,
                                "描述": data.desc,      // decimal type
                            },
                            customEditors:{//禁止相关字段的可编辑功能（注意，用beforeedit的cancel方法会让所有字段不可编辑）
                                "数据库ID":new Ext.form.TextField({disabled:true,disabledClass:""}),
                                "数据库名":new Ext.form.TextField({disabled:true,disabledClass:""}),
                                "存储路径":new Ext.form.TextField({disabled:true,disabledClass:""}),
                                "子表数量":new Ext.form.TextField({disabled:true,disabledClass:""}),
                                "所有者":new Ext.form.TextField({disabled:true,disabledClass:""}),
                                "描述":new Ext.form.TextField({disabled:false,disabledClass:""}),
                            },
                            listeners: {
                                propertychange: function (source, name, newValue, oldValue) {//属性值被修改（发生变化）事件
//                            alert(Ext.encode(source));
                                    if(name=="描述" && (newValue!="null"||newValue!="")){
                                        var myMask = new Ext.LoadMask(Ext.getBody(), {
                                            msg: '正在修改，请稍后...',
                                            removeMask: true     //完成后移除
                                        });
                                        myMask.show();
                                        Ext.Ajax.request({
                                            url: '/hivemeta/updateDatabaseDesc',
                                            params: {
                                                dbID: source["数据库ID"],
                                                desc: newValue
                                            },
                                            success: function (response) {
                                                myMask.hide();
                                                var data = Ext.decode(response.responseText);
                                                if (data.success == false) {
                                                    Ext.Msg.alert('错误提示', "修改失败");
                                                    return;
                                                }
                                            }
                                        });
                                    }
                                }
                            },
                        });
//                        // PropertyGrid默认情况下属性字段是可以编辑的，通过这个方法设置属性值字段只能看不能动
//                        dataBasePropertyGrid.on("beforeedit",function(e){
//                            e.cancel = true;
//                            return false;
//                        });
                        dataBasePropertyGrid.updateLayout();
                        tabs.add({
                            id:idStr,
                            title: nodeName,
                            icon:'static/images/extIcons/icons/database.png',
                            closable: true,
                            autoScroll: true,
                            items: [dataBasePropertyGrid]
                        }).show();
                    }
                });
            }
        }
        // 节点点击事件
        function tree_itemclick( node, event )
        {
            var idStr = event.data.id ;
            var nodeName=event.data.name ;
            tab=Ext.getCmp(idStr+'');
            if(tab!=null){//该标签页已经打开
                tabs.setActiveTab(tab);
            }else{
                addTab(idStr,nodeName);
            }
        };
        function previewHiveTableData(tableID,partition){
            var myMask = new Ext.LoadMask(Ext.getBody(), {
                msg: '正在加载数据，请稍后...',
                removeMask: true     //完成后移除
            });
            myMask.show();
            Ext.Ajax.request({
                url:'/hivemeta/previewHiveTableData',
                params:{
                    tableID:tableID,
                    partition:partition
                },
                success: function(response) {
                    myMask.hide();
                    var text = response.responseText;
                    var datajson = JSON.parse(text);
                    if(datajson.success==false){
                        Ext.Msg.alert('错误提示',datajson.msg);
                        return;
                    }
                    var data;
                    try{
                        data = JSON.parse(datajson.msg);
                    }catch (e){
                        Ext.Msg.alert('提示', '数据中包含非法JSON字符,解析失败！\n(可以选择下载分区数据至本地进行预览)');
                        return;
                    }
                    var hiveTableModelJson=data.hiveTableModelJson;
                    hiveTableModelArr=eval("("+"["+data.hiveTableModelJson+"]"+")");;
                    if(data.hiveTableTableContent.length==0){
                        Ext.Msg.alert('提示', '无数据');
                        return;
                    }
                    Ext.create('Ext.window.Window', {
                        title: '数据预览',
                        height: 600,
                        width: 900,
                        layout: 'fit',
                        maximizable: true,//显示窗口最大化按钮
                        items: [
                            Ext.create('Ext.grid.Panel', {
                                title: '预览表[' + data.tableName+']前'+data.hiveTableTableContent.length+'行数据',
                                store: Ext.create('Ext.data.Store', {
                                    model: Ext.define('zesuFileModel', {
                                        extend: 'Ext.data.Model',
                                        fields: hiveTableModelArr
                                    }),
                                    data:data.hiveTableTableContent
                                }),
                                columns: data.hiveTableColumns,
                                viewConfig:{
                                    stripeRows:true,//在表格中显示斑马线
                                    enableTextSelection:true //可以复制单元格文字
                                },
                            }),
                        ]
                    }).show();
                },
            });
        };

        function exportDataWithExcel(tableID,partition){
            var myMask = new Ext.LoadMask(Ext.getBody(), {
                msg: '正在准备数据，请稍后...',
                removeMask: true     //完成后移除
            });
            myMask.show();
            Ext.Ajax.request({
                url: '/hivemeta/exportDataWithExcel',
                params: {
                    tableID: tableID,
                    partition: partition
                },
                success: function (response) {
                    myMask.hide();
                    var text = response.responseText;
                    var datajson = JSON.parse(text);
                    if (datajson.success == false) {
                        Ext.Msg.alert('错误提示', datajson.msg);
                    }
                    var path = datajson.msg;
                    Ext.Msg.show({
                        title: '下载提示',
                        msg: "EXCEL数据准备完毕，开始下载?",
//                       msg: "据准备完毕，开始下载?"+text.split("/")[text.split("/").length-1],
                        buttons: Ext.Msg.YESNO,
                        icon: Ext.Msg.QUESTION,
                        fn: function (btn) {
                            if (btn == 'yes') {
                                window.location = common.CONTEXT_PATH + path;
                            }
                        }
                    });
//                                                                       Ext.Msg.show({
//                                                        title:'文件下载提示',
//                                                        msg: '数据准备完毕，开始下载，请等候...',
////                                                        buttons: Ext.Msg.YESNOCANCEL,
////                                                        icon: Ext.Msg.QUESTION,
//                                                        progress:true,
//                                                        wait:true,
//                                                        processText:'正在导出中...',
////                                                        waitConfig:{interval:100}
//                                                    });
//                                                    Ext.Msg.hide();
//                                                    Ext.Msg.show({
//                                                        title:"标题",
//                                                        progress:true,
//                                                        wait:true,
//                                                        msg:'数据准备完毕开始下载，请等候...',
//                                                        processText:'正在导出中...',
//                                                        width:300,wait:true, waitConfig:{interval:100},	nimEl:'btSave'
//                                                    });
//                                                    Ext.MessageBox.hide();
                 }
             });
        }

        function exportDataForSearch(codeTxt){
            var myMask = new Ext.LoadMask(Ext.getBody(), {
                msg: '正在准备数据，请稍后...',
                removeMask: true     //完成后移除
            });
            myMask.show();
            Ext.Ajax.request({
                url: '/hivemeta/exportDataForSearch',
                params: {
                    codeTxt: codeTxt
                },
                success: function (response) {
                    myMask.hide();
                    var text = response.responseText;
                    var datajson = JSON.parse(text);
                    if (datajson.success == false) {
                        Ext.Msg.alert('错误提示', datajson.msg);
                    }
                    var path = datajson.msg;
                    Ext.Msg.show({
                        title: '下载提示',
                        msg: "EXCEL数据准备完毕，开始下载?",
                        buttons: Ext.Msg.YESNO,
                        icon: Ext.Msg.QUESTION,
                        fn: function (btn) {
                            if (btn == 'yes') {
                                window.location = common.CONTEXT_PATH + path;
                            }
                        }
                    });
                }
            });
        }

        function downloadData(tableID,partition){
            //不能用Ext.Ajax.request的获取后台response流，否则无反映
            window.open("/hivemeta/downloadHiveTableData?tableID="+tableID+"&partition="+partition,"_blank");
        }

        function getRelatedZeusDoc(tableID,field){
            Ext.Ajax.request({
                url:'/hivemeta/getRelatedWord',
                params:{
                    tableID:tableID,
                    field:field
                },
                success: function(response) {
                    var text = response.responseText;
//                    alert(text);
                    var dataArray = eval("("+text+")");
                    if(dataArray.length==0){
                        Ext.Msg.alert('提示', '开发中心无相关引用文档参考');
                        return;
                    }
                    Ext.create('Ext.window.Window', {
                        title: '开发中心引用文档参考',
                        height: 400,
                        width: 750,
                        layout: 'fit',
                        maximizable: true,//显示窗口最大化按钮
                        items: [
//                                    Ext.create('Ext.panel.Panel', {
//                                        title: 'Hello',
//                                        width: 200,
//                                        html: '<p>World!</p>',
//                                        renderTo: Ext.getBody()
//                                    }),
                            Ext.create('Ext.grid.Panel', {
                                title: '可能出现的依赖匹配结果参考表',
                                store: Ext.create('Ext.data.Store', {
                                    model: Ext.define('zesuFileModel', {
                                        extend: 'Ext.data.Model',
                                        fields: [ 'docID','owner','docName', 'docURI','docContent','isAyscSchedule','zeusDocHref']
                                    }),
//                                        data:[{docID:"35",owner:"mobile",docURI:"a/b/c.txt",docContent:"sdfsdfsdfsfsdf",isAyscSchedule:"是"}]
                                    data:dataArray
                                }),
                                columns: [
                                    { xtype:"rownumberer", text: "序号", width:30 },
                                    {text: '文档ID', dataIndex: 'docID',width:50 },
                                    {text: '文档名', dataIndex: 'docName',width:100 },
                                    {text: '所有者', dataIndex: 'owner',width:60},
                                    {text: '已调度', dataIndex: 'isAyscSchedule',width:50},
                                    {text: '文件路径', dataIndex: 'docURI',width:300},
                                    {text: '文本内容', dataIndex: 'docContent',hidden: true},
                                    {text: '操作', dataIndex: 'zeusDocHref',width:100},
                                ],
                                plugins: [{
                                    ptype: 'rowexpander',
                                    rowBodyTpl : new Ext.XTemplate(
                                            '<p><b>文本内容:<br/></b> {docContent}</p>',
                                            { })
                                }],
                                viewConfig:{
                                    stripeRows:true,//在表格中显示斑马线
                                    enableTextSelection:true //可以复制单元格文字
                                },
                            }),
                        ]
                    }).show();
                },
//                failure: function(response) {
//                    alert();
//                }
            });
        };

        function sql_search(codeText){
            var myMask = new Ext.LoadMask(Ext.get('sqlPanel'), {
                msg: '正在执行sql，请稍后...',
                removeMask: true     //完成后移除
            });
            myMask.show();
            var startTime=new Date();  //开始时间
            Ext.Ajax.request({
                url: '/hivemeta/queryBySQL',
                timeout: 3600000,//超时：1小时（默认60秒，超过后，即使后台返回了数据，前台还一直“在等待”无响应）
                params: {
                    codeText: codeText,
                },
                success: function (response) {
                    myMask.hide();
                    var text = response.responseText;
                    var datajson =JSON.parse(text);
                    if(datajson.success==false){
                        Ext.Msg.alert('错误提示',datajson.msg);
                        return;
                    }
                    var data;
                    try{
                        data = JSON.parse(datajson.msg);
                    }catch (e){
                        Ext.Msg.alert('提示', '数据中包含非法JSON字符,解析失败！');
                        return;
                    }
                    var hiveTableModelJson=data.hiveTableModelJson;
                    hiveTableModelArr=eval("("+"["+data.hiveTableModelJson+"]"+")");;
                    if(data.hiveTableTableContent.length==0){
                        Ext.Msg.alert('提示', '无数据');
                        return;
                    }
                    var endTime=new Date();  //开始时间
                    var spendTime = ((endTime.getTime()-startTime.getTime())/1000).toFixed(2);
                    Ext.create('Ext.window.Window', {
                        title: '查询结果  [耗时:'+ spendTime + '秒]',
                        height: 600,
                        width: 900,
                        layout: 'fit',
                        maximizable: true,//显示窗口最大化按钮
                        items: [
                            Ext.create('Ext.grid.Panel', {
                                store: Ext.create('Ext.data.Store', {
                                    model: Ext.define('zesuFileModel', {
                                        extend: 'Ext.data.Model',
                                        fields: hiveTableModelArr
                                    }),
                                    data:data.hiveTableTableContent
                                }),
                                columns: data.hiveTableColumns,
                                viewConfig:{
                                    stripeRows:true,//在表格中显示斑马线
                                    enableTextSelection:true //可以复制单元格文字
                                },
                                dockedItems: [
                                    {
                                        dock: 'top',
                                        xtype: 'toolbar',
                                        items: [
//                                            '->',
//                                            {
//                                                xtype: 'button',
//                                                text: '搜索',
//                                                tooltip: '点此进行搜索'
//                                            },
                                            {
                                                text:'<font color="blue"><b>EXCEL导出</b></font>',
                                                icon: 'static/images/extIcons/icon/page_excel.png',  // Use a URL in the icon config
                                                tooltip: '以EXCEL格式导出本次查询结果数据',
                                                handler: function() {
                                                    exportDataForSearch(codeText);
                                                }
                                            }

                                        ]
                                    }
                                ]
//                                dockedItems:[
//                                    //添加搜索控件
//                                    {
//                                        dock: 'top',
//                                        xtype: 'toolbar',
//                                        items: {
//                                            width: 200,
//                                            fieldLabel: '搜索姓名',
//                                            labelWidth: 100,
//                                            xtype: 'searchfield',
//////                                            store: store
//                                        }
//                                    }
//                                ],
                            }),
                        ],
                    }).show();
                }
            });
        };

        function sql_update(codeText) {
            var myMask = new Ext.LoadMask(Ext.get('sqlPanel'), {
                msg: '正在执行sql，请稍后...',
                removeMask: true     //完成后移除
            });
            myMask.show();
            Ext.Ajax.request({
                url: '/hivemeta/updateBySQL',
                timeout: 3600000,//超时：1小时（默认60秒，超过后，即使后台返回了数据，前台还一直“在等待”无响应）
                params: {
                    codeText: codeText,
                },
                success: function (response) {
                    myMask.hide();
                    var obj = Ext.decode(response.responseText);
                    if (obj.success == false) {
                        Ext.Msg.alert('错误提示', obj.msg);
                    }else{
                        Ext.Msg.alert('提示', obj.msg);
                    }
                }
            });
        };

        Ext.onReady(function () {
            var codeMirrorEditor;//代码编辑器对象（定义在下面），在此进行变量声明

            Ext.QuickTips.init();
//            Ext.create('Ext.tip.ToolTip', {
//                target: 'tableFieldInfoShow',
//                html: '最简单的提示'
//            });

            Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
            var viewport = Ext.create('Ext.Viewport', {
                id: 'border-example',
                layout: 'border',
                items: [
                    // create instance immediately
//                    Ext.create('Ext.Component', {
//                        region: 'north',
//                        height: 40, // give north and south regions a height
//                        autoEl: {
//                            tag: 'div',
//                            html: '<h3>益盟软件数据仓库元数据管理平台</h3>'
//                        }
//                    }),
                    {
//                        // lazily created panel (xtype:'panel' is default)
                        region: 'south',
                        contentEl: 'south',
//                        split: true,
                        height: 25,
//                        minSize: 100,
//                        maxSize: 200,
//                        collapsible: true,
                        collapsed: false,
//                        title: 'South',
                        margins: '0 0 0 0'
                    },
                    {//右侧搜索布局
                        xtype: 'panel',
                        region: 'east',
                        title: '数据检索',
                        layout: 'accordion',
                        collapsed: true ,
                        collapsible: true,
//                        animCollapse: true,
                        split: true,
                        width: 350, // give east and west regions a width
                        minSize: 175,
                        maxSize: 400,
                        margins: '0 5 0 0',
//                        tabPosition: 'bottom',
                        items: [
                            {
                                title:'<font style="font-weight:bold;color:black;">元数据检索</font>',
                                xtype:'panel',
                                items:[
                                    {
                                        xtype: 'form',
                                        layout: 'hbox',
//                                        title: '数据搜索',
                                        url: common.CONTEXT_PATH + '/search',
//                                dockedItems: [{
//                                    dock: 'top',
//                                    xtype: 'toolbar',
//                                    items: ['->', {
//                                        xtype: 'button',
//                                        text: '搜索',
//                                        tooltip: '点此进行搜索'
//                                    }]
//                                }],
                                        items: [
                                            {
                                                xtype: "combobox",
                                                name: "searchDataType",
//                                fieldLabel: "搜索类型",
                                                store: Ext.create("Ext.data.Store", {
                                                    fields: ["Name", "Value"],
                                                    data: [
                                                        { Name: "全部", Value: 0 },
                                                        { Name: "表", Value: 2 },
                                                        { Name: "数据库", Value: 1 },
                                                        { Name: "字段", Value: 3 },
                                                    ]
                                                }),
                                                width:120,
                                                editable: false,
                                                displayField: "Name",
                                                valueField: "Value",
                                                emptyText: "选择搜索类型",
                                                queryMode: "local",
                                                allowBlank: false,
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'searchKeys',
                                                width:170,
                                                emptyText: '请输入数据库名/表名/字段名',
                                                allowBlank: false,  // requires a non-empty value
//                                fieldLabel: 'Email Address',
//                                vtype: 'email'  // requires value to be a valid email address format
                                                listeners : {//输入框回车事件
                                                    specialkey : function(field, e) {
                                                        if (e.getKey() == Ext.EventObject.ENTER) {
                                                            Ext.getCmp('searchBtn').getEl().dom.click();//模拟按钮点击事件
                                                        }
                                                    },
                                                },
                                            },
//                            Ext.create('Ext.Button', {
//                                text: '搜索',
//                                tooltip: '点此进行搜索',
//                                handler: function() {
//                                    alert('You clicked the button!');
//                                }
//                            }),
//                            Ext.create('Ext.Button', {
//                                text: '搜索2',
//                                tooltip: '点此进行搜索',
//                                handler: function() {
//                                    alert('You clicked the button!');
//                                }
//                            }),
                                            {
                                                id:'searchBtn',
                                                xtype: 'button',
                                                text: '<font color="blue"><b>搜索</b></font>',
                                                icon: 'static/images/extIcons/icons/zoom.png',
//                                        iconCls : 'icon-search',
//                                        glyph : 0xf0c9,
                                                width:60,
                                                formBind: true, //only enabled once the form is valid
                                                disabled: true,
                                                handler: function () {
                                                    var form = this.up('form').getForm();
                                                    if (form.isValid()) {// 验证合法后使用
                                                        form.submit({
                                                            method: 'POST',
                                                            success: function (form, action) {
                                                                //将后台传来的数组型的字符串转换为js中的数组对象(store中的data属性的数据为数组类型)
                                                                var dataArray = eval("("+action.result.msg+")");
                                                                if(dataArray.length==0){
                                                                    Ext.Msg.alert('提示', '无结果，请换个条件试试');
                                                                    return;
                                                                }
                                                                Ext.getCmp("result_id").removeAll();
                                                                Ext.getCmp("result_id").add(new Ext.grid.GridPanel({
                                                                            title:"搜索结果：",
                                                                            height:600,
                                                                            store: Ext.create('Ext.data.Store', {
                                                                                model: Ext.define('searchResult', {
                                                                                    extend: 'Ext.data.Model',
                                                                                    fields: [ 'dataID','dataType','name', 'ownTable', 'ownDatabase' ]
                                                                                }),
//                                                        data: [
//                                                            { name: 'Bart', email: 'lisa@simpsons.com', phone: '555-111-1224' },
//                                                            { name: 'Bart', email: 'bart@simpsons.com', phone: '555-222-1234' },
//                                                            { name: 'Homer', email: 'home@simpsons.com', phone: '555-222-1244' },
//                                                            { name: 'Marge', email: 'marge@simpsons.com', phone: '555-222-1254' }
//                                                        ],
                                                                                data:dataArray,
//                                                        autoLoad: true,
                                                                                groupField: 'dataType',
//                                                        sortInfo: {field: 'dataType',direction:'ASC'}
                                                                            }),
//                                                    title: 'Application Users',
                                                                            columns: [
                                                                                {
                                                                                    text: '数据ID',
                                                                                    sortable: false,
                                                                                    hidden: true,
                                                                                    dataIndex: 'dataID'
                                                                                },
                                                                                {
                                                                                    text: '类型',//数据库、表、字段
                                                                                    sortable: false,
                                                                                    hidden: true,
                                                                                    dataIndex: 'dataType'
                                                                                },
                                                                                {
                                                                                    text: '匹配数据',
                                                                                    width: 150,
                                                                                    sortable: false,
                                                                                    hideable: false,
                                                                                    dataIndex: 'name'
                                                                                },
                                                                                {
                                                                                    text: '所属表',
                                                                                    width: 150,
                                                                                    dataIndex: 'ownTable',
                                                                                },
                                                                                {
                                                                                    text: '所属库',
                                                                                    flex: 1,
                                                                                    dataIndex: 'ownDatabase'
                                                                                }
                                                                            ],
                                                                            features: [{ ftype: 'grouping' }],//启用分组
                                                                            listeners:{
                                                                                itemclick : function(){},//单击行事件
                                                                                itemdblclick : function(){//双击行事件
                                                                                    var record = this.getSelectionModel().getLastSelected();
                                                                                    var dataType=record.get("dataType");
                                                                                    var name=record.get("name");
                                                                                    var dataID=record.get("dataID");
                                                                                    if(dataType=="字段"){
                                                                                        name = record.get("ownTable");
                                                                                    }
                                                                                    addTab(dataID,name);
                                                                                }
                                                                            },
                                                                        })
                                                                );
                                                            },
                                                            // 提交失败的回调函数
                                                            failure: function (form, action) {
                                                                Ext.Msg.alert('Failed', action.result.msg);
                                                            }
                                                        });
                                                    }
                                                },
                                            },
                                        ]
                                    },
                                    {
                                        id: 'result_id',
                                        xtype:'panel',
//                                title:'搜索结果：',
                                    },
                                ]
                            },
                            {
                              xtype:'panel',
                              title: '<font style="font-weight:bold;color:black;">执行SQL</font>',
//                                width: 200,
//                                height: 500,
//                                html: '<p>World!</p>',
                                items:[
//                                    {
//                                        contentEl: 'sqlSearch',
//                                        closable: false,//tab是否可关闭，默认为false
//                                        autoScroll: true
//                                    }
                                    {
                                        id:'sqlPanel',
                                        xtype:'panel',
//                                        title: 'Sample TextArea',
//                                        width: 400,
//                                        bodyPadding:10,
                                        layout: 'vbox',
                                        items: [
                                            {
                                                xtype: 'textareafield',
                                                id:'sqlEditor',
                                                height:300,
                                                width:500,
                                                grow: true,
//                                                flex:'3',
//                                                name: 'message',
//                                                fieldLabel:'Message',
//                                                anchor: '100%',
//                                                style:'font-size:15px;'
                                            },
//                                            {
//                                                xtype: 'button',
//                                                text: '<font color="blue"><b>运行</b></font>',
//                                                icon:'static/images/extIcons/action_go.gif',
////                                                tyle:'margin-left:50;margin-top:50px;',
//                                                handler: function () {},
//                                            },
                                        ],
                                        dockedItems: [
                                            {
                                                dock: 'top',
                                                xtype: 'toolbar',
                                                items: [
            //                                    '->',
                                                    {
                                                        id:'sqlType',
                                                        xtype: "combobox",
                                                        name: "sqlType",
                                                        store: Ext.create("Ext.data.Store", {
                                                            fields: ["Name", "Value"],
                                                            data: [
                                                                { Name: "查询", Value: 1 },
                                                                { Name: "更新", Value: 2 },
                                                            ]
                                                        }),
                                                        width:100,
                                                        editable: false,
                                                        displayField: "Name",
                                                        valueField: "Value",
                                                        emptyText: "选择SQL类型",
                                                        queryMode: "local",
                                                        allowBlank: false,
                                                    },
                                                    {
                                                        xtype: 'button',
                                                        text: '<font color="blue"><b>运行</b></font>',
                                                        icon:'static/images/extIcons/icons/icon_run.png',
                                                        tooltip: '执行SQL',
                                                        handler: function () {
                                                            var codeText = codeMirrorEditor.getValue();
                                                            if(codeText==""){
                                                                Ext.Msg.alert('提示',"请输入SQL");
                                                                return;
                                                            }
                                                            if(Ext.getCmp('sqlType').value==1){//sql类型为查询
                                                                sql_search(codeText);
                                                            }else if(Ext.getCmp('sqlType').value==2){//sql类型为更新
                                                                sql_update(codeText);
                                                            }else{
                                                                Ext.Msg.alert('提示',"请选择一种SQL类型");
                                                            }
                                                        },
                                                    },
                                                    {
                                                        xtype: 'button',
                                                        text: '<font color="blue"><b>运行选中部分SQL</b></font>',
                                                        icon:'static/images/extIcons/icons/icon_run.png',
                                                        tooltip: '执行SQL',
                                                        handler: function () {
                                                            var codeText = codeMirrorEditor.getSelection();//获取鼠标选中区域的代码
                                                            if(codeText==""){
                                                                Ext.Msg.alert('提示',"请选择要运行的SQL代码");
                                                                return;
                                                            }
                                                            if(Ext.getCmp('sqlType').value==1){//sql类型为查询
                                                                sql_search(codeText);
                                                            }else if(Ext.getCmp('sqlType').value==2){//sql类型为更新
                                                                sql_update(codeText);
                                                            }else{
                                                                Ext.Msg.alert('提示',"请选择一种SQL类型");
                                                            }
                                                        },
                                                    },
                                                ]
                                            }
                                         ],
                                    }
                                ]
                            },
                        ]
                    },
                    {
                        region: 'west',
                        stateId: 'navigation-panel',
                        id: 'west-panel', // see Ext.getCmp() below
//                        title: '数据库树形浏览',
                        split: true,
                        width: 330,
                        minWidth: 175,
                        maxWidth: 400,
                        collapsible: false,
                        animCollapse: true,
                        margins: '0 0 0 5',
                        layout: 'accordion',
                        items: [
                            databaseTree
                        ]
                    },
                    tabs
                   ]
            });
            // get a reference to the HTML element with id "hideit" and add a click listener to it
            Ext.get("hideit").on('click', function () {
                // get a reference to the Panel that was created with id = 'west-panel'
                var w = Ext.getCmp('west-panel');
                // expand or collapse that Panel based on its collapsed property state
                w.collapsed ? w.expand() : w.collapse();
            });
            //设置sql编辑器
            var myTextarea = document.getElementById('sqlEditor');
            codeMirrorEditor = CodeMirror.fromTextArea(myTextarea, {
                mode: "text/x-mysql",
                lineNumbers: true,
            });
//            var myTextarea02 = document.getElementById('sqlEditor02');
//            codeMirrorEditor02 = CodeMirror.fromTextArea(myTextarea02, {
//                mode: "text/x-mysql",
//                lineNumbers: true,
//            });
        });
    </script>
</head>

<body>
<!-- use class="x-hide-display" to prevent a brief flicker of the content -->
<div id="west" class="x-hide-display">
    <p>Hi. I'm the west panel.</p>
</div>
<div id="center2" class="x-hide-display">
    <a id="hideit" href="#">Toggle the west region</a>
    <p>My closable attribute is set to false so you can't close me. The other center panels can be closed.</p>
</div>
<div id="center1" class="x-hide-display">

    <%--<div class="jumbotron">--%>
        <%--<h1>Hello, world!</h1>--%>
        <%--<p>...</p>--%>
        <%--<p><a class="btn btn-primary btn-lg" href="#" role="button">Learn more</a></p>--%>
    <%--</div>--%>

    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">欢迎使用数据仓库元数据管理平台</h3>
        </div>
        <div class="panel-body">
            <ul class="list-group">
                <li class="list-group-item"><span>当前登录用户：<font color="blue"><%=((TbUser)request.getSession().getAttribute("user")).getUserName()%></font></span></li>
                <%--<li class="list-group-item"><span>用户角色：<font color="blue">管理员</font></span></li>--%>
                <%--<li class="list-group-item">--%>
                    <%--<u><span><a href="<%=PropertiesUtil.getProperties(PropertiesUtil.class,"common.properties").getProperty("zeus.url")%>" target="_blank"><font color="green">点此跳转到大数据调度中心</font></a></span></u>--%>
                <%--</li>--%>
            </ul>

        </div>
    </div>

</div>
<div id="props-panel" class="x-hide-display" style="width:200px;height:200px;overflow:hidden;"></div>
<div id="south" class="x-hide-display">
    <p align="center"><font color="gray">数据仓库元数据管理平台 V1.0</font></p>
    <%--<input id="sqlSearch02" class="x-hide-display">--%>
        <%--<textarea id="sqlEditor02" name="sqlEditor02" ></textarea>--%>
        <%--&lt;%&ndash;<button type="button">Click Me!</button>&ndash;%&gt;--%>
    <%--</div>--%>
</div>

</body>
</html>