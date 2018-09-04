package com.kepler.registry;

import com.kepler.KeplerLocalException;
import com.kepler.config.PropertiesUtils;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
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

    private Delay delay = new Delay();

    public RegistryContext(List<Registry> registries) {
        this.registries = registries;
    }

    @Override
    public void export(Service service, Object instance) throws Exception {
        this.delay.exported(service, instance);
    }

    @Override
    public void logout(Service service) throws Exception {
        for (Registry registry : activeRegistries) {
            registry.unRegistration(service);
        }
    }

    @Override
    public void subscribe(Service service) throws Exception {
        for (Registry registry : activeRegistries) {
            registry.discovery(service);
        }
    }

    @Override
    public void unsubscribe(Service service) throws Exception {
        for (Registry registry : activeRegistries) {
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
        // 延迟发布
        try {
            this.delay.reach();
            for (Registry registry : activeRegistries) {
                registry.onRefreshEvent(event);
            }
        } catch (Throwable throwable) {
            throw new KeplerLocalException(throwable);
        }
    }

    private void exported4delay(Service service, Object instance) throws Exception {
        for (Registry registry : activeRegistries) {
            registry.registration(service, instance);
        }
    }

    /**
     * 服务端口启动成功后, 再发布服务
     *
     * @author tudesheng
     */
    protected class Delay {

        private List<Pair<Service, Object>> services = new ArrayList<>();

        private boolean started = false;

        public synchronized void exported(Service service, Object instance) throws Exception {
            if (this.started) {
                // 如果已启动则直接发布(场景: 断线重连)
                RegistryContext.this.exported4delay(service, instance);
            } else {
                // 未重启则加入缓存
                this.services.add(new Pair<>(service, instance));
            }
        }

        /**
         * 触发延迟加载
         *
         * @throws Exception
         */
        synchronized void reach() throws Exception {
            if (!this.started) {
                for (Pair<Service, Object> pair : services) {
                    RegistryContext.this.exported4delay(pair.key(), pair.val());
                }
                // 切换状态并清空缓存
                started = true;
                services = null;
            }
        }

    }

    protected class Pair<K, V> {

        private K key;

        private V val;

        private Pair(K key, V val) {
            this.key = key;
            this.val = val;
        }

        public K key() {
            return key;
        }

        public V val() {
            return val;
        }
    }

}
