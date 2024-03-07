package com.example.cheap.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.cheap.R
import com.example.cheap.models.Record

class CustomListAdapter(context: Context, private val data: List<Record>) :
    ArrayAdapter<Record>(context, R.layout.custom_list_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.custom_list_item, parent, false)
            holder = ViewHolder()
            holder.imageView = itemView.findViewById(R.id.imageView)
            holder.descriptionTextView = itemView.findViewById(R.id.descriptionTextView)
            holder.priceTextView = itemView.findViewById(R.id.priceTextView)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val record = data[position]

        // Convert Base64 encoded image data to Bitmap
        val imageData = Base64.decode(record.item?.imageData, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        holder.imageView.setImageBitmap(bitmap)

        holder.descriptionTextView.text = "Description: ${record.item?.description}"
        holder.priceTextView.text = "Price: ${record.item?.cost}"

        return itemView!!
    }

    private class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var descriptionTextView: TextView
        lateinit var priceTextView: TextView
    }
}