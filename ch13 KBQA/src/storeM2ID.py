# -*- coding: utf-8 -*-
# @Time    : 2019/12/17 14:08
# @Author  : yjli
# @File    : storeM2ID.py

import sqlite3
import filePath

class Mention2ID:
    def __init__(self):
        self.conn = sqlite3.connect('KB.db')
        self.curs = self.conn.cursor()
        self.m2idName = 'mention2id1'

    def creatM2ID(self):
        self.curs.execute('CREATE TABLE '+self.m2idName+'(mention TEXT PRIMARYKEY,id TEXT)')
        self.curs.execute('CREATE INDEX index_mention ON '+self.m2idName+'(mention)')
        self.conn.commit()
        print("数据库创建成功")

    def loadM2ID(self ,M2IDPath=filePath.MENTION2ID_DIC_PATH):
        with open(M2IDPath, 'r', encoding='utf-8') as f:
            lineCount, skip = 0, 0
            while True:
                line =f.readline().rstrip()  # 逐行读取
                if not line:  # 到 EOF，返回空字符串，则终止循环
                    break
                lineCount += 1
                try:
                    lineElements = line.split(' ||| ')
                    if len(lineElements) == 2 and lineElements[0] != lineElements[1]:
                        tempRST = [lineElements[0].replace(' ', ''), lineElements[1].replace('\t', ' ')]
                        self.curs.execute('INSERT INTO ' + self.m2idName + ' VALUES(?,?)', tempRST)
                    else:
                        if len(lineElements) != 2:
                            skip += 1
                            continue
                except ValueError:
                    skip += 1
                    continue
            self.conn.commit()
            f.close()
            print('total line number:', lineCount)
            print('skipped:', skip)

    def queryM2ID(self,subjectName):
        query = 'SELECT * FROM ' + self.m2idName + ' WHERE mention = ?'
        queryRst = self.curs.execute(query, [subjectName]).fetchall()
        return queryRst

    def close(self):
        self.conn.close()

if __name__ == '__main__':
    m2id = Mention2ID()
    m2id.creatM2ID()
    m2id.loadM2ID()
    rst = m2id.queryM2ID('李娜')
    print(rst)
    m2id.close()


