package com.ftang.catmind.ui.panel.child

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ftang.catmind.CatMind
import com.ftang.catmind.R
import com.ftang.catmind.model.ViewProperties
import kotlinx.android.synthetic.main.cat_mind_properties_layout.view.*
class CatMindPropertiesChildPanel( override val priority: Int ) : CatMindChildPanel {

    override val title = "Properties"

    private var adapter: ViewPropsAdapter? = null

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreateView(context: Context): View {
        val root = LayoutInflater.from(context).inflate(R.layout.cat_mind_properties_layout, null)

        val targetView : View? = CatMind.targetViewReference?.get()
        if (targetView != null) {
            root.view_props_list.adapter = ViewPropsAdapter(targetView).also {
                adapter = it
//                targets.addOnDrawListener(it)
            }
        }
        return root
    }

    override fun onDestroyView() {

    }

    private class ViewPropsAdapter(targetView: View) : RecyclerView.Adapter<ViewPropsHolder>() {

        private var props: List<Pair<String, Any?>> = ViewProperties(targetView).toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPropsHolder {
            val textView = TextView(parent.context)
            textView.textSize = 11f
            textView.setTextColor(Color.WHITE)
            textView.isSingleLine = false
            textView.movementMethod = LinkMovementMethod.getInstance()
            return ViewPropsHolder(textView)
        }

        override fun getItemCount(): Int = props.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewPropsHolder, position: Int) {
            val (name, value) = props[position]
            val s = SpannableStringBuilder(name)
                .append(": ")
                .append(if (value is CharSequence) value else value.toString())
            s.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.cat_mind_primary_color
                    )
                ),
                0,
                name.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.text.text = s
        }
    }

    private class ViewPropsHolder(val text: TextView) : RecyclerView.ViewHolder(text)

}