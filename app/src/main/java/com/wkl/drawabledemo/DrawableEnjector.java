package com.wkl.drawabledemo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2019-07-02
 */
public class DrawableEnjector {

    private DrawableEnjector() {
        throw new UnsupportedOperationException();
    }

    public static void enject() {
        try {
            Class<?> delegateClass = Class.forName("android.support.v7.widget.AppCompatDrawableManager$InflateDelegate");
            Object delegateIns = Proxy.newProxyInstance(DrawableEnjector.class.getClassLoader(), new Class[]{delegateClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            try {

                                Context c = (Context) args[0];
                                return LogoDrawable.createFromXmlInner(c.getResources(), (XmlPullParser) args[1],
                                        (AttributeSet) args[2], (Resources.Theme) args[3]);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
            Class<?> appcompat = Class.forName("android.support.v7.widget.AppCompatDrawableManager");
            Method get = appcompat.getMethod("get");

            Object appcompatIns = get.invoke(null);

            Method addDelegate = appcompat.getDeclaredMethod("addDelegate", String.class, delegateClass);
            addDelegate.setAccessible(true);

            addDelegate.invoke(appcompatIns, "LogoDrawable", delegateIns);
            if (Build.VERSION.SDK_INT < 24) {
                addDelegate.invoke(appcompatIns, LogoDrawable.class.getName(), delegateIns);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
