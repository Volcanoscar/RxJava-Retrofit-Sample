package com.example.retrofit.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofit.R;
import com.example.retrofit.api.GitHub;
import com.example.retrofit.call.HttpBinService;
import com.example.retrofit.call.MyCall;
import com.example.retrofit.call.MyCallback;
import com.example.retrofit.calladapterfactory.ErrorHandlingCallAdapterFactory;
import com.example.retrofit.model.Contributor;
import com.example.retrofit.model.Ip;
import com.example.retrofit.services.SimpleService;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.HttpException;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar mProgressBar;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mTextView = (TextView) findViewById(R.id.tv_result);
//        formalGet();
//        rxGet();
        getError();
    }


    /**
     * 同步:发送一个请求,等待返回,然后再发送下一个请求
     * <p/>
     * 异步:发送一个请求,不等待返回,随时可以再发送下一个请求
     */

    private void formalGet() {
        mProgressBar.setVisibility(View.VISIBLE);
        GitHub gitHub = SimpleService.getInstance().getGitHubApi();

        Call<List<Contributor>> call = gitHub.contributors("square", "retrofit");
        //asynchronous
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Response<List<Contributor>> response, Retrofit retrofit) {

                Logger.d("response.isSuccess():" + "\n" + response.isSuccess());
                Logger.d("response.code():" + "\n" + response.code());
                Logger.d("response.message():" + "\n" + response.message());
                Logger.d("response.headers().toString:" + "\n" + response.headers().toString());

                mProgressBar.setVisibility(View.GONE);
                List<Contributor> contibutorList = response.body();

                if (contibutorList == null) {
                    Logger.d("contibutorList==null");
                    //404 or the response cannot be converted to List<Contributor>.
                    ResponseBody responseBody = response.errorBody();

                    if (responseBody != null) {
                        try {
                            Logger.d("responseBody!=null");
                            final String errorMessage = responseBody.string();
                            Logger.d("errorMessage:" + "\n" + errorMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTextView.setText(errorMessage);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.d("responseBody==null");
                    }


                } else {
                    Logger.d("contibutorList==null");
                    mTextView.setText(contibutorList.toString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Logger.d("onFailure:" + "\n" + t.getLocalizedMessage());
                mTextView.setText("" + "\n" + t.getLocalizedMessage());
            }
        });
    }


    private void rxGet() {
        GitHub gitHub = SimpleService.getInstance().getGitHubApi();
        Observable<List<Contributor>> observable = gitHub.getContributorObservable("square", "retrofit");
        observable.subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Contributor>>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "onCompleted", Toast.LENGTH_SHORT).show();
                        Logger.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "onError" + "\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mTextView.setText("onError:" + "\n" + e.getMessage());
                        Logger.d("onError:" + "\n" + e.getMessage());

                        //TODO:处理错误
                        if (e instanceof HttpException) {
                            HttpException httpException = (HttpException) e;
                            Logger.d("httpException.code():" + "\n" + httpException.code());
                            Logger.d("httpException.message():" + "\n" + httpException.message());
                            Response<List<Contributor>> response = (Response<List<Contributor>>) httpException.response();
                            Logger.d("response.isSuccess():" + "\n" + response.isSuccess());
                            Logger.d("response.code():" + "\n" + response.code());
                            Logger.d("response.message():" + "\n" + response.message());
                            Logger.d("response.headers().toString:" + "\n" + response.headers().toString());
                        }
                    }

                    @Override
                    public void onNext(List<Contributor> contributors) {
                        Toast.makeText(MainActivity.this, "onNext", Toast.LENGTH_SHORT).show();
                        mTextView.setText(contributors.toString());
                        Logger.d("size:" + "\n" + contributors.size());
                    }
                });
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
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Customize the request
                Request request = original.newBuilder()
                        .header("Accept", "application/json/gzip")
                        .header("Authorization", "auth-token")
                        .method(original.method(), original.body())
                        .build();

                com.squareup.okhttp.Response response = chain.proceed(request);

                // Customize or return the response
                return response;
            }
        });

        return client;
    }

    private void getError() {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://httpbin.org")
                .client(getOkHttpClient())
                .addCallAdapterFactory(new ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HttpBinService service = retrofit.create(HttpBinService.class);
        MyCall<Ip> ip = service.getIp();
        ip.enqueue(new MyCallback<Ip>() {

            @Override
            public void success(final Response<Ip> response) {
                updateUI(response.body().origin);
                Logger.d("response.isSuccess():" +"\n" + response.isSuccess());
                Logger.d("response.code():" +"\n" + response.code());
                Logger.d("response.message():" +"\n" + response.message());
                Logger.d("response.body():" +"\n" + response.body().origin);
                Logger.d("response.headers().toString:" +"\n" + response.headers().toString());
            }

            @Override
            public void unauthenticated(Response<?> response) {
                updateUI(response.code() +"\n" + " " +"\n" + response.message());
                Logger.d("response.isSuccess():" +"\n" + response.isSuccess());
                Logger.d("response.code():" +"\n" + response.code());
                Logger.d("response.message():" +"\n" + response.message());
                Logger.d("response.headers().toString:" +"\n" + response.headers().toString());
            }

            @Override
            public void clientError(Response<?> response) {
                updateUI(response.code() +"\n" + " " +"\n" + response.message());
                Logger.d("response.isSuccess():" +"\n" + response.isSuccess());
                Logger.d("response.code():" +"\n" + response.code());
                Logger.d("response.message():" +"\n" + response.message());
                Logger.d("response.headers().toString:" +"\n" + response.headers().toString());
            }

            @Override
            public void serverError(Response<?> response) {
                updateUI(response.code() +"\n" + " " +"\n" + response.message());
                Logger.d("response.isSuccess():" +"\n" + response.isSuccess());
                Logger.d("response.code():" +"\n" + response.code());
                Logger.d("response.message():" +"\n" + response.message());
                Logger.d("response.headers().toString:" +"\n" + response.headers().toString());
            }

            @Override
            public void networkError(IOException e) {
                updateUI("networkError " +"\n" + e.getMessage());
                Logger.d("response.IOException:" +"\n" + e.getMessage());
            }

            @Override
            public void unexpectedError(Throwable t) {
                updateUI("unexpectedError " +"\n" + t.getMessage());
                Logger.d("response.Throwable:"   +"\n" + t.getMessage());
            }
        });
    }


    private synchronized void updateUI( final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
