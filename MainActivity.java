package adept.tech.iroyin;

import adept.tech.iroyin.Adapter.ListSourceAdapter;
import adept.tech.iroyin.Common.Common;
import adept.tech.iroyin.Interface.NewsService;
import adept.tech.iroyin.Model.Website;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;

import com.google.gson.Gson;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    RecyclerView listWebsite;
    RecyclerView.LayoutManager layoutManager;
    NewsService mService;
    ListSourceAdapter adapter;
    AlertDialog dialog;
    SwipeRefreshLayout swipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init cache
        Paper.init(this);

        //Init Service
        mService = Common.getNewsService();

        //Init View
        swipeLayout = findViewById(R.id.swipe);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWebsiteSource(true);
            }
        });

        listWebsite = findViewById(R.id.listSource);
        listWebsite.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listWebsite.setLayoutManager(layoutManager);

        dialog = new SpotsDialog(this);

        loadWebsiteSource(false);
    }

    private void loadWebsiteSource(boolean isRefreshed) {

        if(!isRefreshed)
        {

            String cache = Paper.book().read("cache");
                 if(cache != null && !cache.isEmpty() && !cache.equals("null")) // If have cache
                {
                    Website website = new Gson().fromJson(cache,Website.class); // Convert cache from Json to Object
                    adapter = new ListSourceAdapter(getBaseContext(),website);
                    adapter.notifyDataSetChanged();
                    listWebsite.setAdapter(adapter);
                }
                else // If not have cache
                {
                    dialog.show();
                    //Fetch new data
                    mService.getSources().enqueue(new Callback<Website>() {
                        @Override
                        public void onResponse(Call<Website> call, Response<Website> response) {
                            adapter  = new ListSourceAdapter(getBaseContext(),response.body());
                            adapter.notifyDataSetChanged();
                            listWebsite.setAdapter(adapter);

                            //Save to cache
                            Paper.book().write("cache",new Gson().toJson(response.body()));

                            dialog.dismiss();

                        }

                        @Override
                        public void onFailure(Call<Website> call, Throwable t) {

                        }
                    });
                }

        }
        else // If from Swipe to Refresh
        {

            swipeLayout.setRefreshing(true);
            //Fetch new data
            mService.getSources().enqueue(new Callback<Website>() {
                @Override
                public void onResponse(Call<Website> call, Response<Website> response) {
                    adapter  = new ListSourceAdapter(getBaseContext(),response.body());
                    adapter.notifyDataSetChanged();
                    listWebsite.setAdapter(adapter);

                    //Save to cache
                    Paper.book().write("cache",new Gson().toJson(response.body()));

                    //Dismiss refresh progressring
                    swipeLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<Website> call, Throwable t) {

                }
            });

        }
    }
}
