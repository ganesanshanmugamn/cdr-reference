package com.vmware.logger.model;

import java.io.Serializable;
import java.util.List;

public class UserList implements Serializable {

    private static final long serialVersionUID = 8555951568658877358L;
    private List<User> users;

    public UserList() {
    }

    public UserList(List<User> users) {

        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
