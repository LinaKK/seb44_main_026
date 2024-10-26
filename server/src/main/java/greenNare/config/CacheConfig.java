package greenNare.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10));

        RedisCacheConfiguration productsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(null);

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration("products", productsConfig)
                .cacheDefaults(redisCacheConfiguration)
                .build();

    }
}
