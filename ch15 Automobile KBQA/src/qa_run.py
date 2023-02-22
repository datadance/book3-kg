from property_classify import get_property
from ner_link import get_entity_id_dict, ner_main
from data_loader import extract_assist_words_dict, get_property_dict,txt2triplet_list, get_conception_tree
from filter import filter_text
from filepath import DataPath
from answer_match import link_entity_result, return_answer
from utils import word_segment

def run(question):
	'''
	问答系统的run函数
	:param question: 问句字符串
	:return: 问句对应的答案字符串
	'''
	print('问题：', question)
	ws_list = word_segment(question)
	# 拒识
	question_list = filter_text(ws_list)
	# 实体识别， 属性分类
	assist_words_dict = extract_assist_words_dict(DataPath.assist_words_path)
	# 读取路径
	alias_class_entity_tripet_path = DataPath.alias_class_entity_tripet_path
	# 导入数据
	triplet_list = txt2triplet_list(alias_class_entity_tripet_path)
	entity_class_dict, entity_id_dict = get_entity_id_dict(triplet_list)
	# 实体识别与链接
	ner_result = ner_main(question_list, entity_id_dict, triplet_list, assist_words_dict)
	conception_tree_path = DataPath.conception_tree_path
	conception_tree = get_conception_tree(conception_tree_path)
	property_dict = get_property_dict(conception_tree)
	# 属性分类与推理
	property_result = get_property(question_list, assist_words_dict, property_dict)
	# 实体与属性进行连接
	ner_property_link = link_entity_result(property_result, ner_result, conception_tree)
	# print(ner_property_link)
	# 载入知识库
	kg_triplet = txt2triplet_list(DataPath.kg_triplet_path)
	# 匹配知识库并返回答案
	answer = return_answer(kg_triplet, ner_property_link, question_list)
	return answer

if __name__ == '__main__':
	# question = '空调怎么吹凉风'
	question = '你能回答我怎么改变后面空调的风速吗'
	# question = '空调制热不能用'
	# question = '这个空调怎么调高温度啊'
	# question = '这个座椅怎么靠后一点啊'
	while True:
		question = input("输入问句(输入'退出'即可退出系统)：")
		if question != '退出':
			print(run(question))
		else:
			break
	print('已退出')
