<template>
    <view class="page">
        <!-- 顶部背景区（点空白处换背景） -->
        <view class="header" @tap="changeBg">
            <image class="bg" :src="bgSrc" mode="aspectFill" />
            <view class="user-row">
                <view class="avatar-wrap" @tap.stop="changeAvatar">
                    <image v-if="avatar" :src="avatarUrl" class="avatar" mode="aspectFill" />
                    <view v-else class="avatar avatar-fallback">{{ avatarText }}</view>
                </view>
                <view class="user-meta">
                    <view class="nickname">{{ nickname || '未设置昵称' }}</view>
                    <view class="signature">{{ signature || '这个人很懒，还没有写签名' }}</view>
                </view>
            </view>
        </view>

        <!-- 数据统计栏 -->
        <view class="stats-card">
            <view class="stat">
                <view class="stat-num">{{ stats.streak || 0 }}</view>
                <view class="stat-label">连续打卡</view>
            </view>
            <view class="stat">
                <view class="stat-num">{{ stats.totalDays || 0 }}</view>
                <view class="stat-label">累计天数</view>
            </view>
            <view class="stat">
                <view class="stat-num">{{ totalHours }}</view>
                <view class="stat-label">累计小时</view>
            </view>
            <view class="stat">
                <view class="stat-num">{{ todayText }}</view>
                <view class="stat-label">今日打卡</view>
            </view>
        </view>

        <!-- 菜单列表 -->
        <view class="menu">
            <view class="menu-item" @tap="goEditProfile">
                <text class="mi-icon">📝</text>
                <text class="mi-text">编辑资料</text>
                <text class="mi-arrow">›</text>
            </view>
            <view class="menu-item" @tap="goChangePassword">
                <text class="mi-icon">🔒</text>
                <text class="mi-text">修改密码</text>
                <text class="mi-arrow">›</text>
            </view>
            <view class="menu-item" @tap="goRecords">
                <text class="mi-icon">📋</text>
                <text class="mi-text">我的打卡记录</text>
                <text class="mi-arrow">›</text>
            </view>
            <view class="menu-item" @tap="goChat">
                <text class="mi-icon">🤖</text>
                <text class="mi-text">AI 学习助手</text>
                <text class="mi-arrow">›</text>
            </view>
        </view>

        <view class="logout" @tap="logout">退出登录</view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'
import { getProfile, updateAvatar, updateBg, getStats } from '@/common/api.js'

export default {
    data() {
        return {
            nickname: '',
            username: '',
            userId: '',
            avatar: '',
            birthday: '',
            bg: '',
            signature: '',
            stats: {}
        }
    },
    computed: {
        avatarText() {
            return this.nickname ? this.nickname.charAt(0) : '?'
        },
        avatarUrl() {
            return this.avatar ? BASE_URL + this.avatar : ''
        },
        bgSrc() {
            return this.bg ? BASE_URL + this.bg : '/static/mine-bg.png'
        },
        totalHours() {
            return Math.floor((this.stats.totalMinutes || 0) / 60)
        },
        todayText() {
            return this.stats.todayChecked ? (this.stats.todayMinutes + '分') : '未打卡'
        }
    },
    onShow() {
        this.loadProfile()
        this.loadStats()
    },
    methods: {
        loadProfile() {
            getProfile().then((data) => {
                this.nickname = data.nickname || ''
                this.username = data.username || ''
                this.userId = data.userId || ''
                this.avatar = data.avatar || ''
                this.birthday = data.birthday || ''
                this.bg = data.bg || ''
                this.signature = data.signature || ''
                uni.setStorageSync('nickname', data.nickname || '')
                uni.setStorageSync('avatar', data.avatar || '')
                uni.setStorageSync('signature', data.signature || '')
            })
        },
        loadStats() {
            getStats().then((data) => {
                this.stats = data
            })
        },
        changeAvatar() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                success: (res) => {
                    const filePath = res.tempFilePaths[0]
                    uni.uploadFile({
                        url: BASE_URL + '/api/upload/avatar',
                        filePath,
                        name: 'file',
                        header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
                        success: (uploadRes) => {
                            let data = {}
                            try {
                                data = JSON.parse(uploadRes.data)
                            } catch (e) {
                                uni.showToast({ title: '上传失败', icon: 'none' })
                                return
                            }
                            if (data.code === 200) {
                                const url = data.data.url
                                updateAvatar({ avatarUrl: url }).then((profile) => {
                                    this.avatar = profile.avatar || ''
                                    uni.setStorageSync('avatar', profile.avatar || '')
                                    uni.showToast({ title: '头像已更新', icon: 'success' })
                                })
                            } else {
                                uni.showToast({ title: data.message || '上传失败', icon: 'none' })
                            }
                        },
                        fail: () => {
                            uni.showToast({ title: '上传失败，请检查后端', icon: 'none' })
                        }
                    })
                }
            })
        },
        changeBg() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                success: (res) => {
                    const filePath = res.tempFilePaths[0]
                    uni.uploadFile({
                        url: BASE_URL + '/api/upload/image',
                        filePath,
                        name: 'file',
                        header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
                        success: (uploadRes) => {
                            let data = {}
                            try {
                                data = JSON.parse(uploadRes.data)
                            } catch (e) {
                                uni.showToast({ title: '上传失败', icon: 'none' })
                                return
                            }
                            if (data.code === 200) {
                                updateBg({ url: data.data.url }).then((profile) => {
                                    this.bg = profile.bg || ''
                                    uni.showToast({ title: '背景已更新', icon: 'success' })
                                })
                            } else {
                                uni.showToast({ title: data.message || '上传失败', icon: 'none' })
                            }
                        },
                        fail: () => {
                            uni.showToast({ title: '上传失败，请检查后端', icon: 'none' })
                        }
                    })
                }
            })
        },
        goEditProfile() {
            uni.navigateTo({ url: '/pages/edit-profile/edit-profile' })
        },
        goChangePassword() {
            uni.navigateTo({ url: '/pages/change-password/change-password' })
        },
        goRecords() {
            uni.navigateTo({ url: '/pages/records/records' })
        },
        goChat() {
            uni.navigateTo({ url: '/pages/chat/chat' })
        },
        logout() {
            uni.showModal({
                title: '提示',
                content: '确定退出登录吗？',
                success: (res) => {
                    if (res.confirm) {
                        uni.removeStorageSync('token')
                        uni.removeStorageSync('userId')
                        uni.removeStorageSync('nickname')
                        uni.removeStorageSync('avatar')
                        uni.reLaunch({ url: '/pages/login/login' })
                    }
                }
            })
        }
    }
}
</script>

<style scoped>
.page {
    min-height: 100vh;
    background: #F5F6FA;
    padding-bottom: 60rpx;
}
.header {
    position: relative;
    height: 380rpx;
}
.bg {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
}
.user-row {
    position: relative;
    display: flex;
    align-items: center;
    padding: 90rpx 40rpx 0;
}
.avatar-wrap {
    position: relative;
    width: 150rpx;
    height: 150rpx;
    flex-shrink: 0;
    margin-right: 28rpx;
}
.avatar {
    width: 150rpx;
    height: 150rpx;
    border-radius: 50%;
    border: 6rpx solid rgba(255, 255, 255, 0.9);
    box-sizing: border-box;
    background: #FFFFFF;
}
.avatar-fallback {
    background: rgba(255, 255, 255, 0.35);
    color: #FFFFFF;
    font-size: 60rpx;
    text-align: center;
    line-height: 138rpx;
}
.user-meta {
    flex: 1;
}
.nickname {
    font-size: 40rpx;
    font-weight: bold;
    color: #FFFFFF;
}
.signature {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.85);
    margin-top: 12rpx;
}
.stats-card {
    position: relative;
    display: flex;
    background: #FFFFFF;
    border-radius: 20rpx;
    margin: -70rpx 24rpx 0;
    padding: 30rpx 0;
    box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.06);
}
.stat {
    flex: 1;
    text-align: center;
}
.stat-num {
    font-size: 40rpx;
    font-weight: bold;
    color: #4F8CFF;
}
.stat-label {
    font-size: 22rpx;
    color: #999999;
    margin-top: 8rpx;
}
.menu {
    background: #FFFFFF;
    border-radius: 20rpx;
    margin: 24rpx 24rpx 0;
    overflow: hidden;
}
.menu-item {
    display: flex;
    align-items: center;
    padding: 34rpx 32rpx;
    border-bottom: 1rpx solid #F5F5F5;
}
.menu-item:last-child {
    border-bottom: none;
}
.mi-icon {
    font-size: 34rpx;
    margin-right: 22rpx;
}
.mi-text {
    flex: 1;
    font-size: 30rpx;
    color: #333333;
}
.mi-arrow {
    color: #CCCCCC;
    font-size: 36rpx;
}
.logout {
    margin: 40rpx 24rpx 0;
    background: #FFFFFF;
    border-radius: 20rpx;
    text-align: center;
    color: #E24B4A;
    font-size: 30rpx;
    padding: 30rpx 0;
}
</style>
