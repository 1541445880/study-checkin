<template>
    <view class="container">
        <view v-if="list.length === 0 && loaded" class="empty">暂无打卡记录，快去打卡吧</view>

        <view v-for="item in list" :key="item.id" class="swipe-item">
            <!-- 删除按钮（藏在卡片后面） -->
            <view class="swipe-delete" @tap.stop="confirmDelete(item)">删除</view>

            <!-- 卡片主体（可左滑） -->
            <view class="card record-item swipe-content"
                  :style="contentStyle(item)"
                  @touchstart="onTouchStart($event, item)"
                  @touchmove="onTouchMove($event, item)"
                  @touchend="onTouchEnd($event, item)"
                  @tap="onTapCard(item)">
                <view class="record-header">
                    <text class="record-date">{{ item.checkDate }}</text>
                    <text class="record-minutes">{{ item.studyMinutes }} 分钟</text>
                </view>
                <view class="record-content">{{ item.content || '（无内容）' }}</view>
                <view v-if="item.imageList && item.imageList.length" class="record-imgs">
                    <image v-for="(img, i) in item.imageList" :key="i" :src="imgUrl(img)" class="record-img"
                           mode="aspectFill" @tap.stop="preview(item, i)" />
                </view>
                <view class="record-footer">
                    <view v-if="item.mood" class="record-mood">{{ item.mood }}</view>
                    <text class="record-hint">左滑删除 · 点击编辑</text>
                </view>
            </view>
        </view>

        <view v-if="list.length > 0" class="load-more">
            <text v-if="loading">加载中...</text>
            <text v-else-if="noMore">没有更多了</text>
        </view>

        <!-- 悬浮新增按钮 -->
        <view class="fab" @tap="goAdd">+</view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'
import { getList, deleteCheckIn } from '@/common/api.js'

const DELETE_W = 70 // 删除按钮宽度(px)，与样式保持一致

export default {
    data() {
        return {
            list: [],
            page: 1,
            size: 20,
            noMore: false,
            loaded: false,
            loading: false,
            offsets: {},      // 每条记录的滑出位移(id -> px)
            dragId: null,
            startOffset: 0,
            startX: 0,
            startY: 0,
            moved: false,
            noTransition: false,
            deleteWidth: DELETE_W
        }
    },
    onLoad() {
        this.load()
    },
    onReachBottom() {
        this.loadMore()
    },
    onPullDownRefresh() {
        this.page = 1
        this.noMore = false
        this.list = []
        this.offsets = {}
        this.load(() => uni.stopPullDownRefresh())
    },
    methods: {
        contentStyle(item) {
            const x = this.offsets[item.id] || 0
            const t = this.noTransition ? 'none' : 'transform 0.25s'
            return `transform: translateX(${x}px); transition: ${t};`
        },
        onTouchStart(e, item) {
            // 关掉其它已展开的
            Object.keys(this.offsets).forEach((k) => {
                if (Number(k) !== item.id && this.offsets[k] !== 0) {
                    this.$set(this.offsets, k, 0)
                }
            })
            this.dragId = item.id
            this.startOffset = this.offsets[item.id] || 0
            this.startX = e.touches[0].clientX
            this.startY = e.touches[0].clientY
            this.moved = false
            this.noTransition = false
        },
        onTouchMove(e, item) {
            if (this.dragId !== item.id) return
            const dx = e.touches[0].clientX - this.startX
            const dy = e.touches[0].clientY - this.startY
            // 纵向滚动时不管
            if (Math.abs(dy) > Math.abs(dx)) return
            if (Math.abs(dx) > 8) this.moved = true
            this.noTransition = true
            const target = Math.min(0, Math.max(-this.deleteWidth, this.startOffset + dx))
            this.$set(this.offsets, item.id, target)
        },
        onTouchEnd(e, item) {
            if (this.dragId !== item.id) return
            this.dragId = null
            this.noTransition = false
            const cur = this.offsets[item.id] || 0
            const open = cur < -this.deleteWidth / 2
            this.$set(this.offsets, item.id, open ? -this.deleteWidth : 0)
        },
        onTapCard(item) {
            // 滑动过，视为拖拽结束，不触发跳转
            if (this.moved) {
                this.moved = false
                return
            }
            // 展开状态：点一下先收起
            if ((this.offsets[item.id] || 0) !== 0) {
                this.$set(this.offsets, item.id, 0)
                return
            }
            // 正常点击 → 进入编辑回显
            uni.navigateTo({ url: '/pages/add/add?id=' + item.id })
        },
        load(done) {
            if (this.loading) {
                if (done) done()
                return
            }
            this.loading = true
            getList(this.page, this.size).then((data) => {
                if (data.length < this.size) {
                    this.noMore = true
                }
                this.list = this.list.concat(data)
                this.loaded = true
            }).finally(() => {
                this.loading = false
                if (done) done()
            })
        },
        loadMore() {
            if (this.noMore || this.loading) return
            this.page += 1
            this.load()
        },
        goAdd() {
            uni.navigateTo({ url: '/pages/add/add' })
        },
        imgUrl(img) {
            return img && img.indexOf('http') === 0 ? img : (BASE_URL + img)
        },
        preview(item, i) {
            const urls = (item.imageList || []).map(img => this.imgUrl(img))
            uni.previewImage({ current: urls[i], urls })
        },
        confirmDelete(item) {
            uni.showModal({
                title: '删除记录',
                content: `确定删除 ${item.checkDate} 的打卡记录吗？`,
                confirmColor: '#E24B4A',
                success: (res) => {
                    if (res.confirm) {
                        deleteCheckIn(item.id).then(() => {
                            this.list = this.list.filter(i => i.id !== item.id)
                            this.$delete(this.offsets, item.id)
                            uni.showToast({ title: '已删除', icon: 'success' })
                        })
                    }
                }
            })
        }
    }
}
</script>

<style scoped>
.swipe-item {
    position: relative;
    overflow: hidden;
    border-radius: 12px;
    margin-bottom: 16px;
}
.swipe-delete {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 70px;
    background: linear-gradient(180deg, #FF6B6B, #E24B4A);
    color: #FFFFFF;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
}
.swipe-content {
    position: relative;
    margin-bottom: 0;
    z-index: 1;
}
.record-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.record-date {
    font-size: 30rpx;
    font-weight: bold;
}
.record-minutes {
    font-size: 26rpx;
    color: #4F8CFF;
}
.record-content {
    font-size: 26rpx;
    color: #666666;
    margin-top: 12rpx;
}
.record-imgs {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16rpx;
}
.record-img {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
    margin: 0 16rpx 16rpx 0;
}
.record-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 14rpx;
}
.record-mood {
    font-size: 22rpx;
    color: #7A8BAD;
    background: #EEF3FC;
    border-radius: 8rpx;
    padding: 4rpx 14rpx;
}
.record-hint {
    font-size: 20rpx;
    color: #CCCCCC;
}
.empty {
    text-align: center;
    color: #BBBBBB;
    padding: 120rpx 0;
    font-size: 28rpx;
}
.load-more {
    text-align: center;
    color: #999999;
    font-size: 26rpx;
    padding: 24rpx 0;
}
.fab {
    position: fixed;
    right: 40rpx;
    bottom: 80rpx;
    width: 104rpx;
    height: 104rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #4F8CFF, #6FA4FF);
    color: #FFFFFF;
    font-size: 60rpx;
    text-align: center;
    line-height: 96rpx;
    box-shadow: 0 8rpx 24rpx rgba(79, 140, 255, 0.4);
    z-index: 99;
}
</style>
