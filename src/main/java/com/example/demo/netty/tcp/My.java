package com.example.demo.netty.tcp;

import reactor.ipc.netty.http.server.HttpServer;

public class My extends HttpServer {

    private My(Builder builder) {
        super(builder);
    }
}
