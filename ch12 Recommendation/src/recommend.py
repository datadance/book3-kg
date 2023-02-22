import json
from utils import Parameters
import pandas as pd

# id对照
def entity_id_to_book_id(init_data):
    rec_ent_to_book = dict()
    for ind in init_data.index:
        book_id = init_data.loc[ind, 'book_id']
        entity_id = init_data.loc[ind, 'entity_id']
        rec_ent_to_book[entity_id] = book_id
    return rec_ent_to_book


# 推荐系统设计
def recommend_sys(path, num):
    f = open(path + '\\result.json', encoding='utf-8')
    setting = json.load(f)
    f.close()
    path = Parameters.args.dataset_path
    # 读取存储图书信息数据的csv文件
    book_data = pd.read_csv(path + '/book_data.csv', encoding='utf-8')

    rec_ent_to_book = entity_id_to_book_id(book_data)
    print('*'*10 + '欢迎使用基于知识图谱的图书推荐系统' + '*'*10)
    print('-'*52)
    while True:
        flag = int(input('\n按数字键1开始，按数字键0退出：'))
        try:
            if flag == 1:
                reader_id = input('[读者ID(0-6035)]:')
                rec_num = int(input('[推荐数量(1-%d)]:' % num))
                print('推荐图书ID为：')
                i = 1
                for satori_id in setting[reader_id]:
                    if i <= rec_num:
                        print(rec_ent_to_book[satori_id], end='    ')
                        i += 1
            elif flag == 0:
                break
            else:
                print('指令有误！')
        except Exception as r:
            print('%s，该读者ID不存在！' % r)
    print('-' * 52)
    print('*' * 19 + '谢谢使用，再见！' + '*' * 18)
