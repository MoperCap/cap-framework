package org.moper.cap.example.bean;

import org.moper.cap.context.annotation.Component;

@Component
public class ServiceDemo {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}