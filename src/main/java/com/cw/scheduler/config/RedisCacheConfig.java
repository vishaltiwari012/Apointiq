package com.cw.scheduler.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Configure type validator
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        // Configure ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Store type info as a property instead of wrapper array
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // Create serializer with configured ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Custom TTLs per cache name
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("userProfiles", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("allUsers", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("usersByRole", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("inactiveUsers", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("activeUsers", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("userById", defaultConfig.entryTtl(Duration.ofHours(1)));

        cacheConfigurations.put("providerProfiles", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("providerApplications", defaultConfig.entryTtl(Duration.ofHours(24)));

        cacheConfigurations.put("providerSchedules", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("allSchedulesByDay", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("providerScheduleForDay", defaultConfig.entryTtl(Duration.ofHours(6)));

        cacheConfigurations.put("serviceReviews", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("userReviews", defaultConfig.entryTtl(Duration.ofHours(12)));

        cacheConfigurations.put("providerOfferedServices", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("allOfferedServices", defaultConfig.entryTtl(Duration.ofHours(3)));

        cacheConfigurations.put("userNotifications", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userNotificationsByType", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put("individualServicesByOfferedService", defaultConfig.entryTtl(Duration.ofHours(6)));

        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("categoriesWithServiceCount", defaultConfig.entryTtl(Duration.ofHours(12)));

        cacheConfigurations.put("userAppointments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("providerAppointments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("upcomingAppointments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("appointmentsByDate", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

