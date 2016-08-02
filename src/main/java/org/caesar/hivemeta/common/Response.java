package org.caesar.hivemeta.common;
/**
 * @author: jack zhu
 * @since: 2014-08 
 * @describe:  
 * 如果success true，data返回的是实际的数据
 * 如果false，则data返回的是错误消息
 */
public class Response {
    private boolean success;
    private Object msg;

    public Response(){
        this.success = true;
    }

    public Response(Object msg){
        this.success = false;
        this.msg = msg;
    }

    public Response(boolean success, Object msg){
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getMsg() {
        return msg;
    }
}
