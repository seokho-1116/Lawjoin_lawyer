package com.example.lawjoin_lawyer.signup

import com.example.lawjoin_lawyer.databinding.ActivitySignupLawyerBinding
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.lawjoin_lawyer.MainActivity
import com.example.lawjoin_lawyer.R
import com.example.lawjoin_lawyer.data.model.Lawyer
import com.example.lawjoin_lawyer.data.model.LawyerOffice
import com.example.lawjoin_lawyer.data.model.LawyerSingUpInfo
import com.example.lawjoin_lawyer.data.repository.LawyerRepository
import com.example.lawjoin_lawyer.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

@RequiresApi(Build.VERSION_CODES.O)
class SignUpLawyerActivity: AppCompatActivity() {
    private lateinit var lawyer: LawyerSingUpInfo
    private lateinit var lawyerRepository: LawyerRepository
    private lateinit var binding: ActivitySignupLawyerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        lawyer = LawyerSingUpInfo()
        lawyerRepository = LawyerRepository.getInstance()
        binding = ActivitySignupLawyerBinding.inflate(layoutInflater)
        val googleBtn = binding.btnGoogleLogin.getChildAt(0) as TextView
        googleBtn.text = "구글 계정으로 로그인하기"
        binding.btnGoogleLogin.setOnClickListener {
            loginGoogle()
            removeButton()
        }

        binding.btnKakaoLogin.setOnClickListener {
            loginKakao()
            removeButton()
        }

        binding.btnStartLogin.setOnClickListener {
            if (isNotBlankAllEditText() && binding.cbAgree.isChecked) {
                lawyer.name = binding.edtLawyerName.text.toString()
                lawyer.career = binding.edtLawyerCareer.text.toString().split("\n").toMutableList()
                lawyer.certificate = binding.edtLawyerCertificate.text.toString().split("\n").toMutableList()
                lawyer.categories = binding.edtLawyerCategory.text.toString().split("\n").toMutableList()
                lawyer.office = LawyerOffice(
                    binding.edtLawyerOfficeName.text.toString(),
                    binding.edtLawyerOfficePhone.text.toString(),
                    binding.edtLawyerOfficeOpeningTime.text.toString(), binding.edtLawyerOfficeClosingTime.text.toString(),
                    binding.edtLawyerOfficeLocation.text.toString()
                )
                lawyer.basicCounselTime = binding.edtLawyerBasicCounselTime.text.toString().toLong()
                lawyer.introduce = binding.edtLawyerIntroduce.text.toString()

               lawyerRepository.saveWaitList(lawyer) {
                   Toast.makeText(this, "변호사 정보가 제출되었습니다. 정보 확인을 위해 대기해주세요.", Toast.LENGTH_SHORT).show()
                   finish()
                }
            } else {
                Toast.makeText(this, "모든 정보가 입력되지 않았습니다. 다시 한 번 확인해주세요.",Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        setContentView(binding.root)
    }

    private fun isNotBlankAllEditText(): Boolean {
        for (edt in binding.lyUnderSv.children) {
            if (edt is EditText && edt.text.isBlank()) {
                return false
            }
        }
        return true
    }


    private fun removeButton() {
        binding.btnKakaoLogin.visibility = View.GONE
        binding.btnGoogleLogin.visibility = View.GONE
    }

    private fun loginKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                saveUser()
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
                lawyer.email = user.kakaoAccount?.email.toString()
                lawyer.uid = user.id.toString()
                lawyer.profileUrl = user.kakaoAccount?.profile.toString()
            }
        }
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

                    lawyer.uid = user.uid
                    lawyer.email = user.email.toString()
                    lawyer.profileUrl = user.photoUrl.toString()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun loginGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}