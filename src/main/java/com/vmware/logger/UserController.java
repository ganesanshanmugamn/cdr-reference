package com.vmware.logger;


import com.vmware.logger.httpclient.APIClient;
import com.vmware.logger.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {


    @Autowired
    @Qualifier("RestTemplate")
    APIClient restTemplate;

    @GetMapping("/")
    public UserList getUserList() {
        return restTemplate.getUsers();
    }

}
