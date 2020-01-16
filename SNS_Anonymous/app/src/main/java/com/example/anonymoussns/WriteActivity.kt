package com.example.anonymoussns

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anonymoussns.model.Comment
import com.example.anonymoussns.model.Post
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_write.*
import kotlinx.android.synthetic.main.card_background.view.*
import kotlinx.android.synthetic.main.card_post.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class WriteActivity : AppCompatActivity() {
    // 현재 선택된 배경이미지의 포지션을 저장하는 변수
    var currentBgPosition = 0
    
    val bgList = mutableListOf(
        "android.resource://com.example.anonymoussns/drawable/default_bg"
        , "android.resource://com.example.anonymoussns/drawable/bg2"
        , "android.resource://com.example.anonymoussns/drawable/bg3"
        , "android.resource://com.example.anonymoussns/drawable/bg4"
        , "android.resource://com.example.anonymoussns/drawable/bg5"
        , "android.resource://com.example.anonymoussns/drawable/bg6"
        , "android.resource://com.example.anonymoussns/drawable/bg7"
        , "android.resource://com.example.anonymoussns/drawable/bg8"
        , "android.resource://com.example.anonymoussns/drawable/bg9"
    )

    // 글쓰기 모드를 저장하는 변수
    var mode = "post"

    // 댓글쓰기인 경우 글의 ID
    var postId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        //전달받은 intent에서 댓글모드인지 확인
        intent.getStringExtra("mode")?.let {
            mode = intent.getStringExtra("mode")
            postId = intent.getStringExtra("postId")
        }

        // actionbar 의 타이틀을 "글쓰기" 로 변경
        supportActionBar?.title = if (mode == "post") "글쓰기" else "댓글쓰기"

        // recyclerView 에서 사용할 레이아웃 매니저를 생성한다.
        val layoutManager = LinearLayoutManager(this@WriteActivity)
        // recyclerView 를 횡으로 스크롤 할것이므로 layoutManager 의 방향을 HORIZONTAL 로 설정한다.
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        // recyclerView 에 레이아웃 매니저를 방금 생성한 layoutManger 로 설정한다.
        recyclerView.layoutManager = layoutManager
        // recyclerView 에 adapter 를 설정한다.
        recyclerView.adapter = MyAdapter()


        // 공유하기 버튼이 클릭된 경우에 이벤트리스너를 설정한다.
        sendButton.setOnClickListener {
            //메세지가 없는 경우 토스트 메세지로 알림
            if (TextUtils.isEmpty(input.text)) {
                Toast.makeText(applicationContext, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mode == "post") {
                //Post객체 생성
                val post = Post()
                //Firebase의 Posts 참조에서 객체를 저장하기 위한 새로운 키를 생성하고 참조를 newRef에 저장
                val newRef = FirebaseDatabase.getInstance().getReference("Posts").push()
                //글이 쓰여진 시간은 Firebase 서버의 시간으로 설정
                post.writeTime = ServerValue.TIMESTAMP
                //배경 Uri주소를 현재 선택된 배경의 주소로 할당
                post.bgUri = bgList[currentBgPosition]
                //메세지는 input EditText의 텍스트 내용을 할당
                post.message = input.text.toString()
                //글쓴 사람의 ID는 디바이스의 아이디로 할당
                post.writerId = getMyId()
                //글의 ID는 새로 생성된 파이어베이스 참조의 키로 할당
                post.postId = newRef.key.toString()
                post.commentCount = 0
                //post객체를 새로 생성한 참조에 저장
                newRef.setValue(post)

                Toast.makeText(applicationContext, "공유되었습니다.", Toast.LENGTH_SHORT).show()
                finish()

            } else {
                val comment = Comment()
                //Firebase의 Posts참조에서 객체를 저장하기 위한 새로운 키를 생성하고 참조를 newRef에 저장
                val newRef = FirebaseDatabase.getInstance().getReference("Comments/$postId").push()
                val postRef = FirebaseDatabase.getInstance().getReference("Posts/$postId")
                comment.writeTime = ServerValue.TIMESTAMP
                comment.bgUri = bgList[currentBgPosition]
                //메세지는 input EditText의 텍스트 내용을 할당
                comment.message = input.text.toString()
                //글쓴 사람의 id는 디바이스의 아이디로 할당
                comment.writerId = getMyId()
                //글의 Id는 새로 생성된 파이어베이스 참조의 키로 할당
                comment.commentId = newRef.key.toString()
                //댓글이 속한 글의 ID
                comment.postId = postId
                comment.commentCount = getCommentCount()

                val map: MutableMap<String, Any> = mutableMapOf()
                map.put("commentCount", getCommentCount())

                postRef.updateChildren(map)

                newRef.setValue(comment)
                Toast.makeText(applicationContext, "공유되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
        //디바이스의 아이디를 가져오는 함수
        fun getMyId(): String {
            return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun getCommentCount(): Int {
            var comment = Comment()
            comment.commentCount = comment.commentCount + 1
            return comment.commentCount
        }


        //RecyclerView 에서 사용하는 View 홀더 클래스

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = itemView.imageView
        }


        //RecyclerView 의 어댑터 클래스

        inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {

            //RecyclerView 에서 각 행에서 그릴 ViewHolder 를 생성할때 불리는 메소드

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                // RecyclerView 에서 사용하는 ViewHolder 클래스를 card_background 레이아웃 리소스 파일을 사용하도록 생성한다.
                return MyViewHolder(
                    LayoutInflater.from(this@WriteActivity).inflate(
                        R.layout.card_background,
                        parent,
                        false
                    )
                )
            }


           // RecyclerView 에서 몇개의 행을 그릴지 기준이 되는 메소드

            override fun getItemCount(): Int {
                return bgList.size
            }


            //각 행의 포지션에서 그려야할 ViewHolder UI 에 데이터를 적용하는 메소드

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                // 이미지 로딩 라이브러리인 피카소 객체로 뷰홀더에 존재하는 imageView 에 이미지 로딩
                Picasso.get()
                    .load(Uri.parse(bgList[position]))
                    .fit()
                    .centerCrop()
                    .into(holder.imageView)
                // 각 배경화면 행이 클릭된 경우에 이벤트 리스너 설정
                holder.itemView.setOnClickListener {
                    // 선택된 배경의 포지션을 currentBgPosition 에 저장
                    currentBgPosition = position
                    // 이미지 로딩 라이브러리인 피카소 객체로 뷰홀더에 존재하는 글쓰기 배경 이미지뷰에 이미지 로딩
                    Picasso.get()
                        .load(Uri.parse(bgList[position]))
                        .fit()
                        .centerCrop()
                        .into(writeBackground)
                }
            }
        }
    }
