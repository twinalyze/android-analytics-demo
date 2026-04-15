package com.twinalyze.servicedemo.util

import android.app.Activity

fun getActivityFullName(activity: Activity?=null,fragment: androidx.fragment.app.Fragment?=null) : String{
    return if(fragment != null){
        fragment.javaClass.name ?: ""
    }else{
        activity?.javaClass?.name ?: ""
    }
}