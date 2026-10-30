// 请求封装
// 本地开发时用 localhost:8080，部署后改成服务器地址（如 http://你的公网IP:8080 或域名）
const BASE_URL = ''

export {
	BASE_URL
}

function request(options) {
	return new Promise((resolve, reject) => {
		const token = uni.getStorageSync('token')
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: {
				'Content-Type': 'application/json',
				'Authorization': token ? 'Bearer ' + token : ''
			},
			success: (res) => {
				if (res.statusCode === 200) {
					if (res.data.code === 200) {
						resolve(res.data.data)
					} else {
						uni.showToast({
							title: res.data.message || '操作失败',
							icon: 'none'
						})
						reject(res.data)
					}
				} else if (res.statusCode === 401) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}, 600)
					reject(res)
				} else {
					uni.showToast({
						title: '请求失败',
						icon: 'none'
					})
					reject(res)
				}
			},
			fail: (err) => {
				uni.showToast({
					title: '网络错误，请检查后端是否启动',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

export default request