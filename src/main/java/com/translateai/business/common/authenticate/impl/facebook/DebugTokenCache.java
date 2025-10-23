package com.translateai.business.common.authenticate.impl.facebook;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugTokenCache {

    private static class Entry {
        final Map<String, Object> data;
        final Instant expiresAt;

        Entry(Map<String, Object> data, Instant expiresAt) {
            this.data = data;
            this.expiresAt = expiresAt;
        }
    }

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();
    private final long ttlMs;

    public DebugTokenCache(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public Map<String, Object> get(String token) {
        Entry e = cache.get(token);
        if (e == null) return null;
        if (Instant.now().isAfter(e.expiresAt)) {
            cache.remove(token);
            return null;
        }
        return e.data;
    }

    public void put(String token, Map<String, Object> data) {
        cache.put(token, new Entry(data, Instant.now().plusMillis(ttlMs)));
    }

}
