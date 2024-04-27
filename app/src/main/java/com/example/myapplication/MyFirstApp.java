package com.example.myapplication;
import com.microsoft.CloudServices;
public class MyFirstApp extends android.app.Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        CloudServices.initialize(this);
    }
}
