package com.vmware.logger.httpclient;

import com.vmware.logger.model.UserList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("Jersey")
public class JerseyClient implements APIClient {


    @Override
    public UserList getUsers() {
        return null;
    }


}
