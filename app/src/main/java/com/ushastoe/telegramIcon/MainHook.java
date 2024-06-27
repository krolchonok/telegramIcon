package com.ushastoe.telegramIcon;

import android.content.res.XModuleResources;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class MainHook implements IXposedHookInitPackageResources,IXposedHookZygoteInit {
    private XModuleResources moduleResources = null;

    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.moduleResources = XModuleResources.createInstance(startupParam.modulePath, null);
    }

    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        List<String> list = Arrays.asList("org.telegram.messenger.web", "tw.nekomimi.nekogram");
        if (list.contains(resparam != null ? resparam.packageName : null)) {
            XposedBridge.log("Injecting Solar icons...");

            Class<?> drawablesJavaClassSelf = R.drawable.class;
            Field[] drawablesSelf = drawablesJavaClassSelf.getDeclaredFields();

            int totalAttempts = 0;
            int successfulReplacements = 0;
            for (Field field : drawablesSelf) {
                int resourceIdSelf;
                try {
                    resourceIdSelf = field.getInt(drawablesJavaClassSelf);
                } catch (Exception e) {
                    continue;
                }
                try {
                    if (field.getName().contains("solar")) {
                        totalAttempts++;
                        resparam.res.setReplacement(resparam.packageName,
                                "drawable",
                                field.getName().replace("_solar", ""),
                                moduleResources.fwd(resourceIdSelf));
                        successfulReplacements++;
                    }
                } catch (Exception e) {
                    XposedBridge.log(e.toString());
                }
            }
            XposedBridge.log("Succeeded " + successfulReplacements + ", failed " + (totalAttempts - successfulReplacements));
        }

    }
}
