package com.alorma.github.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.info.FileInfo;
import com.alorma.github.ui.activity.base.BackActivity;
import com.alorma.github.ui.fragment.FileFragment;

/**
 * Created by Bernat on 20/07/2014.
 */
public class FileActivity extends BackActivity {


	public static Intent createLauncherIntent(Context context, FileInfo fileInfo) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(FileFragment.FILE_INFO, fileInfo);

		Intent intent = new Intent(context, FileActivity.class);
		intent.putExtras(bundle);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generic_toolbar);

		FileInfo info = getIntent().getExtras().getParcelable(FileFragment.FILE_INFO);

		FileFragment fileFragment = FileFragment.getInstance(info);
		fileFragment.setArguments(getIntent().getExtras());
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content, fileFragment);
		ft.commit();
	}
}
