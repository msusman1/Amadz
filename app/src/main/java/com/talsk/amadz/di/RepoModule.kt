package com.talsk.amadz.di

import com.talsk.amadz.core.DefaultCallAdapter
import com.talsk.amadz.core.DefaultDtmfToneGenerator
import com.talsk.amadz.core.DefaultNotificationController
import com.talsk.amadz.data.CallLogRepositoryImpl
import com.talsk.amadz.data.ContactsRepositoryImpl
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repos.CallLogRepository
import com.talsk.amadz.domain.repos.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    abstract fun bindNotificationController(
        notificationController: DefaultNotificationController
    ): NotificationController

    @Binds
    abstract fun bindCallAdapter(
        callAdapter: DefaultCallAdapter
    ): CallAdapter

    @Binds
    abstract fun bindCallLogRepo(
        impl: CallLogRepositoryImpl
    ): CallLogRepository

    @Binds
    abstract fun bindContactRepo(
        impl: ContactsRepositoryImpl
    ): ContactRepository

    @Binds
    abstract fun bindToneGenerator(
        impl: DefaultDtmfToneGenerator
    ): DtmfToneGenerator
}
