# KotlinMultiFlavorError
Sample project for a Kotlin build error when using flavors and instrumentation tests

## Reproduction Scenario

1. Checkout
2. Run the instrumentation tests

## Failure

Running the Tests fails with the following Stacktrace
```
03-20 16:02:43.639    3573-3573/? E/AndroidRuntime﹕ FATAL EXCEPTION: main
    Process: at.tschillo.kotlinmultiflavorerror, PID: 3573
    java.lang.IllegalAccessError: Class ref in pre-verified class resolved to unexpected implementation
            at at.tschillo.kotlinmultiflavorerror.App.onCreate(App.kt:8)
            at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1007)
            at android.app.ActivityThread.handleBindApplication(ActivityThread.java:4476)
            at android.app.ActivityThread.access$1500(ActivityThread.java:145)
            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1266)
            at android.os.Handler.dispatchMessage(Handler.java:102)
            at android.os.Looper.loop(Looper.java:136)
            at android.app.ActivityThread.main(ActivityThread.java:5149)
            at java.lang.reflect.Method.invokeNative(Native Method)
            at java.lang.reflect.Method.invoke(Method.java:515)
            at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:789)
            at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:605)
            at dalvik.system.NativeStart.main(Native Method)
```

Here are the relevant lines from LogCat
```
03-20 15:46:22.171  27205-27205/? W/dalvikvm﹕ Class resolved by unexpected DEX: Lat/tschillo/kotlinmultiflavorerror/App;(0x418ca440):0x754d4000 ref [Lat/tschillo/kotlinmultiflavorerror/SomeClass;] Lat/tschillo/kotlinmultiflavorerror/SomeClass;(0x418ca440):0x75158000
03-20 15:46:22.171  27205-27205/? W/dalvikvm﹕ (Lat/tschillo/kotlinmultiflavorerror/App; had used a different Lat/tschillo/kotlinmultiflavorerror/SomeClass; during pre-verification)
```

## Suspected Reason

I suspect the kotlin plugin compiles the code under directory `src/flavor1` to both the regular apk and the androidTest apk. When I look at the `app/build/intermediates/classes` directory, the file `SomeClass.class` is present in the directory `app/build/intermediates/classes/androidTest`, as well as in `app/build/intermediates/classes/flavor1`. Note that `SomeJavaClass.class` is only present in `app/build/intermediates/classes/flavor1`, which is probalby the correct and desired behaviour. 

## Possible Workaround

I found a workaround for this problem by disabling the Kotlin compilation tasks for the `androidTest` apk. This has the severe disadvantage that you can't use any kotlin code in instrumentation tests.

Just add this to your build.gradle file and do a clean build:
```
task disableKotlin << {
    def allKotlin = project.tasks.findAll {
        it.name.endsWith('TestKotlin')
    }
    println '##############################################################'
    allKotlin.each { task ->
        println sources
        println 'Disabling task: ' + task.name
        task.onlyIf { false }
    }
    println '##############################################################'
}

preBuild.dependsOn disableKotlin
```