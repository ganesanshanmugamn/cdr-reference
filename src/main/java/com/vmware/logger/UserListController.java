package com.vmware.logger;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class UserListController {


    @Autowired
    RestTemplate restTemplate;


    @GetMapping("/")
    public String getUserList() {

        return "Success";


    }

}
