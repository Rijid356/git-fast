package com.gitfast.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    private val TAG = "GoogleAuthManager"

    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    /**
     * Sign in with Google using the Credential Manager API.
     * Requires an activity context (not application context).
     *
     * The web client ID is auto-discovered from google-services.json via
     * the default_web_client_id string resource.
     */
    suspend fun signIn(activityContext: Context): Result<FirebaseUser> {
        return try {
            val webClientId = activityContext.getString(
                activityContext.resources.getIdentifier(
                    "default_web_client_id", "string", activityContext.packageName
                )
            )

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(activityContext, request)

            val googleIdToken = GoogleIdTokenCredential
                .createFrom(result.credential.data)
                .idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: throw IllegalStateException("Sign-in succeeded but user is null")

            Log.d(TAG, "Signed in as ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
            // Clear credential state so the account picker shows next time
            val credentialManager = CredentialManager.create(
                firebaseAuth.app.applicationContext
            )
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Signed out")
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out error", e)
        }
    }
}
