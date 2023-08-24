package com.kissspace.android.viewmodel

import com.kissspace.android.http.Api
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.model.search.SearchModel
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.logE

class SearchViewModel : BaseViewModel() {
    fun searchContent(content: String?, pageNum: Int, block: ((SearchModel) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        logE("content$content")
        param["keyword"] = content
        param["pageNum"] = pageNum
        param["pageSize"] = Constants.PageSize
        request<SearchModel>(Api.API_SEARCH_CONTENT, Method.GET, param, onSuccess = {
            block?.invoke(it)
        }, onError = {
            customToast(it.message)
        }
        )
    }

}