package com.example.lawjoin_lawyer.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.lawjoin_lawyer.MainActivity
import com.example.lawjoin_lawyer.common.AuthUtils
import com.example.lawjoin_lawyer.data.repository.LawyerRepository
import com.example.lawjoin_lawyer.databinding.ActivityLoginBinding
import com.example.lawjoin_lawyer.signup.SignUpLawyerActivity


//TODO: 처음 화면으로 설정
@RequiresApi(Build.VERSION_CODES.O)
class LoginActivity: AppCompatActivity() {
    private lateinit var lawyerRepository: LawyerRepository
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lawyerRepository = LawyerRepository.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)

        checkAuth()

        binding.btnStartLogin.setOnClickListener {
            startActivity(Intent(this, SignUpLawyerActivity::class.java))
        }
    }

    private fun checkAuth() {
        AuthUtils.getCurrentUser { authUserDto, _ ->
            if (authUserDto != null) {
                lawyerRepository.findLawyerById(authUserDto.uid!!) { lawyer ->
                    if (lawyer != null) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        lawyerRepository.findWaitLawyerById(authUserDto.uid!!) { waitLawyer ->
                            if (waitLawyer == null) {
                                Toast.makeText(this, "심사에서 거부되었습니다.", Toast.LENGTH_SHORT).show()
                                return@findWaitLawyerById
                            } else {
                                Toast.makeText(this, "현재 변호사 정보를 심사중입니다.", Toast.LENGTH_SHORT).show()
                                binding.btnStartLogin.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            setContentView(binding.root)
        }
    }
}