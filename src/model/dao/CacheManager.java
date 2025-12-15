package model.dao;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestionnaire de cache avancé pour optimiser les performances
 * Implémente le pattern Singleton avec cache LRU
 */
public class CacheManager {
    private static CacheManager instance;
    private static final int MAX_CACHE_SIZE = 100;
    
    // Caches pour différents types de données
    private Map<String, CacheEntry<?>> userCache;
    private Map<String, CacheEntry<?>> moduleCache;
    private Map<String, List<?>> queryCache;
    
    // Statistiques de cache
    private long cacheHits = 0;
    private long cacheMisses = 0;
    
    private CacheManager() {
        this.userCache = new ConcurrentHashMap<>();
        this.moduleCache = new ConcurrentHashMap<>();
        this.queryCache = new ConcurrentHashMap<>();
    }
    
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
    
    // Classe interne pour les entrées de cache avec TTL
    private static class CacheEntry<T> {
        private T data;
        private long timestamp;
        private long ttl; // Time to live en millisecondes
        
        public CacheEntry(T data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
        
        public T getData() {
            return data;
        }
    }
    
    /**
     * Met en cache un utilisateur
     */
    public void cacheUser(String code, Object user, long ttl) {
        if (userCache.size() >= MAX_CACHE_SIZE) {
            evictOldestEntry(userCache);
        }
        userCache.put(code, new CacheEntry<>(user, ttl));
    }
    
    /**
     * Récupère un utilisateur du cache
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedUser(String code, Class<T> type) {
        CacheEntry<?> entry = userCache.get(code);
        if (entry != null && !entry.isExpired()) {
            cacheHits++;
            return (T) entry.getData();
        }
        cacheMisses++;
        userCache.remove(code);
        return null;
    }
    
    /**
     * Met en cache un module
     */
    public void cacheModule(String code, Object module, long ttl) {
        if (moduleCache.size() >= MAX_CACHE_SIZE) {
            evictOldestEntry(moduleCache);
        }
        moduleCache.put(code, new CacheEntry<>(module, ttl));
    }
    
    /**
     * Récupère un module du cache
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedModule(String code, Class<T> type) {
        CacheEntry<?> entry = moduleCache.get(code);
        if (entry != null && !entry.isExpired()) {
            cacheHits++;
            return (T) entry.getData();
        }
        cacheMisses++;
        moduleCache.remove(code);
        return null;
    }
    
    /**
     * Met en cache les résultats d'une requête
     */
    public void cacheQuery(String queryKey, List<?> results) {
        queryCache.put(queryKey, new ArrayList<>(results));
    }
    
    /**
     * Récupère les résultats d'une requête du cache
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getCachedQuery(String queryKey) {
        List<?> results = queryCache.get(queryKey);
        if (results != null) {
            cacheHits++;
            return (List<T>) new ArrayList<>(results);
        }
        cacheMisses++;
        return null;
    }
    
    /**
     * Invalide le cache d'un utilisateur
     */
    public void invalidateUser(String code) {
        userCache.remove(code);
        invalidateRelatedQueries("user_" + code);
    }
    
    /**
     * Invalide le cache d'un module
     */
    public void invalidateModule(String code) {
        moduleCache.remove(code);
        invalidateRelatedQueries("module_" + code);
    }
    
    /**
     * Invalide les requêtes liées à une clé
     */
    private void invalidateRelatedQueries(String keyPrefix) {
        queryCache.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .collect(Collectors.toList())
            .forEach(queryCache::remove);
    }
    
    /**
     * Éviction de l'entrée la plus ancienne (LRU)
     */
    private void evictOldestEntry(Map<String, CacheEntry<?>> cache) {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, CacheEntry<?>> entry : cache.entrySet()) {
            if (entry.getValue().timestamp < oldestTime) {
                oldestTime = entry.getValue().timestamp;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }
    
    /**
     * Nettoie toutes les entrées expirées
     */
    public void cleanExpiredEntries() {
        cleanExpiredFromMap(userCache);
        cleanExpiredFromMap(moduleCache);
    }
    
    private void cleanExpiredFromMap(Map<String, CacheEntry<?>> cache) {
        List<String> expiredKeys = cache.entrySet().stream()
            .filter(entry -> entry.getValue().isExpired())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        expiredKeys.forEach(cache::remove);
    }
    
    /**
     * Vide tous les caches
     */
    public void clearAll() {
        userCache.clear();
        moduleCache.clear();
        queryCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
    
    /**
     * Obtient les statistiques du cache
     */
    public CacheStats getStats() {
        return new CacheStats(
            cacheHits,
            cacheMisses,
            userCache.size(),
            moduleCache.size(),
            queryCache.size()
        );
    }
    
    /**
     * Classe pour les statistiques du cache
     */
    public static class CacheStats {
        public final long hits;
        public final long misses;
        public final int userCacheSize;
        public final int moduleCacheSize;
        public final int queryCacheSize;
        
        public CacheStats(long hits, long misses, int userSize, int moduleSize, int querySize) {
            this.hits = hits;
            this.misses = misses;
            this.userCacheSize = userSize;
            this.moduleCacheSize = moduleSize;
            this.queryCacheSize = querySize;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total == 0 ? 0 : (double) hits / total * 100;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Cache Stats: Hits=%d, Misses=%d, Hit Rate=%.2f%%, Users=%d, Modules=%d, Queries=%d",
                hits, misses, getHitRate(), userCacheSize, moduleCacheSize, queryCacheSize
            );
        }
    }
}