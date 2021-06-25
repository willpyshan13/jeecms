<template>
    <view class="neil-modal" @touchmove.stop="bindTouchmove" v-if="isOpen === true || isOpen === 'true'">
        <view class="neil-modal__mask" :class="showAnimation === true || showAnimation === 'true' ? 'neil-modal--show' : ''" @click="closeModal"></view>
        <view v-if="showContent"
			class="neil-modal__container" :class="showAnimation === true || showAnimation === 'true' ? 'neil-modal--show' : ''">
            <view class="neil-modal__header fsu-34 Medium" v-if="title.length > 0">{{title}}</view>
            <view class="neil-modal__content"  :style="{textAlign:align}">
                <template v-if="content">
                    <text class="modal-content fsu-34">{{content}}</text>
                </template>
                <template v-else>
                    <slot />
                </template>
            </view>
            <view class="neil-modal__footer">
				<view v-if="showCancel" class="neil-modal__footer-left fsu-34 t-color Semilight" @click="clickLeft" :style="{color:cancelColor}"
                    hover-class="neil-modal__footer-hover" :hover-start-time="20" :hover-stay-time="70">
                    {{cancelText}}
                </view>
				<view class="neil-modal__footer-right fsu-34 t-color Semilight" @click="clickRight" :style="{color:confirmColor}" hover-class="neil-modal__footer-hover"
				    :hover-start-time="20" :hover-stay-time="70">
				    {{confirmText}}
				</view>
				
                
                
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        name: 'neil-modal',
        props: {
            title: { //标题
                type: String,
                default: ''
            },
            content: String, //提示的内容
            align: { //content 的对齐方式left/center/right
                type: String,
                default: 'center'
            },
            cancelText: { //取消按钮的文字，默认为"取消"
                type: String,
                default: '取消'
            },
            cancelColor: { //取消按钮颜色
                type: String,
                default: '#666666'
            },
            confirmText: { //确定按钮的文字，默认为"确定"
                type: String,
                default: '确定'
            },
            confirmColor: { //确认按钮颜色
                type: String,
                default: '#666666'
            },
            showCancel: { //是否显示取消按钮，默认为 true
                type: [Boolean, String],
                default: true
            },
            show: { //是否显示模态框
                type: [Boolean, String],
                default: false
            },
			showContent: { //是否显示内容，默认为 true
                type: [Boolean, String],
                default: true
            },
        },
        data() {
            return {
                isOpen: false,
                showAnimation:false,//是否显示遮罩层（透明度变为1）
                inWatchTimeout:false,//是否超时。避免两个弹框的出现
                inMethodsTimeout:false
            }
        },
        watch: {
            show(val) {
                if(this.inWatchTimeout){
                    return
                }
                if (val) {
                	this.isOpen = val
                    setTimeout(() => {
                    	this.showAnimation = val
                    }, 50)
                } else {
                    this.showAnimation = val
                    this.inWatchTimeout = true
                	setTimeout(() => {
                		this.isOpen = val
                        this.inWatchTimeout = false
                	}, 200)
                }
            }
        },
        created() {
        	this.isOpen = this.show
        	setTimeout(() => {
        		this.showAnimation = this.show
        	}, 50)
        },
        methods: {
            bindTouchmove() {},
            clickLeft() {
                setTimeout(() => {
                	this.$emit('cancel')
                }, 200)
                this.closeModal()
            },
            clickRight() {
                setTimeout(() => {
                	this.$emit('confirm')
                }, 200)
                this.closeModal()
            },
            closeModal() {
                this.showAnimation = false
                this.inMethodsTimeout = true
                setTimeout(() => {
                	this.isOpen = false
                    this.inMethodsTimeout = false
                    this.$emit('close')
                }, 200)
            }
        }
    }
</script>

<style lang="scss">
    $bg-color-mask:rgba(0, 0, 0, 0.5); //遮罩颜色
    $bg-color-hover:#f1f1f1; //点击状态颜色

    .neil-modal {
        position: fixed;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        z-index: 1000;
		box-sizing: border-box;

        &__header {
            position: relative;
			
            // overflow: hidden;
            // text-overflow: ellipsis;
			width: 528upx;
			margin-left: 70upx;
			display: flex;
			justify-content: center;
            // white-space: nowrap;
            line-height: 1.5;
           color:rgba(51,51,51,1);
            text-align: center;
			line-height: 1;
			box-sizing: border-box;
			margin-bottom: 80upx;
			line-height:48upx;
            &::after {
                content: " ";
                position: absolute;
                left: 0;
                bottom: 0;
                right: 0;
                height: 1px;
                // border-top: 1px solid #e5e5e5;
                transform-origin: 0 0;
                transform: scaleY(.5);
            }
        }

        &__container {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%,-50%) ;
            transition: transform 0.3s;
			padding: 90upx 0;
			width: 670upx;
            border-radius: 10upx;
            background-color: #fff;
            overflow: hidden;
            opacity: 0;
            transition: opacity 200ms ease-in;
			
        }

        &__content {
            position: relative;
            color: #333;
            font-size: 28upx;
            box-sizing: border-box;
            line-height: 1;
			text-align: center;
            &::after {
                content: " ";
                position: absolute;
                left: 0;
                bottom: -1px;
                right: 0;
                height: 1px;
                transform-origin: 0 0;
                transform: scaleY(.5);
            }
        }

        &__footer {
            position: relative;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            color:rgba(102,102,102,1);
            font-size: 28upx;
            display: flex;
            flex-direction: row;
			margin-left: 115upx;
            &-left,
            &-right {
                position: relative;
                flex: 1;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                height: 72upx;
				border:1px solid rgba(204,204,204,1);
				border-radius:36upx;
                line-height: 72upx;
				width: 200upx;
                text-align: center;
                background-color: #fff;
				color:rgba(102,102,102,1);
            }

            &-right {
                color:rgba(102,102,102,1);
				margin-left: 40upx;
				margin-right: 115upx;
            }

            &-left::after {
                content: " ";
                position: absolute;
                right: -1px;
                top: 0;
                width: 1px;
                bottom: 0;
                transform-origin: 0 0;
                transform: scaleX(.5);
            }

            &-hover {
                // background-color: $bg-color-hover;
            }
        }

        &__mask {
            display: block;
            position: absolute;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: $bg-color-mask;
            opacity: 0;
            transition: opacity 200ms ease-in;
        }

        &--padding {
            padding: 32upx 24upx;
            min-height: 90upx;
        }
        &--show{
            opacity: 1;
        }
    }
</style>
