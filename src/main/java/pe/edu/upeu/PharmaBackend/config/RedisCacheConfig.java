package pe.edu.upeu.PharmaBackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
public class RedisCacheConfig implements CachingConfigurer {
    public static final String CATEGORIAS = "categorias";
    public static final String PRODUCTOS = "productos";
    public static final String PRODUCTO = "producto";
    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);
    private final AtomicReference<String> generacion = new AtomicReference<>(UUID.randomUUID().toString());

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connection,
                                   @Value("${spring.cache.redis.time-to-live:10m}") Duration ttl) {
        return RedisCacheManager.builder(connection)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .computePrefixWith(cache -> "pharmasoft:s13:" + generacion.get() + ":" + cache + "::")
                        .disableCachingNullValues()
                        .entryTtl(ttl))
                .initialCacheNames(Set.of(CATEGORIAS, PRODUCTOS, PRODUCTO))
                .disableCreateOnMissingCache()
                .transactionAware()
                .enableStatistics()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException error, Cache cache, Object key) {
                registrar("lectura", error, cache);
            }

            @Override
            public void handleCachePutError(RuntimeException error, Cache cache, Object key, Object value) {
                registrar("escritura", error, cache);
            }

            @Override
            public void handleCacheEvictError(RuntimeException error, Cache cache, Object key) {
                invalidacionFallida(error, cache);
            }

            @Override
            public void handleCacheClearError(RuntimeException error, Cache cache) {
                invalidacionFallida(error, cache);
            }
        };
    }

    private void invalidacionFallida(RuntimeException error, Cache cache) {
        generacion.set(UUID.randomUUID().toString());
        registrar("invalidación", error, cache);
    }

    private void registrar(String operacion, RuntimeException error, Cache cache) {
        log.warn("Caché degradada | operación={} | caché={} | tipo={}",
                operacion, cache.getName(), error.getClass().getSimpleName());
    }
}
