package com.talsk.amadz.ui.home

import androidx.lifecycle.ViewModel
import com.talsk.amadz.domain.entity.SimInfo
import com.talsk.amadz.domain.repo.SimInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val simInfoProvider: SimInfoProvider
) : ViewModel() {

    fun getSimsInfo(): List<SimInfo> = simInfoProvider.getSimsInfo()
}
