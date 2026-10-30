<template>
    <view class="focus-page">
        <!-- 倒计时 -->
        <view class="timer-display">{{ fmt(remaining) }}</view>

        <!-- 专注计划 -->
        <view v-if="!started" class="chips">
            <view class="chips-label">⏱ 专注时长</view>
            <view class="chips-row">
                <text v-for="m in options" :key="m"
                      :class="['chip', minutes === m ? 'active' : '']"
                      @tap="selectMinutes(m)">{{ m }}<text class="chip-unit">分</text></text>
            </view>
        </view>
        <view v-else class="plan-hint">{{ running ? '专注中，放下手机，加油！' : '已暂停，休息一下' }}</view>

        <!-- 主按钮 -->
        <view class="big-btn" :class="{ running: started }" @tap="tapMain">
            {{ !started ? '开始专注' : (running ? '暂 停' : '继 续') }}
        </view>

        <!-- 放弃 -->
        <view v-if="started" class="giveup" @tap="giveUp">放弃本次专注</view>

        <!-- 底部装饰 -->
        <view class="deco">
            <text class="deco-item">🪴</text>
            <text class="deco-item deco-cat">🐱</text>
            <text class="deco-item">📖</text>
            <text class="deco-item">📚</text>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            minutes: 25,
            options: [15, 25, 45, 60],
            remaining: 25 * 60,
            started: false,
            running: false,
            endAt: 0,
            timer: null
        }
    },
    onUnload() {
        this.stopTimer()
    },
    onHide() {
        // 切走后计时继续（基于时间戳，回来自动校正）
    },
    methods: {
        fmt(sec) {
            const m = String(Math.floor(sec / 60)).padStart(2, '0')
            const s = String(sec % 60).padStart(2, '0')
            return `${m}:${s}`
        },
        selectMinutes(m) {
            this.minutes = m
            this.remaining = m * 60
        },
        tapMain() {
            if (!this.started) {
                this.start()
            } else if (this.running) {
                this.pause()
            } else {
                this.resume()
            }
        },
        start() {
            this.started = true
            this.running = true
            this.endAt = Date.now() + this.remaining * 1000
            this.timer = setInterval(() => this.tick(), 250)
        },
        pause() {
            this.stopTimer()
            this.running = false
        },
        resume() {
            this.running = true
            this.endAt = Date.now() + this.remaining * 1000
            this.timer = setInterval(() => this.tick(), 250)
        },
        tick() {
            const remain = Math.round((this.endAt - Date.now()) / 1000)
            if (remain <= 0) {
                this.remaining = 0
                this.finish()
                return
            }
            this.remaining = remain
        },
        finish() {
            this.stopTimer()
            const mins = this.minutes
            this.reset()
            uni.showModal({
                title: '🎉 专注完成！',
                content: `本次专注 ${mins} 分钟，去打卡记录一下吧`,
                confirmText: '去打卡',
                cancelText: '留在页面',
                success: (res) => {
                    if (res.confirm) {
                        uni.navigateTo({ url: '/pages/add/add?minutes=' + mins })
                    }
                }
            })
        },
        giveUp() {
            uni.showModal({
                title: '放弃专注',
                content: '确定放弃本次专注吗？本次将不会记录。',
                confirmColor: '#E24B4A',
                success: (res) => {
                    if (res.confirm) {
                        this.stopTimer()
                        this.reset()
                    }
                }
            })
        },
        reset() {
            this.started = false
            this.running = false
            this.remaining = this.minutes * 60
        },
        stopTimer() {
            if (this.timer) {
                clearInterval(this.timer)
                this.timer = null
            }
        }
    }
}
</script>

<style scoped>
.focus-page {
    position: relative;
    height: calc(100vh - var(--window-top) - var(--window-bottom));
    overflow: hidden;
    background: #FBF3E4;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}
.timer-display {
    font-size: 150rpx;
    font-weight: bold;
    color: #5B4A3F;
    letter-spacing: 8rpx;
    font-variant-numeric: tabular-nums;
}
.chips {
    margin-top: 40rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.chips-label {
    font-size: 24rpx;
    color: #B08D4F;
    margin-bottom: 20rpx;
    letter-spacing: 2rpx;
}
.chips-row {
    display: flex;
}
.chip {
    min-width: 108rpx;
    box-sizing: border-box;
    padding: 14rpx 0;
    border-radius: 40rpx;
    margin: 0 12rpx;
    font-size: 30rpx;
    text-align: center;
    color: #8A6A2F;
    background: rgba(232, 181, 77, 0.15);
    border: 2rpx solid rgba(232, 181, 77, 0.25);
    transition: all 0.2s;
}
.chip.active {
    background: #F9D77E;
    border-color: #E8B54D;
    color: #7A5A1E;
    font-weight: bold;
    box-shadow: 0 6rpx 20rpx rgba(232, 181, 77, 0.4);
    transform: scale(1.05);
}
.chip-unit {
    font-size: 22rpx;
    margin-left: 4rpx;
}
.plan-hint {
    margin-top: 20rpx;
    color: #B08D4F;
    font-size: 28rpx;
}
.big-btn {
    margin-top: 90rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: #F9D77E;
    border: 8rpx solid #E8B54D;
    color: #7A5A1E;
    font-size: 40rpx;
    font-weight: bold;
    text-align: center;
    line-height: 284rpx;
    box-shadow: 0 10rpx 40rpx rgba(232, 181, 77, 0.35);
    box-sizing: border-box;
}
.big-btn.running {
    background: #FBE7B5;
}
.giveup {
    margin-top: 50rpx;
    color: #C0A876;
    font-size: 26rpx;
    text-decoration: underline;
}
.deco {
    position: absolute;
    bottom: 20rpx;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    align-items: flex-end;
    pointer-events: none;
}
.deco-item {
    font-size: 70rpx;
    margin: 0 20rpx;
}
.deco-cat {
    font-size: 100rpx;
}
</style>
