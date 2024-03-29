package com.company.ait.tobemom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.company.ait.tobemom.databinding.FragmentChatBinding
import com.company.ait.tobemom.dto.MessageAdapter
import com.company.ait.tobemom.dto.MessageModel
import com.company.ait.tobemom.dto.SendChatReq
import com.company.ait.tobemom.dto.SendChatRes
import com.company.ait.tobemom.utils.GlobalApplication
import com.company.ait.tobemom.utils.RetrofitObject
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var generativeModel: GenerativeModel
    private lateinit var messageAdapter: MessageAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment의 레이아웃을 inflate하고 바인딩 객체를 초기화
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //GenerativeModel 초기화
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "YOUR_GEMINI_KEY"
        )

        messageAdapter = MessageAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = messageAdapter

        val firstMessage = arguments?.getString("firstm") ?: "Hello, I'm Dr. Gemini.\nFeel free to ask me any questions about pregnancy!"
        messageAdapter.apply {
            addItem(MessageModel.ReceiverMessage(firstMessage))
        }

        binding.btnChatSend.setOnClickListener {
            val userMessage = binding.etChatInput.text.toString()
            val roomId = arguments?.getInt("roomId") ?: 1

            // Coroutine을 사용하여 비동기로 호출
            lifecycleScope.launch {
                processWithGemini(userMessage, roomId)
            }
        }
    }

    private suspend fun processWithGemini(userMessage: String, roomId: Int) {
        // 사용자의 메시지를 보내고 서버 응답을 처리
        val sendReq = SendChatReq(roomId, userMessage)
        send(sendReq)

        try {
            // Coroutine을 사용하여 비동기로 호출
            val prompt = "사용자 메시지에 응답: '$userMessage'"
            val generatedResponse = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            // 생성된 응답에서 텍스트 가져오기
            val generatedReply = generatedResponse.text

            if (generatedReply != null) {
                // 생성된 응답을 RecyclerView 어댑터에 추가
                withContext(Dispatchers.Main) {
                    messageAdapter.addItem(MessageModel.ReceiverMessage(generatedReply))
                }
            } else {
                Log.d("GEMINI_GENERATE", "응답이 없거나 text가 null입니다.")
            }

        } catch (e: Exception) {
            Log.d("GEMINI_GENERATE", "API 호출 실패: ${e.message}")
        }
    }

    private fun send(sendReq: SendChatReq) {
        try {
            // 사용자의 메시지를 즉시 어댑터에 추가
            messageAdapter.apply {
                addItem(MessageModel.SenderMessage(sendReq.message ?: ""))
            }

            val jwt = GlobalApplication.spf.Jwt
            if (jwt != null) {
                RetrofitObject.chatApi.sendChat(jwt, sendReq)
                    .enqueue(object : Callback<SendChatRes> {
                        override fun onResponse(call: Call<SendChatRes>, response: Response<SendChatRes>) {
                            if (response.isSuccessful) {
                                val sendChatResult: SendChatRes? = response.body()

                                // 답변을 얻은 후에 어댑터에 추가
                                val reply = sendChatResult?.result?.firstMessage ?: "기본 답변"
                                messageAdapter.apply {
                                    addItem(MessageModel.ReceiverMessage(reply))
                                }

                                Log.d("SENDCHAT", "onResponse 성공: $sendChatResult")
                            } else {
                                Log.d("SENDCHAT", "onResponse 실패")
                            }
                        }

                        override fun onFailure(call: Call<SendChatRes>, t: Throwable) {
                            Log.d("SENDCHAT/ERROR", t.message.toString())
                        }
                    })
            } else {
                Log.d("SENDCHAT/ERROR", "Jwt is null")
            }
        } catch (e: Exception) {
            Log.d("SENDCHAT/ERROR", e.message.toString())
        }
    }

}
