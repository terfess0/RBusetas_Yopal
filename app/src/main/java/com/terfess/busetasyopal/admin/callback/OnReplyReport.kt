package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnReplyReport {
    fun OnSuccessReply()
    fun OnErrorReply(error: FirebaseEnums)
}