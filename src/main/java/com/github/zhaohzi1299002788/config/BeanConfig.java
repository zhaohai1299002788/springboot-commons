package com.github.zhaohzi1299002788.config;

import com.github.zhaohzi1299002788.distributed.redis.DistributedLockTemplate;
import com.github.zhaohzi1299002788.distributed.redis.SingleDistributedLockTemplate;
import com.github.zhaohzi1299002788.distributed.zookeeper.DistributedLockZkp;
import com.github.zhaohzi1299002788.snowflake.SnowflakeId;
import com.github.zhaohzi1299002788.whole.WebExceptionHandle;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.io.IOException;


@Configurable
public class BeanConfig {

    @Bean
    public SnowflakeId getSnowflakeId(SnowflakeId snowflakeId) {
        return snowflakeId;
    }

    @Value("classpath:/redisson-conf.yml")
    Resource configFile;

    @Value("${zkConnString}")
    String zkConnString;

    @Value("${lockName}")
    String lockName;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(configFile.getInputStream());
        return Redisson.create(config);
    }

    @Bean
    public DistributedLockTemplate distributedLockTemplate(RedissonClient redissonClient) {
        return new SingleDistributedLockTemplate(redissonClient);
    }

    @Bean
    public DistributedLockZkp distributedLockZkp(DistributedLockZkp distributedLockZkp) {
        return new DistributedLockZkp(zkConnString, lockName);
    }

    @Bean
    public WebExceptionHandle webExceptionHandle(WebExceptionHandle webExceptionHandle) {
        return new WebExceptionHandle();
    }
}
