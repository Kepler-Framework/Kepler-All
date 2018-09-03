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

    private Long leaseId;

    private EtcdClient(Collection<String> endpoints, long ttl) {
        etcdClient = Client.builder().endpoints(endpoints).build();
        kvClient = etcdClient.getKVClient();
        leaseClient = etcdClient.getLeaseClient();
        watchClient = etcdClient.getWatchClient();
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

    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (leaseId != null) {
            LOGGER.info("Revoking lease with id=" + leaseId);
            leaseClient.revoke(leaseId).get();
        }
        etcdClient.close();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void init() throws Exception {
        //申请租约并keep alive
        try {
            leaseId = leaseClient.grant(ttl).get().getID();
            leaseClient.keepAlive(leaseId);
        } catch (Exception e) {
            LOGGER.error("Failed to grant etcd lease, message=" + e.getMessage());
        }

    }

    public static class Builder {
        private String scheme = "http";
        private Set<String> endpoints = new HashSet<>();
        private long ttl = 5;

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
