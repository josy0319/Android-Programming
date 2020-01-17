package com.example.lotto_generator

import java.text.SimpleDateFormat
import java.util.*

object LottoNumberMaker{
    //랜덤으로 추출하여 6개의 로또 번호를 만드는 함수
    fun getRandomLottoNumbers(): MutableList<Int> {
        val lottoNumbers = mutableListOf<Int>()

        for(i in 1..6){
            var number = 0
            do{
                number = getRandomLottoNumber()
                //lottoNumbers에 number 변수의 값이 없을 때까지 반복
            }while(lottoNumbers.contains(number))

            //중복이 없는 상태에서 추출된 번호를 리스트에 추가
            lottoNumbers.add(number)
        }
        return lottoNumbers
    }

    //랜덤으로 1~45 번호 중 하나의 번호를 생성하는 함수
    fun getRandomLottoNumber() : Int{
        return Random().nextInt(45) + 1
    }
    //셔플을 사용해 로또 번호 생성
    fun getShuffleLottsNumbers(): MutableList<Int> {
        val list = mutableListOf<Int>()
        for(number in 1..45){
            list.add(number)
        }
        list.shuffle()
        return list.subList(0,6)
    }
    //입력받은 이름에 대한 해시코드를 사용하여 로또번호를 섞고 결과를 반환
    fun getLottoNumbersFromHash(str:String):MutableList<Int>{
        val list = mutableListOf<Int>()
        for(number in 1..45){
            list.add(number)
        }
        val targetString = SimpleDateFormat("yyyy-MM-dd",Locale.KOREA).format(Date()) + str

        list.shuffle(Random(targetString.hashCode().toLong()))

        return list.subList(0,6)
    }
}