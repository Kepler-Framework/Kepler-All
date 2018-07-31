package com.kepler.registry;

import com.kepler.config.PropertiesUtils;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 注册中心
 * @author longyaokun
 */
public class RegistryContext implements Exported, Imported, ApplicationContextAware, InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    public static final String ZOOKEEPER = "zookeeper";

    public static final String REGISTRY = PropertiesUtils.get(RegistryContext.class.getName().toLowerCase() + ".registry", ZOOKEEPER);

    private Registry registry;

    private ApplicationContext applicationContext;

    @Override
    public void export(Service service, Object instance) throws Exception {
        registry.registration(service, instance);
    }

    @Override
    public void logout(Service service) throws Exception {
        registry.unRegistration(service);
    }

    @Override
    public void subscribe(Service service) throws Exception {
        registry.discovery(service);
    }

    @Override
    public void unsubscribe(Service service) throws Exception {
        registry.unDiscovery(service);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for(Registry registry : applicationContext.getBeansOfType(Registry.class).values()) {
            if(REGISTRY.equalsIgnoreCase(registry.registryName())) {
                this.registry = registry;
                break;
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registry.onRefreshEvent(event);
    }
}
