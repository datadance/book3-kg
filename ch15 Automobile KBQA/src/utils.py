import pickle
import jieba
import math

from filepath import DataPath

# 读取jieba中文分词所需的用户自定义词典数据以及停止词数据
jieba.load_userdict(DataPath.util_path + "userdicts.txt")
stopwords = [line.strip() for line in open(DataPath.util_path + 'hlt_stop_words.txt', encoding='UTF-8').readlines()]


def word_segment(sentence):
	"""利用jieba进行中文分词"""
	words = jieba.cut(sentence.strip().replace('\n', '').replace('\r', '').replace('\t', ''), cut_all=False)
	stayed_list = []
	for word in words:
		if word not in stopwords:
			stayed_list.append(word.strip())
	return stayed_list


def save_model(model, file_name):
	"""用于保存模型"""
	with open(file_name, "wb") as f:
		pickle.dump(model, f)


def load_model(file_name):
	"""用于加载模型"""
	with open(file_name, "rb") as f:
		model = pickle.load(f)
	return model


def compute_cosine(words1, words2):
	"""计算words1与words2的相似度"""
	words1_dict = {}
	words2_dict = {}
	for word in words1:
		if word != '' and word in words1_dict:
			num = words1_dict[word]
			words1_dict[word] = num + 1
		elif word != '':
			words1_dict[word] = 1
		else:
			continue
	for word in words2:
		if word != '' and word in words2_dict:
			num = words2_dict[word]
			words2_dict[word] = num + 1
		elif word != '':
			words2_dict[word] = 1
		else:
			continue

	dic1 = sorted(words1_dict.items(), key=lambda asd: asd[1], reverse=True)
	dic2 = sorted(words2_dict.items(), key=lambda asd: asd[1], reverse=True)

	words_key = []
	for i in range(len(dic1)):
		words_key.append(dic1[i][0])
	for i in range(len(dic2)):
		if dic2[i][0] in words_key:
			pass
		else:
			words_key.append(dic2[i][0])
	vect1 = []
	vect2 = []
	for word in words_key:
		if word in words1_dict:
			vect1.append(words1_dict[word])
		else:
			vect1.append(0)
		if word in words2_dict:
			vect2.append(words2_dict[word])
		else:
			vect2.append(0)

	s = 0
	sq1 = 0
	sq2 = 0
	for i in range(len(vect1)):
		s += vect1[i] * vect2[i]
		sq1 += pow(vect1[i], 2)
		sq2 += pow(vect2[i], 2)
	try:
		result = round(float(s) / (math.sqrt(sq1) * math.sqrt(sq2)), 2)
	except ZeroDivisionError:
		result = 0.0
	return result
