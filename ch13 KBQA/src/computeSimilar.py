# -*- coding: utf-8 -*-
# @Time    : 2019/12/17 14:24
# @Author  : yjli
# @File    : computeSimilar.py

import filePath
import jieba
import numpy as np
from gensim.models import Word2Vec
import jieba.posseg as pseg
import re


class Similar:
    def __init__(self):
        #loading model
        print('获取词向量模型')
        self.model = Word2Vec.load(filePath.WIKI_WORD2VEC_PATH)
        print('成功导入')
        self.ner = ['nr','ns','nt','nz']  #[人名，地名，机构团体，其他专名]
        self.attri = ['n','v','r','p']  #[名词，动词，代词，介词]
        self.remove = ['x','w']  #标点符号和非素语


    def getWV(self, word, flag, usePSEG:bool):
        wvRST = np.zeros(250, dtype=np.float32)
        try:
            if usePSEG is False:
                return self.model.wv[word] * 1
            if flag in self.remove:
                return self.model.wv[word] * 0
            if flag in self.attri:
                return self.model.wv[word] * 1.2
            if flag in self.ner:
                return self.model.wv[word] * 0.8
            else:
                return self.model.wv[word] * 1
        except KeyError:
            # print("input word %s not in dict. skip this turn" % word)
            return wvRST

    def questionVec(self,sentence,usePSEG:bool=True,stoppath=filePath.STOP_WORDS_PATH):
        cutRST = pseg.cut(sentence)
        wvRST = np.zeros(250,dtype=np.float32)
        stop=[line.strip() for line in open(stoppath,encoding='utf-8').readlines()]
        start = True
        for word,flag in cutRST:
            if word not in stop:
                if start:
                    wvRST = self.getWV(word, flag, usePSEG)
                    start = False
                else:
                    wvRST = np.vstack((wvRST,self.getWV(word, flag, usePSEG)))
        return np.mean(wvRST,axis=0) if wvRST.ndim==2 else wvRST  #对各列求平均

    def answerVec(self, message: str):
        messages = message.split('|||')
        wvRST = np.zeros(250, dtype=np.float32)
        start = True
        for idx in range(len(messages)):
            if start:
                wvRST = ((idx % 2) + 0.5) * self.questionVec(messages[idx])    #除法的取模
                start = False
            else:
                wvRST = np.vstack((wvRST, ((idx % 2) + 0.5) * self.questionVec(messages[idx])))
        return np.mean(wvRST, axis=0) if wvRST.ndim == 2 else wvRST

    def textS(self, text1, text2, usePSEG: bool = True):
        qwv = self.questionVec(text1, usePSEG)
        awv = self.answerVec(text2)
        cos_sim = np.dot(qwv, awv) / (np.linalg.norm(qwv) * np.linalg.norm(awv))
        return cos_sim

    def vectorS(self, v1, v2):
        cos_sim = np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))
        return cos_sim

if __name__ == '__main__':
    similar = Similar()
    # wv=similar.questionEnb("红楼梦是谁写的")
    # print(wv)
