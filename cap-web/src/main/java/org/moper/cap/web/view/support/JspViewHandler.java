package org.moper.cap.web.view.support;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.view.ModelAndView;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * JSP 模板视图处理器
 * 
 * 职责：
 * 1. 检查返回值是否为 ModelAndView
 * 2. 将模型数据放入 request attribute
 * 3. 转发请求到 JSP 文件进行渲染
 * 
 * 用途：
 * - 支持传统的 MVC 模式
 * - 渲染 JSP 模板
 * - 支持动态页面生成
 * 
 * 优先级：350
 * 
 * 示例：
 * @GetMapping("/users/{id}")
 * public ModelAndView getUser(@PathVariable Long id) {
 *     User user = userService.findById(id);
 *     ModelAndView mav = new ModelAndView("user/detail");
 *     mav.addAttribute("user", user);
 *     mav.addAttribute("title", "用户详情");
 *     return mav;
 * }
 */
@Slf4j
@Priority(350)
public class JspViewHandler implements ViewHandler {

    private static final String JSP_VIEW_PREFIX = "/WEB-INF/views/";
    private static final String JSP_VIEW_SUFFIX = ".jsp";

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        return returnType != null && ModelAndView.class.isAssignableFrom(returnType);
    }

    @Override
    public void handle(Object returnValue,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ModelAndView mav = (ModelAndView) returnValue;
        String viewName = mav.getViewName();

        // 验证视图名称
        if (viewName == null || viewName.isBlank()) {
            throw new IllegalArgumentException("View name cannot be null or blank");
        }

        // 防止路径遍历攻击
        if (viewName.contains("..")) {
            throw new IllegalArgumentException("View name contains invalid path traversal sequence: " + viewName);
        }

        // 将模型数据放入 request attribute，供 JSP 使用
        for (Map.Entry<String, Object> entry : mav.getModel().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
            log.debug("设置 request attribute: {} = {}", entry.getKey(), entry.getValue());
        }

        // 构建 JSP 文件路径
        String jspPath = buildJspPath(viewName);
        log.debug("转发请求到 JSP: {}", jspPath);

        // 使用 RequestDispatcher 转发请求到 JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        if (dispatcher == null) {
            throw new IllegalStateException("JSP file not found: " + jspPath);
        }

        dispatcher.forward(request, response);
    }

    /**
     * 构建 JSP 文件的完整路径
     * 
     * 规则：
     * 1. 如果 viewName 以 / 开头，直接使用
     * 2. 否则，添加 /WEB-INF/views/ 前缀和 .jsp 后缀
     * 
     * 示例：
     * "user/detail" → "/WEB-INF/views/user/detail.jsp"
     * "/custom/page" → "/custom/page.jsp"
     */
    private String buildJspPath(String viewName) {
        if (viewName.startsWith("/")) {
            // 视图名称已包含完整路径
            if (!viewName.endsWith(JSP_VIEW_SUFFIX)) {
                return viewName + JSP_VIEW_SUFFIX;
            }
            return viewName;
        } else {
            // 添加默认的前缀和后缀
            return JSP_VIEW_PREFIX + viewName + JSP_VIEW_SUFFIX;
        }
    }
}
