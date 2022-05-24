package de.lemke.nakbuch.domain.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import de.lemke.nakbuch.R

class ViewPagerAdapterImageView(context: Context, private val images: List<Uri>) :
    PagerAdapter() {
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.img, container, false)
        //PhotoView imageView = itemView.findViewById(R.id.imageView);
        //imageView.setImageURI(mImages.get(position));
        val imageView: SubsamplingScaleImageView = itemView.findViewById(R.id.imageView)
        imageView.setImage(ImageSource.uri(images[position]))
        imageView.maxScale = imageView.maxScale * 3
        //imageView.setOnLongClickListener();
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}