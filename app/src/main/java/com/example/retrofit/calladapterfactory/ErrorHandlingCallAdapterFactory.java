package com.example.retrofit.calladapterfactory;

import com.example.retrofit.adapter.MyCallAdapter;
import com.example.retrofit.call.MyCall;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Retrofit;

public class ErrorHandlingCallAdapterFactory implements CallAdapter.Factory {
    @Override
    public CallAdapter<MyCall<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        TypeToken<?> token = TypeToken.of(returnType);
        if (token.getRawType() != MyCall.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "MyCall must have generic type (e.g., MyCall<ResponseBody>)");
        }
        final Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        return new CallAdapter<MyCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public <R> MyCall<R> adapt(Call<R> call) {
                return new MyCallAdapter<>(call);
            }
        };
    }
}