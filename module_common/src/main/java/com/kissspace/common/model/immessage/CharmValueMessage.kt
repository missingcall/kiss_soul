package com.kissspace.common.model.immessage

data class CharmValueMessage(var microphoneMessageList: List<CharmValueBean>)
data class CharmValueBean(var microphoneNumber: Int, var charmValue: Long)