package com.kepler.config.manager;

import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.watch.WatchEvent;
import com.kepler.config.PropertiesUtils;
import com.kepler.etcd.EtcdClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fatduo
 * @date 14/09/2018
 */
public class EtcdConfigManager implements ConfigManager {

    private static final Log LOGGER = LogFactory.getLog(EtcdConfigManager.class);

    /**
     * 保存配置信息路径
     */
    private static final String ROOT = PropertiesUtils.get(EtcdConfigManager.class.getName().toLowerCase() + ".root", "/kepler/config");

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<String, String> snapshot = new ConcurrentHashMap<>();

    private final EtcdClient etcdClient;

    private volatile boolean shutdown;

    public EtcdConfigManager(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    public void init() throws Exception {
        GetResponse getResponse = etcdClient.getAllByPrefix(ROOT).get();

        long revision = -1;
        for (KeyValue keyValue : getResponse.getKvs()) {
            snapshot.put(keyValue.getKey().toStringUtf8().substring(ROOT.length() + 1), keyValue.getValue().toStringUtf8());
            long modRevision = keyValue.getModRevision();
            if (modRevision > revision) {
                revision = modRevision;
            }
        }

        this.executor.submit(new ConfigWatcher(revision + 1));
    }

    public void destroy() throws Exception {
        this.shutdown = true;
        this.etcdClient.close();
        this.executor.shutdownNow();
    }

    @Override
    public String get(String namespace, String key) {
        return snapshot.get(namespace + "/" + key);
    }

    private class ConfigWatcher implements Runnable {

        private long revision;

        ConfigWatcher(long revision) {
            this.revision = revision;
        }

        @Override
        public void run() {
            Watch.Watcher watcher = EtcdConfigManager.this.etcdClient.watch(EtcdConfigManager.ROOT, revision);
            while (!EtcdConfigManager.this.shutdown) {
                try {
                    for (WatchEvent watchEvent : watcher.listen().getEvents()) {
                        switch (watchEvent.getEventType()) {
                            case PUT:
                                // add
                                KeyValue current = watchEvent.getKeyValue();
                                snapshot.put(current.getKey().toStringUtf8().substring(ROOT.length() + 1),
                                        current.getValue().toStringUtf8());
                                break;
                            case DELETE:
                                // delete
                                KeyValue prev = watchEvent.getPrevKV();
                                snapshot.remove(prev.getKey().toStringUtf8().substring(ROOT.length() + 1));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    EtcdConfigManager.LOGGER.error("Config manager watcher failed, message=" + e.getMessage(), e);
                }
            }
            EtcdConfigManager.LOGGER.info("Config manager watcher thread shutdown");
        }
    }
}
