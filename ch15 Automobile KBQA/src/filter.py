def filter_text(ws_list:list):
	'''
	:param ws_list: 经过中文分词处理后的问句词语列表
	:return: 经过拒识、敏感词等过滤操作的新列表
	'''
	# 读取存储拒识、敏感词等数据的文件
	f_entity = open(r'data/kg_data/filter.txt', 'r', encoding='utf-8')
	lines = [line.strip('\n') for line in f_entity.readlines()]
	f_entity.close()
	new_list = []
	for word in ws_list:
		if word not in lines:
			new_list.append(word)
	return new_list
