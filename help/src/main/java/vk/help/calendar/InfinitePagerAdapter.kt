package vk.help.calendar

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class InfinitePagerAdapter(private val adapter: PagerAdapter) : PagerAdapter() {

    private val realCount: Int
        get() = adapter.count

    override fun getCount(): Int {
        return Integer.MAX_VALUE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val virtualPosition = position % realCount
        return adapter.instantiateItem(container, virtualPosition)
    }

    override fun destroyItem(container: ViewGroup, position: Int, tempObject: Any) {
        val virtualPosition = position % realCount
        adapter.destroyItem(container, virtualPosition, tempObject)
    }

    override fun finishUpdate(container: ViewGroup) {
        adapter.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, tempObject: Any): Boolean {
        return adapter.isViewFromObject(view, tempObject)
    }

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) {
        adapter.restoreState(bundle, classLoader)
    }

    override fun saveState(): Parcelable? {
        return adapter.saveState()
    }

    override fun startUpdate(container: ViewGroup) {
        adapter.startUpdate(container)
    }
}