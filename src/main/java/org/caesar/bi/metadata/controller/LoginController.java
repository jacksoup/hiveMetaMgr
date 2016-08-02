package org.caesar.bi.metadata.controller;

import org.caesar.bi.metadata.common.Response;
import org.caesar.bi.metadata.dao.CommonDML;
import org.caesar.bi.metadata.entity.metaMang.TbUser;
import org.caesar.utils.encryption.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by caesar on 2016/4/26.
 */
@Controller
public class LoginController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * 显示主页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() throws Exception {
        return "/index";
    }

    /**
     * 显示登录页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() throws Exception {
        return "/login";
    }

    /**
     * 登录验证操作
     * @param session
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public
    @ResponseBody
    Response login(HttpSession session, String userName, String password) throws Exception {
        TbUser user = CommonDML.login(userName,new MD5().getMD5ofStr(password));
        if (user != null) {
            logger.info(String.format("用户[ %s ]登录系统！", user.getUserName()));
            session.setAttribute("user", user);
            return new Response();
        } else {
            return new Response("用户名或密码错误！");
        }
    }

    public static void main(String[] args) {

    }

}
