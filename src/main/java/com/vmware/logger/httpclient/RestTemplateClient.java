package com.vmware.logger.httpclient;

import com.vmware.logger.model.User;
import com.vmware.logger.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
@Qualifier("RestTemplate")
public class RestTemplateClient implements APIClient {

    @Autowired
    RestTemplate restTemplate;

    @Override
    public UserList getUsers() {
        User[] response = restTemplate
                .getForObject(USER_LIST, User[].class);
        return new UserList(Arrays.asList(response));
    }


}
