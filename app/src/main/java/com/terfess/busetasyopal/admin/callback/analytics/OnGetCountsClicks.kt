package com.terfess.busetasyopal.admin.callback.analytics

interface OnGetCountsClicks {
    interface OnGetRouteMaxClicks{
        fun OnSuccessMax(route: String)
        fun OnErrorGetStat(errorType: String)
    }
    interface OnGetRouteMinClicks{
        fun OnSuccessMin(route: String)
        fun OnErrorGetStatMin(errorType: String)
    }
    interface OnGetTotalClicks{
        fun OnSuccessTotal(clicks: Int)
        fun OnErrorGetTotalStat(errorType: String)
    }
}