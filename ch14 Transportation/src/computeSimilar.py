import filePath
import numpy as np
import jieba.posseg as pseg
from utils import word_segment

class Similar:
    def __init__(self):
        # loading model
        print('获取词向量模型')
        # 预训练词向量文件以文本格式存储, 按照打开txt文本的方式, 每一行包含一个单词及其词向量, 存储为字典。
        f = open(filePath.WIKI_WORD2VEC_PATH, 'r', encoding='utf-8')
        self.embeddings_index = {}
        for line in f:
            values = line.split()
            word = values[0]
            self.embeddings_index[word] = np.asarray(values[1:], dtype='float32')
        f.close()
        print('成功导入')
        self.ner = ['nr','ns','nt','nz']  # [人名，地名，机构团体，其他专名]
        self.attri = ['n','v','r','p']  # [名词，动词，代词，介词]
        self.remove = ['x','w']  # 标点符号和非素语

    def getWV(self, word, flag, usePSEG:bool):
        wvRST = np.zeros(300, dtype=np.float32)
        try:
            if usePSEG is False:
                return self.embeddings_index[word] * 1
            # 非重要词性，标点符号和非素语，权重为0
            if flag in self.remove:
                return self.embeddings_index[word] * 0
            # 重要词性，名词、动词、代词、介词，权重为1.2
            if flag in self.attri:
                return self.embeddings_index[word] * 1.2
            # 命名实体，人名、地名、机构团体、其他专名，权重为0.8
            if flag in self.ner:
                return self.embeddings_index[word] * 0.8
            else:
                return self.embeddings_index[word] * 1
        except KeyError:
            return wvRST

    def questionVec(self,sentence,usePSEG):
        # 分词，加载用户自定义词典并过滤符号
        sentence = ''.join(word_segment(sentence))
        if usePSEG:
            cutRST = pseg.cut(sentence)
            # 设置初始向量
            wvRST = np.zeros(300, dtype=np.float32)
            # 获取停用词
            start = True
            for word, flag in cutRST:
                if start:
                    wvRST = self.getWV(word, flag, usePSEG)
                    start = False
                else:
                    # 按垂直方向（行顺序）堆叠数组构成一个新的数组
                    wvRST = np.vstack((wvRST, self.getWV(word, flag, usePSEG)))
        else:
            cutWS = word_segment(sentence)
            wvRST = np.zeros(300, dtype=np.float32)
            start = True
            for word in cutWS:
                flag = 'none'
                if start:
                    wvRST = self.getWV(word, flag, usePSEG)
                    start = False
                else:
                    wvRST = np.vstack((wvRST, self.getWV(word, flag, usePSEG)))
        return np.mean(wvRST, axis=0) if wvRST.ndim == 2 else wvRST

    def answerVec(self, message: str, usePSEG):
        messages = message.split('|||')
        messages = word_segment(''.join(messages[:-1]).replace(' ', '') )
        # 设置初始向量
        wvRST = np.zeros(300, dtype=np.float32)
        start = True
        for idx in range(len(messages)):
            flag = 'none'
            if start:
                wvRST = ((idx % len(messages)) + 0.5) * self.getWV(messages[idx], flag, usePSEG)
                start = False
            else:
                wvRST = np.vstack((wvRST, ((idx % len(messages)) + 0.5) * self.getWV(messages[idx], flag, usePSEG)))

        return np.mean(wvRST, axis=0) if wvRST.ndim == 2 else wvRST

    def textS(self, text1, text2, usePSEG: bool = True):
        qwv = self.questionVec(text1, usePSEG)
        awv = self.answerVec(text2, usePSEG)
        cos_sim = np.dot(qwv, awv) / (np.linalg.norm(qwv) * np.linalg.norm(awv))
        return cos_sim

    def vectorS(self, v1, v2):
        cos_sim = np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))
        return cos_sim
