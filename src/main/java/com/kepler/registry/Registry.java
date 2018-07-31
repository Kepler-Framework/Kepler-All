package com.kepler.registry;

import com.kepler.service.Service;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 注册中心客户端
 * @author longyaokun
 */
public interface Registry {


    /**
     * 服务注册
     * @param service
     * @param instance
     * @throws Exception
     */
    void registration(Service service, Object instance) throws Exception;

    /**
     * 服务注销
     * @param service
     * @throws Exception
     */
    void unRegistration(Service service) throws Exception;

    /**
     * 服务发现
     * @param service
     * @throws Exception
     */
    void discovery(Service service) throws Exception;

    /**
     * 取消服务发现
     * @param service
     * @throws Exception
     */
    void unDiscovery(Service service) throws Exception;

    /**
     * 注册中心名称
     * @return
     */
    String registryName();

    /**
     * 监听spring 上下文刷新事件
     * @param event
     */
    void onRefreshEvent(ContextRefreshedEvent event);

}
