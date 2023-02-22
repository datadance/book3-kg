import sqlite3
import filePath

class KnowledgeBase:
    def __init__(self):
        self.conn = sqlite3.connect(filePath.KNOWLEDGE_BASE_PATH)
        self.curs = self.conn.cursor()
        self.kbName = 'kb1'

    def creatKB(self):
        self.curs.execute('CREATE TABLE '+self.kbName+'(subject TEXT ,predicte TEXT,object TEXT)')
        self.curs.execute('CREATE INDEX index_subject ON '+self.kbName+'(subject)')
        self.conn.commit()

    def executeKB(self, kbName_):
        self.conn.execute('drop table ' + kbName_)

    def loadKB(self, KBPath=filePath.KNOWLEDGE_BASE_PATH):
        with open(KBPath, 'r', encoding='utf-8') as f:
            lineCount, skip = 0, 0
            while True:
                line =f.readline().rstrip()  # 逐行读取
                if not line:  # 到 EOF，返回空字符串，则终止循环
                    break
                lineCount += 1
                try:
                    if line.split(' ||| ')[1]==line.split(' ||| ')[2]:  # 删除predicate和object一样的三元组
                        continue
                    else:
                        self.curs.execute('INSERT INTO '+self.kbName+' VALUES(?,?,?)',line.split(' ||| '))
                        # print(line.split())
                except ValueError:
                    skip += 1
                    continue
            self.conn.commit()
            f.close()
            print('total line number:', lineCount)
            print('skipped:', skip)

    def queryKB(self,subjectName):
        query = "SELECT * FROM "+self.kbName+" WHERE subject like ?"
        queryRst = self.curs.execute(query,[subjectName]).fetchall()
        return queryRst

if __name__ == '__main__':
    kb = KnowledgeBase()
    # kb.executeKB('kb1')
    kb.creatKB()
    kb.loadKB()
    rst = kb.queryKB('%繁华大道%')
    print(rst)
