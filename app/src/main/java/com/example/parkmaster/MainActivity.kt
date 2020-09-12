package com.example.parkmaster

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GameCanvas.GameCanvasListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResult(msg: String?): Int {

        runOnUiThread(Thread(Runnable {
            try {
                result.text = msg
                result.visibility = View.VISIBLE
                val timer = object: CountDownTimer(1000, 500) {
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        result.visibility = View.INVISIBLE
                    }
                }
                timer.start()

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }))

        return 0
    }
}