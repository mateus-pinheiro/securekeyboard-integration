package com.superdigital.securekeyboardpoc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboard
import com.globile.santander.mobisec.securekeyboard.watchers.NotEmptyTextWatcher





class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {


        SanKeyboardManager.setSanTapJackedCallback { event ->
            Toast.makeText(
                applicationContext,
                "You have been p0wnd",
                Toast.LENGTH_LONG
            ).show()

            false

        }

//        login_password.onFilterTouchEventForSecurity(object: SanTapJackedCallback {
//            override fun onObscuredTouchEvent(event: MotionEvent?): Boolean {}
//        })

        return super.onCreateView(parent, name, context, attrs)
    }
}
