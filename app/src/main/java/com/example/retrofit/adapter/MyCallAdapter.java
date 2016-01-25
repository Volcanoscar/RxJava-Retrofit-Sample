package com.example.retrofit.adapter;

import com.example.retrofit.call.MyCall;
import com.example.retrofit.call.MyCallback;


import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MyCallAdapter<T> implements MyCall<T> {
    private final Call<T> call;

    public MyCallAdapter(Call<T> call) {
        this.call = call;
    }

    @Override
    public void cancel() {
        call.cancel();
    }

    @Override
    public void enqueue(final MyCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Response<T> response, Retrofit retrofit) {
                                int code = response.code();
                if (code >= 200 && code < 300) {
                    callback.success(response);
                } else if (code == 401) {
                    callback.unauthenticated(response);
                } else if (code >= 400 && code < 500) {
                    callback.clientError(response);
                } else if (code >= 500 && code < 600) {
                    callback.serverError(response);
                } else {
                    callback.unexpectedError(new RuntimeException("Unexpected response " + response));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof IOException) {
                    callback.networkError((IOException) t);
                } else {
                    callback.unexpectedError(t);
                }
            }
//            @Override
//            public void onResponse(Response<T> response) {
//                int code = response.code();
//                if (code >= 200 && code < 300) {
//                    callback.success(response);
//                } else if (code == 401) {
//                    callback.unauthenticated(response);
//                } else if (code >= 400 && code < 500) {
//                    callback.clientError(response);
//                } else if (code >= 500 && code < 600) {
//                    callback.serverError(response);
//                } else {
//                    callback.unexpectedError(new RuntimeException("Unexpected response " + response));
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                if (t instanceof IOException) {
//                    callback.networkError((IOException) t);
//                } else {
//                    callback.unexpectedError(t);
//                }
//            }
        });
    }

    @Override
    public MyCall<T> clone() {
        return new MyCallAdapter<>(call.clone());
    }
}