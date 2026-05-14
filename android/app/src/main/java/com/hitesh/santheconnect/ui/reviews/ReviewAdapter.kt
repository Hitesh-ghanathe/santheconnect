package com.hitesh.santheconnect.ui.reviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.hitesh.santheconnect.R
import com.hitesh.santheconnect.data.model.Review
import com.hitesh.santheconnect.databinding.ItemReviewCardBinding
import com.hitesh.santheconnect.utils.toDisplayDate

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemReviewCardBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(review: Review) {
            b.tvAuthor.text = review.authorName
            b.tvDate.text = review.timestamp.toDisplayDate()
            b.tvRating.text = "%.1f ★".format(review.rating)

            // Text or transcription
            val displayText = if (review.transcribedText.isNotBlank()) {
                "\"${review.transcribedText}\""
            } else {
                review.text
            }
            b.tvReviewText.text = displayText
            b.tvReviewText.visibility = if (displayText.isBlank()) View.GONE else View.VISIBLE

            // Voice note indicator
            b.layoutVoiceNote.visibility =
                if (review.voiceNoteUrl.isNotBlank()) View.VISIBLE else View.GONE

            // Photo
            if (review.photoUrl.isNotBlank()) {
                b.ivReviewPhoto.visibility = View.VISIBLE
                b.ivReviewPhoto.load(review.photoUrl) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(16f))
                    placeholder(R.drawable.ic_image_placeholder)
                }
            } else {
                b.ivReviewPhoto.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(a: Review, b: Review) = a.id == b.id
            override fun areContentsTheSame(a: Review, b: Review) = a == b
        }
    }
}
