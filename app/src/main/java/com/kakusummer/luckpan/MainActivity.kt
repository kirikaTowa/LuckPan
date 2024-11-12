package com.kakusummer.luckpan

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakusummer.luckpan.widget.LuckyPan
import com.kakusummer.luckpan.widget.LuckyPanAnimEndCallBack

class MainActivity : AppCompatActivity() {

    private var pan: LuckyPan? = null
    private var imgStart: ImageView? = null
    private val mItemStrs =
        arrayOf("苹果", "火龙果", "草莓", "柠檬", "黄桃", "猕猴桃", "西瓜", "葡萄")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        pan = findViewById(R.id.pan) as LuckyPan
        imgStart = findViewById(R.id.img_start) as ImageView
        pan?.setItems(mItemStrs)
        pan?.setLuckNumber(2)
        pan?.luckPanAnimEndCallBack = object : LuckyPanAnimEndCallBack {
            override fun onAnimEnd(str: String?) {
                Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
            }
        }
        imgStart?.setOnClickListener(View.OnClickListener { pan?.startAnim() })


    }
}