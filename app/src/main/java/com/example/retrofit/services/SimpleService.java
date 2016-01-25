/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.retrofit.services;

import com.example.retrofit.api.GitHub;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

public class SimpleService {

    private static final String API_URL = "https://api.github.com";

    protected void SimpleService() {
    }

    private volatile static SimpleService instance = null;

    public static SimpleService getInstance() {
        if (instance == null) {
            synchronized (SimpleService.class) {
                if (instance == null) {
                    instance = new SimpleService();
                }
            }
        }
        return instance;
    }

    /**
     * OkHttp拦截器
     *
     * @return OkHttpClient
     */
    private static OkHttpClient getOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Customize the request
                Request request = original.newBuilder()
                        .header("Accept", "application/json/gzip")
                        .header("Authorization", "auth-token")
                        .method(original.method(), original.body())
                        .build();

                Response response = chain.proceed(request);

                // Customize or return the response
                return response;
            }
        });

        return client;
    }

    private volatile static GitHub gitHubApi = null;

    public static GitHub getGitHubApi() {
        if (gitHubApi == null) {
            synchronized (SimpleService.class) {
                if (gitHubApi == null) {
                    gitHubApi = new Retrofit.Builder()
                            .baseUrl(API_URL)
                            .client(getOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build().create(GitHub.class);
                }
            }
        }
        return gitHubApi;
    }


//    public static void main(String... args) throws IOException {
//        // Create a very simple REST adapter which points the GitHub API.
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(API_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Create an instance of our GitHub API interface.
//        GitHub github = retrofit.create(GitHub.class);
//
//        // Create a call instance for looking up Retrofit contributors.
//        Call<List<Contributor>> call = github.contributors("square", "retrofit");
//
//        // Fetch and print a list of the contributors to the library.
//        List<Contributor> contributors = call.execute().body();
//        for (Contributor contributor : contributors) {
//            System.out.println(contributor.login + " (" + contributor.contributions + ")");
//        }
//    }
}
