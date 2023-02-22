from py2neo import Graph, Node, Relationship
import pymysql

graph = Graph("http://localhost:7474/", auth=("neo4j", "123456"))

dbconn = pymysql.connect(
    database="kg_recommend",
    user="root",  # MySQL数据库用户名
    password="123456",  # 密码
    port=3306,
    charset='utf8')  # 建立数据库连接
cursor = dbconn.cursor()

# 得到存储在mysql的关系表
cursor.execute('select id,relation,entity from kg_data')
rs = cursor.fetchall()

for r in rs:
    # 进行节点的匹配
    org1 = graph.nodes.match("book", name=r[0]).first()
    if org1 is None:
        # 新建节点
        a = Node('book', name=r[0])
        graph.create(a)
    org2 = graph.nodes.match("entity", name=r[2]).first()
    if org2 is not None:
        # 建立关系
        rship = Relationship(org2, r[1][10:], a)
        graph.create(rship)
    else:
        b = Node('entity', name=r[2])
        graph.create(b)
        rship = Relationship(a, r[1][10:], b)
        graph.create(rship)

cursor.close()
dbconn.close()
