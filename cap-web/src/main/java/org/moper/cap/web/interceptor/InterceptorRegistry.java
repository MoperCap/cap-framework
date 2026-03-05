package org.moper.cap.web.interceptor;

import java.util.List;

public interface InterceptorRegistry {

    List<RequestInterceptor> getInterceptors();
}
