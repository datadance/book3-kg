from tqdm import tqdm
import os

from filepath import DataPath
from data_loader import txt2triplet_list
from utils import word_segment


def get_entity_class(alias_class_entity_tripet):
	'''
	从实体与实体类别之间的三元组数据中提取实体类别
	:param alias_class_entity_tripet: 三元组数据（名称-类别-唯一标识实体名）
	:return: 列表，列表元素为三元组中id、类别、实体名（用空格间隔）格式用\t分隔的字符串
	'''
	entity_class = []
	# tqdm用来展示进度条
	for i in tqdm(range(len(alias_class_entity_tripet))):
		triplet = alias_class_entity_tripet[i]
		entity_class_value = []
		entity_class_value.append(triplet['entity'])
		if triplet['alias'] != '':
			# 将alias字典中的"|"替换为空格
			entity_class_value += triplet['alias'].replace('\n', '').replace(' ', '').split('|')
		# 将三元组字典数据转化为用‘\t’分隔的一条字符串，该条字符串拥有所有实体信息
		entity_info = str(triplet['id']) + '\t' + triplet['_class'] + '\t' + ' '.join(entity_class_value)
		entity_class.append(entity_info)
	return entity_class

def generate_nodes_edges(entity_class):
	'''
	生成nodes、edges数据文件并保存
	:param entity_class:get_entity_class函数中生成的列表
	'''
	nodes = []  # 点数据（实体经过分词的各个点）
	edges = []  # 点与点之间的边（能组成一个实体的点之间存在一条边）

	for line in entity_class:
		# 抽取出entity_class中的实体名信息
		entity_list = line.split('\t')[2].strip('\n').split(' ')
		entity_list.append(line.split('\t')[1]) # 将实体类别也一并加入
		for e in entity_list:
			# 进行分词切分处理，生成nodes和edges
			e_word_list = word_segment(e)
			for i in range(len(e_word_list)):
				if e_word_list[i] not in nodes:
					nodes.append(e_word_list[i])
				if i != len(e_word_list)-1:
					tup = (e_word_list[i], e_word_list[i+1], line.split('\t')[0], str(len(e_word_list)))
					if tup not in edges:
						edges.append(tup)
	# 检查entity文件夹是否存在，若否则创建entity文件夹
	if not os.path.exists('data/entity'):
		os.makedirs('data/entity')
	# 	生成nodes数据文件并保存
	f = open('data/entity/nodes.txt', 'w', encoding='utf-8')
	for w in nodes:
		f.write(str(w))
		f.write('\n')
	f.close()
	# 	生成edges数据文件并保存
	f = open('data/entity/edges.txt', 'w', encoding='utf-8')
	for t in edges:
		f.write(str(t))
		f.write('\n')
	f.close()


if __name__ == '__main__':
	alias_class_entity_tripet_path = DataPath.alias_class_entity_tripet_path
	alias_class_entity_triplet = txt2triplet_list(alias_class_entity_tripet_path)
	entity_class = get_entity_class(alias_class_entity_triplet)
	generate_nodes_edges(entity_class)
