import json
import filePath

def trans_data2QA_data(trans_data_path, QA_data_path):
    '''
    将交通模拟生成的json文件中的数据按照问答系统抽取为需要的知识图谱三元组格式并保存
    :param trans_data_path: 交通模拟生成的数据路径
    :param QA_data_path: 交通领域知识问答数据库路径
    '''
    file = open(trans_data_path, 'r', encoding='utf-8')
    f = open(QA_data_path, "w", encoding='utf-8')
    for line in file.readlines():
        dic = json.loads(line)
        # 为了完善我们的三元组知识库，我们为每个关系设立了三个关键词，适用我们的问答系统
        has_symptom_str1 = dic['name'] + ' ||| ' + '症状' + ' ||| ' + dic['symptom']
        has_symptom_str2 = dic['name'] + ' ||| ' + '问题' + ' ||| ' + dic['symptom']
        has_symptom_str3 = dic['name'] + ' ||| ' + '需要解决' + ' ||| ' + dic['symptom']
        intersection_poi_str1 = dic['intersection'] + ' ||| ' + '标志建筑' + ' ||| ' + dic['poi']
        intersection_poi_str2 = dic['intersection'] + ' ||| ' + '兴趣点' + ' ||| ' + dic['poi']
        intersection_poi_str3 = dic['intersection'] + ' ||| ' + '坐标' + ' ||| ' + dic['poi']
        should_adapt_str1 = dic['symptom'] + ' ||| ' + '应该采取' + ' ||| ' + dic['scheme']
        should_adapt_str2 = dic['symptom'] + ' ||| ' + '解决办法' + ' ||| ' + dic['scheme']
        should_adapt_str3 = dic['symptom'] + ' ||| ' + '策略' + ' ||| ' + dic['scheme']
        intersection_channel_str1 = dic['intersection'] + ' ||| ' + '通道' + ' ||| ' + dic['channel']
        intersection_channel_str2 = dic['intersection'] + ' ||| ' + '路径' + ' ||| ' + dic['channel']
        intersection_channel_str3 = dic['intersection'] + ' ||| ' + '向导' + ' ||| ' + dic['channel']
        f.write(has_symptom_str1+"\n")
        f.write(has_symptom_str2+"\n")
        f.write(has_symptom_str3+"\n")
        f.write(intersection_poi_str1+"\n")
        f.write(intersection_poi_str2+"\n")
        f.write(intersection_poi_str3+"\n")
        f.write(should_adapt_str1+"\n")
        f.write(should_adapt_str2+"\n")
        f.write(should_adapt_str3+"\n")
        f.write(intersection_channel_str1+"\n")
        f.write(intersection_channel_str2+"\n")
        f.write(intersection_channel_str3+"\n")
    file.close()
    f.close()

if __name__ == '__main__':
    trans_data_path = filePath.DATA_PATH
    QA_data_path = filePath.KG_DATA_PATH
    trans_data2QA_data(trans_data_path, QA_data_path)