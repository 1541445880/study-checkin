<script>
export default {
    onLaunch: function() {
        console.log('App Launch')
    }
}
</script>

<style>
/* 全局样式 */
page {
    background-color: #F5F6FA;
    font-size: 14px;
    color: #333333;
}

.container {
    padding: 20px;
}

.card {
    background-color: #FFFFFF;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.btn-primary {
    background-color: #4F8CFF;
    color: #FFFFFF;
    border-radius: 24px;
    font-size: 16px;
    height: 48px;
    line-height: 48px;
    text-align: center;
}

.btn-primary.disabled {
    background-color: #B8CDFF;
}
</style>
