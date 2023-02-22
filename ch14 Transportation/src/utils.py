import jieba
import filePath

jieba.load_userdict(filePath.USERDICTS_PATH)
stopwords = [line.strip() for line in open(filePath.STOP_WORDS_PATH, encoding='UTF-8').readlines()]

def word_segment(sentence):
	"""利用jieba进行中文分词"""
	words = jieba.cut(sentence.strip().replace('\n', '').replace('\r', '').replace('\t', ''), cut_all=False)
	stayed_list = []
	for word in words:
		if word not in stopwords:
			stayed_list.append(word.strip())
	return stayed_list

class Trans_Source_Data:
	land_name_list = ['肥西县', '肥东县', '长丰县', '瑶海区', '庐阳区', '蜀山区', '包河区', '政务区', '滨湖新区', '新站区', '高新区', '经开区']
	# 道路干线名
	road_name_list = ['长江西路', '金寨路', '芜湖路', '望江路', '黄山路', '潜山路', '徽州大道', '永和路',
	                  '阜阳路', '淮河路', '胜利路', '蒙城路', '龙川路', '南二环', '合作化路', '阜南路',
	                  '铜陵路', '临泉路', '和平路', '合裕路', '临泉路', '马鞍山路', '宿松路', '宁国路',
	                  '包河大道', '水阳江路', '东至路', '休宁路', '习友路', '望江西路', '北京路', '繁华大道']
	# 通道
	channel_list = ['直行', '右转', '左转', '直右', '三向', '非机动车']
	# 道路问题
	symptom1_list = ['早高峰', '晚高峰', '平峰', '东北方向', '西方向', '东方向', '北方向', '南方向', '东南方向']
	symptom2_list = ['失衡', '溢出', '拥堵', '接线端子错误', '未划分时段', '未设置非对称相位', '绿冲突', '配时方案不合理']
	# 数字和字母组成的随机字符串
	random_str_list = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'z', 'y', 'x', 'w', 'v', 'u', 't', 's', 'r', 'q',
	                   'p', 'o',
	                   'n', 'm', 'l', 'k', 'j', 'i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a']
