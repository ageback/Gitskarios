package com.alorma.github.ui.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.ui.activity.base.BackActivity;
import com.alorma.github.ui.fragment.ProfileFragment;

/**
 * Created by Bernat on 15/07/2014.
 */
public class ProfileActivity extends BackActivity {

    public static Intent createLauncherIntent(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    public static Intent createLauncherIntent(Context context, String username) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ProfileFragment.USERNAME, username);
        return intent;
    }
    public static Intent createIntentFilterLauncherActivity(Context context, String username) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ProfileFragment.USERNAME, username);
        intent.putExtra(ProfileFragment.FROM_INTENT_FILTER, true);
        return intent;
    }

    public static Intent createLauncherIntent(Context context, User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ProfileFragment.USER, user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(ProfileFragment.USER)) {
                User user = getIntent().getParcelableExtra(ProfileFragment.USER);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, ProfileFragment.newInstance(user));
                ft.commit();
            } else if (getIntent().getExtras().containsKey(ProfileFragment.USERNAME)) {
                String username = getIntent().getStringExtra(ProfileFragment.USERNAME);
                boolean fromIntentFilter = getIntent().getBooleanExtra(ProfileFragment.FROM_INTENT_FILTER, false);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, ProfileFragment.newInstance(username, fromIntentFilter));
                ft.commit();
            } else {
                finish();
            }
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(android.R.id.content, ProfileFragment.newInstance());
            ft.commit();
        }
    }
}