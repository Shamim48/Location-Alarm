package com.shamim.LocationAlarm;

import com.shamim.LocationAlarm.nearby.NearbyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface NearbyService {
    @GET
    Call<NearbyResponse>getNearbyPlaces(@Url String endUrl);
}
