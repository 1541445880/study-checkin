<template>
    <view class="container">
        <!-- 数据卡片 -->
        <view class="stat-row">
            <view class="stat-card">
                <view class="stat-num">{{ stats.streak || 0 }}</view>
                <view class="stat-label">连续天数</view>
            </view>
            <view class="stat-card">
                <view class="stat-num">{{ stats.totalDays || 0 }}</view>
                <view class="stat-label">累计天数</view>
            </view>
            <view class="stat-card">
                <view class="stat-num">{{ totalHours }}</view>
                <view class="stat-label">累计小时</view>
            </view>
        </view>

        <!-- 热力图 -->
        <view class="card">
            <view class="card-title">打卡热力图（近一年）</view>
            <scroll-view scroll-x class="heatmap-scroll">
                <view class="heatmap">
                    <view v-for="(week, wi) in heatmapGrid" :key="wi" class="heatmap-week">
                        <view v-for="(day, di) in week" :key="di" class="heatmap-day"
                              :style="{ backgroundColor: colorLevel(day.minutes) }"></view>
                    </view>
                </view>
            </scroll-view>
            <view class="legend">
                <text class="legend-text">少</text>
                <view class="legend-box" style="background:#EBEDF0"></view>
                <view class="legend-box" style="background:#C6E48B"></view>
                <view class="legend-box" style="background:#7BC96F"></view>
                <view class="legend-box" style="background:#239A3B"></view>
                <view class="legend-box" style="background:#196127"></view>
                <text class="legend-text">多</text>
            </view>
        </view>

        <!-- 心情分布饼图 -->
        <view class="card">
            <view class="card-title">心情分布</view>
            <view v-if="moodChart.length > 0" class="pie-wrap">
                <view class="pie" :style="pieStyle">
                    <view class="pie-hole">
                        <text class="hole-num">{{ totalMood }}</text>
                        <text class="hole-label">次打卡</text>
                    </view>
                </view>
                <view class="pie-legend">
                    <view v-for="s in moodChart" :key="s.mood" class="legend-item">
                        <view class="legend-dot" :style="{ background: s.color }"></view>
                        <text class="legend-name">{{ s.mood }}</text>
                        <text class="legend-count">{{ s.count }} 次</text>
                        <text class="legend-pct">{{ s.pct }}%</text>
                    </view>
                </view>
            </view>
            <view v-else class="empty">还没有心情数据，打卡时选个心情吧</view>
        </view>

        <!-- 最近打卡 -->
        <view class="card">
            <view class="card-title">最近打卡</view>
            <view v-if="recentList.length === 0" class="empty">暂无记录</view>
            <view v-for="item in recentList" :key="item.id" class="recent-item">
                <text class="recent-date">{{ item.checkDate }}</text>
                <text class="recent-content">{{ item.content || '（无内容）' }}</text>
                <text class="recent-minutes">{{ item.studyMinutes }}分钟</text>
            </view>
        </view>
    </view>
</template>

<script>
import { getStats, getList } from '@/common/api.js'

export default {
    data() {
        return {
            stats: {},
            recentList: []
        }
    },
    computed: {
        totalHours() {
            return Math.floor((this.stats.totalMinutes || 0) / 60)
        },
        moodChart() {
            const list = this.stats.moodStats || []
            if (!list.length) return []
            const colors = ['#4F8CFF', '#7BC96F', '#FFB84F', '#9B8CFF', '#FF7B8C']
            const total = list.reduce((s, m) => s + m.count, 0)
            let acc = 0
            return list.map((m, i) => {
                const start = (acc / total) * 360
                const pct = m.count / total
                acc += m.count
                const end = (acc / total) * 360
                return {
                    mood: m.mood,
                    count: m.count,
                    color: colors[i % colors.length],
                    pct: Math.round(pct * 100),
                    start,
                    end
                }
            })
        },
        totalMood() {
            return this.moodChart.reduce((s, m) => s + m.count, 0)
        },
        pieStyle() {
            if (!this.moodChart.length) return ''
            const parts = this.moodChart.map(s => `${s.color} ${s.start}deg ${s.end}deg`)
            return `background: conic-gradient(${parts.join(', ')})`
        },
        heatmapGrid() {
            const end = new Date()
            const start = new Date()
            start.setFullYear(start.getFullYear() - 1)
            // 对齐到周一
            const startDay = new Date(start)
            const dow = (startDay.getDay() + 6) % 7
            startDay.setDate(startDay.getDate() - dow)

            const map = {}
            ;(this.stats.heatmap || []).forEach(item => {
                map[item.date] = item.minutes
            })

            const weeks = []
            const cur = new Date(startDay)
            while (cur <= end && weeks.length < 54) {
                const week = []
                for (let i = 0; i < 7; i++) {
                    const d = new Date(cur)
                    const key = this.formatDate(d)
                    week.push({
                        date: key,
                        minutes: d > end ? -1 : (map[key] || 0)
                    })
                    cur.setDate(cur.getDate() + 1)
                }
                weeks.push(week)
            }
            return weeks
        }
    },
    onShow() {
        this.load()
    },
    methods: {
        load() {
            getStats().then((data) => {
                this.stats = data
            })
            getList(1, 10).then((data) => {
                this.recentList = data
            })
        },
        formatDate(d) {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        },
        colorLevel(minutes) {
            if (minutes < 0) return 'transparent'
            if (minutes <= 0) return '#EBEDF0'
            if (minutes < 30) return '#C6E48B'
            if (minutes < 60) return '#7BC96F'
            if (minutes < 120) return '#239A3B'
            return '#196127'
        }
    }
}
</script>

<style scoped>
.stat-row {
    display: flex;
    margin-bottom: 16px;
}
.stat-card {
    flex: 1;
    background: #FFFFFF;
    border-radius: 12px;
    padding: 24rpx 0;
    text-align: center;
    margin-right: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.stat-card:last-child {
    margin-right: 0;
}
.stat-num {
    font-size: 44rpx;
    font-weight: bold;
    color: #4F8CFF;
}
.stat-label {
    font-size: 24rpx;
    color: #999999;
    margin-top: 8rpx;
}
.heatmap-scroll {
    width: 100%;
}
.heatmap {
    display: flex;
    width: max-content;
}
.heatmap-week {
    display: flex;
    flex-direction: column;
    margin-right: 6rpx;
}
.heatmap-day {
    width: 24rpx;
    height: 24rpx;
    border-radius: 4rpx;
    margin-bottom: 6rpx;
}
.legend {
    display: flex;
    align-items: center;
    margin-top: 16rpx;
}
.legend-box {
    width: 24rpx;
    height: 24rpx;
    border-radius: 4rpx;
    margin: 0 6rpx;
}
.legend-text {
    font-size: 22rpx;
    color: #999999;
}
.pie-wrap {
    display: flex;
    align-items: center;
}
.pie {
    width: 220rpx;
    height: 220rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 40rpx;
    flex-shrink: 0;
}
.pie-hole {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    background: #FFFFFF;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
.hole-num {
    font-size: 36rpx;
    font-weight: bold;
    color: #333333;
}
.hole-label {
    font-size: 20rpx;
    color: #999999;
}
.pie-legend {
    flex: 1;
}
.legend-item {
    display: flex;
    align-items: center;
    margin-bottom: 18rpx;
}
.legend-dot {
    width: 20rpx;
    height: 20rpx;
    border-radius: 50%;
    margin-right: 14rpx;
}
.legend-name {
    font-size: 26rpx;
    color: #333333;
    width: 96rpx;
}
.legend-count {
    font-size: 24rpx;
    color: #999999;
    flex: 1;
}
.legend-pct {
    font-size: 24rpx;
    color: #4F8CFF;
    font-weight: bold;
}
.recent-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #F0F0F0;
}
.recent-item:last-child {
    border-bottom: none;
}
.recent-date {
    font-size: 26rpx;
    color: #666666;
    width: 180rpx;
}
.recent-content {
    flex: 1;
    font-size: 26rpx;
    color: #333333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.recent-minutes {
    font-size: 26rpx;
    color: #4F8CFF;
}
.empty {
    text-align: center;
    color: #BBBBBB;
    padding: 40rpx 0;
    font-size: 26rpx;
}
</style>
