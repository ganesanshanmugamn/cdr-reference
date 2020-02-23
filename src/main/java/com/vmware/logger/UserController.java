package com.vmware.logger;


import com.vmware.logger.model.User;
import com.vmware.logger.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RestController
public class UserController {


    public static final String USER_LIST = "https://jsonplaceholder.typicode.com/users";

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/")
    public UserList getUserList() {
        User[] response = restTemplate
                .getForObject(USER_LIST, User[].class);
        return new UserList(Arrays.asList(response));
    }

}
