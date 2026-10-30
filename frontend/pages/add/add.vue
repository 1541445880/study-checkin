<template>
    <view class="container">
        <view class="card">
            <view class="card-title">{{ isEdit ? '编辑打卡' : (isToday ? '今日打卡' : '补签打卡') }}</view>

            <!-- 日期选择 -->
            <view class="form-item">
                <text class="label">打卡日期</text>
                <picker mode="date" :value="date" :end="today" :start="minDate" @change="onDateChange">
                    <view class="picker-value">
                        <text :class="{ 'is-today': isToday }">{{ dateText }}</text>
                        <text class="picker-arrow">▾</text>
                    </view>
                </picker>
                <view v-if="!isToday" class="date-tip">补签：补记过去某一天的学习</view>
            </view>

            <!-- 学习时长 -->
            <view class="form-item">
                <text class="label">学习时长（分钟）</text>
                <input class="input" v-model="minutes" type="number" placeholder="如 60" placeholder-class="ph" />
                <view class="quick-minutes">
                    <text v-for="m in quickList" :key="m" :class="['qm', Number(minutes) === m ? 'active' : '']"
                          @tap="minutes = m">{{ m }}min</text>
                </view>
            </view>

            <!-- 学习内容 -->
            <view class="form-item">
                <text class="label">学习内容</text>
                <textarea class="textarea" v-model="content" placeholder="今天学了什么？（选填）"
                          placeholder-class="ph" :maxlength="200" auto-height />
            </view>

            <!-- 心情 -->
            <view class="form-item">
                <text class="label">心情</text>
                <view class="moods">
                    <text v-for="m in moods" :key="m" :class="['mood', mood === m ? 'active' : '']"
                          @tap="mood = m">{{ m }}</text>
                </view>
            </view>

            <!-- 打卡照片 -->
            <view class="form-item">
                <text class="label">打卡照片（选填，不限数量）</text>
                <view class="img-grid">
                    <view v-for="(img, i) in images" :key="i" class="img-cell">
                        <image :src="imgUrl(img)" class="img-thumb" mode="aspectFill" @tap="previewImage(i)" />
                        <view class="img-del" @tap.stop="removeImage(i)">×</view>
                    </view>
                    <view class="img-cell img-add" @tap="chooseImages">
                        <text class="img-add-icon">＋</text>
                    </view>
                </view>
                <view v-if="uploading" class="uploading-tip">上传中...</view>
            </view>

            <view class="btn-primary" :class="{ disabled: submitting }" @tap="submit">
                {{ submitting ? '提交中...' : (isEdit ? '保存修改' : (isToday ? '打 卡' : '补 签')) }}
            </view>
        </view>
    </view>
</template>

<script>
import { BASE_URL } from '@/common/request.js'
import { doCheckIn, getCheckIn, updateCheckIn } from '@/common/api.js'

export default {
    data() {
        const now = new Date()
        const today = this.fmt(now)
        const min = new Date(now.getTime() - 365 * 24 * 3600 * 1000)
        return {
            today,
            minDate: this.fmt(min),
            date: today,
            minutes: '',
            content: '',
            mood: '专注',
            moods: ['专注', '高效', '一般', '充实', '疲惫'],
            quickList: [15, 30, 60, 90, 120],
            images: [],
            uploading: false,
            submitting: false,
            editId: null
        }
    },
    onLoad(options) {
        // 编辑模式：回显某条记录
        if (options && options.id) {
            this.editId = options.id
            this.loadRecord(options.id)
            return
        }
        // 支持从专注页带时长过来
        if (options && options.minutes && Number(options.minutes) > 0) {
            this.minutes = String(options.minutes)
        }
    },
    computed: {
        isEdit() {
            return !!this.editId
        },
        isToday() {
            return this.date === this.today
        },
        dateText() {
            if (this.isToday) return '今天（' + this.date + '）'
            return this.date
        }
    },
    methods: {
        fmt(d) {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        },
        loadRecord(id) {
            getCheckIn(id).then((r) => {
                if (!r) return
                this.date = r.checkDate || this.today
                this.minutes = String(r.studyMinutes || '')
                this.content = r.content || ''
                this.mood = r.mood || '专注'
                this.images = r.imageList || []
            })
        },
        onDateChange(e) {
            this.date = e.detail.value
        },
        imgUrl(img) {
            return img && img.indexOf('http') === 0 ? img : (BASE_URL + img)
        },
        chooseImages() {
            uni.chooseImage({
                count: 9,
                sizeType: ['compressed'],
                success: (res) => {
                    const paths = res.tempFilePaths || []
                    if (paths.length) this.uploadImages(paths)
                }
            })
        },
        uploadImages(paths) {
            this.uploading = true
            let pending = paths.length
            const ok = []
            paths.forEach((p) => {
                this.uploadOne(p).then((url) => {
                    ok.push(url)
                }).catch(() => {}).finally(() => {
                    pending--
                    if (pending === 0) {
                        this.uploading = false
                        if (ok.length) this.images = this.images.concat(ok)
                        if (ok.length < paths.length) {
                            uni.showToast({ title: '部分图片上传失败', icon: 'none' })
                        }
                    }
                })
            })
        },
        uploadOne(filePath) {
            return new Promise((resolve, reject) => {
                uni.uploadFile({
                    url: BASE_URL + '/api/upload/image',
                    filePath,
                    name: 'file',
                    header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
                    success: (res) => {
                        try {
                            const data = JSON.parse(res.data)
                            if (data.code === 200 && data.data && data.data.url) {
                                resolve(data.data.url)
                            } else {
                                reject(new Error(data.message || '上传失败'))
                            }
                        } catch (e) {
                            reject(e)
                        }
                    },
                    fail: reject
                })
            })
        },
        removeImage(i) {
            this.images.splice(i, 1)
        },
        previewImage(i) {
            const urls = this.images.map(img => this.imgUrl(img))
            uni.previewImage({ current: urls[i], urls })
        },
        submit() {
            if (this.submitting) return
            if (!this.minutes || Number(this.minutes) <= 0) {
                uni.showToast({ title: '请填写学习时长', icon: 'none' })
                return
            }
            this.submitting = true
            const payload = {
                studyMinutes: Number(this.minutes),
                content: this.content,
                mood: this.mood,
                checkDate: this.date,
                images: this.images
            }
            const req = this.isEdit ? updateCheckIn(this.editId, payload) : doCheckIn(payload)
            req.then(() => {
                uni.showToast({ title: this.isEdit ? '已保存' : (this.isToday ? '打卡成功！' : '补签成功！'), icon: 'success' })
                setTimeout(() => {
                    uni.navigateBack()
                }, 800)
            }).finally(() => {
                this.submitting = false
            })
        }
    }
}
</script>

<style scoped>
.card-title {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 24rpx;
}
.form-item {
    margin-bottom: 28rpx;
}
.label {
    display: block;
    font-size: 26rpx;
    color: #666666;
    margin-bottom: 12rpx;
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
.is-today {
    color: #4F8CFF;
    font-weight: bold;
}
.picker-arrow {
    color: #BBBBBB;
}
.date-tip {
    font-size: 22rpx;
    color: #E8A23D;
    margin-top: 10rpx;
}
.input {
    height: 84rpx;
    line-height: 84rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
}
.quick-minutes {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16rpx;
}
.qm {
    padding: 10rpx 24rpx;
    background: #F5F6FA;
    border-radius: 28rpx;
    margin-right: 14rpx;
    margin-bottom: 10rpx;
    font-size: 24rpx;
    color: #666666;
}
.qm.active {
    background: #4F8CFF;
    color: #FFFFFF;
}
.textarea {
    width: 100%;
    min-height: 140rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}
.moods {
    display: flex;
    flex-wrap: wrap;
}
.mood {
    padding: 12rpx 28rpx;
    background: #F5F6FA;
    border-radius: 32rpx;
    margin-right: 16rpx;
    margin-bottom: 12rpx;
    font-size: 26rpx;
    color: #666666;
}
.mood.active {
    background: #4F8CFF;
    color: #FFFFFF;
}
.img-grid {
    display: flex;
    flex-wrap: wrap;
}
.img-cell {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    margin: 0 16rpx 16rpx 0;
}
.img-thumb {
    width: 100%;
    height: 100%;
    border-radius: 12rpx;
}
.img-del {
    position: absolute;
    top: -12rpx;
    right: -12rpx;
    width: 44rpx;
    height: 44rpx;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    color: #FFFFFF;
    text-align: center;
    line-height: 40rpx;
    font-size: 30rpx;
}
.img-add {
    border: 2rpx dashed #CCCCCC;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}
.img-add-icon {
    font-size: 60rpx;
    color: #BBBBBB;
}
.uploading-tip {
    font-size: 24rpx;
    color: #999999;
    margin-top: 8rpx;
}
.btn-primary.disabled {
    opacity: 0.6;
}
</style>
