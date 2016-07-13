package com.movie.flickster;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements PosterFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    String mFilterType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.fragment_movie_detail) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                Log.d(LOG_TAG, "In onCreate, state is null");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie_detail, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
            Uri contentUri = getIntent().getData();
            if(null != contentUri) {
                onItemSelected(contentUri);
            }
        } else {
            mTwoPane = false;
        }
        Log.d(LOG_TAG, "In onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String filterType = Utility.getPreferredFilterType(this);
        if (filterType != null && !filterType.equals(mFilterType)) {
            PosterFragment pf = (PosterFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_movie_posters);
            if ( null != pf ) {
                pf.onFilterTypeChanged();
            }
            mFilterType = filterType;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie_detail, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

}
