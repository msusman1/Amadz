package com.talsk.amadz.di

import com.talsk.amadz.data.ContactPhotoProviderImpl
import com.talsk.amadz.data.BlockedNumberRepositoryImpl
import com.talsk.amadz.core.DefaultCallAdapter
import com.talsk.amadz.core.DefaultDtmfToneGenerator
import com.talsk.amadz.core.DefaultNotificationController
import com.talsk.amadz.data.SimInfoProviderImpl
import com.talsk.amadz.data.CallLogRepositoryImpl
import com.talsk.amadz.data.ContactDetailProviderImpl
import com.talsk.amadz.data.ContactsRepositoryImpl
import com.talsk.amadz.data.DefaultRingToneController
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.RingToneController
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactDetailProvider
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.domain.repo.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindRingtoneController(
        defaultRingToneController: DefaultRingToneController
    ): RingToneController

    @Binds
    @Singleton
    abstract fun bindNotificationController(
        notificationController: DefaultNotificationController
    ): NotificationController

    @Binds
    @Singleton
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
    @Singleton
    abstract fun bindToneGenerator(
        impl: DefaultDtmfToneGenerator
    ): DtmfToneGenerator

    @Binds
    @Singleton
    abstract fun bindContactPhotoProvider(
        impl: ContactPhotoProviderImpl
    ): ContactPhotoProvider

    @Binds
    @Singleton
    abstract fun bindContactDetailProvider(
        impl: ContactDetailProviderImpl
    ): ContactDetailProvider

    @Binds
    @Singleton
    abstract fun bindSimInfoProvider(
        impl: SimInfoProviderImpl
    ): SimInfoProvider

    @Binds
    @Singleton
    abstract fun bindBlockedNumberRepository(
        impl: BlockedNumberRepositoryImpl
    ): BlockedNumberRepository
}
