package com.doctor.ensomnia.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.doctor.ensomnia.adapters.RecentConversationsAdapter
import com.doctor.ensomnia.data.pojo.ChatMessage
import com.doctor.ensomnia.data.pojo.User
import com.doctor.ensomnia.databinding.ActivityRecentChatBinding
import com.doctor.ensomnia.utilites.Constants
import com.doctor.ensomnia.utilites.ConversionListener
import com.doctor.ensomnia.viewmodel.ChatViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentChatActivity : AppCompatActivity(),ConversionListener {
    private lateinit var binding: ActivityRecentChatBinding
    private lateinit var mConversationsAdapter: RecentConversationsAdapter
    private lateinit var mConversions: ArrayList<ChatMessage>
    private val chatViewModel: ChatViewModel by viewModels<ChatViewModel>()
    private var mDoctorId:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityRecentChatBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init() {
        CoroutineScope(Dispatchers.Main).launch {
            mDoctorId = chatViewModel.getDoctorId(this@RecentChatActivity)

        }
        mConversions = ArrayList()
        mConversationsAdapter = RecentConversationsAdapter(mConversions, this@RecentChatActivity)
        binding.conversationsRecyclerView.adapter = mConversationsAdapter
    }



    @SuppressLint("NotifyDataSetChanged")
    private val eventListener: EventListener<QuerySnapshot> =
        EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val senderId: String =
                            documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        val receiverId: String =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        val chatMessage = ChatMessage()
                        chatMessage.senderId = senderId
                        chatMessage.receiverId = receiverId
                        if (mDoctorId.equals(senderId)) {
                            chatMessage.conversionImage =
                                documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                            chatMessage.conversionName =
                                documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
                            chatMessage.conversionId =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        } else {
                            chatMessage.conversionImage =
                                documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
                            chatMessage.conversionName =
                                documentChange.document.getString(Constants.KEY_SENDER_NAME)
                            chatMessage.conversionId =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                        }
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        mConversions.add(chatMessage)
                    } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                        for (i in 0..mConversions.size) {
                            val senderId: String =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                            val receiverId: String =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                            if (mConversions[i].senderId.equals(senderId) && mConversions[i].receiverId.equals(
                                    receiverId
                                )
                            ) {
                                mConversions[i].message =
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                mConversions[i].dateObject =
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                                break
                            }
                        }
                    }
                }
                mConversions.sortWith { obj1: ChatMessage, obj2: ChatMessage ->
                    obj2.dateObject!!.compareTo(obj1.dateObject)
                }
                mConversationsAdapter.notifyDataSetChanged()
                binding.conversationsRecyclerView.smoothScrollToPosition(0)
                binding.conversationsRecyclerView.visibility = View.VISIBLE
            }
        }

    override fun onConversionClicked(patient: User) {

    }
}