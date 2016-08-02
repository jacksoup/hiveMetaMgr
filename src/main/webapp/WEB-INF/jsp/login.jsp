<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/common/tag.jsp" %>
<html>
<head>
    <%@include file="/common/header.jsp" %>
    <style>
        body {
            background: #fff url('static/images/1.jpg') 50% 0 no-repeat;
        }
        #div1 {
            position: absolute;
            width: 400px;
            height: 200px;
            left: 50%;
            top: 50%;
            margin-left: -200px;
            margin-top: -100px;
        }
    </style>
    <script type="text/javascript">
        Ext.onReady(function () {
            //导入相关js库
            Ext.require([
                'Ext.form.Panel',
                'Ext.layout.container.Anchor'
            ]);
            var filterPanel = Ext.create('Ext.form.Panel', {
                renderTo: Ext.get("div1"),//渲染到哪个对象上，这里是id为"div1"的对象上
                title: '用户登录',
                url: common.CONTEXT_PATH + '/login',
                defaults: {
                    anchor: '100%'
                },
                width: 320,
                bodyPadding: 10,
                defaultType: 'textfield',//容器或组件的默认类型
                items: [
                    {
                        allowBlank: false,
                        fieldLabel: '用户名',
                        name: 'userName',
                        emptyText: '请输入用户名',
                        allowBlank: false
                    },
                    {
                        allowBlank: false,
                        fieldLabel: '密 码',
                        name: 'password',
                        emptyText: '请输入密码',
                        inputType: 'password',
                        allowBlank: false
                    },
                ],
                buttons: [
                    {
                        text: '重置',
                        handler: function () {
                            this.up('form').getForm().reset();
                        }
                    },
                    {
                    text: '登录',
                    formBind: true, //only enabled once the form is valid
                    disabled: true,
                    handler: function () {
                        var form = this.up('form').getForm();
                        if (form.isValid()) {// 验证合法后使用
                            form.submit({
                                method: 'POST',
                                success: function (form, action) {
                                    window.location=common.CONTEXT_PATH +"/index";
                                },
                                // 提交失败的回调函数
                                failure: function (form, action) {
                                    Ext.Msg.show({
                                        title:'登录提示',
                                        msg: action.result.msg,
                                        buttons: Ext.Msg.YES,
                                        icon: Ext.Msg.ERROR
//                                        icon: Ext.Msg.WARNING
                                    });
//                                    Ext.Msg.alert('Failed', action.result.msg);
                                }
                            });
                        }
                    }
                }]
            });
        });
    </script>
</head>
<body>
<div>
</div>
    <div id="div1" align="center"><h1>数据仓库元数据管理平台</h1></div>
</body>
</html>
