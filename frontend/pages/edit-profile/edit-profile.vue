<template>
    <view class="container">
        <view class="card">
            <!-- 头像 -->
            <view class="avatar-section" @tap="changeAvatar">
                <image v-if="avatar" :src="avatarUrl" class="avatar" mode="aspectFill" />
                <view v-else class="avatar avatar-fallback">📷</view>
                <text class="avatar-hint">点击更换头像</text>
            </view>

            <view class="form-item">
                <text class="label">昵称</text>
                <input class="input" v-model="nickname" placeholder="请输入昵称" placeholder-class="ph" :maxlength="20" />
            </view>

            <view class="form-item">
                <text class="label">性别</text>
                <picker mode="selector" :range="genderOptions" :value="genderIndex" @change="onGenderChange">
                    <view class="picker-value">
                        <text :class="{ placeholder: !gender }">{{ gender || '请选择性别' }}</text>
                        <text class="picker-arrow">▾</text>
                    </view>
                </picker>
            </view>

            <view class="form-item">
                <text class="label">出生日期</text>
                <picker mode="date" :value="birthday || today" :end="today" :start="'1950-01-01'" @change="onBirthdayChange">
                    <view class="picker-value">
                        <text :class="{ placeholder: !birthday }">{{ birthday || '请选择出生日期' }}</text>
                        <text class="picker-arrow">▾</text>
                    </view>
                </picker>
            </view>

            <view class="form-item">
                <text class="label">所在地区</text>
                <picker mode="multiSelector" :range="range" :value="regionIndex"
                        @columnchange="onColumnChange" @change="onRegionChange">
                    <view class="picker-value">
                        <text :class="{ placeholder: !regionPicked }">{{ regionPicked ? regionText : '请选择省 / 市 / 区' }}</text>
                        <text class="picker-arrow">▾</text>
                    </view>
                </picker>
            </view>

            <view class="form-item">
                <text class="label">个性签名</text>
                <textarea class="textarea" v-model="signature" placeholder="写一句介绍自己吧（会展示在我的页面）"
                          placeholder-class="ph" :maxlength="60" auto-height />
                <text class="counter">{{ signature.length }}/60</text>
            </view>

            <view class="btn-primary" :class="{ disabled: saving }" @tap="save">保 存</view>
        </view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'
import { getProfile, updateProfile, updateAvatar } from '@/common/api.js'
import regionData from '@/common/region.js'

export default {
    data() {
        return {
            nickname: '',
            birthday: '',
            today: '',
            signature: '',
            avatar: '',
            gender: '',
            genderOptions: ['男', '女'],
            provinces: [],
            cities: [],
            districts: [],
            regionIndex: [0, 0, 0],
            regionPicked: false,
            saving: false
        }
    },
    computed: {
        avatarUrl() {
            return this.avatar ? BASE_URL + this.avatar : ''
        },
        genderIndex() {
            const i = this.genderOptions.indexOf(this.gender)
            return i >= 0 ? i : 0
        },
        range() {
            return [this.provinces, this.cities, this.districts]
        },
        regionText() {
            const pi = this.regionIndex[0]
            const ci = this.regionIndex[1]
            const di = this.regionIndex[2]
            const p = this.provinces[pi] || ''
            const c = this.cities[ci] || ''
            const d = this.districts[di] || ''
            return [p, c, d].filter(x => x).join(' ')
        }
    },
    onLoad() {
        const now = new Date()
        const y = now.getFullYear()
        const m = String(now.getMonth() + 1).padStart(2, '0')
        const d = String(now.getDate()).padStart(2, '0')
        this.today = `${y}-${m}-${d}`

        // 初始化省市区第一级
        this.provinces = regionData.map(x => x.name)
        this.cities = regionData[0].cities.map(c => c.name)
        this.districts = regionData[0].cities[0].districts

        getProfile().then((data) => {
            this.nickname = data.nickname || ''
            this.birthday = data.birthday || ''
            this.signature = data.signature || ''
            this.avatar = data.avatar || ''
            this.gender = data.gender || ''
            this.applyRegion(data.province, data.city, data.district)
        })
    },
    methods: {
        onBirthdayChange(e) {
            this.birthday = e.detail.value
        },
        onGenderChange(e) {
            this.gender = this.genderOptions[e.detail.value]
        },
        applyRegion(province, city, district) {
            if (!province) return
            const pi = this.provinces.findIndex(x => x === province)
            if (pi < 0) return
            const cities = regionData[pi].cities
            this.cities = cities.map(c => c.name)
            const ci = cities.findIndex(c => c.name === city)
            const realCi = ci >= 0 ? ci : 0
            this.districts = cities[realCi].districts
            const di = this.districts.findIndex(x => x === district)
            this.regionIndex = [pi, realCi, di >= 0 ? di : 0]
            this.regionPicked = true
        },
        onColumnChange(e) {
            const column = e.detail.column
            const value = e.detail.value
            if (column === 0) {
                this.regionIndex = [value, 0, 0]
                this.cities = regionData[value].cities.map(c => c.name)
                this.districts = regionData[value].cities[0].districts
            } else if (column === 1) {
                this.regionIndex = [this.regionIndex[0], value, 0]
                this.districts = regionData[this.regionIndex[0]].cities[value].districts
            } else {
                this.regionIndex = [this.regionIndex[0], this.regionIndex[1], value]
            }
        },
        onRegionChange(e) {
            this.regionIndex = e.detail.value
            this.regionPicked = true
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
                                updateAvatar({ avatarUrl: data.data.url }).then((profile) => {
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
        save() {
            if (!this.nickname.trim()) {
                uni.showToast({ title: '昵称不能为空', icon: 'none' })
                return
            }
            if (this.saving) return
            this.saving = true
            const pi = this.regionIndex[0]
            const ci = this.regionIndex[1]
            const di = this.regionIndex[2]
            updateProfile({
                nickname: this.nickname.trim(),
                birthday: this.birthday || '',
                signature: this.signature.trim(),
                gender: this.gender || '',
                province: this.regionPicked ? (this.provinces[pi] || '') : '',
                city: this.regionPicked ? (this.cities[ci] || '') : '',
                district: this.regionPicked ? (this.districts[di] || '') : ''
            }).then((profile) => {
                uni.setStorageSync('nickname', this.nickname.trim())
                uni.setStorageSync('signature', profile.signature || '')
                uni.showToast({ title: '保存成功', icon: 'success' })
                setTimeout(() => uni.navigateBack(), 800)
            }).finally(() => {
                this.saving = false
            })
        }
    }
}
</script>

<style scoped>
.avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 40rpx;
}
.avatar {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: #F5F6FA;
}
.avatar-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    color: #BBBBBB;
}
.avatar-hint {
    font-size: 24rpx;
    color: #999999;
    margin-top: 14rpx;
}
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
.picker-value {
    height: 84rpx;
    line-height: 84rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.picker-value .placeholder {
    color: #BBBBBB;
}
.picker-arrow {
    color: #BBBBBB;
}
.textarea {
    width: 100%;
    min-height: 120rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}
.counter {
    display: block;
    text-align: right;
    font-size: 22rpx;
    color: #BBBBBB;
    margin-top: 8rpx;
}
.btn-primary.disabled {
    opacity: 0.6;
}
</style>
