package com.petpal.swimmer_customer.ui.findinfo

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Nickname
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.internal.ViewUtils.showKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.databinding.FragmentFindInfoBinding
import com.petpal.swimmer_customer.ui.main.MainActivity
import com.petpal.swimmer_customer.data.repository.CustomerUserRepository

class FindInfoFragment : Fragment() {
    lateinit var fragmentFindInfoBinding: FragmentFindInfoBinding
    lateinit var viewModel: FindInfoViewModel
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFindInfoBinding = FragmentFindInfoBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        val factory = FindInfoViewModelFactory(CustomerUserRepository())
        viewModel = ViewModelProvider(this, factory).get(FindInfoViewModel::class.java)


        val navController = findNavController()
        NavigationUI.setupWithNavController(fragmentFindInfoBinding.toolbarFindInfo, navController)

        arguments?.let {
            when (it.getString("key")) {
                "findId" -> {
                    fragmentFindInfoBinding.toolbarFindInfo.run {
                        title = "아이디 찾기"
                        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                        setNavigationOnClickListener {
                            findNavController().popBackStack()
                        }
                    }
                    fragmentFindInfoBinding.idFindLayout.visibility = View.VISIBLE
                    fragmentFindInfoBinding.passwordResetLayout.visibility = View.GONE
                    fragmentFindInfoBinding.ButtonFindInfoToLogin.visibility = View.GONE
                    fragmentFindInfoBinding.ButtonToFindPassword.visibility = View.GONE
                    fragmentFindInfoBinding.doneImage.visibility = View.GONE
//                    if(!fragmentFindInfoBinding.ButtonFindInfoToLogin.isVisible || !fragmentFindInfoBinding.ButtonToFindPassword.isVisible)
//                    {
//                        fragmentFindInfoBinding.ButtonFindId.gravity=
//                    }
                }

                "resetPassword" -> {
                    fragmentFindInfoBinding.toolbarFindInfo.run {
                        title = "비밀번호 찾기"
                        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                        setNavigationOnClickListener {
                            findNavController().popBackStack()
                        }
                    }
                    fragmentFindInfoBinding.idFindLayout.visibility = View.GONE
                    fragmentFindInfoBinding.passwordResetLayout.visibility = View.VISIBLE
                }
            }
        }
        //비밀번호 찾으러 가기 버튼
        fragmentFindInfoBinding.ButtonToFindPassword.setOnClickListener {
            fragmentFindInfoBinding.idFindLayout.visibility = View.GONE
            fragmentFindInfoBinding.passwordResetLayout.visibility = View.VISIBLE
            fragmentFindInfoBinding.ButtonFindInfoToLogin.visibility = View.GONE
            fragmentFindInfoBinding.ButtonToFindPassword.visibility = View.GONE
        }
        //이메일 찾기 버튼
        fragmentFindInfoBinding.ButtonFindId.setOnClickListener {
            val nickname = fragmentFindInfoBinding.editTextFinIdNickname.text.toString()
            val phoneNumber = fragmentFindInfoBinding.editTextFindIdPhoneNumber.text.toString()

            if (!checkNickname(nickname) || !checkPhoneNumberId(phoneNumber)) {
                return@setOnClickListener
            }

            viewModel.findEmailByInfo(nickname, phoneNumber)?.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    fragmentFindInfoBinding.editTextFinIdNickname.text.clear()
                    fragmentFindInfoBinding.editTextFindIdPhoneNumber.text.clear()
                    fragmentFindInfoBinding.textViewFoundId.text = "당신의 이메일은 "
                    fragmentFindInfoBinding.textViewFoundId2.text = "${it.email}입니다"
                    fragmentFindInfoBinding.editTextFinIdNickname.visibility = View.GONE
                    fragmentFindInfoBinding.editTextFindIdPhoneNumber.visibility = View.GONE
                    fragmentFindInfoBinding.ButtonFindId.visibility = View.GONE
                    fragmentFindInfoBinding.ButtonFindInfoToLogin.visibility = View.VISIBLE
                    fragmentFindInfoBinding.ButtonToFindPassword.visibility = View.VISIBLE
                    fragmentFindInfoBinding.doneImage.visibility = View.VISIBLE


                } else {
                    fragmentFindInfoBinding.textViewFoundId.text = "입력한 정보로 등록된 사용자가 없습니다."
                    fragmentFindInfoBinding.editTextFinIdNickname.text.clear()
                    fragmentFindInfoBinding.editTextFindIdPhoneNumber.text.clear()
                }
            })
        }
        //로그인하러가기 버튼
        fragmentFindInfoBinding.ButtonFindInfoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
        //비밀번호 재설정 버튼
        fragmentFindInfoBinding.ButtonResetPassword.setOnClickListener {

            val email = fragmentFindInfoBinding.editTextFindPwEmail.text.toString()
            val phoneNumber = fragmentFindInfoBinding.editTextFindPwPhoneNumber.text.toString()
            viewModel.resetPassword(email, phoneNumber)?.observe(viewLifecycleOwner, Observer {
                if (it == true) {
                    // 다이얼로그 표시
                    AlertDialog.Builder(requireContext())
                        .setTitle("알림")
                        .setMessage("비밀번호 재전송 이메일을 발송했습니다.")
                        .setPositiveButton("확인") { dialog, _ ->
                            // 사용자가 '확인' 버튼을 누르면 로그인 화면으로 돌아가는 로직
                            findNavController().navigate(R.id.action_findInfoFragment_to_LoginFragment)
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    // 실패에 대한 처리. 토스트나 다른 방법을 사용하셔도 됩니다.
                    Toast.makeText(requireContext(), "비밀번호 재설정 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return fragmentFindInfoBinding.root
    }

    private fun checkNickname(nickname: String): Boolean {
        if (nickname.isEmpty()) {
            fragmentFindInfoBinding.editTextFinIdNickname.error = "닉네임을 입력해주세요."
            Handler(Looper.getMainLooper()).postDelayed({
                fragmentFindInfoBinding.editTextFinIdNickname.error = ""
                fragmentFindInfoBinding.editTextFinIdNickname.text?.clear()
                showKeyboard(fragmentFindInfoBinding.editTextFinIdNickname)
            }, 2000)
            return false
        }
        return true
    }

    private fun checkPhoneNumberId(phoneNumber: String): Boolean {
        if (phoneNumber.isEmpty()) {
            fragmentFindInfoBinding.editTextFindIdPhoneNumber.error = "전화번호를 입력해주세요."
            Handler(Looper.getMainLooper()).postDelayed({
                fragmentFindInfoBinding.editTextFindIdPhoneNumber.error = ""
                fragmentFindInfoBinding.editTextFindIdPhoneNumber.text?.clear()
                showKeyboard(fragmentFindInfoBinding.editTextFindIdPhoneNumber)
            }, 2000)
            return false
        }
        return true
    }
    private fun checkEmail(email:String):Boolean{
        if(email.isEmpty()){
            fragmentFindInfoBinding.editTextFindPwEmail.error="이메일을 입력해주세요."
        }else if(!email.contains("@")){
            fragmentFindInfoBinding.editTextFindPwEmail.error="이메일 양식이 올바르지 않습니다."
        }

        return true
    }


    fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

class FindInfoViewModelFactory(private val repository: CustomerUserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FindInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}