package com.kepler.registry.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.options.WatchOption;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * etcd客户端封装
 *
 * @author longyaokun
 */
public class EtcdClient implements AutoCloseable {

    private static final Log LOGGER = LogFactory.getLog(EtcdClient.class);

    private final Client etcdClient;

    private final KV kvClient;

    private final Lease leaseClient;

    private final Watch watchClient;

    private final long ttl;

    private volatile Long leaseId;

    private EtcdClient(Collection<String> endpoints, long ttl) {
        this.etcdClient = Client.builder().endpoints(endpoints).build();
        this.kvClient = this.etcdClient.getKVClient();
        this.leaseClient = this.etcdClient.getLeaseClient();
        this.watchClient = this.etcdClient.getWatchClient();
        this.ttl = ttl;
    }

    public CompletableFuture<PutResponse> put(String key, byte[] value) {
        ByteSequence keyByteSequence = ByteSequence.fromString(key);
        ByteSequence valueByteSequence = ByteSequence.fromBytes(value);
        return kvClient.put(keyByteSequence, valueByteSequence, PutOption.newBuilder().withLeaseId(leaseId).build());
    }

    public CompletableFuture<DeleteResponse> delete(String key) {
        return kvClient.delete(ByteSequence.fromString(key));
    }

    public CompletableFuture<GetResponse> getAllByPrefix(String prefix) {
        ByteSequence prefixByteSequence = ByteSequence.fromString(prefix);
        GetOption prefixOption = GetOption.newBuilder().withPrefix(prefixByteSequence).build();
        return kvClient.get(prefixByteSequence, prefixOption);
    }

    public Watch.Watcher watch(String prefix) {
        ByteSequence prefixByteSequence = ByteSequence.fromString(prefix);
        return watchClient.watch(prefixByteSequence,
                WatchOption.newBuilder().withPrefix(prefixByteSequence).withPrevKV(true).build());
    }

    public Watch.Watcher watch(String prefix, long revision) {
        ByteSequence prefixByteSequence = ByteSequence.fromString(prefix);
        return watchClient.watch(prefixByteSequence,
                WatchOption.newBuilder().withRevision(revision).withPrefix(prefixByteSequence).withPrevKV(true).build());
    }

    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (leaseId != null) {
            EtcdClient.LOGGER.info("Revoking lease with id=" + leaseId);
            leaseClient.revoke(leaseId).get();
        }
        etcdClient.close();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void init() throws Exception {
        //为了兼容不使用etcd的情况，这里不允许抛异常
        try {
            leaseAndKeepAlive();
        } catch (Exception e) {
            LOGGER.error("Failed to grant etcd lease, message=" + e.getMessage());
        }
    }

    /**
     * 申请租约并keep alive
     *
     * @throws Exception
     */
    public void leaseAndKeepAlive() throws Exception {
        leaseId = leaseClient.grant(ttl).get().getID();
        leaseClient.keepAlive(leaseId);
    }

    public boolean leaseExpired() {
        try {
            leaseClient.keepAliveOnce(leaseId).get();
        } catch (Exception e) {
            EtcdClient.LOGGER.error("lease expired for leaseId[" + leaseId + "],message=" + e.getMessage());
            return true;
        }
        return false;
    }

    public static class Builder {
        private String scheme = "http";
        private Set<String> endpoints = new HashSet<>();
        private long ttl = 30;

        private Builder() {
        }

        public EtcdClient build() throws IllegalStateException {
            if (endpoints.isEmpty()) {
                throw new IllegalStateException("Provide at least one endpoint!");
            }
            return new EtcdClient(
                    endpoints.stream()
                            .map(endpoint -> scheme + "://" + endpoint)
                            .collect(Collectors.toList()),
                    ttl
            );
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder endpoint(String address, int port) {
            this.endpoints.add(address + ":" + port);
            return this;
        }

        public Builder endpoint(String addressAndPorts) {
            for (String addressPort : addressAndPorts.split(",")) {
                this.endpoints.add(addressPort.trim());
            }
            return this;
        }

        public Builder ttlSeconds(long ttlSeconds) {
            this.ttl = ttlSeconds;
            return this;
        }
    }
}
