package com.example.chafund.core.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.entity.MonthEntity
import com.example.chafund.core.data.session.Session
import com.example.chafund.core.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthManager(
    private val monthDao: MonthDao,
    private val session: Session,
    private val dispatchers: DispatcherProvider,
) : LifecycleEventObserver {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) detectAndPromote()
    }

    fun detectAndPromote() {
        scope.launch {
            val today = LocalDate.now()
            val candidate = MonthEntity(
                year = today.year,
                month = today.monthValue,
                label = DateTimeFormat.monthLabel(today.year, today.monthValue),
                isCurrent = true,
                createdAt = System.currentTimeMillis(),
            )
            val id = monthDao.upsertByYearMonth(candidate)
            monthDao.promoteToCurrent(id)
            session.setCurrentMonth(id)
        }
    }
}
