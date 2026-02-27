package org.moper.cap.example.boot;

import org.moper.cap.core.annotation.Component;
import org.moper.cap.bean.annotation.Autowired;
import org.moper.cap.example.bean.ServiceDemo;

@Component
public class AutowiredBeanDemo {
    @Autowired
    public ServiceDemo serviceDemo;

    public void showGreeting() {
        System.out.println(serviceDemo.greet("cap-example"));
    }
}