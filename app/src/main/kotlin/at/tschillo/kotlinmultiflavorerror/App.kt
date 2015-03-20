package at.tschillo.kotlinmultiflavorerror

import android.app.Application

class App : Application() {

    override fun onCreate() {
        val someResult = SomeClass.doSomething()
        val someJavaResult = SomeJavaClass.doSomethingInJava();
    }
}