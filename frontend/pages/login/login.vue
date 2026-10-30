<template>
    <view class="login-page">
        <view class="logo">
            <text class="logo-title">学习打卡</text>
            <text class="logo-sub">记录每一天的成长</text>
        </view>
        <view class="form">
            <view class="tabs">
                <text :class="['tab', mode === 'login' ? 'active' : '']" @tap="switchMode('login')">登录</text>
                <text :class="['tab', mode === 'register' ? 'active' : '']" @tap="switchMode('register')">注册</text>
            </view>
            <input class="input" v-model="username" placeholder="用户名" placeholder-class="ph" />
            <input class="input" v-model="password" type="password" placeholder="密码" placeholder-class="ph" />
            <input v-if="mode === 'register'" class="input" v-model="nickname" placeholder="昵称（选填）" placeholder-class="ph" />
            <view class="btn-primary" @tap="submit">{{ mode === 'login' ? '登 录' : '注 册' }}</view>
            <view class="tip">演示账号：demo / 123456</view>
        </view>
    </view>
</template>

<script>
import { login, register } from '@/common/api.js'

export default {
    data() {
        return {
            mode: 'login',
            username: '',
            password: '',
            nickname: ''
        }
    },
    methods: {
        switchMode(m) {
            this.mode = m
        },
        submit() {
            if (!this.username || !this.password) {
                uni.showToast({ title: '请填写用户名和密码', icon: 'none' })
                return
            }
            if (this.mode === 'login') {
                login({ username: this.username, password: this.password }).then((data) => {
                    uni.setStorageSync('token', data.token)
                    uni.setStorageSync('userId', data.userId)
                    uni.setStorageSync('nickname', data.nickname)
                    uni.setStorageSync('avatar', data.avatar || '')
                    uni.setStorageSync('signature', data.signature || '')
                    uni.reLaunch({ url: '/pages/index/index' })
                })
            } else {
                register({ username: this.username, password: this.password, nickname: this.nickname }).then(() => {
                    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
                    this.mode = 'login'
                    this.password = ''
                })
            }
        }
    }
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #4F8CFF 0%, #6FA4FF 100%);
    padding: 120rpx 60rpx;
    box-sizing: border-box;
}
.logo {
    text-align: center;
    margin-bottom: 80rpx;
}
.logo-title {
    display: block;
    font-size: 56rpx;
    color: #FFFFFF;
    font-weight: bold;
}
.logo-sub {
    display: block;
    margin-top: 16rpx;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.85);
}
.form {
    background: #FFFFFF;
    border-radius: 24rpx;
    padding: 40rpx;
}
.tabs {
    display: flex;
    margin-bottom: 40rpx;
}
.tab {
    flex: 1;
    text-align: center;
    font-size: 32rpx;
    color: #999999;
    padding-bottom: 16rpx;
    border-bottom: 4rpx solid transparent;
}
.tab.active {
    color: #4F8CFF;
    border-bottom-color: #4F8CFF;
    font-weight: bold;
}
.input {
    height: 88rpx;
    line-height: 88rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 0 24rpx;
    margin-bottom: 24rpx;
    font-size: 28rpx;
}
.ph {
    color: #BBBBBB;
}
.btn-primary {
    margin-top: 24rpx;
}
.tip {
    text-align: center;
    color: #BBBBBB;
    font-size: 24rpx;
    margin-top: 24rpx;
}
</style>
