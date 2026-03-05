package org.moper.cap.web.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 模型和视图包装类
 * 
 * 用于封装：
 * - 视图名称（JSP 文件路径）
 * - 模型数据（传递给视图的数据）
 * 
 * 示例：
 * ModelAndView mav = new ModelAndView("user/detail");
 * mav.addAttribute("user", user);
 * mav.addAttribute("title", "用户详情");
 * return mav;
 */
public class ModelAndView {

    private final String viewName;
    private final Map<String, Object> model;

    public ModelAndView(String viewName) {
        this.viewName = viewName;
        this.model = new HashMap<>();
    }

    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model != null ? new HashMap<>(model) : new HashMap<>();
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return Collections.unmodifiableMap(model);
    }

    public ModelAndView addAttribute(String name, Object value) {
        this.model.put(name, value);
        return this;
    }

    public Object getAttribute(String name) {
        return this.model.get(name);
    }

    public boolean hasAttribute(String name) {
        return this.model.containsKey(name);
    }
}
