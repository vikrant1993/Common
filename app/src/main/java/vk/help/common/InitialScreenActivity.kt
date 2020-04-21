package vk.help.common

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.temp.*
import vk.help.AdapterView
import vk.help.Common
import vk.help.MasterActivity
import vk.help.MasterAdapter
import vk.help.imagepicker.features.ImagePicker
import vk.help.imagepicker.model.Image
import java.io.File

class InitialScreenActivity : MasterActivity() {

    private lateinit var galleryAdapter: MasterAdapter
    private lateinit var pageAdapter: MasterAdapter
    private val tempGalleryList = ArrayList<Image>()
    private var selectPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp)

        setSupportActionBar(toolbar)

        galleryAdapter = MasterAdapter(recyclerView, null, object : AdapterView {
            override fun createChildView(
                parent: ViewGroup,
                position: Int
            ): RecyclerView.ViewHolder {
                return TempGallery(
                    layoutInflater.inflate(
                        R.layout.view_holder_for_temp_gallery,
                        parent,
                        false
                    )
                )
            }

            override fun getChildView(holder: RecyclerView.ViewHolder, position: Int) {
                (holder as TempGallery).setData(position)
            }
        })

        pageAdapter = MasterAdapter(viewPager, null, object : AdapterView {
            override fun createChildView(
                parent: ViewGroup,
                position: Int
            ): RecyclerView.ViewHolder {
                return FullGallery(
                    layoutInflater.inflate(
                        R.layout.view_holder_for_full_temp_gallery,
                        parent,
                        false
                    )
                )
            }

            override fun getChildView(holder: RecyclerView.ViewHolder, position: Int) {
                (holder as FullGallery).setData(tempGalleryList[position])
            }
        })

        viewPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    selectPosition =
                        (viewPager.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    galleryAdapter.notifyDataSetChanged()
                }
            }
        })

        PagerSnapHelper().attachToRecyclerView(viewPager)

        ImagePicker.create(this).theme(R.style.AppTheme).limit(10).showCamera(true).start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_camera, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_ok) {
            val resultsIntent = Intent()
            resultsIntent.putExtra(DATA, getJSON(tempGalleryList))
            setResult(RESULT_OK, resultsIntent)
            finish()
        } else if (id == R.id.action_crop) {
            val option = UCrop.Options()
            val destinationUri = Uri.fromFile(
                File(
                    Environment.getExternalStorageDirectory().absolutePath + "/" + System.currentTimeMillis() + "_" + Common.nameFromURL(
                        tempGalleryList[selectPosition].path
                    )
                )
            )
            UCrop.of(
                Uri.fromFile(File(tempGalleryList[selectPosition].path)),
                destinationUri
            ).withAspectRatio(1f, 1f)
                .withOptions(option).start(this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            tempGalleryList.clear()
            val images = ImagePicker.getImages(data)
            if (images.isNotEmpty()) {
                tempGalleryList.addAll(images)
            }
            galleryAdapter.setData(tempGalleryList)
            pageAdapter.setData(tempGalleryList)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri: Uri = UCrop.getOutput(data!!)!!
            tempGalleryList[selectPosition].path = resultUri.path
            galleryAdapter.notifyDataSetChanged()
            pageAdapter.notifyDataSetChanged()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)!!
            cropError.printStackTrace()
            showErrorToast(cropError.message!!)
        }
    }

    private inner class TempGallery(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<AppCompatImageView>(R.id.imageView)
        private val mainLayout = itemView.findViewById<ConstraintLayout>(R.id.mainLayout)

        fun setData(position: Int) {
            if (position == selectPosition) {
                mainLayout.setBackgroundColor(Color.RED)
            } else {
                mainLayout.setBackgroundColor(Color.WHITE)
            }
            Glide.with(context).load(tempGalleryList[position].path).into(imageView)

            imageView.setOnClickListener {
                selectPosition = position
                galleryAdapter.notifyDataSetChanged()
                viewPager.smoothScrollToPosition(position)
            }
        }
    }

    private inner class FullGallery(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<AppCompatImageView>(R.id.imageView)

        fun setData(data: Image) {
            Glide.with(context).load(data.path).into(imageView)
        }
    }
}