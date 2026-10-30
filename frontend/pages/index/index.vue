<template>
    <view class="container">
        <!-- 顶部：头像 + 问候 -->
        <view class="topbar">
            <view class="avatar-mini" @tap="goMine">
                <image v-if="avatar" :src="avatarUrl" class="avatar-img" mode="aspectFill" />
                <text v-else class="avatar-fallback">{{ avatarText }}</text>
            </view>
            <view class="greet-text">
                <text class="hello">{{ greetText }}，{{ nickname || '同学' }}</text>
                <text class="greet-sub">{{ todayStr }}</text>
            </view>
        </view>

        <!-- 连续打卡英雄卡 -->
        <view class="hero-card">
            <view class="hero-top">
                <view class="streak-block">
                    <text class="streak-num">{{ stats.streak || 0 }}</text>
                    <text class="streak-unit">天</text>
                </view>
                <text class="streak-flame">{{ stats.streak > 0 ? '🔥' : '🌱' }}</text>
            </view>
            <text class="streak-label">连续打卡</text>

            <view class="week">
                <view v-for="(d, i) in week" :key="i" class="week-item">
                    <view class="week-dot" :class="{ on: d.done, today: d.today }">
                        <text v-if="d.today" class="today-text">今</text>
                    </view>
                    <text class="week-day" :class="{ 'week-today': d.today }">{{ d.label }}</text>
                </view>
            </view>

            <view class="hero-footer">累计 {{ stats.totalDays || 0 }} 天 · {{ totalHours }} 小时</view>
        </view>

        <!-- 今日：目标环 + 快速打卡 -->
        <view class="card today-card">
            <view class="today-head">
                <view class="goal-ring" :style="ringStyle">
                    <view class="goal-hole">
                        <text class="goal-num">{{ stats.todayMinutes || 0 }}<text class="goal-unit">分</text></text>
                        <text class="goal-sub">目标 {{ dailyGoal }} 分</text>
                    </view>
                </view>
                <view class="today-meta">
                    <text class="today-pct">{{ goalPct }}%</text>
                    <text class="today-tip">{{ goalTip }}</text>
                    <text class="goal-set" @tap="setGoal">修改目标 ›</text>
                </view>
            </view>

            <view class="quick-row">
                <text v-for="m in quickMinutes" :key="m"
                      :class="['qm', Number(minutes) === m ? 'active' : '']"
                      @tap="minutes = m">{{ m }} 分</text>
            </view>
            <view class="btn-primary" @tap="submit">{{ stats.todayChecked ? '再打一次卡' : '快速打卡' }}</view>
            <view class="full-link" @tap="goAdd">记录内容 / 心情 / 照片 ›</view>
        </view>

        <!-- 每日一句 -->
        <view class="quote-card">
            <view class="quote-bar"></view>
            <view class="quote-body">
                <text class="quote-text">{{ quote }}</text>
                <text v-if="quoteAuthor" class="quote-author">—— {{ quoteAuthor }}</text>
            </view>
        </view>

        <!-- AI 学习助手 -->
        <view class="ai-card" @tap="goChat">
            <view class="bubble-row">
                <view class="chat-bubble q1">今天学点什么好？</view>
                <view class="chat-bubble q2">坚持不下去怎么办…</view>
            </view>
            <view class="ai-bottom">
                <view class="ai-avatar">🤖</view>
                <view class="ai-info">
                    <text class="ai-title">AI 学习助手</text>
                    <text class="ai-desc">学习规划 · 答疑 · 鼓励</text>
                </view>
                <view class="ai-go">去聊聊</view>
            </view>
        </view>

        <!-- 最近打卡 -->
        <view class="card recent-card" @tap="goRecords">
            <view class="card-title">
                最近打卡
                <text class="more-link">全部 ›</text>
            </view>
            <view v-if="recentList.length === 0" class="recent-empty">还没有打卡记录</view>
            <view v-for="item in recentList" :key="item.id" class="recent-item">
                <text class="recent-date">{{ item.checkDate }}</text>
                <text class="recent-content">{{ item.content || '（无内容）' }}</text>
                <text class="recent-min">{{ item.studyMinutes }} 分</text>
            </view>
        </view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'
import { doCheckIn, getStats, getList, getQuote, updateProfile } from '@/common/api.js'

export default {
    data() {
        return {
            stats: {},
            nickname: '',
            avatar: '',
            minutes: '',
            quickMinutes: [15, 30, 60, 90],
            recentList: [],
            quotes: [
                '不积跬步，无以至千里。',
                '每天进步一点点，坚持带来大改变。',
                '种一棵树最好的时间是十年前，其次是现在。',
                '自律给你自由。',
                '你只管努力，剩下的交给时间。',
                '今天的努力，是明天的底气。',
                '慢慢来，比较快。',
                '每一个不曾起舞的日子，都是对生命的辜负。'
            ],
            quote: '',
            quoteAuthor: ''
        }
    },
    computed: {
        totalHours() {
            return Math.floor((this.stats.totalMinutes || 0) / 60)
        },
        greetText() {
            const h = new Date().getHours()
            if (h < 6) return '夜深了'
            if (h < 9) return '早上好'
            if (h < 12) return '上午好'
            if (h < 14) return '中午好'
            if (h < 18) return '下午好'
            return '晚上好'
        },
        todayStr() {
            const d = new Date()
            const week = ['日', '一', '二', '三', '四', '五', '六']
            return `${d.getMonth() + 1}月${d.getDate()}日 星期${week[d.getDay()]}`
        },
        week() {
            const days = ['一', '二', '三', '四', '五', '六', '日']
            const map = {}
            ;(this.stats.heatmap || []).forEach(item => { map[item.date] = item.minutes })
            const today = new Date()
            const dow = (today.getDay() + 6) % 7
            const monday = new Date(today)
            monday.setDate(today.getDate() - dow)
            const result = []
            for (let i = 0; i < 7; i++) {
                const d = new Date(monday)
                d.setDate(monday.getDate() + i)
                const key = this.formatDate(d)
                result.push({
                    label: days[i],
                    done: (map[key] || 0) > 0,
                    today: key === this.formatDate(today)
                })
            }
            return result
        },
        avatarText() {
            return this.nickname ? this.nickname.charAt(0) : '学'
        },
        avatarUrl() {
            return this.avatar ? BASE_URL + this.avatar : ''
        },
        dailyGoal() {
            return this.stats.dailyGoal || 60
        },
        goalPct() {
            const mins = this.stats.todayMinutes || 0
            return Math.min(100, Math.round(mins / this.dailyGoal * 100))
        },
        ringStyle() {
            const deg = this.goalPct * 3.6
            return `background: conic-gradient(#4F8CFF ${deg}deg, #E6F0FB ${deg}deg)`
        },
        goalTip() {
            if (this.goalPct >= 100) return '今日目标已达成，太棒了！'
            const left = Math.max(0, this.dailyGoal - (this.stats.todayMinutes || 0))
            return `还差 ${left} 分钟`
        }
    },
    onLoad() {
        this.nickname = uni.getStorageSync('nickname') || ''
        this.avatar = uni.getStorageSync('avatar') || ''
    },
    onShow() {
        this.nickname = uni.getStorageSync('nickname') || ''
        this.avatar = uni.getStorageSync('avatar') || ''
        this.pickQuote()
        this.loadStats()
        this.loadRecent()
    },
    methods: {
        pickQuote() {
            getQuote().then((data) => {
                if (data && data.content) {
                    this.quote = data.content
                    this.quoteAuthor = data.author || ''
                } else {
                    this.fallbackQuote()
                }
            }).catch(() => {
                this.fallbackQuote()
            })
        },
        fallbackQuote() {
            const pool = this.quotes.filter(q => q !== this.quote)
            const list = pool.length ? pool : this.quotes
            this.quote = list[Math.floor(Math.random() * list.length)]
            this.quoteAuthor = ''
        },
        loadStats() {
            getStats().then((data) => {
                this.stats = data
            })
        },
        loadRecent() {
            getList(1, 3).then((data) => {
                this.recentList = data || []
            })
        },
        formatDate(d) {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        },
        submit() {
            if (!this.minutes || Number(this.minutes) <= 0) {
                uni.showToast({ title: '请选择学习时长', icon: 'none' })
                return
            }
            doCheckIn({
                studyMinutes: Number(this.minutes)
            }).then(() => {
                uni.showToast({ title: '打卡成功！', icon: 'success' })
                this.minutes = ''
                this.loadStats()
                this.loadRecent()
            })
        },
        goAdd() {
            uni.navigateTo({ url: '/pages/add/add' })
        },
        goChat() {
            uni.navigateTo({ url: '/pages/chat/chat' })
        },
        goRecords() {
            uni.navigateTo({ url: '/pages/records/records' })
        },
        goMine() {
            uni.switchTab({ url: '/pages/mine/mine' })
        },
        setGoal() {
            uni.showModal({
                title: '设置每日目标',
                editable: true,
                content: String(this.dailyGoal),
                placeholderText: '输入目标分钟数，如 60',
                success: (res) => {
                    if (!res.confirm) return
                    const n = Number(res.content)
                    if (!n || n <= 0 || n > 1440) {
                        uni.showToast({ title: '请输入 1~1440 的分钟数', icon: 'none' })
                        return
                    }
                    updateProfile({ dailyGoal: n }).then(() => {
                        uni.showToast({ title: '目标已更新', icon: 'success' })
                        this.loadStats()
                    })
                }
            })
        }
    }
}
</script>

<style scoped>
.container {
    padding: 20rpx 24rpx 40rpx;
    background: #F5F6FA;
}
.topbar {
    display: flex;
    align-items: center;
    padding: 20rpx 8rpx 28rpx;
}
.avatar-mini {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: #E6F0FB;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;
    overflow: hidden;
}
.avatar-img {
    width: 100%;
    height: 100%;
}
.avatar-fallback {
    font-size: 36rpx;
    font-weight: bold;
    color: #4F8CFF;
}
.greet-text {
    display: flex;
    flex-direction: column;
}
.hello {
    font-size: 34rpx;
    font-weight: bold;
    color: #333333;
}
.greet-sub {
    font-size: 24rpx;
    color: #999999;
    margin-top: 6rpx;
}
.hero-card {
    background: linear-gradient(135deg, #4F8CFF 0%, #7C5CFF 100%);
    border-radius: 28rpx;
    padding: 40rpx 36rpx;
    color: #FFFFFF;
    box-shadow: 0 12rpx 32rpx rgba(79, 140, 255, 0.28);
}
.hero-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.streak-block {
    display: flex;
    align-items: baseline;
}
.streak-num {
    font-size: 96rpx;
    font-weight: bold;
    line-height: 1;
}
.streak-unit {
    font-size: 30rpx;
    margin-left: 8rpx;
    opacity: 0.85;
}
.streak-flame {
    font-size: 64rpx;
}
.streak-label {
    font-size: 26rpx;
    opacity: 0.9;
    margin-top: 6rpx;
}
.week {
    display: flex;
    justify-content: space-between;
    margin-top: 36rpx;
}
.week-item {
    display: flex;
    flex-direction: column;
    align-items: center;
}
.week-dot {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
}
.week-dot.on {
    background: #FFFFFF;
}
.week-dot.today {
    box-shadow: 0 0 0 4rpx rgba(255, 255, 255, 0.35);
}
.today-text {
    font-size: 22rpx;
    color: #4F8CFF;
    font-weight: bold;
}
.week-day {
    font-size: 22rpx;
    margin-top: 10rpx;
    opacity: 0.75;
}
.week-today {
    opacity: 1;
    font-weight: bold;
}
.hero-footer {
    margin-top: 32rpx;
    font-size: 24rpx;
    opacity: 0.8;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);
    padding-top: 24rpx;
}
.today-card {
    margin-top: 20rpx;
    padding: 32rpx;
}
.today-head {
    display: flex;
    align-items: center;
}
.goal-ring {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}
.goal-hole {
    width: 138rpx;
    height: 138rpx;
    border-radius: 50%;
    background: #FFFFFF;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
.goal-num {
    font-size: 40rpx;
    font-weight: bold;
    color: #4F8CFF;
    line-height: 1.1;
}
.goal-unit {
    font-size: 20rpx;
    font-weight: normal;
    margin-left: 2rpx;
}
.goal-sub {
    font-size: 20rpx;
    color: #999999;
    margin-top: 6rpx;
}
.today-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-left: 32rpx;
}
.today-pct {
    font-size: 56rpx;
    font-weight: bold;
    color: #4F8CFF;
    line-height: 1.1;
}
.today-tip {
    font-size: 24rpx;
    color: #999999;
    margin-top: 8rpx;
}
.goal-set {
    font-size: 24rpx;
    color: #4F8CFF;
    margin-top: 16rpx;
}
.quick-row {
    display: flex;
    margin-top: 28rpx;
}
.qm {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    background: #F5F6FA;
    border-radius: 16rpx;
    margin-right: 16rpx;
    font-size: 26rpx;
    color: #666666;
}
.qm:last-child {
    margin-right: 0;
}
.qm.active {
    background: #E6F0FB;
    color: #4F8CFF;
    font-weight: bold;
}
.btn-primary {
    margin-top: 24rpx;
}
.full-link {
    text-align: center;
    font-size: 24rpx;
    color: #999999;
    margin-top: 20rpx;
}
.quote-card {
    display: flex;
    align-items: stretch;
    margin-top: 20rpx;
    padding: 28rpx;
    background: #FFFFFF;
    border-radius: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.quote-bar {
    width: 6rpx;
    border-radius: 3rpx;
    background: #4F8CFF;
    margin-right: 20rpx;
    flex-shrink: 0;
}
.quote-body {
    flex: 1;
    display: flex;
    flex-direction: column;
}
.quote-text {
    font-size: 28rpx;
    color: #333333;
    line-height: 1.6;
}
.quote-author {
    font-size: 22rpx;
    color: #BBBBBB;
    margin-top: 10rpx;
}
.ai-card {
    margin-top: 20rpx;
    background: linear-gradient(135deg, #1E2A5A 0%, #3A5BBF 60%, #4F8CFF 100%);
    border-radius: 24rpx;
    padding: 32rpx;
    box-shadow: 0 12rpx 32rpx rgba(30, 42, 90, 0.3);
}
.bubble-row {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}
.chat-bubble {
    max-width: 80%;
    font-size: 24rpx;
    color: #FFFFFF;
    background: rgba(255, 255, 255, 0.16);
    border-radius: 20rpx;
    padding: 12rpx 22rpx;
    margin-bottom: 14rpx;
}
.q2 {
    border-top-left-radius: 6rpx;
    background: rgba(255, 255, 255, 0.1);
}
.ai-bottom {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
}
.ai-avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
}
.ai-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}
.ai-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #FFFFFF;
}
.ai-desc {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.75);
    margin-top: 8rpx;
}
.ai-go {
    background: #FFFFFF;
    color: #3A5BBF;
    font-size: 26rpx;
    font-weight: bold;
    padding: 14rpx 34rpx;
    border-radius: 36rpx;
    flex-shrink: 0;
}
.recent-card {
    margin-top: 20rpx;
}
.card-title {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.more-link {
    font-size: 24rpx;
    color: #BBBBBB;
    font-weight: normal;
}
.recent-empty {
    font-size: 26rpx;
    color: #BBBBBB;
    text-align: center;
    padding: 20rpx 0;
}
.recent-item {
    display: flex;
    align-items: center;
    padding: 18rpx 0;
    border-bottom: 1rpx solid #F5F5F5;
}
.recent-item:last-child {
    border-bottom: none;
}
.recent-date {
    font-size: 26rpx;
    color: #666666;
    width: 170rpx;
    flex-shrink: 0;
}
.recent-content {
    flex: 1;
    font-size: 26rpx;
    color: #333333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.recent-min {
    font-size: 24rpx;
    color: #4F8CFF;
    flex-shrink: 0;
}
</style>
