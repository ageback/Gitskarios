package com.alorma.github.ui.activity.repo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.alorma.github.R;
import com.alorma.github.injector.component.ApiComponent;
import com.alorma.github.injector.component.ApplicationComponent;
import com.alorma.github.injector.component.DaggerApiComponent;
import com.alorma.github.injector.module.ApiModule;
import com.alorma.github.injector.module.repository.RepoDetailModule;
import com.alorma.github.presenter.RepositoryPresenter;
import com.alorma.github.sdk.bean.dto.response.UserType;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.ui.activity.ForksActivity;
import com.alorma.github.ui.activity.OrganizationActivity;
import com.alorma.github.ui.activity.ProfileActivity;
import com.alorma.github.ui.fragment.base.BaseFragment;
import com.alorma.github.ui.fragment.detail.repo.BackManager;
import com.alorma.github.ui.fragment.detail.repo.BranchManager;
import com.alorma.github.ui.view.UserAvatarView;
import com.alorma.github.utils.AttributesUtils;
import com.alorma.github.utils.TimeUtils;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.varunest.sparkbutton.SparkButton;
import core.User;
import core.repositories.Repo;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.inject.Inject;

public class RepoAboutFragment extends BaseFragment implements BranchManager, BackManager, com.alorma.github.presenter.View<Repo> {

  private static final int EDIT_REPO = 464;
  private static final String REPO_INFO = "REPO_INFO";

  @Inject RepositoryPresenter presenter;

  private RepoInfo repoInfo;
  private Repo currentRepo;
  private UserAvatarView profileIcon;

  private SparkButton starredPlaceholder;
  private TextView starredTextView;

  private SparkButton watchedPlaceholder;
  private TextView watchedTextView;

  private SparkButton forkedPlaceholder;
  private TextView forkedTextView;

  private TextView authorName;
  private View fork;
  private TextView forkOfTextView;
  private TextView createdAtTextView;

  public static RepoAboutFragment newInstance(RepoInfo repoInfo) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(REPO_INFO, repoInfo);

    RepoAboutFragment f = new RepoAboutFragment();
    f.setArguments(bundle);
    return f;
  }

  @Override
  protected int getLightTheme() {
    return R.style.AppTheme_Repository;
  }

  @Override
  protected int getDarkTheme() {
    return R.style.AppTheme_Dark_Repository;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
    presenter.attachView(this);
  }

  @Override
  public void onDestroy() {
    presenter.detachView();
    super.onDestroy();
  }

  @Override
  protected void injectComponents(ApplicationComponent applicationComponent) {
    super.injectComponents(applicationComponent);
    ApiComponent apiComponent = DaggerApiComponent.builder().applicationComponent(applicationComponent).apiModule(new ApiModule()).build();
    apiComponent.plus(new RepoDetailModule()).inject(this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    return inflater.inflate(R.layout.repo_overview_fragment, null, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View author = view.findViewById(R.id.author);
    profileIcon = (UserAvatarView) author.findViewById(R.id.profileIcon);
    authorName = (TextView) author.findViewById(R.id.authorName);

    fork = view.findViewById(R.id.fork);
    forkOfTextView = (TextView) fork.findViewById(R.id.forkOf);

    createdAtTextView = (TextView) view.findViewById(R.id.createdAt);

    starredPlaceholder = (SparkButton) view.findViewById(R.id.starredPlaceholder);
    starredTextView = (TextView) view.findViewById(R.id.starredTextView);

    watchedPlaceholder = (SparkButton) view.findViewById(R.id.watchedPlaceHolder);
    watchedTextView = (TextView) view.findViewById(R.id.watchedTextView);

    forkedPlaceholder = (SparkButton) view.findViewById(R.id.forkedPlaceholder);
    forkedTextView = (TextView) view.findViewById(R.id.forkedTextView);

    starredPlaceholder.setEventListener((button, buttonState) -> changeStarStatus());
    watchedPlaceholder.setEventListener((button, buttonState) -> changeWatchedStatus());
    starredTextView.setOnClickListener(v -> changeStarStatus());
    watchedTextView.setOnClickListener(v -> changeWatchedStatus());

    forkedTextView.setOnClickListener(v -> {
      if (repoInfo != null) {
        Intent intent = ForksActivity.launchIntent(v.getContext(), repoInfo);
        startActivity(intent);
      }
    });

    fork.setOnClickListener(v -> {
      if (currentRepo != null && currentRepo.parent != null) {
        RepoInfo repoInfo1 = new RepoInfo();
        repoInfo1.owner = currentRepo.parent.owner.getLogin();
        repoInfo1.name = currentRepo.parent.name;
        if (!TextUtils.isEmpty(currentRepo.getDefaultBranch())) {
          repoInfo1.branch = currentRepo.getDefaultBranch();
        }

        Intent intent = RepoDetailActivity.createLauncherIntent(getActivity(), repoInfo1);
        startActivity(intent);
      }
    });

    author.setOnClickListener(v -> {
      if (currentRepo != null && currentRepo.owner != null) {
        if (currentRepo.owner.getType().equals(UserType.User.name())) {
          Intent intent = ProfileActivity.createLauncherIntent(getActivity(), currentRepo.owner);
          startActivity(intent);
        } else if (currentRepo.owner.getType().equals(UserType.Organization.name())) {
          Intent intent = OrganizationActivity.launchIntent(getActivity(), currentRepo.owner.getLogin());
          startActivity(intent);
        }
      }
    });

    loadArguments();
    presenter.execute(repoInfo);
  }

  protected void loadArguments() {
    if (getArguments() != null) {
      repoInfo = (RepoInfo) getArguments().getParcelable(REPO_INFO);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    /*
    getMenuInflater().inflate(R.menu.repo_detail_activity, menu);
     */
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    /*
    if (menu != null) {
      if (currentRepo != null && currentRepo.permissions != null) {
        if (currentRepo.permissions.admin) {
          if (menu.findItem(R.id.action_manage_repo) == null) {
            getMenuInflater().inflate(R.menu.repo_detail_activity_permissions, menu);
          }
          if (menu.findItem(R.id.action_subscribe_push) == null) {
            getMenuInflater().inflate(R.menu.repo_detail_activity_push, menu);
          }
        }
      }

      MenuItem item = menu.findItem(R.id.share_repo);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        item.setIcon(getResources().getDrawable(R.drawable.ic_menu_share_mtrl_alpha, getTheme()));
      } else {
        item.setIcon(getResources().getDrawable(R.drawable.ic_menu_share_mtrl_alpha));
      }

      MenuItem menuChangeBranch = menu.findItem(R.id.action_repo_change_branch);

      if (menuChangeBranch != null) {
        if (currentRepo != null && currentRepo.branches != null && currentRepo.branches.size() > 1) {
          Drawable changeBranch = new IconicsDrawable(this, Octicons.Icon.oct_git_branch).actionBar().colorRes(R.color.white);

          menuChangeBranch.setIcon(changeBranch);
        } else {
          menu.removeItem(R.id.action_repo_change_branch);
        }
      }
    }
     */
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    /*

    if (item.getItemId() == android.R.id.home) {
      finish();
    } else if (item.getItemId() == R.id.share_repo) {
      if (currentRepo != null) {
        String title = currentRepo.getFullName();
        String url = currentRepo.svn_url;

        new ShareAction(this, title, url).setType("Repository").execute();
      }
    } else if (item.getItemId() == R.id.action_open_in_browser) {
      if (currentRepo != null) {
        new ViewInAction(this, currentRepo.getHtmlUrl()).setType("Repository").execute();
      }
    } else if (item.getItemId() == R.id.action_repo_change_branch) {
      changeBranch();
    } else if (item.getItemId() == R.id.action_manage_repo) {
      if (currentRepo != null) {
        Intent intent = ManageRepositoryActivity.createIntent(this, requestRepoInfo, createRepoRequest());
        startActivityForResult(intent, EDIT_REPO);
      }
    } else if (item.getItemId() == R.id.action_add_shortcut) {
      ShortcutUtils.addShortcut(this, requestRepoInfo);
    } else if (item.getItemId() == R.id.action_subscribe_push) {
      WebHookRequest webhook = new WebHookRequest();
      webhook.name = "web";
      webhook.active = true;
      webhook.events = new String[] {
          "issues"
      };
      webhook.config = new WebHookConfigRequest();
      webhook.config.content_type = "json";
      webhook.config.url = "https://cryptic-ravine-97684.herokuapp.com/message";

      new AddWebHookClient(requestRepoInfo.owner, requestRepoInfo.name, webhook).observable()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(webHookResponse -> {
            GcmTopicsHelper.registerInTopic(requestRepoInfo);
          }, throwable -> {

          });
    }
     */
    return super.onOptionsItemSelected(item);
  }

  /*
  private RepoRequestDTO createRepoRequest() {
    RepoRequestDTO dto = new RepoRequestDTO();

    dto.isPrivate = currentRepo.isPrivateRepo();
    dto.name = currentRepo.name;
    dto.description = currentRepo.description;
    dto.default_branch = currentRepo.getDefaultBranch();
    dto.has_downloads = currentRepo.hasDownloads;
    dto.has_wiki = currentRepo.hasWiki;
    dto.has_issues = currentRepo.hasIssues;
    dto.homepage = currentRepo.homepage;

    return dto;
  }

  private void changeBranch() {
    GetRepoBranchesClient repoBranchesClient = new GetRepoBranchesClient(requestRepoInfo);
    Observable<List<Branch>> apiObservable = repoBranchesClient.observable()
        .subscribeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(branches -> {
          if (currentRepo != null) {
            if (currentRepo.branches != null) {
              currentRepo.branches.addAll(branches);
            } else {
              currentRepo.branches = branches;
            }
            CacheWrapper.setRepository(currentRepo);
          }
        });

    Observable<List<Branch>> memCacheObservable = Observable.create(new Observable.OnSubscribe<List<Branch>>() {
      @Override
      public void call(Subscriber<? super List<Branch>> subscriber) {
        try {
          if (!subscriber.isUnsubscribed()) {
            if (currentRepo != null && currentRepo.branches != null) {
              subscriber.onNext(currentRepo.branches);
            }
          }
          subscriber.onCompleted();
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    });

    Observable.concat(memCacheObservable, apiObservable).first().subscribe(new DialogBranchesSubscriber(this, requestRepoInfo) {
      @Override
      protected void onNoBranches() {

      }

      @Override
      protected void onBranchSelected(String branch) {
        requestRepoInfo.branch = branch;
        if (currentRepo != null) {
          currentRepo.setDefaultBranch(branch);
        }
        if (getSupportActionBar() != null) {
          getSupportActionBar().setSubtitle(branch);
        }
        for (Fragment fragment : fragments) {
          if (fragment instanceof BranchManager) {
            ((BranchManager) fragment).setCurrentBranch(branch);
          }
        }
      }
    });
  }
   */

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    /*

    if (requestCode == EDIT_REPO) {
      if (resultCode == RESULT_OK && data != null) {
        RepoRequestDTO repoRequestDTO = data.getParcelableExtra(ManageRepositoryActivity.CONTENT);
        showProgressDialog(R.string.edit_repo_loading);
        EditRepoClient editRepositoryClient = new EditRepoClient(requestRepoInfo, repoRequestDTO);
        editRepositoryClient.observable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Repo>() {
              @Override
              public void onCompleted() {

              }

              @Override
              public void onError(Throwable e) {

              }

              @Override
              public void onNext(Repo repo) {
                onDataReceived(repo, false);
              }
            });
      } else if (resultCode == RESULT_CANCELED) {
        finish();
      }
    }
     */
  }

  private void setData() {
    if (getActivity() != null) {
      if (this.currentRepo != null) {
        User owner = this.currentRepo.owner;
        profileIcon.setUser(owner);
        authorName.setText(owner.getLogin());

        forkedPlaceholder.setChecked(this.currentRepo.parent != null);

        if (this.currentRepo.parent != null) {
          fork.setVisibility(View.VISIBLE);
          forkOfTextView.setCompoundDrawables(getIcon(Octicons.Icon.oct_repo_forked, 24), null, null, null);
          forkOfTextView.setText(String.format("%s/%s", this.currentRepo.parent.owner.getLogin(), this.currentRepo.parent.name));
        }

        createdAtTextView.setCompoundDrawables(getIcon(Octicons.Icon.oct_clock, 24), null, null, null);
        createdAtTextView.setText(TimeUtils.getDateToText(getActivity(), this.currentRepo.getCreatedAt(), R.string.created_at));

        changeStarView();
        changeWatchView();

        setStarsCount(currentRepo.getStargazersCount());

        setWatchersCount(currentRepo.getSubscribersCount());

        forkedTextView.setText(String.valueOf(placeHolderNum(this.currentRepo.forks_count)));
      }
    }
  }

  private void setStarsCount(int stargazers_count) {
    starredTextView.setText(String.valueOf(placeHolderNum(stargazers_count)));
  }

  private void setWatchersCount(int subscribers_count) {
    watchedTextView.setText(String.valueOf(placeHolderNum(subscribers_count)));
  }

  private String placeHolderNum(int value) {
    NumberFormat decimalFormat = new DecimalFormat();
    return decimalFormat.format(value);
  }

  private IconicsDrawable getIcon(IIcon icon, int sizeDp) {
    return new IconicsDrawable(getActivity(), icon).color(AttributesUtils.getAccentColor(getActivity())).sizeDp(sizeDp);
  }

  @Override
  public void setCurrentBranch(String branch) {
    if (getActivity() != null) {
      repoInfo.branch = branch;
    }
  }

  @Override
  public boolean onBackPressed() {
    return true;
  }

  private void changeStarStatus() {
    //presenter.changeStarredState();
  }

  private void changeWatchedStatus() {
    //presenter.changeWatchedState();
  }

  private void changeStarView() {
    if (getActivity() != null) {
      starredPlaceholder.setChecked(!currentRepo.isStarred());

      starredPlaceholder.invalidate();
    }
  }

  private void changeWatchView() {
    if (getActivity() != null) {
      watchedPlaceholder.setChecked(!currentRepo.isStarred());

      watchedPlaceholder.invalidate();
    }
  }

  @Override
  public void showLoading() {

  }

  @Override
  public void hideLoading() {

  }

  @Override
  public void onDataReceived(Repo data, boolean isFromPaginated) {
    this.currentRepo = data;
    if (isAdded()) {
      setData();
    }
  }

  @Override
  public void showError(Throwable throwable) {

  }
}
