package org.moper.cap.example.boot;

import org.moper.cap.context.annotation.Component;
import org.moper.cap.boot.annotation.Autowired;
import org.moper.cap.example.bean.ServiceDemo;

@Component
public class AutowiredBeanDemo {
    @Autowired
    public ServiceDemo serviceDemo;

    public void showGreeting() {
        System.out.println(serviceDemo.greet("cap-example"));
    }
}