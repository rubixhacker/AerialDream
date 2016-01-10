package com.hackedbuce.aerialdream;

import com.hackedbuce.aerialdream.data.Videos;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by rubix on 1/10/2016.
 */
public interface AerialService {

    @GET("{season}/videos/entries.json")
    Observable<List<Videos>> getVideos(@Path("season") String season);
}
