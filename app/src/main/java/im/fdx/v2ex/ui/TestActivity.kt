package im.fdx.v2ex.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.logi
import org.jetbrains.anko.toast
import java.util.*

class TestActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)
    }
}
