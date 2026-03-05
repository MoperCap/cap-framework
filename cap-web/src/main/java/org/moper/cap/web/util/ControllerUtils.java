package org.moper.cap.web.util;

import org.moper.cap.web.annotation.controller.Controller;
import org.moper.cap.web.annotation.controller.RestController;

public final class ControllerUtils {
    /**
     * 检查类是否为控制器
     */
    public static boolean isController(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isAnnotationPresent(Controller.class) ||
                clazz.isAnnotationPresent(RestController.class);
    }
}
