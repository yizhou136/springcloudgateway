package com.example.demo.netty.mysql.common;

import com.example.demo.netty.mysql.client.MysqlClient;
import io.netty.channel.Channel;

public class Session {
    private MysqlClient mysqlClient;
    private Channel frontChannel;

    public Session(Channel frontChannel, MysqlClient mysqlClient) {
        this.mysqlClient = mysqlClient;
        this.frontChannel = frontChannel;
    }

    public MysqlClient getMysqlClient() {
        return mysqlClient;
    }

    public void setMysqlClient(MysqlClient mysqlClient) {
        this.mysqlClient = mysqlClient;
    }

    public Channel getFrontChannel() {
        return frontChannel;
    }

    public void setFrontChannel(Channel frontChannel) {
        this.frontChannel = frontChannel;
    }
}
