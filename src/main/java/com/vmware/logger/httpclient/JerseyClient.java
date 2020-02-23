package com.vmware.logger.httpclient;

import com.google.gson.Gson;
import com.vmware.logger.model.UserList;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Component
@Qualifier("Jersey")
public class JerseyClient implements APIClient {


    private static Client createClient() {
        ClientConfig config = new ClientConfig();
        config.register(RequestClientWriterInterceptor.class);
        return ClientBuilder.newClient(config);
    }

    @Override
    public UserList getUsers() {
        String response = createClient().target(USER_LIST)
                .request()
                .get(String.class);
        Gson gson = new Gson();
        UserList users = gson.fromJson(response, UserList.class);
        return users;
    }

}
