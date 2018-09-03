package com.kepler.registry;

import com.kepler.config.PropertiesUtils;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册中心
 *
 * @author longyaokun
 */
public class RegistryContext implements Exported, Imported, InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    private static final Log LOGGER = LogFactory.getLog(Registry.class);

    public static final String ZOOKEEPER = "zookeeper";

    public static final String ETCD = "etcd";

    public static final String REGISTRY = PropertiesUtils.get(RegistryContext.class.getName().toLowerCase() + ".registry", ZOOKEEPER);

    private final List<Registry> registries;

    private List<Registry> activeRegistries;

    public RegistryContext(List<Registry> registries) {
        this.registries = registries;
    }

    @Override
    public void export(Service service, Object instance) throws Exception {
        for(Registry registry : activeRegistries) {
            registry.registration(service, instance);
        }
    }

    @Override
    public void logout(Service service) throws Exception {
        for(Registry registry : activeRegistries) {
            registry.unRegistration(service);
        }
    }

    @Override
    public void subscribe(Service service) throws Exception {
        for(Registry registry : activeRegistries) {
            registry.discovery(service);
        }
    }

    @Override
    public void unsubscribe(Service service) throws Exception {
        for(Registry registry : activeRegistries) {
            registry.unDiscovery(service);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> registryNames = Arrays.asList(REGISTRY.split(","));
        activeRegistries = registries.stream().filter(registry -> registryNames.contains(registry.registryName())).collect(Collectors.toList());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for(Registry registry : activeRegistries) {
            registry.onRefreshEvent(event);
        }
    }

}
