package org.myspringframework.core;

/**
 * ClassName: ApplicationContext
 * PackageName: org.myspringframework.core
 * Description:
 *              MySpringFrameWork框架应用上下文接口
 *
 * @Author CuiBo
 * @Create 2023/8/14 17:56
 * @Version 1.0
 */
public interface ApplicationContext {
    Object getBean(String beanName);
}
