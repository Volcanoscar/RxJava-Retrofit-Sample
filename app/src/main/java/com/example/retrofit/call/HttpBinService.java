package com.example.retrofit.call;

import com.example.retrofit.model.Ip;

import retrofit.http.GET;

public interface HttpBinService {
    @GET("/ip")
    MyCall<Ip> getIp();
}