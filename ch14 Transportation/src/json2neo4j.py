import json
from py2neo import Graph, Node, Relationship
import filePath

# 输入图数据库地址、用户及密码，连接图数据库
graph = Graph('http://localhost:7474', username='neo4j', password='123456')
# 交通数据路径
path = filePath.DATA_PATH

def link2neo4j(label1, label2, relation_name, dic):
    '''
    连接neo4j数据库，并创建三元组
    :param label1: 头节点
    :param label2: 尾节点
    :param relation_name: 关系名
    :param dic: 字典格式储存的交通数据
    '''
    org1 = graph.nodes.match(label1, name=dic[label1]).first()
    if org1 is None:
        node_1 = Node(label1, name=dic[label1])
        graph.create(node_1)
    else:
        node_1 = org1
    org2 = graph.nodes.match(label2, name=dic[label2]).first()
    if org2 is None:
        node_2 = Node(label2, name=dic[label2])
        graph.create(node_2)
        relationship = Relationship(node_2, relation_name, node_1)
        graph.create(relationship)
    else:
        relationship = Relationship(org2, relation_name, node_1)
        graph.create(relationship)

def run(path):
    # 由于文件中有多行，直接读取会出现错误，因此一行一行读取
    file = open(path, 'r', encoding='utf-8')
    for line in file.readlines():
        dic = json.loads(line)
        link2neo4j("poi", "intersection", "intersection_poi", dic)
        link2neo4j("symptom", "name", "has_symptom", dic)
        link2neo4j("scheme", "symptom", "should_adapt", dic)
        link2neo4j("channel", "intersection", "intersection_channel", dic)
    file.close()

if __name__ == '__main__':
    run(path)

