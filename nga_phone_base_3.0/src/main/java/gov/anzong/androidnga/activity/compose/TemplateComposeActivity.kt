package gov.anzong.androidnga.activity.compose

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.justwen.androidnga.ui.compose.BaseComposeFragment

class TemplateComposeActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentStr = intent.getStringExtra("fragment")
        if (fragmentStr != null) {
            commitFragment(fragmentStr)
        }
    }

    private fun commitFragment(fragmentStr: String) {
        try {
            val fragment :BaseComposeFragment = Class.forName(fragmentStr).getDeclaredConstructor().newInstance() as BaseComposeFragment
            fragment.arguments = intent.extras
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
        } catch (e: InstantiationException) {
            Log.e("TemplateComposeActivity", "InstantiationException for $fragmentStr", e)
        } catch (e: IllegalAccessException) {
            Log.e("TemplateComposeActivity", "IllegalAccessException for $fragmentStr", e)
        } catch (e: ClassNotFoundException) {
            Log.e("TemplateComposeActivity", "ClassNotFoundException for $fragmentStr", e)
        } catch (e: Throwable) {
            Log.e("TemplateComposeActivity", "Unexpected exception for $fragmentStr", e)
        }
    }

}