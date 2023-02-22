import json

def extract_assist_words_dict(path):
	'''
	提取辅助词数据并处理为字典格式进行辅助词识别
	:param path: 辅助词文件的文件路径
	:return: 辅助词字典数据
	'''
	assist_words_dict = dict()
	# 读取辅助词数据文件
	f_assist = open(path, 'r', encoding='utf-8')
	for line in f_assist.readlines():
		k = line.strip('\n').split('\t')[0]
		for v in line.strip('\n').split('\t')[1].split(' '):
			assist_words_dict[v] = k
	f_assist.close()
	return assist_words_dict

def get_entity_id_dict(triplet_list):
	'''
	对三元组数据进行处理，提取出实体与id的字典数据
	:param triplet_list: 三元组列表
	:return: 实体类别字典，实体id字典
	'''
	entity_id_dict = {}  # 实体与id的字典数据
	entity_class_dict = {}  # 实体与实体类别的字典数据

	for triplet in triplet_list:
		entity_id_dict[triplet['entity']] = triplet['id']
		entity_class_dict[triplet['entity']] = triplet['_class']
		if triplet['alias'] != '':
			for k in triplet['alias'].split('|'):
				entity_id_dict[k] = triplet['id']
				entity_class_dict[k] = triplet['_class']
	return entity_class_dict, entity_id_dict

def get_conception_tree(path):
	'''
	提取概念树数据并处理为字典格式
	:param path: 概念树数据文件的文件路径
	:return: 概念树数据字典
	'''
	# 读取概念树数据
	f_obj = open(path, encoding='utf-8')
	conception_tree = json.load(f_obj)
	f_obj.close()
	return conception_tree

def get_property_dict(conception):
	'''
	从概念树字典提取属性数据并处理为字典格式，用于属性推理
	:param conception: 概念树数据字典
	:return: 属性与子属性字典
	'''
	property_dict = dict()
	for each in conception:
		property_list = each['property'] # 提取概念树中的属性信息
		for pi in property_list:
			for k, v in pi.items():
				if len(v) != 0:
					for p in v:
						property_dict[p['property']] = k
	return property_dict

def txt2triplet_list(path):
	'''
	读取txt文件并转为三元组字典格式
	:param path: 存放三元组字典数据格式文件的文件路径
	:return: 存放三元组数据的列表
	'''
	f_obj = open(path, encoding='utf-8')
	# 使用json将txt文件每行数据转为字典格式，作为列表的元素存储
	triplet_list = [json.loads(line) for line in f_obj.readlines()]
	f_obj.close()
	return triplet_list


if __name__ == '__main__':
	from filepath import DataPath

	conception_tree_path = DataPath.conception_tree_path
	assist_words_path = DataPath.assist_words_path
	alias_class_entity_tripet_path = DataPath.alias_class_entity_tripet_path

	conception_tree = get_conception_tree(conception_tree_path)
	property_dict = get_property_dict(conception_tree)
	triplet_list = txt2triplet_list(alias_class_entity_tripet_path)
	entity_class_dict, entity_id_dict = get_entity_id_dict(triplet_list)
	print(entity_class_dict)

