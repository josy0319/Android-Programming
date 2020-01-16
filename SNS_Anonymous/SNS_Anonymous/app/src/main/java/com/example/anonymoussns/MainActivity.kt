package com.example.anonymoussns

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anonymoussns.model.Post
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_post.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val posts: MutableList<Post> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        //글 목록을 저장하는 변수

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "글 목록"

        //하단의 floatingACtionButton이 클릭될때의 리스너를 설정한다.
        floatingActionButton.setOnClickListener {
            // Intent 생성
            val intent = Intent(this, WriteActivity::class.java)

            // Intent 로 WirteActivity 실행
            startActivity(intent)
        }

        //RecyclerView에 LayoutManager 설정
        val layoutManager = LinearLayoutManager(this@MainActivity)
        //RecyclerView의 아이템을 역순으로 정렬하게 함
        layoutManager.reverseLayout = true
        //RecyclerView의 아이템을 쌓는 순서를 끝부터 쌓게 함
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

        //Firebase에서 Post 데이터를 가져온 후 posts변수에 저장
        FirebaseDatabase.getInstance().getReference("/Posts")
            .orderByChild("writeTime").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    p0?.let { p0 ->
                        //snapshot의 데이터를 post객체로 가져옴
                        val post = p0.getValue(Post::class.java)
                        post?.let {
                            //새 글이 마지막 부분에 추가된 경우
                            if (p1 == null) {
                                //글 목록을 저장하는 변수에 post 객체 추가
                                posts.add(it)
                                //RecyclerView의 adapter에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                //글이 중간에 삽입된 경우 p1로 한단계 앞의 데이터의 위치를 찾은 뒤 데이터를 추가한다.
                                val prevIndex = posts.map { it.postId }.indexOf(p1)
                                posts.add(prevIndex + 1, post)
                                //RecyclerView의 apapter에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                //글이 변경된 경우
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    p0?.let { p0 ->
                        //p0의 데이터를 post 객체로 가져옴
                        val post = p0.getValue(Post::class.java)
                        post?.let { post ->
                            //글이 변경된 경우 글의 앞의 데이터 인덱스에 데이터를 변경한다.
                            val prevIndex = posts.map { it.postId }.indexOf(p1)
                            posts[prevIndex + 1] = post
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                //글의 순서가 이동한 경우
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    p0?.let {
                        //p0의 데이터를 post객체로 가져옴
                        val post = p0.getValue(Post::class.java)
                        post?.let { post ->
                            //기존의 인덱스를 구한다
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            //기존의 데이터를 지운다
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                            //p1가 없는 경우 맨 마지막으로 이동 된 것
                            if (p1 == null) {
                                posts.add(post)
                                recyclerView.adapter?.notifyItemChanged(posts.size - 1)
                            } else {
                                //p1 다음 글로 추가
                                val prevIndex = posts.map { it.postId }.indexOf(p1)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }

                //글이 삭제된 경우
                override fun onChildRemoved(p0: DataSnapshot) {
                    p0?.let {
                        //p0의 데이터를 post객체로 가져옴
                        val post = p0.getValue(Post::class.java)
                        post?.let { post ->
                            //기존에 저장된 인덱스를 찾아서 해당 인덱스의 데이터를 삭제한다
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    //취소가 된 경우 에러를 로그로 보여준다
                    p0?.toException()?.printStackTrace()
                }
            })
    }
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //글의 배경 이미지 뷰
        val imageView : ImageView = itemView.imageView
        //글의 내용 텍스트뷰
        val contentsText : TextView = itemView.contentsText
        //글쓴 시간 텍스트뷰
        val timeTextView : TextView = itemView.timeTextView
        //댓글 개수 텍스트뷰
        val commentCountText : TextView = itemView.commentCountText
    }

    //RecayclerView의 어댑터 클래스
    inner class MyAdapter: RecyclerView.Adapter<MyViewHolder>(){
        //RecyclerView에서 각 행에서 그릴 ViewHolder를 생성할떄 불리는 메소드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.card_post,parent,false))
        }
        //RecyclerView에서 몇개의 행을 그릴지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return posts.size
        }
        //각 행의 포지션에서 그려야 할 ViewHolder UI에 데이터를 적용하는 메소드
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
           val post = posts[position]
            //배경 이미지 설정
            Picasso.get().load(Uri.parse(post.bgUri)).fit().centerCrop().into(holder.imageView)
            //카드에 글을 세팅
            holder.contentsText.text = post.message
            //글이 쓰여진 시간
            holder.timeTextView.text = getDiffTimeText(post.writeTime as Long)
            //댓글 개수는 현재 상태에서는 0으로 일단 세팅
            holder.commentCountText.text = post.commentCount.toString()
            //카드가 클릭되는 경우 DetailActivity를 실행
            holder.itemView.setOnClickListener{
                //상세화면을 호출할 intent를 생성한다
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                //선택된 카득의 ID정보를 intent에 추가한다
                intent.putExtra("postId",post.postId)
                //intent로 상세화면을 시작한다
                intent.putExtra("commentCount",post.commentCount)
                intent.putExtra("message",post.message)

                startActivity(intent)
            }
        }
    }
    //글이 쓰여진 시간을 "방금전","~시간전", "yyyy년 MM월 dd일 HH:mm" 포맷으로 반환해주는 메소드
    fun getDiffTimeText(targetTime : Long): String{
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime,curDateTime).minutes
        if(diffDay == 0){
            if(diffHours ==0 && diffMinutes ==0){
                return "방금 전"
            }
            return if (diffHours > 0){
                "" + diffHours + "시간 전"
            } else "" + diffMinutes + "분 전"
        }else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime))
        }
    }
}
