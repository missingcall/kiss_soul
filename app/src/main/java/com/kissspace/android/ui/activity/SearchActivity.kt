package com.kissspace.android.ui.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.android.R
import com.kissspace.android.databinding.AppActivitySearchBinding
import com.kissspace.android.ui.fragment.SearchRoomFragment
import com.kissspace.android.ui.fragment.SearchUserFragment
import com.kissspace.android.viewmodel.SearchViewModel
import com.kissspace.common.ext.safeClick
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.common.widget.tablayout.CustomTabLayoutBean
import com.kissspace.util.isNotEmpty
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.logE
import com.kissspace.util.trimString
import kotlin.text.isNotEmpty

/**
 * @Author gaohangbo
 * @Date 2023/1/16 18:49.
 * @Describe 搜索用户和房间页面
 */
@Router(path = RouterPath.PATH_SEARCH)
class SearchActivity : com.kissspace.common.base.BaseActivity(R.layout.app_activity_search) {
    private val mBinding by viewBinding<AppActivitySearchBinding>()
    private val mViewModel by viewModels<SearchViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        addTab()
        initHistorySearch()
        mBinding.tvCancel.safeClick {
            finish()
        }
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@SearchActivity) {
                override fun getItemCount(): Int = 2
                override fun createFragment(position: Int): Fragment =
                    if (position == 0) {
                        SearchRoomFragment.newInstance(0)
                    } else {
                        SearchUserFragment.newInstance(1)
                    }

                override fun getItemId(position: Int): Long = position.toLong()

                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    //缓存fragment
                    recyclerView.setItemViewCacheSize(1)
                }
            }
        }
        mBinding.commonSearchView.onEditorActionBlock={
            searchContent(it,true)
        }
        getSearchData(mViewModel.viewModelScope,mBinding.commonSearchView.mBinding.etSearch){
            if (it.isNotEmpty()) {
                searchData(it.toString())
            } else {
                mBinding.tvSearchHistory.visibility = View.VISIBLE
                mBinding.flowlayout.visibility = View.VISIBLE
                mBinding.ivClear.visibility = View.VISIBLE
                mBinding.customTabLayout.visibility = View.INVISIBLE
                mBinding.viewPager.visibility = View.INVISIBLE
            }
        }

        mBinding.customTabLayout.visibility = View.INVISIBLE

        mBinding.viewPager.visibility = View.INVISIBLE

        mBinding.commonSearchView.mBinding.cancel.safeClick {
            finish()
        }

        mBinding.ivClear.safeClick {
            CommonConfirmDialog(this@SearchActivity, "是否清空所有搜索记录？") {
                if (this) {
                    MMKVProvider.searchContent = ""
                    mBinding.flowlayout.removeAllViewsInLayout()
                }
            }.show()
        }

    }

    private fun searchContent(searchContent: String,saveHistory: Boolean) {
        if (saveHistory) {
            saveHistory()
        }
        searchData(searchContent)
    }

    private fun searchData(content: String?) {
        mBinding.customTabLayout.visibility = View.VISIBLE
        mBinding.tvSearchHistory.visibility = View.GONE
        mBinding.flowlayout.visibility = View.GONE
        mBinding.ivClear.visibility = View.GONE
        val fragment = supportFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
        if(fragment!=null&&!fragment.isDetached){
            if(fragment is SearchRoomFragment){
                fragment.searchContent(content, true)
            }else if(fragment is SearchUserFragment){
                fragment.searchContent(content, true)
            }
        }
        mBinding.viewPager.visibility = View.VISIBLE
    }


    private fun addTab() {
        mBinding.customTabLayout.setOnTabChangedListener {
            mBinding.viewPager.currentItem = it
        }
        mBinding.customTabLayout.setViewPager(mBinding.viewPager)
        val test1 = CustomTabLayoutBean(
            "房间",
            0,
            R.mipmap.app_search_choose_room,
            true
        )

        val test2 = CustomTabLayoutBean(
            "用户",
            1,
            R.mipmap.app_search_choose_user,
            false
        )
        mBinding.customTabLayout.addTabItem(test1)
        mBinding.customTabLayout.addTabItem(test2)
    }

    fun getSearchContent(): String {
        return mBinding.commonSearchView.mBinding.etSearch.text.trimString()
    }


    fun saveHistory() {
        mBinding.commonSearchView.mBinding.etSearch.text?.let {
                var searchContent = MMKVProvider.searchContent
                logE("searchContent$searchContent")
                searchContent=
                    StringBuilder(searchContent).append(":$it").toString()
                val historyList =
                    searchContent.split(":").filter { content -> content.isNotEmptyBlank() }
                        .toMutableList()
                filterHistoryList(searchContent,historyList).let { content ->
                    MMKVProvider.searchContent = content
                }
                logE("添加后----" + MMKVProvider.searchContent)
            }
    }
    private fun initHistorySearch() {
        val searchContent: String = MMKVProvider.searchContent
        val historyList = searchContent.split(":").filter { text -> text.isNotEmpty() }
            .toMutableList()
        filterHistoryList(searchContent,historyList).let {
            MMKVProvider.searchContent = it
        }
    }

    private fun filterHistoryList(searchContent:String,historyList: MutableList<String>): String {
        var text=searchContent
        val filterHistoryList = historyList.distinct().toMutableList()
        if (filterHistoryList.size >= 10) {
            //移除最近一个
            filterHistoryList.removeFirst()
            text= text.substring(text.indexOfFirst { it.toString() == ":" },text.length)
        }
        //重新添加view
        setHistoryList(filterHistoryList)
        return text
    }


    private fun setHistoryList(historyList: MutableList<String>) {
        addTextViewList(historyList)
    }

    private fun addTextViewList(historyList: MutableList<String>) {
        val layout = mBinding.flowlayout
        layout.removeAllViewsInLayout()
        for (word in historyList) {
            val textView = LayoutInflater.from(layout.context)
                .inflate(
                    R.layout.app_item_search_layout,
                    layout,
                    false
                ) as TextView
            textView.text = word

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            textView.safeClick {
                searchData(textView.text.toString())
                mBinding.commonSearchView.mBinding.etSearch.setText(textView.text.toString())
                mBinding.commonSearchView.mBinding.etSearch.setSelection(mBinding.commonSearchView.mBinding.etSearch.text.toString().length)
            }
            layout.addView(textView, 0)
        }
    }

}