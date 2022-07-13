package me.dio.firstapplication.data;

import java.util.List;

import me.dio.firstapplication.domain.Match;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MatchesAPI {

    @GET("matches.json")
    Call<List<Match>> getMatches();
}
