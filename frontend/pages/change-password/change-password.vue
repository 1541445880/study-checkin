<template>
    <view class="container">
        <view class="card">
            <view class="form-item">
                <text class="label">旧密码</text>
                <input class="input" v-model="oldPassword" type="password" placeholder="请输入旧密码" placeholder-class="ph" />
            </view>
            <view class="form-item">
                <text class="label">新密码</text>
                <input class="input" v-model="newPassword" type="password" placeholder="6-20 位" placeholder-class="ph" />
            </view>
            <view class="form-item">
                <text class="label">确认新密码</text>
                <input class="input" v-model="confirmPassword" type="password" placeholder="再次输入新密码" placeholder-class="ph" />
            </view>

            <view class="btn-primary" :class="{ disabled: saving }" @tap="save">确认修改</view>
        </view>
    </view>
</template>

<script>
import { updatePassword } from '@/common/api.js'

export default {
    data() {
        return {
            oldPassword: '',
            newPassword: '',
            confirmPassword: '',
            saving: false
        }
    },
    methods: {
        save() {
            if (!this.oldPassword) {
                uni.showToast({ title: '请输入旧密码', icon: 'none' })
                return
            }
            if (this.newPassword.length < 6 || this.newPassword.length > 20) {
                uni.showToast({ title: '新密码需 6-20 位', icon: 'none' })
                return
            }
            if (this.newPassword !== this.confirmPassword) {
                uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
                return
            }
            if (this.saving) return
            this.saving = true
            updatePassword({
                oldPassword: this.oldPassword,
                newPassword: this.newPassword
            }).then(() => {
                uni.showToast({ title: '密码已修改，请重新登录', icon: 'success' })
                setTimeout(() => {
                    uni.removeStorageSync('token')
                    uni.reLaunch({ url: '/pages/login/login' })
                }, 900)
            }).finally(() => {
                this.saving = false
            })
        }
    }
}
</script>

<style scoped>
.form-item {
    margin-bottom: 32rpx;
}
.label {
    display: block;
    font-size: 26rpx;
    color: #666666;
    margin-bottom: 12rpx;
}
.input {
    height: 84rpx;
    line-height: 84rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
}
.ph {
    color: #BBBBBB;
}
.btn-primary.disabled {
    opacity: 0.6;
}
</style>
