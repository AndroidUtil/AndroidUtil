/*
 Copyright © 2015, 2016 Jenly Yu <a href="mailto:jenly1314@gmail.com">Jenly</a>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 	http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package com.androidutil.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class DisplayUtils {

    private DisplayUtils(){
        throw new AssertionError();
    }

    /**
     * dip转px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue){
        final float scale = getDisplayMetrics(context).density;
        return (int)(dipValue * scale + 0.5f);
    }

    /**
     * px转dip
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue){
        final float scale = getDisplayMetrics(context).density;
        return (int)(pxValue / scale + 0.5f);
    }

    /**
     * 获取显示区域 DisplayMetrics
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        return context.getResources().getDisplayMetrics();
    }

    /**
     * 获取显示区域的宽 (不包括虚拟导航栏)
     * @param context
     * @return
     */
    public static int getDisplayWidth(Context context){
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 获取显示区域的高  (不包括虚拟导航栏)
     * @param context
     * @return
     */
    public static int getDisplayHeight(Context context){
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * 获取实际显示区域 DisplayMetrics
     * @param context
     * @return
     */
    public static DisplayMetrics getRealMetrics(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay()
                .getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 获取实际显示区域的宽
     * @param context
     * @return
     */
    public static int getRealWidth(Context context){
        return getRealMetrics(context).widthPixels;
    }

    /**
     * 获取实际显示区域的宽
     * @param context
     * @return
     */
    public static int getRealHeight(Context context){
        return getRealMetrics(context).heightPixels;
    }

}
