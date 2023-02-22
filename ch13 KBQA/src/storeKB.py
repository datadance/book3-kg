# -*- coding: utf-8 -*-
# @Time    : 2019/12/17 11:23
# @Author  : yjli
# @File    : storeKB.py

import sqlite3
import filePath
import json

class KnowledgeBase:
    def __init__(self):
        self.conn = sqlite3.connect('KB.db')
        self.curs = self.conn.cursor()
        self.kbName = 'kb1'

    def creatKB(self):
        self.curs.execute('CREATE TABLE '+self.kbName+'(subject TEXT ,predicte TEXT,object TEXT)')
        self.curs.execute('CREATE INDEX index_subject ON '+self.kbName+'(subject)')
        self.conn.commit()

    def loadKB(self, KBPath=filePath.KNOWLEDGE_BASE_PATH):
        with open(KBPath, 'r', encoding='utf-8') as f:
            lineCount, skip = 0, 0
            while True:
                line =f.readline().rstrip()  # 逐行读取
                if not line:  # 到 EOF，返回空字符串，则终止循环
                    break
                lineCount += 1
                try:
                    if line.split(' ||| ')[1]==line.split(' ||| ')[2]:  #删除predicate和object一样的三元组
                        continue
                    else:
                        self.curs.execute('INSERT INTO '+self.kbName+' VALUES(?,?,?)',line.split(' ||| '))
                except ValueError:
                    skip += 1
                    continue
            self.conn.commit()
            f.close()
            print('total line number:', lineCount)
            print('skipped:', skip)

    def queryKB(self,subjectName):
        query = 'SELECT * FROM '+self.kbName+' WHERE subject = ?'
        queryRst = self.curs.execute(query,[subjectName]).fetchall()
        return queryRst

    def close(self):
        self.conn.close()

if __name__ == '__main__':
    kb = KnowledgeBase()
    # kb.creatKB()
    # kb.loadKB()
    rst = kb.queryKB('李娜')
    print(rst)
    kb.close()
