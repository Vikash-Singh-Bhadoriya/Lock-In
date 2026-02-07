package com.vikashsinghapp.lockin.di

import android.content.Context
import com.vikashsinghapp.lockin.system.alarm.AlarmPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
// AlarmPlayer must not outlive the service
@InstallIn(SingletonComponent::class)
object AlarmPlayerModule {

    @Provides
    @Singleton
    fun provideAlarmPlayer(@ApplicationContext context: Context): AlarmPlayer {
        return AlarmPlayer(context)
    }
}