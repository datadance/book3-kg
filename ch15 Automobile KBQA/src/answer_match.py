from utils import word_segment, compute_cosine

def link_entity_result(property_result, ner_result, conception_tree):
	'''
	通过概念树，连接符合概念数设计的实体与属性
	:param property_result: 属性分类与推理的结果
	:param ner_result: 实体识别与链接的结果
	:param conception_tree: 概念树
	:return: 存储符合概念树设计的实体、属性的字典数据
	'''
	ner_ = []
	# 实体识别的结果进行处理，提取出实体以及实体类别
	for e, e_class in ner_result:
		for dic in conception_tree:
			if dic['conception'] == e_class:
				property_list = [list(property_)[0] for property_ in dic['property']]
	# 判断实体识别出的实体类别与属性分类的结果是否匹配
				if property_result in property_list:
					ner_.append(e)
				else:
					pass

	if len(ner_) >= 1: # 如果实体属性匹配结果不为0，则读取最后一个结果
		ner_property_link = dict()
		ner_property_link['entity'] = ner_[-1]
		ner_property_link['property'] = property_result
	else: # 否则结果为空
		ner_property_link = ''
	return ner_property_link

def return_answer(kg_triplet, ner_property_link, question_list):
	'''
	# 在知识库中搜索并返回答案
	:param kg_triplet: 知识图谱三元组
	:param ner_property_link: 符合概念树设计的实体、属性的字典数据
	:param question_list: 对问句进行中文分词处理后列表
	:return: 存储结果的字符串
	'''
	if ner_property_link != '': # 实体属性匹配有结果
		answer = 'Insufficient knowledge base capability'
		for triplet in kg_triplet:
			if triplet['entity'] == ner_property_link['entity'] and triplet['property'] == ner_property_link['property']:
				answer = triplet['value'] # 搜索返回答案
				break
			else:
				pass  # 搜索不到答案，则返回知识库能力不足
	else: # 如果实体属性匹配没有结果，即实体识别属性分类的结果匹配不到答案
		score = 0.0  # 利用相似度算法在知识库中匹配答案
		answer = 'Questions are not understood'
		for triplet in kg_triplet:
			answer_word_list = word_segment(triplet['entity'] + triplet['property']) # 将知识库中的实体和属性进行分词
			if score > compute_cosine(question_list, answer_word_list): # 选择相似度最大的知识库三元组数据，返回相应的答案
				pass
			else:
				score = compute_cosine(question_list, answer_word_list)
				answer = triplet['value']
	return answer



