package com.example.cookbook.ui.menu_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookbook.App
import com.example.cookbook.databinding.FragmentMenuBinding
import com.example.cookbook.di.menu.MenuSubcomponent
import com.example.cookbook.entity.categories.Category
import com.example.cookbook.ui.categories_fragment.CategoriesView
import com.example.cookbook.ui.main_activity.interfaces.BackButtonListener
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

const val CURRENT_CATEGORY = "current_category"

class MenuFragment : MvpAppCompatFragment(), CategoriesView, BackButtonListener {

    private lateinit var currentCategory: Category

    companion object {
        fun newInstance(bundle: Bundle): MenuFragment {
            val fragment = MenuFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _vb: FragmentMenuBinding? = null
    private val vb
        get() = _vb!!

    private var menuSubcomponent: MenuSubcomponent? = null
    private val presenter: MenuPresenter by moxyPresenter {

        menuSubcomponent = App.instance.initMenuSubcomponent()
        MenuPresenter().apply {
            menuSubcomponent?.inject(this)
        }
    }

    var adapter: MenuRVAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currentCategory = (arguments?.getParcelable(CURRENT_CATEGORY) as Category?)!!
        _vb = FragmentMenuBinding.inflate(inflater, container, false)

        return vb.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }


    override fun init() {
        vb.apply {
            menu.layoutManager = LinearLayoutManager(context)
            currentCategory.let { presenter.loadMenu(it) }
            adapter = MenuRVAdapter(presenter.menuListPresenter).apply {
                menuSubcomponent?.inject(this)
            }
            vb.menu.adapter = adapter
        }
    }


    override fun updateList() {
        adapter?.notifyDataSetChanged()
    }

    override fun release() {
        menuSubcomponent = null
        App.instance.releaseMenuSubComponent()
    }

    override fun backPressed() = presenter.backPressed()

}
