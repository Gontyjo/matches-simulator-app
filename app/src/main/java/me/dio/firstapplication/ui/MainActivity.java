package me.dio.firstapplication.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.dio.firstapplication.R;
import me.dio.firstapplication.data.MatchesAPI;
import me.dio.firstapplication.databinding.ActivityMainBinding;
import me.dio.firstapplication.domain.Match;
import me.dio.firstapplication.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bindind;
    private MatchesAPI matchesApi;
    private MatchesAdapter matchesAdapter = new MatchesAdapter(Collections.emptyList());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindind.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gontyjo.github.io/matches-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        matchesApi = retrofit.create(MatchesAPI.class);
    }

    private void setupMatchesList() {
        bindind.rvMatches.setHasFixedSize(true);
        bindind.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        bindind.rvMatches.setAdapter(matchesAdapter);

        findMatchesFromApi();
    }

    private void setupMatchesRefresh() {
        bindind.srlMatches.setOnRefreshListener(this::findMatchesFromApi);
    }

    private void setupFloatingActionButton() {
        bindind.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
            //throw new RuntimeException("Teste Crashlytics");
        });
    }


    private void findMatchesFromApi() {
        bindind.srlMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(@NonNull Call<List<Match>> call, @NonNull Response<List<Match>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    bindind.rvMatches.setAdapter(matchesAdapter);
                } else {
                    showErrorMessage();
                }
                bindind.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                showErrorMessage();
                bindind.srlMatches.setRefreshing(false);
            }
        });
    }

    private void showErrorMessage() {
        Snackbar.make(bindind.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}
