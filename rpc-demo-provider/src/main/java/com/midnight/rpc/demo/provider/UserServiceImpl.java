package com.midnight.rpc.demo.provider;

import com.midnight.rpc.core.annotation.RpcProvider;
import com.midnight.rpc.demo.api.User;
import com.midnight.rpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RpcProvider
public class UserServiceImpl implements UserService {
    @Autowired
    Environment env;

    @Override
    public User findById(int id) {
        return new User(id, "Midnight-" + env.getProperty("server.port") + "-" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "重载方法 Midnight-" + System.currentTimeMillis() + "-" + name);
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public String getName() {
        return "Midnight123456";
    }

    @Override
    public long getId(User user) {
        return user.getId();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName(int id) {
        return "Midnight111";
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{4L, 5L, 6L};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }


    @Override
    public User find(int timeout) {
        String port = env.getProperty("server.port");
        if ("8081".equals(port)) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(666, "MID-666-" + port);
    }

    @Override
    public User isolate(int id) {
        String port = env.getProperty("server.port");
        if ("8081".equals(port) || "8094".equals(port)) {
            System.out.println(100 / id);
        }
        return new User(id, "midnight-" + port);
    }

    @Override
    public User gray(int id) {
        String port = env.getProperty("server.port");

        return new User(id, "midnight-gray-v2-" + port);
    }

}