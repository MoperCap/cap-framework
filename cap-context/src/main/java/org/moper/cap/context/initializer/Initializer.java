package org.moper.cap.context.initializer;

import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.context.exception.InitializerException;
import org.moper.cap.context.runner.RunnerType;

/**
 * 框架启动阶段构造机
 */
public abstract class Initializer implements AutoCloseable, Comparable<Initializer> {

    private final RunnerType type;

    private final int order;

    private final String name;

    private final String description;

    /**
     * 构造机构造函数，默认构造机类型为EXTENSION
     *
     * @param order 同级别构造机优先级，数值越小优先执行
     */
    public Initializer(int order){
        this(RunnerType.EXTENSION, order, "", "");
    }

    /**
     * 构造机构造函数
     *
     * @param type 构造机类型，决定了构造机的执行顺序，框架最小运行内核构造机优先执行，第三方扩展能力构造机最后执行
     * @param order 同级别构造机优先级，数值越小优先执行
     */
    public Initializer(RunnerType type, int order) {
        this(type, order, "", "");
    }

    /**
     * 构造机构造函数
     *
     * @param type 构造机类型，决定了构造机的执行顺序，框架最小运行内核构造机优先执行，第三方扩展能力构造机最后执行
     * @param order 同级别构造机优先级，数值越小优先执行
     * @param name 构造机名(不要求唯一, 可以为""，但不可以为null)
     * @param description 构造机相关描述(可以为""，但不可以为null)
     */
    public Initializer(RunnerType type, int order, String name, String description) {
        this.type = type;
        this.order = order;
        this.name = name;
        this.description = description;
    }

    /**
     * 构造机类型，决定了构造机的执行顺序，框架最小运行内核构造机优先执行，第三方扩展能力构造机最后执行
     *
     * @return 构造机类型
     */
    public RunnerType type(){
        return type;
    }

    /**
     * 同级别构造机优先级，数值越小优先执行
     *
     * @return 同级别构造机优先级
     */
    public int order(){
        return order;
    }

    /**
     * 构造机名(不要求唯一, 可以为""，但不可以为null)
     */
    public String name() {
        return name;
    }

    /**
     * 构造机相关描述(可以为""，但不可以为null)
     */
    public  String description() {
        return description;
    }

    /**
     * 框架启动阶段执行
     *
     * @param context 初始化上下文
     */
    public abstract void initialize(BootstrapContext context) throws Exception;

    @Override
    public void close() throws InitializerException {
        // do nothing
    }

    /**
     * 构造机比较方法，先比较构造机类型，类型优先级高的构造机优先执行；
     * 若构造机类型相同，则比较同级别构造机优先级，数值越小优先执行
     */
    @Override
   public int compareTo( Initializer o){
        if(type() != o.type()) {
            return Integer.compare(type().priority(), o.type().priority());
        }else {
            return Integer.compare(order(), o.order());
        }
    }
}
