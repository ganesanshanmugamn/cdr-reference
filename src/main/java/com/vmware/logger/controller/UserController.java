package com.vmware.logger.controller;


import com.vmware.logger.cache.UserCache;
import com.vmware.logger.config.CacheEventLogger;
import com.vmware.logger.httpclient.APIClient;
import com.vmware.logger.model.User;
import com.vmware.logger.model.UserList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserCache userCache;

    @Autowired
    @Qualifier("RestTemplate")
    APIClient restTemplate;

    @GetMapping
    public UserList getUserList() {
        return restTemplate.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserDetail(@PathVariable int id) {
        User user = userCache.getUser(id, null);
        if (user != null) {
            log.info("-------------------Retrieve from Cache------------");
            return user;
        }
        User user1 = restTemplate.getUser(id);
        userCache.getUser(id, user1);
        return user1;
    }


}
