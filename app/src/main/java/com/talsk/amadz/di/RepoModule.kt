package com.talsk.amadz.di

import com.talsk.amadz.core.CallUiEffects
import com.talsk.amadz.core.CallUiEffectsHandler
import com.talsk.amadz.core.DefaultCallOrchestrator
import com.talsk.amadz.core.DefaultDtmfToneGenerator
import com.talsk.amadz.core.DefaultNotificationController
import com.talsk.amadz.data.BlockedNumberRepositoryImpl
import com.talsk.amadz.data.CallLogRepositoryImpl
import com.talsk.amadz.data.ContactPhotoBitmapProviderImpl
import com.talsk.amadz.data.ContactsRepositoryImpl
import com.talsk.amadz.data.SimInfoProviderImpl
import com.talsk.amadz.domain.CallOrchestrator
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactPhotoBitmapProvider
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.domain.repo.SimInfoProvider
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
    abstract fun bindNotificationController(
        notificationController: DefaultNotificationController
    ): NotificationController


    @Binds
    @Singleton
    abstract fun bindCallOrchestrator(
        callOrchestrator: DefaultCallOrchestrator
    ): CallOrchestrator

    @Binds
    @Singleton
    abstract fun bindCallUiEffects(
        impl: CallUiEffectsHandler
    ): CallUiEffects

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
        impl: ContactPhotoBitmapProviderImpl
    ): ContactPhotoBitmapProvider

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
