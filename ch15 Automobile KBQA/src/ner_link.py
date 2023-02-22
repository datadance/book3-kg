from utils import word_segment
from data_loader import get_entity_id_dict

def get_entity_words():
	'''
	从nodes中读取实体的词数据，转化为列表格式
	:return: 存储nodes的列表
	'''
	# 读取nodes数据
	entity_words_path = './data/entity/nodes.txt'
	f_entity = open(entity_words_path, 'r', encoding='utf-8')
	nodes_list = [line.strip('\n') for line in f_entity.readlines()]
	f_entity.close()
	return nodes_list

def extract_entity_from_ws_sentence(ws_list, nodes_list, assist_words_dict):
	'''
	从经过分词的问句中提取在实体词语数据列表中的词
	:param ws_list: 经过中文分词处理后的问句词语列表
	:param nodes_list: 存储nodes的列表
	:param assist_words_dict: 存储辅助词的字典
	:return:列表，存储用户问句中在nodes_list和assist_words中的词
	'''
	ws_entity_list = list()
	for i in range(len(ws_list)):
		if ws_list[i] in nodes_list:
			if ws_list[i] in assist_words_dict.keys():
				ws_entity_list.append(assist_words_dict[ws_list[i]])
			else:
				ws_entity_list.append(ws_list[i])

	return ws_entity_list

def get_entity_id(edges_list, assist_words_dict, ws_entity_list):
	'''
	获得实体词语的id
	:param edges_list: 存储edges的列表
	:param assist_words_dict: 存储辅助词的字典
	:param ws_entity_list: 符合匹配条件的词列表
	:return:
	'''
	res = dict()
	res_score = dict()
	# 通过边数据，我们可以找到相对应的实体id
	for line in edges_list:
		line_ = [l[1:-1] for l in line.split(', ')] # 去掉冒号
		for i in range(len(line_)):
			if line_[i] in assist_words_dict.keys():
				line_[i] = assist_words_dict[line_[i]]
		# 边的点都在用户问句分词实体列表里面
		if line_[0] != line_[1] and line_[0] in ws_entity_list and line_[1] in ws_entity_list:
			# 将实体ID作为key，值为组成实体的词
			if line_[2] in res.keys():
				res[line_[2]].add(line_[0])
				res[line_[2]].add(line_[1])
			else:
				res[line_[2]] = set()
				res[line_[2]].add(line_[0])
				res[line_[2]].add(line_[1])
				# 计算权重
				res_score[line_[2]] = int(line_[3])
	m = 0
	e_id = ''
	for k, v in res_score.items():
		# 分数进行处理，选取最小值对应的实体id
		score = (len(res[k])**3)/v
		if score > m:
			m = score
			e_id = k
	return e_id

def id2entity(id_, id_entity_triplet):
	'''
	获得id对应的实体
	:param id_: 字符串格式的id
	:param id_entity_triplet: 三元组数据
	:return: id对应的实体，id对应的实体类别
	'''
	if id_ != '':
		for triplet in id_entity_triplet:
			if triplet["id"] == int(id_):
				return triplet["entity"], triplet["_class"]

def entity_recognition_from_entity_dict(text, entity_id_dict):
	'''
	实体识别
	:param text: 问句字符串
	:param entity_id_dict: 实体id字典
	:return: 问句字符串中含有实体的对应id的列表
	'''
	entity_id_list = []
	for k, v in entity_id_dict.items():
		if k in text and v not in entity_id_list:
			entity_id_list.append(v)
	return entity_id_list

def ner_main(question_list, entity_id_dict, alias_class_entity_triplet, assist_words_dict):
	'''
	读取辅助词数据以及实体三元组数据，进行实体链接
	:param question_list:问句分词列表
	:param entity_id_dict:实体id字典
	:param alias_class_entity_triplet:实体类别三元组
	:param assist_words_dict:辅助词字典
	:return:存储结果的列表，单个列表元素中第一个字符串是实体唯一标识名，第二个字符串是实体类别
	'''
	result = []
	entity_id_list = entity_recognition_from_entity_dict(''.join(question_list), entity_id_dict)
	nodes_list = get_entity_words()
	ws_entity_list = extract_entity_from_ws_sentence(question_list, nodes_list, assist_words_dict)
	# 读取边数据
	edges_path = r'./data/entity/edges.txt'
	f_edges = open(edges_path, 'r', encoding='utf-8')
	edges_list = [line.strip('\n').lstrip('(').strip(')') for line in f_edges.readlines()]
	f_edges.close()
	# 得到实体识别后的实体id
	r = get_entity_id(edges_list, assist_words_dict, ws_entity_list)
	if r != '':
		entity_id_list.append(r)
	for id_ in entity_id_list:
		result.append(id2entity(id_, alias_class_entity_triplet))
	return result


if __name__ == '__main__':
	from filter import filter_text
	from filepath import DataPath
	from data_loader import txt2triplet_list, extract_assist_words_dict

	alias_class_entity_tripet_path = DataPath.alias_class_entity_tripet_path

	triplet_list = txt2triplet_list(alias_class_entity_tripet_path)

	entity_class_dict, entity_id_dict = get_entity_id_dict(triplet_list)

	assist_words_dict = extract_assist_words_dict(DataPath.assist_words_path)
	# question = '你能回答我怎么增大后面空调的出风口的暖风风速吗'
	question = '这个空调怎么调高温度啊'

	# question = '主动紧急制动不管用怎么办'
	# question = '怎么调节车内温度'
	# question = '红旗HS7上市多久了'
	print(question)
	ws_list = word_segment(question)
	question_list = filter_text(ws_list)

	ner_result = ner_main(question_list, entity_id_dict, triplet_list, assist_words_dict)
	print(ner_result)
