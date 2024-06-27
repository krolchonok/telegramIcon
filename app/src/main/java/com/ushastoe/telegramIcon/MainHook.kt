package com.ushastoe.telegramIcon

import android.content.res.XModuleResources
import com.ushastoe.telegramIcon.R
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class MainHook : IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private var moduleResources: XModuleResources? = null

    override fun initZygote(startupParam: StartupParam?) {
        this.moduleResources = XModuleResources.createInstance(startupParam!!.modulePath, null)
    }

    override fun handleInitPackageResources(lpparam: XC_InitPackageResources.InitPackageResourcesParam?) {
        val list = listOf("org.telegram.messenger.web", "tw.nekomimi.nekogram")
        if (lpparam?.packageName in list)
        {
            XposedBridge.log("Injecting Solar icons...")

            val drawablesJavaClassSelf = R.drawable::class.java
            val drawablesSelf = drawablesJavaClassSelf.declaredFields

            var totalAttempts = 0
            var successfulReplacements = 0
            for (field in drawablesSelf) {
                val resourceIdSelf: Int
                try {
                    resourceIdSelf = field.getInt(drawablesJavaClassSelf)
                } catch (e: Exception) {
                    continue
                }
                try {
                    if (field.name.contains("solar")) {
                        totalAttempts++
                        lpparam?.res?.setReplacement(lpparam.packageName,
                            "drawable",
                            field.name.replace("_solar", ""),
                            moduleResources!!.fwd(resourceIdSelf))
                        successfulReplacements++
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            XposedBridge.log("Succeeded $successfulReplacements, failed ${totalAttempts - successfulReplacements}")
        }
    }
}