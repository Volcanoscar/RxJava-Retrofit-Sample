package com.example.retrofit.api;

import com.example.retrofit.model.Contributor;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface GitHub {

    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(@Path("owner") String owner, @Path("repo") String repo);

    @GET("/repos/{owner}/{repo}/contributors")
    Observable<List<Contributor>> getContributorObservable(@Path("owner") String owner, @Path("repo") String repo);


}
