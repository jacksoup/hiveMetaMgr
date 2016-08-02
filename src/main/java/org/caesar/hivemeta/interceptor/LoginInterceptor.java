package org.caesar.hivemeta.interceptor;

import org.caesar.hivemeta.entity.metaMang.TbUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author caesar
 * @since 2014-08 
 * @describe 对请求进行拦截，需要登录才能访问相关url，如果为登录或者登录超时则导向登录界面，当登录正确时会回到所请求的url
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
	private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
	/**
	 * @return 返回true，这样才会向下继续调用其他的拦截器或者调用业务控制器(Controller)去处理该请求；返回false，这样该请求就不会被处理。
	 * */
	@Override
	// 方法在业务处理器（Controller）处理请求之前被调用
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession();
		TbUser user = (TbUser) session.getAttribute("user");
		if (user == null) {// 未登录或者session超时
			String perURL = request.getContextPath() + request.getServletPath().toString();//记录当前非方访问的URL
			session.setAttribute("perURL", perURL);//将当前访问的URL放入session中
			System.err.println(perURL);
			logger.warn("未登录用户非法访问！");
			response.sendRedirect(request.getContextPath() + "/login");// 重定向到登录页面(getContextPath()方法获得项目根目录<goodsbank>)
			// request.getRequestDispatcher("/showLogin").forward(request,response);//转发到登录页面
			return false;// 如果不return 会发生java.lang.IllegalStateException:ommitted异常
		}else{
			if(session.getAttribute("perURL") != null){
				response.sendRedirect(session.getAttribute("perURL").toString());//跳转到上次非法访问的URL(重定向到当前页)
				session.setAttribute("perURL",null);
				return false;
			}
		}
		return true;
	}
}
