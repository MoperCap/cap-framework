package org.moper.cap.example.context;

import org.moper.cap.context.annotation.ComponentScan;
import org.moper.cap.context.annotation.Subscription;

@ComponentScan("org.moper.cap.example")
@Subscription("example-sub")
public class ExampleConfig {}