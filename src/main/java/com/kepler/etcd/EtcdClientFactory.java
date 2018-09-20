package com.kepler.etcd;

import com.kepler.config.PropertiesUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author longyaokun
 */
public class EtcdClientFactory implements FactoryBean<EtcdClient> {

    /**
     * etcd服务主机列表,逗号分隔。i.e. localhost:2379,localhost:2380
     */
    public static final String HOST = PropertiesUtils.get(EtcdClientFactory.class.getName().toLowerCase() + ".host", "");

    /**
     * 与etcd的通信协议
     */
    private static final String SCHEME = PropertiesUtils.get(EtcdClientFactory.class.getName().toLowerCase() + ".scheme", "http");

    /**
     * keep alive 心跳间隔，单位s
     */
    private static final long TTL = PropertiesUtils.get(EtcdClientFactory.class.getName().toLowerCase() + ".ttl", 30L);


    @Override
    public EtcdClient getObject() throws Exception {
        EtcdClient etcdClient = EtcdClient.newBuilder().scheme(SCHEME).endpoint(HOST).ttlSeconds(TTL).build();
        etcdClient.init();
        return etcdClient;
    }

    @Override
    public Class<?> getObjectType() {
        return EtcdClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
