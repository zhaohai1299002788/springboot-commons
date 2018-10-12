package com.github.zhaohzi1299002788.config;

import com.github.zhaohzi1299002788.snowflake.SnowflakeId;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

/**
 * @Author: 海仔
 * @Date: 2018/10/12 0012 23:06
 * @Version 1.0
 */
@Configurable
public class BeanConfig {

    @Bean
    public SnowflakeId getSnowflakeId(SnowflakeId snowflakeId) {
        return snowflakeId;
    }
}
