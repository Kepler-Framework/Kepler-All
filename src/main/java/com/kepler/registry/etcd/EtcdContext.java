package com.kepler.registry.etcd;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.host.HostsContext;
import com.kepler.host.impl.ServerHost;
import com.kepler.registry.Registry;
import com.kepler.registry.RegistryContext;
import com.kepler.serial.Serials;
import com.kepler.service.ImportedListener;
import com.kepler.service.Service;
import com.kepler.service.ServiceInstance;
import com.kepler.zookeeper.ZkSerial;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * etcd注册中心
 *
 * @author longyaokun
 */
public class EtcdContext implements Registry {

    private static final Log LOGGER = LogFactory.getLog(EtcdContext.class);

    /**
     * 保存服务信息路径
     */
    public static final String ROOT = PropertiesUtils.get(EtcdContext.class.getName().toLowerCase() + ".root", "/kepler");


    /**
     * 是否发布
     */
    private static final String EXPORT_KEY = EtcdContext.class.getName().toLowerCase() + ".export";

    private static final boolean EXPORT_VAL = PropertiesUtils.get(EtcdContext.EXPORT_KEY, true);

    /**
     * 是否导入
     */
    private static final String IMPORT_KEY = EtcdContext.class.getName().toLowerCase() + ".import";

    private static final boolean IMPORT_VAL = PropertiesUtils.get(EtcdContext.IMPORT_KEY, true);

    private final ImportedListener listener;

    private final HostsContext hosts;

    private final ServerHost local;

    private final Profile profile;

    private final Serials serials;

    private final EtcdClient etcdClient;

    public EtcdContext(ImportedListener listener, HostsContext hosts, ServerHost local, Profile profile, Serials serials, EtcdClient etcdClient) {
        this.listener = listener;
        this.hosts = hosts;
        this.local = local;
        this.profile = profile;
        this.serials = serials;
        this.etcdClient = etcdClient;
    }

    public void init() throws Exception {

    }

    public void destroy() throws Exception {
        this.etcdClient.close();
    }

    @Override
    public void registration(Service service, Object instance) throws Exception {
        // 是否发布远程服务
        if (!PropertiesUtils.profile(this.profile.profile(service), EtcdContext.EXPORT_KEY, EtcdContext.EXPORT_VAL)) {
            EtcdContext.LOGGER.warn("Disabled export service: " + service + " ... ");
            return;
        }

        // 生成节点信息，复用原zk的对象(Profile Tag, Priority)
        ZkSerial serial = new ZkSerial(new ServerHost.Builder(this.local).setTag(PropertiesUtils.profile(this.profile.profile(service), Host.TAG_KEY, Host.TAG_VAL)).setPriority(PropertiesUtils.profile(this.profile.profile(service), Host.PRIORITY_KEY, Host.PRIORITY_DEF)).toServerHost(), service);
        // 存入etcd
        this.etcdClient.put(key(service), this.serials.def4output().output(serial, ServiceInstance.class));

        EtcdContext.LOGGER.info("Export service to etcd: " + service + " ... ");

    }

    @Override
    public void unRegistration(Service service) throws Exception {
        this.etcdClient.delete(key(service));
    }

    @Override
    public void discovery(Service service) throws Exception {
        // 是否加载远程服务
        if (!PropertiesUtils.profile(this.profile.profile(service), EtcdContext.IMPORT_KEY, EtcdContext.IMPORT_VAL)) {
            EtcdContext.LOGGER.warn("Disabled import service from etcd: " + service + " ... ");
            return;
        }
        List<ServiceInstance> instances = etcdClient.getAllByPrefix(prefix(service))
                .thenApply(getResponse -> getResponse.getCount() > 0 ? getResponse.getKvs().stream()
                        .map(kv -> this.serials.def4input().input(kv.getValue().getBytes(), ServiceInstance.class))
                        .collect(Collectors.toList()) : null)
                .get();

        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        instances.forEach(instance -> {
            try {
                listener.add(instance);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
        // TODO 添加 Watcher
    }

    @Override
    public void unDiscovery(Service service) throws Exception {

    }

    @Override
    public String registryName() {
        return RegistryContext.ETCD;
    }

    @Override
    public void onRefreshEvent(ContextRefreshedEvent event) {

    }

    private String key(Service service) {
        return prefix(service) + "/" + this.local.sid();
    }

    private String prefix(Service service) {
        return EtcdContext.ROOT + "/" + service.service() + "/" + service.versionAndCatalog();
    }
}
