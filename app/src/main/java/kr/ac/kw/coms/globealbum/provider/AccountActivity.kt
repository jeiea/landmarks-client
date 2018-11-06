package kr.ac.kw.coms.globealbum.provider

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kr.ac.kw.coms.globealbum.MainActivity
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.common.LifeScope
import kr.ac.kw.coms.globealbum.common.app
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onKey
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*


class AccountActivity : AppCompatActivity(), CoroutineScope {
  private val life = SupervisorJob()
  override val coroutineContext = Dispatchers.Main.immediate + life

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_account)

    loadInitialFragment(
      when {
        RemoteJava.client.profile != null -> ProfileFragment()
        else -> LoadingFragment()
      }
    )
    bindBackButton()
  }

  private fun loadInitialFragment(frag: Fragment) {
    supportFragmentManager
      .beginTransaction()
      .add(R.id.cl_fragment_main, frag)
      .commit()
  }

  private fun bindBackButton() {
    tv_top_back.onClick { onBackPressed() }
    supportFragmentManager.addOnBackStackChangedListener(this::onFragmentChanged)
    onFragmentChanged()
  }

  private fun onFragmentChanged() {
    val isBackstackExists = supportFragmentManager.backStackEntryCount > 0
    tv_top_back.visibility = if (isBackstackExists) View.VISIBLE else View.GONE
  }
}

class LoadingFragment : Fragment() {

  private val scope = LifeScope(this)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_splash, container, false)
  }

  override fun onStart() {
    super.onStart()
    scope.launch {
      postLoginTransition()
    }
  }

  private suspend fun postLoginTransition() {
    val ac = activity!!
    val loginFrag = LoginFragment()
    ac.supportFragmentManager
      .beginTransaction()
      .add(R.id.cl_fragment_main, loginFrag)
      .hide(loginFrag)
      .commit()
    delay(1000)
    ac.supportFragmentManager
      .beginTransaction()
      .remove(this)
      .show(loginFrag)
      .commit()
  }
}

class LoginFragment : Fragment(), CoroutineScope {
  private val life = SupervisorJob()
  override val coroutineContext = Dispatchers.Main.immediate + life

  private lateinit var animation: RotatingAnimationTask

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_login, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    animation = RotatingAnimationTask(activity!!, iv_loading)
    tryAutoLogin()

    btn_login.onClick { loginByUI() }
    et_password.onKey { _, keyCode, event ->
      if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        loginByUI()
      }
    }
    btn_account.setOnClickListener {
      val frag = SignUpFragment()
      activity!!.supportFragmentManager
        .beginTransaction()
        .add(R.id.cl_fragment_main, frag)
        .show(frag)
        .addToBackStack(null)
        .commit()
    }
    cb_remember_id.onCheckedChange { _, checked ->
      if (!checked) {
        app.login = null
      }
    }
    btn_forgot.onClick {
      toast("미구현 기능입니다")
    }
    app.login?.also {
      cb_remember_id.isChecked = true
      et_login.setText(it)
    }
    app.password?.also {
      cb_auto_login.isChecked = true
      et_password.setText(it)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    life.cancel()
  }

  private fun tryAutoLogin() {
    val id = app.login
    val pw = app.password
    if (id != null && pw != null) {
      loginSolely(id, pw)
    }
  }

  private fun loginByUI() {
    animation.hideKeyboard()

    val id = et_login.text.toString()
    val pass = et_password.text.toString()
    if (!id.isBlank() && !pass.isBlank()) {
      loginSolely(id, pass)
    }
  }

  private fun loginSolely(id: String, pass: String) = animation.withRotation(this) {
    RemoteJava.client.login(id, pass)
    saveAutoLoginInfo(id, pass)

    val intent = Intent(context, MainActivity::class.java)
    startActivity(intent)
    activity?.finish()
  }

  private fun saveAutoLoginInfo(id: String, pass: String) {
    val autologin = cb_auto_login?.isChecked ?: true
    val rememberId = cb_remember_id?.isChecked ?: true
    app.login = if (autologin || rememberId) id else null
    app.password = if (autologin) pass else null
  }
}

class RotatingAnimationTask(private val activity: Activity, private val ivLoading: View) {
  private val mutex = Mutex()
  private var animation: Job? = null

  fun hideKeyboard() {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    activity.currentFocus?.windowToken?.also { imm.hideSoftInputFromWindow(it, 0) }
  }

  fun withRotation(scope: CoroutineScope, block: suspend () -> Unit) = scope.launch {
    mutex.withLock {
      animation?.cancelAndJoin()
      animation = coroutineContext[Job]
    }

    val animation = launch { rotateLoading() }
    try {
      block()
    }
    catch (e: CancellationException) {
    }
    catch (e: Throwable) {
      activity.toast("$e")
    }
    finally {
      animation.cancelAndJoin()
    }
  }

  private suspend fun rotateLoading() {
    try {
      ivLoading.visibility = View.VISIBLE
      while (true) {
        ivLoading.rotation += 17f
        delay(1000 / 30)
      }
    }
    finally {
      ivLoading.visibility = View.GONE
    }
  }
}

class SignUpFragment : Fragment(), CoroutineScope {
  private val life = SupervisorJob()
  override val coroutineContext = Dispatchers.Main.immediate + life

  private lateinit var animation: RotatingAnimationTask

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_sign_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    animation = RotatingAnimationTask(activity!!, iv_sign_up_loading)
    btn_sign_up.onClick {
      val login = et_sign_up_login.text.toString()
      val pass = et_sign_up_password.text.toString()
      val email = et_sign_up_email.text.toString()
      val nick = et_sign_up_nickname.text.toString()
      register(login, pass, email, nick)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    life.cancel()
  }

  private fun register(ident: String, pass: String, email: String, nick: String) =
    animation.withRotation(this) {
      RemoteJava.client.register(ident, pass, email, nick)
      activity?.supportFragmentManager?.popBackStack()
    }
}

class ProfileFragment : Fragment(), CoroutineScope {
  private val life = SupervisorJob()
  override val coroutineContext = Dispatchers.Main.immediate + life
  private val extraChannel = Channel<String>(1)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_profile, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tv_profile_top_back.onClick {
      activity?.onBackPressed()
    }
    btn_profile_logout.onClick {
      app.password = null
      activity!!.supportFragmentManager
        .beginTransaction()
        .replace(R.id.cl_fragment_main, LoadingFragment())
        .commit()
    }
    tv_profile_nick.text = RemoteJava.client.profile?.data?.nick
    tv_profile_extra.text = "..."
    launch { tv_profile_extra.text = extraChannel.receive() }
    requestProfile()
  }

  private fun requestProfile() = launch(Dispatchers.IO) {
    val profile = RemoteJava.client.getProfile()
    val numDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
    val date = numDate.format(profile.registered)
    val extra = "Joined: ${date}\n" +
      "Number of pictures: ${profile.pictureCount}\n" +
      "Number of collections: ${profile.collectionCount}"
    extraChannel.offer(extra)
  }

  override fun onDestroy() {
    super.onDestroy()
    life.cancel()
  }
}