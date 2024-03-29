package com.company.ait.tobemom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.company.ait.tobemom.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    var birthname: String = "Sweety"
    var ddaymonth: Int = 0
    var ddayweek: Int = 2
    var ddayday: Int = 260

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        //아기 변경
        changeBaby()

        //태명 및 디데이 변경
        binding.homeBirthnameTv.text = birthname  //추후 수정 예정
        binding.homeDnumTv.text = ddayday.toString()  //추후 수정 예정
        binding.homeDaycountWeekcntTv.text = ddayweek.toString()  //추후 수정 예정
        binding.homeDaycountMonthcntTv.text = ddaymonth.toString()  //추후 수정 예정

//        //현재 주수에서의 아기 상태 사진
//        binding.homeBabygrowthIv.setImageResource(R.drawable.demo_babygrowth)

        //체크리스트로 이동
        goCheck()

        return view
    }

    private fun changeBaby() {
        binding.homeLogoBtn.setOnClickListener {
            val intent = Intent(activity, ChangeBabyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goCheck() {
        binding.homeGocheckBtn.setOnClickListener {
            childFragmentManager.beginTransaction().apply {
                val fragment = ChecklistFragment.newInstance() // newInstance 메서드 호출
                replace(R.id.fragment_container, fragment) // fragment_container 사용
                addToBackStack(null)
            }.commit()
        }
    }
}