import CryptoJS from 'crypto-js'
import request from '@/api'
const encryptKey = 'WfJTKO9S4eLkrPz2JKrAnzdb'
const encryptIV = 'D076D35C'
var pubkeyHex = '04AF0FCC45059AA342221352E5268614F2FF7A430497B156C0DEE6E751AB44E4957E9E69299E2CD38E25985B7BD34E0E7BBA683DE4725A6A8CD07E19BFF8BEF44D'

// 深度复制
export function deepClone (obj) {
  let result = Array.isArray(obj) ? [] : {}
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      if (typeof obj[key] === 'object') {
        result[key] = deepClone(obj[key])
      } else {
        result[key] = obj[key]
      }
    }
  }
  return result
}
// 3DES加密
export async function desEncrypt (str, key = encryptKey, iv = encryptIV) {
  const cipherMode = 0 // 1 - C1C3C2，0 - C1C2C3，默认为1
  var codeStr = ''
  
    var cryptoKey = CryptoJS.enc.Utf8.parse(key)
    var cryptoIv = CryptoJS.enc.Utf8.parse(iv.substr(0, 8))
    var encodeStr = CryptoJS.TripleDES.encrypt(str, cryptoKey, {
      iv: cryptoIv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    })
    codeStr = encodeStr.toString()
	 
  return codeStr  

}

// 3DES解密
export function desDecrypt (str, key = encryptKey, iv = encryptIV) {
  var cryptoKey = CryptoJS.enc.Utf8.parse(key)
  var cryptoIv = CryptoJS.enc.Utf8.parse(iv.substr(0, 8))
  var decryptStr = CryptoJS.TripleDES.decrypt(str, cryptoKey, {
    iv: cryptoIv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return decryptStr.toString(CryptoJS.enc.Utf8)
}

// 随机生成由字母+数字的字符串
export function randomWord (randomFlag, min, max) {
  // randomFlag: Boolean 是否随机个数
  // min 最少个数
  // max 最大个数
  var str = ''
  var range = min
  var arr = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']
  // 随机产生
  if (randomFlag) {
    range = Math.round(Math.random() * (max - min)) + min
  }
  var pos = ''
  for (var i = 0; i < range; i++) {
    pos = Math.round(Math.random() * (arr.length - 1))
    str += arr[pos]
  }
  return str
}

export function formatTime(time) {
	if (typeof time !== 'number' || time < 0) {
		return time
	}

	var hour = parseInt(time / 3600)
	time = time % 3600
	var minute = parseInt(time / 60)
	time = time % 60
	var second = time

	return ([hour, minute, second]).map(function (n) {
		n = n.toString()
		return n[1] ? n : '0' + n
	}).join(':')
}

export function formatLocation(longitude, latitude) {
	if (typeof longitude === 'string' && typeof latitude === 'string') {
		longitude = parseFloat(longitude)
		latitude = parseFloat(latitude)
	}

	longitude = longitude.toFixed(2)
	latitude = latitude.toFixed(2)

	return {
		longitude: longitude.toString().split('.'),
		latitude: latitude.toString().split('.')
	}
}

export var dateUtils = {
	UNITS: {
		'年': 31557600000,
		'月': 2629800000,
		'天': 86400000,
		'小时': 3600000,
		'分钟': 60000,
		'秒': 1000
	},
	humanize: function (milliseconds) {
		var humanize = '';
		for (var key in this.UNITS) {
			if (milliseconds >= this.UNITS[key]) {
				humanize = Math.floor(milliseconds / this.UNITS[key]) + key + '前';
				break;
			}
		}
		return humanize || '刚刚';
	},
	format: function (dateStr) {
		var date = this.parse(dateStr)
		var diff = Date.now() - date.getTime();
		if (diff < this.UNITS['天']) {
			return this.humanize(diff);
		}
		var _format = function (number) {
			return (number < 10 ? ('0' + number) : number);
		};
		return date.getFullYear() + '/' + _format(date.getMonth() + 1) + '/' + _format(date.getDay()) + '-' +
			_format(date.getHours()) + ':' + _format(date.getMinutes());
	},
	parse: function (str) { //将"yyyy-mm-dd HH:MM:ss"格式的字符串，转化为一个Date对象
		var a = str.split(/[^0-9]/);
		return new Date(a[0], a[1] - 1, a[2], a[3], a[4], a[5]);
  },
  
};
