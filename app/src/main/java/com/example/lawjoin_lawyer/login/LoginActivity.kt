package com.example.lawjoin_lawyer.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.lawjoin.MainActivity
import com.example.lawjoin.R
import com.example.lawjoin.data.model.ChatRoom
import com.example.lawjoin.data.model.User
import com.example.lawjoin.data.repository.ChatRoomRepository
import com.example.lawjoin.data.repository.UserRepository
import com.example.lawjoin.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient

@RequiresApi(Build.VERSION_CODES.O)
class LoginActivity: AppCompatActivity() {
    private var chatRoomRepository = ChatRoomRepository.getInstance()
    private var userRepository = UserRepository.getInstance()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAuth()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val googleBtn = binding.btnGoogleLogin.getChildAt(0) as TextView
        googleBtn.text = "구글 계정으로 로그인하기"
        binding.btnGoogleLogin.setOnClickListener {
            loginGoogle()
        }

        binding.btnKakaoLogin.setOnClickListener {
            loginKakao()
        }

        binding.btnStartLogin.setOnClickListener {
            loginEmail()
        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        setContentView(binding.root)
    }

    private fun checkAuth() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        return@accessTokenInfo
                    }
                    else {
                        Log.e(TAG, "카카오 로그인 토큰 에러", error)
                    }
                }
                else {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun loginEmail() {
        if (binding.edtLoginEmail.text.toString().isBlank() &&
            binding.edtLoginPassword.toString().isBlank())
            Toast.makeText(this, "아이디 또는 패스워드를 입력해주세요", Toast.LENGTH_SHORT).show()
        else {
            auth.signInWithEmailAndPassword(
                    binding.edtLoginEmail.text.toString(), binding.edtLoginPassword.text.toString()
                )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("로그인", "성공")
                        val currentUser = auth.currentUser!!
                        userRepository.saveUser(currentUser.uid) {
                            it.setValue(currentUser)
                        }
                        addChatRoom(currentUser.uid)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loginKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                saveUser()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    saveUser()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun saveUser() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                userRepository.saveUser(user.id.toString()) {
                    val successUser = User(
                        user.id.toString(), user.kakaoAccount?.email, user.kakaoAccount?.name,
                        user.kakaoAccount?.profile.toString()
                    )
                    addChatRoom(user.id.toString())
                    it.setValue(successUser)
                }
            }
        }
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    Log.d(TAG, "signInWithCredential:success")
                    val currentUser = User(
                        user.uid, user.email, user.displayName,
                        user.photoUrl.toString()
                    )
                    userRepository.saveUser(user.uid) {
                        it.setValue(currentUser)
                    }
                    addChatRoom(user.uid)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun loginGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun addChatRoom(uid: String) {
        val chatRoomBot = ChatRoom(mapOf(), listOf(uid,"BOT"))
        val chatRoomGpt = ChatRoom(mapOf(), listOf(uid,"GPT"))

        chatRoomRepository.findUserInitChatRoom(uid, "BOT", "GPT") {
            if (!it.exists()) {
                chatRoomRepository.saveChatRoomUnder(uid) { reference ->
                    reference.child("BOT")
                        .setValue(chatRoomBot)

                    reference.child("GPT")
                        .setValue(chatRoomGpt)
                }
                Log.d(TAG, "BOT, GPT 채팅방 저장 성공")
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}