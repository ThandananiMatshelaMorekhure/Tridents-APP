package student.projects.tridentsmartsolutions

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult

import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.app.Activity
import android.credentials.GetCredentialException
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.NoCredentialException


class GoogleSignInUtils {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun doGoogleSignIn(
        activity: Activity,  // ✅ CHANGED from Context to Activity
        scope: CoroutineScope,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
        loginPage: () -> Unit
    ) {
        val credentialManager = CredentialManager.create(activity)
        val credentialOption = getCredentialOptions(activity)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(credentialOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity  // ✅ Now it's an Activity
                )

                when (result.credential) {
                    is CustomCredential -> {
                        if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(result.credential.data)
                            val googleTokenId = googleIdTokenCredential.idToken
                            val authCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
                            val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                            val user = authResult.user

                            user?.let {
                                if (it.isAnonymous.not()) {
                                    loginPage.invoke()
                                }
                            }
                        }
                    }
                    else -> {
                        // Handle other credential types if needed
                    }
                }
            } catch (e: NoCredentialException) {
                launcher?.launch(getIntent())
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }
    }

    fun getIntent(): Intent {
        return Intent("android.settings.ADD_ACCOUNT_SETTINGS").apply {
            putExtra("account_types", arrayOf("com.google"))
        }
    }

    fun getCredentialOptions(context: Context): CredentialOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()
    }
}