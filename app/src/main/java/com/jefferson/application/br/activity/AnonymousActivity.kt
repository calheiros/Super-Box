package com.jefferson.application.br.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.rotationMatrix
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.jefferson.application.br.R

class AnonymousActivity : MyCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anonymous)
        val recyclerView : RecyclerView = findViewById(R.id.my_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter  = MyAdapter(this)
    }

    class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val root = LayoutInflater.from(context)
                .inflate(R.layout.item_preference_switch, parent, false)
            return Holder(root)
        }

        override fun getItemCount(): Int {
            return 1
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.iconView.setImageResource(R.drawable.ic_calculator_variant)
            holder.textView.text = "Calculator"
        }

        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView
            val iconView : ImageView

            init {
                textView = itemView.findViewById(R.id.pref_title_label)
                iconView = itemView.findViewById(R.id.pref_icon_view)
            }
        }
    }
}