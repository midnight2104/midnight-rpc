package com.midnight.rpc.demo.api;

public interface UserService {

    User findById(int id);

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
}