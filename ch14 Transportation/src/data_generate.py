import json
import random
import filePath
from utils import Trans_Source_Data

def trans_data_generate(path, num, Trans_Source_Data):
    '''
    模拟规则，生成交通数据
    :param path: 交通模拟生成的数据路径
    :param num: 数据生成条数
    :param Trans_Source_Data: 储存交通源数据的对象
    :return:
    '''
    f = open(path, 'w', encoding='utf-8')
    i = 1
    while i < num: # 设置数据生成条数阈值
        i += 1
        land_name = ''.join(random.sample(Trans_Source_Data.land_name_list, 1))
        road_name = ''.join(random.sample(Trans_Source_Data.road_name_list, 1))
        # 交口：道路干线名-道路干线名
        intersection1 = ''.join(random.sample(Trans_Source_Data.road_name_list, 1))
        channel = '-'.join(random.sample(Trans_Source_Data.channel_list, random.randint(1, 6)))
        symptom1 = ''.join(random.sample(Trans_Source_Data.symptom1_list, random.randint(1, 3)))
        symptom2 = '-'.join(random.sample(Trans_Source_Data.symptom2_list, 1))
        # 标志建筑点，值为5位随机字符的字符串
        poi = ''.join(random.sample(Trans_Source_Data.random_str_list, 5))
        # 问题解决方案，值为15位随机字符的字符串
        scheme = ''.join(random.sample(Trans_Source_Data.random_str_list, 15))
        name = str(land_name) + str(road_name)  # name字段为两个字段的随机匹配组合
        symptom = str(symptom1) + str(symptom2)  # symptom字段为两个字段的随机匹配组合
        # 设置条件，使生成的路口字段包含当前路的路名
        if road_name != intersection1:
            intersection = str(road_name) + '-' + str(intersection1)
        else:
            intersection = str(road_name)
        data = dict()
        data["name"] = name
        data["scheme"] = scheme
        data["channel"] = channel
        data["poi"] = poi
        data["symptom"] = symptom
        data["intersection"] = intersection
        # 存到json中，为后面顺利读取，数据按这里的存储格式存储
        json_str = json.dumps(data, ensure_ascii=False)
        f.write(json_str+"\n")
    f.close()

if __name__ == '__main__':
    random.seed(666)
    path = filePath.DATA_PATH
    num = 15000
    trans_data_generate(path, num, Trans_Source_Data)
