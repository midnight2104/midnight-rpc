package com.midnight.rpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {

    User findById(int id);

    User findById(long id);

    User findById(int id, String name);

    int getId(int id);

    long getId(long id);

    String getName();

    long getId(User user);

    long getId(float id);


    String getName(int id);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    User find(int timeout);

    User isolate(int id);

    User gray(int id);

    List<User> getList(List<User> users);

    Map<String, User> getMap(Map<String, User> userMap);

    User ex(boolean flag);

    Boolean getFlag(boolean b);

    User[] findUsers(User[] users);

    String echoParameter(String key);


}