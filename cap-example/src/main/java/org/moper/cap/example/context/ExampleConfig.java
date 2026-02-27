package org.moper.cap.example.context;

import org.moper.cap.context.annotation.ComponentScan;
import org.moper.cap.core.annotation.Component;

@ComponentScan("org.moper.cap.example")
@Component.Subscription("example-sub")
public class ExampleConfig {}