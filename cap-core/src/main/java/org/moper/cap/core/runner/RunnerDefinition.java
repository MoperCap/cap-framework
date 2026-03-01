package org.moper.cap.core.runner;


import org.moper.cap.core.annotation.RunnerMeta;

/**
 * 框架执行器定义字段 </br>
 *
 * @param priority 执行器顺序，数值越小优先级越高
 * @param clazz 执行器类，不能为null，且必须被@RunnerMeta注解标注
 * @param runner 执行器实例，不能为null
 * @param type 执行器类型，不能为null
 * @param name 执行器名称，默认为执行器类名
 * @param description 执行器描述
 */
public record RunnerDefinition<T extends Runner>(
        int priority,
        Class<? extends T> clazz,
        T runner,
        RunnerType type,
        String name,
        String description
) implements Comparable<RunnerDefinition<T>> {

    public RunnerDefinition{
        if(type == null) {
            throw new IllegalArgumentException("RunnerType must not be null");
        }

        if(clazz == null) {
            throw new IllegalArgumentException("BootstrapRunner class must not be null");
        }else if(clazz.getAnnotation(RunnerMeta.class) == null){
            throw new IllegalArgumentException("BootstrapRunner class must be annotated with @RunnerMeta");
        }

        if(runner == null) {
            throw new IllegalArgumentException("BootstrapRunner must not be null");
        }

        if(name == null || name.isBlank()) {
            name = clazz.getSimpleName();
        }
    }


    @Override
    public int compareTo(RunnerDefinition<T> o) {
        return Integer.compare(this.priority, o.priority);
    }

    @Override
    public String toString() {
        return "RunnerDefinition{" +
                "priority=" + priority +
                ", clazz=" + clazz.getName() +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
