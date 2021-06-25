import schema from 'async-validator';

export default {
	// 返回验证器
	// this.$rules.validator(formData, rules).then().catch({ errors, fields })
	validator: (data, rules) => {
		var validator = new schema(rules);
		return validator.validate(data).catch(({errors}) => {
			throw errors[0].message
		})
	},
	// 必填
	required: (message = '此项必填') => {
		return {
			required: true,
			message
		}
	},
	// 英文数字
	enNum: (message = '请输入英文、数字') => {
		const reg = /^[A-Za-z0-9]+$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 邮箱
	email: (message = '请输入正确的邮箱地址') => {
		const reg = /^[a-zA-Z0-9][a-zA-Z0-9 . _-]+(@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+)$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 中文
	chinese: (message = '请输入中文') => {
		const reg = /^[\u4e00-\u9fa5]+$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 英文
	english: (message = '请输入英文') => {
		const reg = /^[A-Za-z]+$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 数字
	float: (message = '请输入合法的数字') => {
		const reg = /^[0-9]+([.]{1}[0-9]+){0,1}$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 英文数字
	enNum: (message = '请输入英文、数字') => {
		const reg = /^[A-Za-z0-9]+$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 整数
	number: (message = '请输入合法的数字') => {
		const reg = /^[0-9]\d*$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 整数
	cnEnNum: (message = '请输入中文、大小写英文、数字') => {
		const reg = /^[\u4e00-\u9fa5|A-Za-z0-9]+$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 手机号
	mobile: (message = '请输入正确的手机号码') => {
		const reg =  /^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(16[2|6|7])|(18[0-9])|(17([0-1]|[3]|[5-8]))|(19[1|8|9]))\d{8}$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 座机
	phone: (message = '请输入正确的座机号码') => {
		const reg =  /^((\d{3,4}-\d{7,8})|(\d{7}-\d{1,12})|(\d{8}-\d{1,11})|(\d{11}-\d{1,8})|(\d{7,8})|(\d{11,20})|(\d{3}-\d{8}-\d{1,7})|(\d{3}-\d{7}-\d{1,8})|(\d{4}-\d{7}-\d{1,7})|(\d{4}-\d{8}-\d{1,6}))$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 座机和手机
	phoneAll: (message = '请输入正确的电话号码') => {
		const reg =  /^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(16[2|6|7])|(18[0-9])|(17([0-1]|[3]|[5-8]))|(19[1|8|9]))\d{8}$|^((\d{3,4}-\d{7,8})|(\d{7}-\d{1,12})|(\d{8}-\d{1,11})|(\d{11}-\d{1,8})|(\d{7,8})|(\d{11,20})|(\d{3}-\d{8}-\d{1,7})|(\d{3}-\d{7}-\d{1,8})|(\d{4}-\d{7}-\d{1,7})|(\d{4}-\d{8}-\d{1,6}))$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 身份证
	identity: (message = '请输入正确的身份证号') => {
		const reg =  /^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$|^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X|x)$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	// 身份证
	postal: (message = '请输入正确的邮政编码') => {
		const reg = /^\d{6}$/
		const validator = (rule, value, callback) => {
			if (value === '' || reg.test(value)) {
				callback()
			} else {
				callback(message)
			}
		}
		return {
			validator
		}
	},
	custom: (message = '',fn) => {
		const validator = (rule, value, callback) => {
			fn(rule, value, callback)
		}
		return {
			validator
		}
	},
}
