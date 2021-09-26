package com.vmware.logger.cache;

import com.vmware.logger.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserCache {

    private final static Logger log = LoggerFactory.getLogger(UserCache.class);


    @Cacheable(value = "userDetailsCache", key = "#id", unless="#result == null")
    public User getUser(int id, User user) {
        return user;
    }
}
