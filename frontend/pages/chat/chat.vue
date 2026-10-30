<template>
    <view class="chat-page">
        <scroll-view class="msg-list" scroll-y :scroll-top="scrollTop" :scroll-with-animation="true">
            <view v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role === 'user' ? 'right' : 'left'">
                <view v-if="m.role === 'assistant'" class="avatar ai">🤖</view>
                <view v-if="m.role === 'user'" class="avatar user">
                    <image v-if="avatar" :src="avatarUrl" class="avatar-img" mode="aspectFill" />
                    <text v-else>我</text>
                </view>
                <view class="bubble" :class="[m.role, m.error ? 'error' : '']">
                    <text v-if="m.error">{{ m.text }}</text>
                    <text v-else-if="m.loading && !m.text" class="thinking"><text class="dot">●</text><text class="dot">●</text><text class="dot">●</text></text>
                    <text v-else>{{ m.text }}<text v-if="m.loading" class="cursor">▍</text></text>
                </view>
            </view>
            <view class="list-bottom"></view>
        </scroll-view>

        <view class="input-bar">
            <view class="clear-btn" @tap="clearHistory">🗑</view>
            <input class="chat-input" v-model="inputText" placeholder="聊聊学习 / 打卡的问题…"
                   placeholder-class="ph" confirm-type="send" @confirm="send" :disabled="streaming" />
            <view class="send-btn" :class="{ disabled: streaming || !inputText.trim() }" @tap="send">发送</view>
        </view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'

export default {
    data() {
        return {
            messages: [],
            inputText: '',
            streaming: false,
            scrollTop: 0,
            avatar: ''
        }
    },
    computed: {
        avatarUrl() {
            return this.avatar ? BASE_URL + this.avatar : ''
        }
    },
    onLoad() {
        // 恢复本地保存的聊天记录；没有则显示欢迎语
        const saved = uni.getStorageSync('chat_history')
        if (saved && saved.length > 0) {
            this.messages = saved.map(m => ({
                role: m.role,
                text: m.text || '',
                loading: false,
                error: m.error || false
            }))
        } else {
            this.initWelcome()
        }
        this.scrollToBottom()
    },
    onShow() {
        // 同步用户头像（可能刚在「我的」页换过）
        this.avatar = uni.getStorageSync('avatar') || ''
    },
    methods: {
        initWelcome() {
            this.messages = [{
                role: 'assistant',
                text: '你好呀，我是你的学习打卡助教～ 学习计划、坚持不下去、还是想聊聊今天学了什么？都可以跟我说。',
                loading: false,
                error: false
            }]
        },
        saveHistory() {
            const data = this.messages.map(m => ({
                role: m.role,
                text: m.text,
                error: m.error || false
            }))
            uni.setStorageSync('chat_history', data)
        },
        clearHistory() {
            uni.showModal({
                title: '清空对话',
                content: '确定清空所有聊天记录吗？',
                confirmColor: '#E24B4A',
                success: (res) => {
                    if (res.confirm) {
                        uni.removeStorageSync('chat_history')
                        this.initWelcome()
                        this.scrollToBottom()
                    }
                }
            })
        },
        scrollToBottom() {
            this.$nextTick(() => {
                this.scrollTop = 999999
            })
        },
        async send() {
            const text = this.inputText.trim()
            if (!text || this.streaming) return

            this.inputText = ''
            this.messages.push({ role: 'user', text, loading: false, error: false })
            const aiMsg = { role: 'assistant', text: '', loading: true, error: false }
            this.messages.push(aiMsg)
            this.streaming = true
            this.saveHistory()
            this.scrollToBottom()

            const history = this.messages.slice(0, -2).map(m => ({ role: m.role, content: m.text }))
            const token = uni.getStorageSync('token')

            // 打字机缓冲：收到的内容先进 rawBuffer，再由定时器逐字吐到 aiMsg.text
            let rawBuffer = ''
            let streamEnded = false
            let typeTimer = null

            const tick = () => {
                if (rawBuffer.length > 0) {
                    const n = rawBuffer.length < 3 ? 1 : 2
                    aiMsg.text += rawBuffer.slice(0, n)
                    rawBuffer = rawBuffer.slice(n)
                    this.scrollToBottom()
                } else if (streamEnded) {
                    clearInterval(typeTimer)
                    aiMsg.loading = false
                    if (!aiMsg.text && !aiMsg.error) {
                        aiMsg.text = '（对方没有返回内容）'
                    }
                    this.saveHistory()
                }
            }
            typeTimer = setInterval(tick, 18)

            try {
                const resp = await fetch(BASE_URL + '/api/chat/stream', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({ message: text, history })
                })
                if (!resp.ok) {
                    throw new Error('HTTP ' + resp.status)
                }
                const reader = resp.body.getReader()
                const decoder = new TextDecoder('utf-8')
                let buffer = ''
                while (true) {
                    const { done, value } = await reader.read()
                    if (done) break
                    buffer += decoder.decode(value, { stream: true })

                    let idx
                    while ((idx = buffer.indexOf('\n\n')) !== -1) {
                        const event = buffer.slice(0, idx)
                        buffer = buffer.slice(idx + 2)
                        for (const line of event.split('\n')) {
                            if (!line.startsWith('data:')) continue
                            const data = line.slice(5).trim()
                            if (data === '[DONE]') continue
                            if (data.startsWith('[ERROR]')) {
                                aiMsg.error = true
                                aiMsg.text = data.slice(7)
                                rawBuffer = ''
                                streamEnded = true
                                break
                            }
                            try {
                                rawBuffer += JSON.parse(data)
                            } catch (e) {
                                // 忽略无法解析的行
                            }
                        }
                    }
                }
                streamEnded = true
            } catch (err) {
                streamEnded = true
                rawBuffer = ''
                aiMsg.loading = false
                aiMsg.error = true
                if (!aiMsg.text) {
                    aiMsg.text = '连接失败：请确认后端已启动且已配置豆包 API Key'
                }
                this.saveHistory()
            } finally {
                this.streaming = false
                this.scrollToBottom()
            }
        }
    }
}
</script>

<style scoped>
.chat-page {
    display: flex;
    flex-direction: column;
    height: calc(100vh - var(--window-top));
    box-sizing: border-box;
    background: #F5F6FA;
}
.msg-list {
    flex: 1;
    padding: 24rpx;
    box-sizing: border-box;
    overflow: hidden;
}
.msg-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 28rpx;
}
.msg-row.right {
    flex-direction: row-reverse;
}
.avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    text-align: center;
    line-height: 72rpx;
    font-size: 30rpx;
    flex-shrink: 0;
    overflow: hidden;
}
.avatar.ai {
    background: #EEF3FC;
    font-size: 40rpx;
    line-height: 72rpx;
    margin-right: 16rpx;
}
.avatar.user {
    background: linear-gradient(135deg, #4F8CFF, #7C5CFF);
    color: #FFFFFF;
    margin-left: 16rpx;
}
.avatar-img {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
}
.bubble {
    max-width: 72%;
    padding: 20rpx 24rpx;
    border-radius: 20rpx;
    font-size: 28rpx;
    line-height: 1.6;
    word-break: break-all;
    white-space: pre-wrap;
}
.bubble.assistant {
    background: #FFFFFF;
    color: #333333;
    border-top-left-radius: 6rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.bubble.user {
    background: #4F8CFF;
    color: #FFFFFF;
    border-top-right-radius: 6rpx;
}
.bubble.error {
    background: #FDECEC;
    color: #D9534F;
}
.cursor {
    color: #4F8CFF;
    animation: blink 1s step-start infinite;
}
@keyframes blink {
    50% { opacity: 0; }
}
.thinking {
    color: #999999;
    letter-spacing: 4rpx;
}
.dot {
    animation: bounce 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
    0%, 60%, 100% { opacity: 0.3; }
    30% { opacity: 1; }
}
.list-bottom {
    height: 20rpx;
}
.input-bar {
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #FFFFFF;
    border-top: 1rpx solid #F0F0F0;
    padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.clear-btn {
    width: 64rpx;
    height: 76rpx;
    line-height: 76rpx;
    text-align: center;
    font-size: 36rpx;
    margin-right: 8rpx;
    flex-shrink: 0;
}
.chat-input {
    flex: 1;
    height: 76rpx;
    line-height: 76rpx;
    background: #F5F6FA;
    border-radius: 38rpx;
    padding: 0 28rpx;
    font-size: 28rpx;
}
.ph {
    color: #BBBBBB;
}
.send-btn {
    width: 128rpx;
    height: 76rpx;
    line-height: 76rpx;
    text-align: center;
    background: #4F8CFF;
    color: #FFFFFF;
    border-radius: 38rpx;
    font-size: 28rpx;
    margin-left: 16rpx;
    flex-shrink: 0;
}
.send-btn.disabled {
    background: #B8CDFF;
}
</style>
