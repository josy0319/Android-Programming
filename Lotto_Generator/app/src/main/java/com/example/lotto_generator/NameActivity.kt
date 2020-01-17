package com.example.lotto_generator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_name.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        //로또 번호 확인 버튼의 클릭이벤트 리스너 설정
        goButton.setOnClickListener {
            if(TextUtils.isEmpty(editText.text.toString()))return@setOnClickListener

            val intent = Intent(this, ResultActivity::class.java)

            //intent의 결과 데이터를 전달한다
            //전달하는 리스트는 이름의 해시코드로 생성한 로또 번호
            intent.putIntegerArrayListExtra("result", ArrayList(LottoNumberMaker.getLottoNumbersFromHash(editText.text.toString())))

            //입력받은 이름을 추가로 전달
            intent.putExtra("name",editText.text.toString())

            //ResultActivity를 시작하는 인텐트를 만들고 startActivity로 실행
            startActivity(intent)
        }
        backButton.setOnClickListener {
            finish()
        }
    }
}
