package org.ww.ai;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResult;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;
import kotlinx.coroutines.flow.Flow;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /*
        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show());
        */
        installKeyBoardAutoHide();

        AppDatabase appDatabase = AppDatabase.getInstance(this);
        Log.d("DATABASE", "entries: " + appDatabase.renderResultDao());
        if(appDatabase.renderResultDao() != null) {
            Flow<List<RenderResult>> listFlow = appDatabase.renderResultDao().getAll();
            Log.d("FLOW", listFlow.toString());
        }


    }

    private void installKeyBoardAutoHide() {
        EditText editText = (EditText) findViewById(R.id.editTextTextMultiLine);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if(v.getId() == R.id.editTextTextMultiLine && !hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
    }

}