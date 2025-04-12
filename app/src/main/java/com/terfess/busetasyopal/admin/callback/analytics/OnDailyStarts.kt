package com.terfess.busetasyopal.admin.callback.analytics

interface OnDailyStarts {
    fun OnGetDailyStarts(dailyStarts: String)

    interface OnTotalStarts{
        fun OnGetTotalStarts(totalStarts: String)
    }
}