# 框架内部BootstrapRunner相关执行器说明



| 类名                                   | 级别   | 优先级 | 所在模块 | 简要说明                                                     |
| -------------------------------------- | ------ | ------ | -------- | ------------------------------------------------------------ |
| SystemPropertyBootstrapRunner          | KERNEL | 100    | cap-boot | 注册OS环境变量和JVM环境变量到PropertyOfficer                 |
| CommandArgumentsBootstrapRunner        | KERNEL | 110    | cap-boot | 注册命令行参数到PropertyOfficer                              |
| StaticResourcePropertyBootstrapRunner  | KERNEL | 120    | cap-boot | 加载用户指定资源路径下所有的application配置文件并将其中信息注册到PropertyOfficer中 |
| ActiveProfilePropertyBootstrapRunner   | KERNEL | 130    | cap-boot | 根据PropertyOfficer中"application.profiles.active"的属性值加载用户指定资源路径下对应的配置文件 |
| ClassBeanRegisterBootstrapRunner       | KERNEL | 300    | cap-boot | 扫描用户指定软件包下所有被@Capper注解标注的类，并根据注解中的相关元信息注册相应的BeanDefinition到BeanContainer中 |
| FactoryBeanRegisterBootstrapRunner     | KERNEL | 310    | cap-boot | 扫描用户指定软件包下所有被@Capper注解标注的方法，并根据注解中的相关元信息注册相应的BeanDefinition到BeanContainer中 （注：无论@Capper方法所在的类是否标注有@Capper注解，均会被视为Bean进行管理；若@Capper方法返回的类同样标注有） |
| LifecycleMethodRegisterBootstrapRunner | KERNEL | 320    | cap-boot | 为所有已注册的BeanDefinition补全生命周期（初始化/销毁）方法信息 |
| BeanInjectionBootstrapRunner           | KERNEL | 350    | cap-boot | 添加有关@Inject注解的拦截器——实现所有Bean类中标记有@Inject注解的字段的依赖注入功能 |
| PropertyValueBootstrapRunner           | KERNEL | 360    | cap-boot | 添加有关@Value注解的拦截器——实现所有Bean类中标记有@Value注解的字段的一次性属性注入功能 |
| PropertySubscriptionBootstrapRunner    | KERNEL | 370    | cap-boot | 添加有关@Subscription、@Subscriber注解的拦截器——实现所有标注@Subscription的Bean类中标记有@Subscriber字段的属性监听（可根据回调函数动态监听属性的变化信息）功能 |
| AopBootstrapRunner                     | KERNEL | 400    | cap-aop  | 添加有关@Aspect注解的拦截器——基于Jdk Proxy与Cglib Proxy实现@Around、@Before、@After三种动态代理模式 |
|                                        |        |        |          |                                                              |
| PreInstantiateSingletonBootstrapRunner | KERNEL | 499    | cap-boot | 预实例化所有非懒加载的单例Bean                               |
|                                        |        |        |          |                                                              |