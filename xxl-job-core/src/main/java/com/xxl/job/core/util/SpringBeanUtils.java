package com.xxl.job.core.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 该方法必须服务启动之后才能使用。
 *
 * spring工具类
 */
@Component
public class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private SpringBeanUtils() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtils.applicationContext = applicationContext;
    }


    public static <T> T createBean(Class<T> clazz) {
        return applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
    }

    /**
     * 获取上下文容器
     */
    public static ApplicationContext getApplicationContext() {
        return SpringBeanUtils.applicationContext;
    }

    /**
     * 获取容器内对象Set列表
     */
    public static <T> Set<T> getBeans(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz).values().stream().collect(Collectors.toSet());
    }

    /**
     * 通过name获取 Bean
     *
     * @param <T> Bean类型
     * @param name Bean名称
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 通过name获取 Bean
     *
     * @param <T> Bean类型
     * @param name Bean名称
     * @param clazz Bean类
     * @return Bean
     */
    public static <T> T getBean(String name, Class<T> clazz)  {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T> Bean类型
     * @param clazz Bean类
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取当前的环境配置，无配置返回null
     */
    public static String[] getActiveProfiles() {
        Environment env = applicationContext.getEnvironment();
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = env.getDefaultProfiles();
        }
        return activeProfiles;
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     */
    public static String getActiveProfile() {
        String[] activeProfiles = getActiveProfiles();
        if (activeProfiles.length == 0) {
            return null;
        }
        return activeProfiles[0];
    }

}
