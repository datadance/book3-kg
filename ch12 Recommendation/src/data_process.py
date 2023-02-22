import numpy as np
import pandas as pd
from utils import Parameters

def extract_spo(book_data):
    '''
    从图书信息数据中提取出三元组数据
    :param book_data: 存储图书信息的数据框
    :return:DataFrame格式文件，存储着图书三元组数据
    '''
    # 创建id-relation-entity的三元组数据框
    kg = pd.DataFrame(columns={"id": "", "relation": "", "entity": ""})
    # 图书信息各类关系数据中，每个非空的值都对应一行三元组，存储在kg中
    for ind in book_data.index:
        for col in range(2, len(book_data.columns)): # 关系所在的列
            if not pd.isnull(book_data.iloc[ind, col]): # 保证非空
                colname = book_data.columns[col]  # 关系名
                kg = kg.append([{'id':int(book_data.loc[ind, 'entity_id']), 'relation':'book.book.'+ colname, 'entity': int(book_data.loc[ind, colname])}],ignore_index=True)
    kg = kg.dropna() # 去掉可能存在的空值
    return kg

def book_entity_index(book_data):
    '''
    提取id对应的序号，存储为字典
    :param book_data: 存储图书信息的数据框
    :return: 图书ID及对应序号字典数据, 知识图谱图书实体ID及对应序号字典数据
    '''
    book_id_index = dict()  # 图书ID及对应序号
    entity_id_index = dict()  # 知识图谱图书实体ID及对应序号
    i = 0
    for ind in book_data.index:
        book_id = str(book_data.loc[ind, 'book_id'])
        entity_id = str(book_data.loc[ind, 'entity_id'])
        book_id_index[book_id] = i
        entity_id_index[entity_id] = i
        i += 1
    return book_id_index, entity_id_index

def ratings_process(book_id_index, path):
    '''
    处理用户阅读历史评分数据，剔除用户已经阅读过的图书id
    将某用户喜欢N个阅读的图书id（评分为1）信息，与还未阅读的N个数据图书id信息（评分为0），一起保存数据为ratings_final.txt
    :param book_id_index: 图书ID及对应序号字典数据
    :param path: 存放评分数据的文件夹路径
    '''
    # 处理读者的评分数据集(ratings.dat)，评分4以上记为：1；生成 ratings_final.txt
    file = path + '/ratings.dat'
    book_index_set = set(book_id_index.values())  # 图书ID的序号
    reader_pos_ratings = dict()  # 读者评分高的图书
    reader_neg_ratings = dict()  # 读者评分低的图书
    for line in open(file, encoding='utf-8').readlines()[1:]:
        array = line.strip().split('::')
        book_id_from_ratings = array[1]
        if book_id_from_ratings not in book_id_index:  # 剔除不在图书数据内的图书ID
            continue
        book_index = book_id_index[book_id_from_ratings]  # item_index：对应图书ID的序号
        reader_id = int(array[0])  # 读者ID
        rating = float(array[2])  # 读者ID 对 图书ID 的评分
        if rating >= 4:
            if reader_id not in reader_pos_ratings:
                reader_pos_ratings[reader_id] = set()
            reader_pos_ratings[reader_id].add(book_index)
        else:
            if reader_id not in reader_neg_ratings:
                reader_neg_ratings[reader_id] = set()
            reader_neg_ratings[reader_id].add(book_index)
    writer = open(path + '/ratings_final.txt', 'w', encoding='utf-8')
    reader_cnt = 0
    reader_id_index = dict()  # 读者ID及对应的序号
    for reader_id, pos_book_index_set in reader_pos_ratings.items():
        if reader_id not in reader_id_index:
            reader_id_index[reader_id] = reader_cnt
            reader_cnt += 1
        reader_index = reader_id_index[reader_id]
        for book_index in pos_book_index_set:
            writer.write('%d\t%d\t1\n' % (reader_index, book_index))
        unwatched_set = book_index_set - pos_book_index_set  # 从全部图书ID序号除去该读者评分高的图书ID序号
        if reader_id in reader_neg_ratings:
            unwatched_set -= reader_neg_ratings[reader_id]  # 再除去该读者评分低的图书ID序号
            # 此时的 unwatched_set 是当前读者还未读过的图书
        for item in np.random.choice(list(unwatched_set), size=len(pos_book_index_set), replace=False):
            # 随机抽取 size 个当前读者还未读过的图书ID序号，用作后面的模型检验评估
            writer.write('%d\t%d\t0\n' % (reader_index, item))
    writer.close()

def spo_process(entity_id_index, kg, path):
    '''
    处理知识图谱三元组数据，生成 kg_final.txt
    :param entity_id_index: 知识图谱图书实体ID及对应序号字典数据
    :param kg: 图书三元组数据
    :param path: 项目数据文件夹路径
    '''
    relation_id_index = dict()  # 关系编码
    entity_cnt = len(entity_id_index)
    relation_cnt = 0
    writer = open(path + '/kg_final.txt', 'w', encoding='utf-8')
    for ind in range(len(kg)):
        head_index = str(kg.loc[ind, 'id'])  # 图书实体ID序号
        relation_str = kg.loc[ind, 'relation']  # 关系
        tail_id = kg.loc[ind, 'entity']  # 目标实体
        if head_index not in entity_id_index:  # 剔除不在图书数据内的图书实体ID序号
            continue
        head = entity_id_index[head_index]
        # 数据都是模拟产生的数字，防止目标实体和图书ID序号数据重合
        if tail_id not in entity_id_index:
            entity_id_index[tail_id] = entity_cnt
            entity_cnt += 1
        tail = entity_id_index[tail_id]
        # 将关系编号
        if relation_str not in relation_id_index:
            relation_id_index[relation_str] = relation_cnt
            relation_cnt += 1
        relation = relation_id_index[relation_str]
        writer.write('%d\t%d\t%d\n' % (head, relation, tail))
    writer.close()
    print('number of entities (containing items): %d' % entity_cnt)
    print('number of relations: %d' % relation_cnt)


if __name__ == '__main__':
    # 存到数据库导入指定的文件夹
    path = Parameters.args.dataset_path
    # 读取存储图书信息数据的csv文件
    book_data = pd.read_csv(path+ '/book_data.csv', encoding='utf-8')
    kg = extract_spo(book_data)
    kg.to_csv(path + '/kg.csv', index=False, header=True)
    book_id_index, entity_id_index = book_entity_index(book_data)
    # ratings_process(book_id_index, path)
    spo_process(entity_id_index, kg, path)
