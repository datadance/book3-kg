# -*- coding: utf-8 -*-
# @Time    : 2019/12/19 10:53
# @Author  : yjli
# @File    : extractQA.py

from storeM2ID import Mention2ID
from storeKB import KnowledgeBase
from computeSimilar import Similar
import jieba
import urllib.request
import urllib.parse
import json


def entityCandidate(question:str,m2id):
    urlPath = 'http://shuyantech.com/api/entitylinking/cutsegment?q='
    urlstr = urlPath+urllib.parse.quote(question)

    urlRST = urllib.request.urlopen(urlstr)
    dic = json.loads(urlRST.read().decode('utf-8'))
    entityList = []
    for entity in dic['entities']:
        if len(entity)==2:
            entityList.append(question[entity[0][0]:entity[0][1]])
    print(entityList)

    for entity in entityList:
        queryRST = m2id.queryM2ID(entity)
        if len(queryRST)>0:
            idstrRST = queryRST[0][1]
            entityList = entityList + idstrRST.split(' ')
    entityList = list(set(entityList))
    print(entityList)
    return entityList

def answerCandidate(entityList:list,kb):
    answerList = []
    for entity in entityList:
        answerList = answerList + kb.queryKB(entity)
    return answerList

