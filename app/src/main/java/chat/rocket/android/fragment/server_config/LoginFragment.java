package chat.rocket.android.fragment.server_config;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.fragment.oauth.GitHubOAuthFragment;
import chat.rocket.android.helper.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;

/**
 * Login screen.
 */
public class LoginFragment extends AbstractServerConfigFragment {
  @Override protected int getLayout() {
    return R.layout.fragment_login;
  }

  private RealmListObserver<MeteorLoginServiceConfiguration> authProvidersObserver =
      new RealmListObserver<MeteorLoginServiceConfiguration>() {
        @Override protected RealmResults<MeteorLoginServiceConfiguration> queryItems(Realm realm) {
          return realm.where(MeteorLoginServiceConfiguration.class)
              .equalTo("serverConfigId", serverConfigId)
              .findAll();
        }

        @Override protected void onCollectionChanged(List<MeteorLoginServiceConfiguration> list) {
          onRenderAuthProviders(list);
        }
      };

  @Override protected void onSetupView() {
    final View btnEmail = rootView.findViewById(R.id.btn_login_with_email);
    final TextView txtUsername = (TextView) rootView.findViewById(R.id.editor_username);
    final TextView txtPasswd = (TextView) rootView.findViewById(R.id.editor_passwd);
    btnEmail.setOnClickListener(view -> {
      final CharSequence username = txtUsername.getText();
      final CharSequence passwd = txtPasswd.getText();
      if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passwd)) {
        return;
      }
      view.setEnabled(false);

      new MethodCallHelper(serverConfigId).loginWithEmail(username.toString(), passwd.toString())
          .continueWith(task -> {
            if (task.isFaulted()) {
              showError(task.getError().getMessage());
              view.setEnabled(true);
            }
            return null;
          });
    });

    final View btnUserRegistration = rootView.findViewById(R.id.btn_user_registration);
    btnUserRegistration.setOnClickListener(view -> {
      UserRegistrationDialogFragment.create(serverConfigId,
          txtUsername.getText().toString(), txtPasswd.getText().toString())
          .show(getFragmentManager(), UserRegistrationDialogFragment.class.getSimpleName());
    });
  }

  private void showError(String errString) {
    Snackbar.make(rootView, errString, Snackbar.LENGTH_SHORT).show();
  }

  private void onRenderAuthProviders(List<MeteorLoginServiceConfiguration> authProviders) {
    final View btnTwitter = rootView.findViewById(R.id.btn_login_with_twitter);
    final View btnGitHub = rootView.findViewById(R.id.btn_login_with_github);

    boolean hasTwitter = false;
    boolean hasGitHub = false;
    for (MeteorLoginServiceConfiguration authProvider : authProviders) {
      if (!hasTwitter
          && "twitter".equals(authProvider.getService())) {
        hasTwitter = true;
        btnTwitter.setOnClickListener(view -> {

        });
      }
      if (!hasGitHub
          && "github".equals(authProvider.getService())) {
        hasGitHub = true;
        btnGitHub.setOnClickListener(view -> {
          showFragmentWithBackStack(GitHubOAuthFragment.create(serverConfigId));
        });
      }
    }

    btnTwitter.setVisibility(hasTwitter ? View.VISIBLE : View.GONE);
    btnGitHub.setVisibility(hasGitHub ? View.VISIBLE : View.GONE);
  }

  @Override public void onResume() {
    super.onResume();
    authProvidersObserver.sub();
  }

  @Override public void onPause() {
    authProvidersObserver.unsub();
    super.onPause();
  }
}
