package com.vmware.logger.httpclient;

import com.vmware.logger.model.UserList;

public interface APIClient {

    String USER_LIST = "https://jsonplaceholder.typicode.com/users";

    UserList getUsers();

}
