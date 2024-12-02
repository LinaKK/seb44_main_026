package greenNare.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;



@Service
public class CacheService {
    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager){
        this.cacheManager = cacheManager;

    }


    public void putCache(String name, String key, Object value){
        Cache cache = cacheManager.getCache(name);
        cache.put(key, value);

    }

    public <T> T getCache(String name, String key, Class<T> type){
        Cache cache = cacheManager.getCache(name);

        if(cache != null){
            Cache.ValueWrapper wrapper = cache.get(key);
            if(wrapper != null){
                Object cacheValue = wrapper.get();
                return type.cast(cacheValue);
            }
        }
        return null;

    }
}
